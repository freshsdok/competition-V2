#!/usr/bin/env python3
"""Review module UAT environment health check.

The script is intentionally self-contained. It checks ports, HTTP reachability,
gateway forwarding, database connectivity, and local middleware dependencies.
It reads optional overrides from scripts/review/review_uat.env or
REVIEW_UAT_ENV_FILE without requiring production configuration changes.
"""

from __future__ import annotations

import datetime as dt
import base64
import hashlib
import http.client
import os
import shlex
import socket
import subprocess
import sys
from pathlib import Path
from typing import Dict, Iterable, List, Optional, Tuple


ROOT_DIR = Path(__file__).resolve().parents[2]
DEFAULT_ENV_FILE = ROOT_DIR / "scripts" / "review" / "review_uat.env"
DEFAULT_REPORT = ROOT_DIR / "logs" / "review-uat" / "check_report.md"


def load_env_file(path: Path) -> None:
    if not path.exists():
        return
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#"):
            continue
        if line.startswith("export "):
            line = line[7:].strip()
        if "=" not in line:
            continue
        key, value = line.split("=", 1)
        key = key.strip()
        value = value.strip().strip('"').strip("'")
        if key and key not in os.environ:
            os.environ[key] = value


def env_str(name: str, default: str) -> str:
    return os.environ.get(name, default)


def env_int(name: str, default: int) -> int:
    raw = os.environ.get(name)
    if raw is None or raw == "":
        return default
    try:
        return int(raw)
    except ValueError:
        return default


def run_cmd(args: List[str], timeout: int = 5) -> Tuple[int, str]:
    try:
        proc = subprocess.run(
            args,
            text=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            timeout=timeout,
            check=False,
        )
        return proc.returncode, proc.stdout.strip()
    except Exception as exc:  # pragma: no cover - defensive diagnostics
        return 1, str(exc)


def lsof_port(port: int) -> str:
    code, output = run_cmd(["lsof", "-nP", f"-iTCP:{port}", "-sTCP:LISTEN"])
    if code != 0:
        return ""
    return output


def socket_check(host: str, port: int, timeout: float = 2.0) -> Tuple[bool, str]:
    try:
        with socket.create_connection((host, port), timeout=timeout):
            return True, "tcp connect ok"
    except Exception as exc:
        return False, str(exc)


def http_get(host: str, port: int, path: str, timeout: float = 4.0) -> Tuple[bool, str]:
    if not path.startswith("/"):
        path = "/" + path
    try:
        conn = http.client.HTTPConnection(host, port, timeout=timeout)
        conn.request("GET", path, headers={"User-Agent": "review-uat-check/1.0"})
        resp = conn.getresponse()
        body = resp.read(160).decode("utf-8", errors="replace").replace("\n", " ")
        conn.close()
        return True, f"HTTP {resp.status} {resp.reason} {path} {body[:80]}".strip()
    except Exception as exc:
        return False, f"{path} failed: {exc}"


def status_from_http(detail: str, allow_auth: bool = True, allow_404: bool = False) -> str:
    if not detail.startswith("HTTP "):
        return "FAIL"
    parts = detail.split()
    try:
        status = int(parts[1])
    except Exception:
        return "FAIL"
    if 200 <= status < 400:
        return "PASS"
    if allow_auth and status in (401, 403):
        return "PASS"
    if allow_404 and status == 404:
        return "WARN"
    if 400 <= status < 500:
        return "WARN"
    return "FAIL"


def add(results: List[Dict[str, str]], status: str, item: str, detail: str) -> None:
    results.append({"status": status, "item": item, "detail": detail})


def summarize_lsof(output: str) -> str:
    if not output:
        return "no listener"
    lines = output.splitlines()
    if len(lines) <= 2:
        return " | ".join(lines)
    return " | ".join([lines[0], *lines[1:4]])


def check_port(results: List[Dict[str, str]], name: str, host: str, port: int) -> None:
    ok, detail = socket_check(host, port)
    lsof = summarize_lsof(lsof_port(port))
    status = "PASS" if ok else "FAIL"
    add(results, status, f"{name} port {host}:{port}", f"{detail}; {lsof}")


def check_http(results: List[Dict[str, str]], name: str, host: str, port: int, paths: Iterable[str]) -> None:
    attempts = []
    best_status = "FAIL"
    for path in paths:
        ok, detail = http_get(host, port, path)
        attempts.append(detail)
        if not ok:
            continue
        candidate = status_from_http(detail, allow_auth=True, allow_404=True)
        if candidate == "PASS":
            best_status = "PASS"
            break
        if candidate == "WARN" and best_status != "PASS":
            best_status = "WARN"
    add(results, best_status, f"{name} HTTP", " ; ".join(attempts))


def check_gateway_path(results: List[Dict[str, str]], host: str, port: int, path: str) -> None:
    ok, detail = http_get(host, port, path)
    status = status_from_http(detail, allow_auth=True, allow_404=False) if ok else "FAIL"
    add(results, status, "gateway review forwarding", detail)


def check_admin_proxy(results: List[Dict[str, str]], host: str, port: int, proxy_prefix: str, review_path: str) -> None:
    path = proxy_prefix.rstrip("/") + "/" + review_path.lstrip("/")
    ok, detail = http_get(host, port, path)
    status = status_from_http(detail, allow_auth=True, allow_404=False) if ok else "FAIL"
    add(results, status, "admin dev proxy to gateway", detail)


def check_mysql(results: List[Dict[str, str]]) -> None:
    host = env_str("MYSQL_HOST", "127.0.0.1")
    port = env_int("MYSQL_PORT", 3306)
    database = env_str("MYSQL_DATABASE", "jiaoxue_test")
    username = env_str("MYSQL_USERNAME", "dev")
    password = os.environ.get("MYSQL_PASSWORD", "")

    tcp_ok, tcp_detail = socket_check(host, port)
    if not tcp_ok:
        add(results, "FAIL", f"mysql tcp {host}:{port}", tcp_detail)
        return

    try:
        import pymysql  # type: ignore
    except Exception:
        ok, detail = mysql_handshake_check(host, port, username, password, database)
        add(results, "PASS" if ok else "FAIL", f"mysql database {database}", detail)
        return

    if not password:
        add(results, "FAIL", f"mysql database {database}", "MYSQL_PASSWORD is not set")
        return

    try:
        conn = pymysql.connect(
            host=host,
            port=port,
            user=username,
            password=password,
            database=database,
            connect_timeout=5,
            charset="utf8mb4",
        )
        with conn.cursor() as cursor:
            cursor.execute("select database(), current_user()")
            row = cursor.fetchone()
        conn.close()
        add(results, "PASS", f"mysql database {database}", f"connected as {row[1]} using database {row[0]}")
    except Exception as exc:
        add(results, "FAIL", f"mysql database {database}", str(exc))


def _read_mysql_packet(sock: socket.socket) -> Tuple[int, bytes]:
    header = sock.recv(4)
    if len(header) != 4:
        raise RuntimeError("short MySQL packet header")
    length = header[0] | (header[1] << 8) | (header[2] << 16)
    seq = header[3]
    payload = b""
    while len(payload) < length:
        chunk = sock.recv(length - len(payload))
        if not chunk:
            raise RuntimeError("short MySQL packet payload")
        payload += chunk
    return seq, payload


def _write_mysql_packet(sock: socket.socket, seq: int, payload: bytes) -> None:
    length = len(payload)
    header = bytes([length & 0xFF, (length >> 8) & 0xFF, (length >> 16) & 0xFF, seq & 0xFF])
    sock.sendall(header + payload)


def _mysql_error(payload: bytes) -> str:
    if payload and payload[0] == 0xFF and len(payload) >= 3:
        errno = payload[1] | (payload[2] << 8)
        msg = payload[9:].decode("utf-8", errors="replace") if len(payload) > 9 else payload[3:].decode("utf-8", errors="replace")
        return f"MySQL error {errno}: {msg}"
    return f"unexpected MySQL packet {payload[:32]!r}"


def _parse_handshake(payload: bytes) -> Tuple[bytes, str]:
    pos = 0
    if not payload or payload[0] < 10:
        raise RuntimeError("unsupported MySQL handshake")
    pos += 1
    nul = payload.index(b"\x00", pos)
    pos = nul + 1
    pos += 4
    auth_part_1 = payload[pos : pos + 8]
    pos += 8
    pos += 1
    capability_lower = payload[pos] | (payload[pos + 1] << 8)
    pos += 2
    pos += 1
    pos += 2
    capability_upper = payload[pos] | (payload[pos + 1] << 8)
    capability = capability_lower | (capability_upper << 16)
    pos += 2
    auth_plugin_len = payload[pos] if capability & 0x00080000 else 21
    pos += 1
    pos += 10
    auth_part_2_len = max(13, auth_plugin_len - 8)
    auth_part_2 = payload[pos : pos + auth_part_2_len]
    pos += auth_part_2_len
    auth_seed = (auth_part_1 + auth_part_2).split(b"\x00", 1)[0]
    plugin = "mysql_native_password"
    if pos < len(payload):
        plugin_bytes = payload[pos:].split(b"\x00", 1)[0]
        if plugin_bytes:
            plugin = plugin_bytes.decode("ascii", errors="replace")
    return auth_seed, plugin


def _scramble_mysql_native(password: str, seed: bytes) -> bytes:
    if not password:
        return b""
    stage1 = hashlib.sha1(password.encode("utf-8")).digest()
    stage2 = hashlib.sha1(stage1).digest()
    stage3 = hashlib.sha1(seed + stage2).digest()
    return bytes(a ^ b for a, b in zip(stage1, stage3))


def _scramble_caching_sha2(password: str, seed: bytes) -> bytes:
    if not password:
        return b""
    stage1 = hashlib.sha256(password.encode("utf-8")).digest()
    stage2 = hashlib.sha256(stage1).digest()
    stage3 = hashlib.sha256(stage2 + seed).digest()
    return bytes(a ^ b for a, b in zip(stage1, stage3))


def _auth_response(plugin: str, password: str, seed: bytes) -> bytes:
    if plugin == "caching_sha2_password":
        return _scramble_caching_sha2(password, seed)
    return _scramble_mysql_native(password, seed)


def _xor_bytes(left: bytes, right: bytes) -> bytes:
    return bytes(a ^ b for a, b in zip(left, right))


def _xor_password_with_seed(password: str, seed: bytes) -> bytes:
    raw = password.encode("utf-8") + b"\x00"
    repeated_seed = (seed * ((len(raw) // len(seed)) + 1))[: len(raw)]
    return _xor_bytes(raw, repeated_seed)


def _mgf1(seed: bytes, length: int, hash_name: str = "sha1") -> bytes:
    counter = 0
    output = b""
    while len(output) < length:
        output += hashlib.new(hash_name, seed + counter.to_bytes(4, "big")).digest()
        counter += 1
    return output[:length]


def _oaep_encode(message: bytes, key_length: int) -> bytes:
    hash_name = "sha1"
    h_len = hashlib.new(hash_name).digest_size
    if len(message) > key_length - 2 * h_len - 2:
        raise RuntimeError("message too long for RSA key")
    label_hash = hashlib.new(hash_name, b"").digest()
    padding = b"\x00" * (key_length - len(message) - 2 * h_len - 2)
    data_block = label_hash + padding + b"\x01" + message
    seed = os.urandom(h_len)
    db_mask = _mgf1(seed, key_length - h_len - 1, hash_name)
    masked_db = _xor_bytes(data_block, db_mask)
    seed_mask = _mgf1(masked_db, h_len, hash_name)
    masked_seed = _xor_bytes(seed, seed_mask)
    return b"\x00" + masked_seed + masked_db


def _read_der_length(data: bytes, pos: int) -> Tuple[int, int]:
    first = data[pos]
    pos += 1
    if first < 0x80:
        return first, pos
    count = first & 0x7F
    if count == 0:
        raise RuntimeError("indefinite DER length is unsupported")
    length = int.from_bytes(data[pos : pos + count], "big")
    return length, pos + count


def _read_der_tlv(data: bytes, pos: int) -> Tuple[int, bytes, int]:
    tag = data[pos]
    pos += 1
    length, pos = _read_der_length(data, pos)
    value = data[pos : pos + length]
    return tag, value, pos + length


def _parse_public_key_pem(pem: bytes) -> Tuple[int, int]:
    text = pem.decode("ascii", errors="ignore")
    if "-----BEGIN" in text:
        body = "".join(line for line in text.splitlines() if not line.startswith("-----"))
        der = base64.b64decode(body)
    else:
        der = pem

    tag, spki, pos = _read_der_tlv(der, 0)
    if tag != 0x30 or pos != len(der):
        raise RuntimeError("invalid public key DER")
    pos = 0
    tag, _alg, pos = _read_der_tlv(spki, pos)
    if tag != 0x30:
        raise RuntimeError("invalid public key algorithm")
    tag, bit_string, pos = _read_der_tlv(spki, pos)
    if tag != 0x03 or not bit_string:
        raise RuntimeError("invalid public key bit string")
    rsa_der = bit_string[1:]
    tag, rsa_seq, pos = _read_der_tlv(rsa_der, 0)
    if tag != 0x30 or pos != len(rsa_der):
        raise RuntimeError("invalid RSA key")
    pos = 0
    tag, modulus_bytes, pos = _read_der_tlv(rsa_seq, pos)
    if tag != 0x02:
        raise RuntimeError("invalid RSA modulus")
    tag, exponent_bytes, pos = _read_der_tlv(rsa_seq, pos)
    if tag != 0x02:
        raise RuntimeError("invalid RSA exponent")
    modulus = int.from_bytes(modulus_bytes.lstrip(b"\x00"), "big")
    exponent = int.from_bytes(exponent_bytes.lstrip(b"\x00"), "big")
    return modulus, exponent


def _rsa_oaep_encrypt(public_key_pem: bytes, message: bytes) -> bytes:
    modulus, exponent = _parse_public_key_pem(public_key_pem)
    key_length = (modulus.bit_length() + 7) // 8
    encoded = _oaep_encode(message, key_length)
    encrypted = pow(int.from_bytes(encoded, "big"), exponent, modulus)
    return encrypted.to_bytes(key_length, "big")


def mysql_handshake_check(host: str, port: int, username: str, password: str, database: str) -> Tuple[bool, str]:
    if not password:
        return False, "MYSQL_PASSWORD is not set"
    try:
        with socket.create_connection((host, port), timeout=5) as sock:
            _seq, handshake = _read_mysql_packet(sock)
            seed, plugin = _parse_handshake(handshake)
            auth = _auth_response(plugin, password, seed)
            capabilities = (
                0x00000001  # CLIENT_LONG_PASSWORD
                | 0x00000004  # CLIENT_LONG_FLAG
                | 0x00000008  # CLIENT_CONNECT_WITH_DB
                | 0x00000200  # CLIENT_PROTOCOL_41
                | 0x00008000  # CLIENT_SECURE_CONNECTION
                | 0x00020000  # CLIENT_MULTI_RESULTS
                | 0x00080000  # CLIENT_PLUGIN_AUTH
            )
            payload = bytearray()
            payload += capabilities.to_bytes(4, "little")
            payload += (16 * 1024 * 1024).to_bytes(4, "little")
            payload += bytes([45])
            payload += b"\x00" * 23
            payload += username.encode("utf-8") + b"\x00"
            payload += bytes([len(auth)]) + auth
            payload += database.encode("utf-8") + b"\x00"
            payload += plugin.encode("ascii", errors="ignore") + b"\x00"
            _write_mysql_packet(sock, 1, bytes(payload))
            _seq, response = _read_mysql_packet(sock)
            if response and response[0] == 0x00:
                return True, f"authenticated to {database} using MySQL protocol plugin {plugin}"
            if response and response[0] == 0x01 and len(response) > 1 and response[1] == 0x03:
                _seq, response = _read_mysql_packet(sock)
                if response and response[0] == 0x00:
                    return True, f"authenticated to {database} using MySQL protocol plugin {plugin}"
            if response and response[0] == 0xFE:
                switch = response[1:]
                switch_plugin = switch.split(b"\x00", 1)[0].decode("ascii", errors="replace")
                switch_seed = switch.split(b"\x00", 1)[1] if b"\x00" in switch else seed
                _write_mysql_packet(sock, 3, _auth_response(switch_plugin, password, switch_seed))
                _seq, response = _read_mysql_packet(sock)
                if response and response[0] == 0x00:
                    return True, f"authenticated to {database} after auth switch to {switch_plugin}"
                if response and response[0] == 0x01 and len(response) > 1 and response[1] == 0x03:
                    _seq, response = _read_mysql_packet(sock)
                    if response and response[0] == 0x00:
                        return True, f"authenticated to {database} after auth switch to {switch_plugin}"
            if response and response[0] == 0x01 and len(response) > 1 and response[1] == 0x04 and plugin == "caching_sha2_password":
                _write_mysql_packet(sock, 3, b"\x02")
                _seq, public_key_packet = _read_mysql_packet(sock)
                public_key = public_key_packet[1:] if public_key_packet.startswith(b"\x01") else public_key_packet
                encrypted_password = _rsa_oaep_encrypt(public_key, _xor_password_with_seed(password, seed))
                _write_mysql_packet(sock, 5, encrypted_password)
                _seq, response = _read_mysql_packet(sock)
                if response and response[0] == 0x00:
                    return True, f"authenticated to {database} using MySQL protocol plugin {plugin} full auth"
                if response and response[0] == 0x01 and len(response) > 1 and response[1] == 0x03:
                    _seq, response = _read_mysql_packet(sock)
                    if response and response[0] == 0x00:
                        return True, f"authenticated to {database} using MySQL protocol plugin {plugin} full auth"
            return False, _mysql_error(response)
    except Exception as exc:
        return False, str(exc)


def check_redis(results: List[Dict[str, str]]) -> None:
    host = env_str("REDIS_HOST", "127.0.0.1")
    port = env_int("REDIS_PORT", 6379)
    ok, detail = socket_check(host, port)
    if not ok:
        add(results, "FAIL", f"redis {host}:{port}", detail)
        return
    try:
        with socket.create_connection((host, port), timeout=2) as sock:
            sock.sendall(b"*1\r\n$4\r\nPING\r\n")
            resp = sock.recv(32)
        if resp.startswith(b"+PONG"):
            add(results, "PASS", f"redis {host}:{port}", "PING -> PONG")
        else:
            add(results, "WARN", f"redis {host}:{port}", f"TCP ok but unexpected PING response {resp!r}")
    except Exception as exc:
        add(results, "WARN", f"redis {host}:{port}", f"TCP ok but PING failed: {exc}")


def check_nacos(results: List[Dict[str, str]]) -> None:
    host = env_str("NACOS_HOST", "127.0.0.1")
    port = env_int("NACOS_PORT", 8848)
    ok, detail = socket_check(host, port)
    if not ok:
        add(results, "FAIL", f"nacos tcp {host}:{port}", detail)
        return
    paths = [
        "/nacos/v1/console/health/readiness",
        "/nacos",
    ]
    attempts = []
    best = "FAIL"
    for path in paths:
        http_ok, http_detail = http_get(host, port, path)
        attempts.append(http_detail)
        if http_ok:
            candidate = status_from_http(http_detail, allow_auth=True, allow_404=True)
            if candidate == "PASS":
                best = "PASS"
                break
            if candidate == "WARN" and best != "PASS":
                best = "WARN"
    add(results, best, f"nacos http {host}:{port}", " ; ".join(attempts))


def write_report(results: List[Dict[str, str]], report_path: Path) -> None:
    report_path.parent.mkdir(parents=True, exist_ok=True)
    now = dt.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    pass_count = sum(1 for r in results if r["status"] == "PASS")
    warn_count = sum(1 for r in results if r["status"] == "WARN")
    fail_count = sum(1 for r in results if r["status"] == "FAIL")

    lines = [
        "# Review UAT Health Check",
        "",
        f"- Generated at: {now}",
        f"- Summary: PASS {pass_count} / WARN {warn_count} / FAIL {fail_count}",
        f"- Root: `{ROOT_DIR}`",
        "",
        "## Environment",
        "",
        f"- GATEWAY_PORT: `{env_int('GATEWAY_PORT', 9889)}`",
        f"- AUTH_PORT: `{env_int('AUTH_PORT', 9224)}`",
        f"- SYSTEM_PORT: `{env_int('SYSTEM_PORT', 9211)}`",
        f"- COMPETITION_PORT: `{env_int('COMPETITION_PORT', 9205)}`",
        f"- ADMIN_PORT: `{env_int('ADMIN_PORT', 8081)}`",
        f"- MYSQL: `{env_str('MYSQL_USERNAME', 'dev')}@{env_str('MYSQL_HOST', '127.0.0.1')}:{env_int('MYSQL_PORT', 3306)}/{env_str('MYSQL_DATABASE', 'jiaoxue_test')}`",
        f"- NACOS: `{env_str('NACOS_HOST', '127.0.0.1')}:{env_int('NACOS_PORT', 8848)}`",
        f"- REDIS: `{env_str('REDIS_HOST', '127.0.0.1')}:{env_int('REDIS_PORT', 6379)}`",
        "",
        "## Results",
        "",
        "| Status | Item | Detail |",
        "| --- | --- | --- |",
    ]
    for result in results:
        detail = result["detail"].replace("|", "\\|")
        lines.append(f"| {result['status']} | {result['item']} | {detail} |")
    lines.append("")
    report_path.write_text("\n".join(lines), encoding="utf-8")


def print_results(results: List[Dict[str, str]], report_path: Path) -> None:
    for result in results:
        print(f"[{result['status']}] {result['item']} - {result['detail']}")
    print(f"Report: {report_path}")


def main() -> int:
    env_file = Path(os.environ.get("REVIEW_UAT_ENV_FILE", str(DEFAULT_ENV_FILE)))
    load_env_file(env_file)

    host = env_str("CHECK_HOST", "127.0.0.1")
    admin_host = env_str("ADMIN_HOST", host)
    gateway_port = env_int("GATEWAY_PORT", 9889)
    auth_port = env_int("AUTH_PORT", 9224)
    system_port = env_int("SYSTEM_PORT", 9211)
    competition_port = env_int("COMPETITION_PORT", 9205)
    admin_port = env_int("ADMIN_PORT", 8081)
    review_path = env_str("GATEWAY_REVIEW_CHECK_PATH", "/competition/review/activity/list?pageNum=1&pageSize=1")
    admin_proxy_prefix = env_str("ADMIN_PROXY_PREFIX", "/dev-api")
    report_path = Path(os.environ.get("REVIEW_UAT_CHECK_REPORT", str(DEFAULT_REPORT)))

    services = [
        ("gateway", gateway_port, ["/actuator/health", "/"]),
        ("auth", auth_port, ["/actuator/health", "/"]),
        ("system", system_port, ["/actuator/health", "/"]),
        ("competition", competition_port, ["/actuator/health", "/"]),
        ("admin", admin_port, ["/"]),
    ]

    results: List[Dict[str, str]] = []
    for name, port, _paths in services:
        check_port(results, name, admin_host if name == "admin" else host, port)
    for name, port, paths in services:
        check_http(results, name, admin_host if name == "admin" else host, port, paths)

    check_gateway_path(results, host, gateway_port, review_path)
    check_admin_proxy(results, admin_host, admin_port, admin_proxy_prefix, review_path)
    check_mysql(results)
    check_nacos(results)
    check_redis(results)

    write_report(results, report_path)
    print_results(results, report_path)
    return 1 if any(result["status"] == "FAIL" for result in results) else 0


if __name__ == "__main__":
    sys.exit(main())
