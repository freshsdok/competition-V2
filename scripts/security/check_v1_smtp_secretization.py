#!/usr/bin/env python3
"""验证 V1 SMTP 配置只引用受控环境变量，且不输出任何配置值。"""

from pathlib import Path
import sys


REPOSITORY_ROOT = Path(__file__).resolve().parents[2]
MODULES_ROOT = (
    REPOSITORY_ROOT
    / "old-code"
    / "teaching-modules"
)
CONFIG_FILES = (
    "teaching-system/src/main/resources/bootstrap-dev.yml",
    "teaching-system/src/main/resources/bootstrap-test.yml",
    "teaching-system/src/main/resources/bootstrap-cluster.yml",
    "teaching-system/src/main/resources/bootstrap-filing.yml",
    "teaching-wxApp/src/main/resources/bootstrap-dev.yml",
    "teaching-wxApp/src/main/resources/bootstrap-test.yml",
    "teaching-wxApp/src/main/resources/bootstrap-cluster.yml",
    "teaching-wxApp/src/main/resources/bootstrap-filing.yml",
)
EXPECTED_LINE = "password: ${V1_SMTP_PASSWORD:}"


def main() -> int:
    failures: list[str] = []
    for file_name in CONFIG_FILES:
        path = MODULES_ROOT / file_name
        if not path.is_file():
            failures.append(f"{file_name}: MISSING")
            continue
        password_lines = [
            line.strip()
            for line in path.read_text(encoding="utf-8").splitlines()
            if line.strip().startswith("password:")
        ]
        if password_lines != [EXPECTED_LINE]:
            # 失败信息只报告文件和分类，避免把意外重新提交的秘密值打印到 CI 日志。
            failures.append(f"{file_name}: SMTP_PASSWORD_NOT_SECRETIZED")

    if failures:
        print("V1_SMTP_SECRETIZATION=FAIL")
        for failure in failures:
            print(failure)
        return 1

    print("V1_SMTP_SECRETIZATION=PASS")
    print(f"CHECKED_CONFIG_COUNT={len(CONFIG_FILES)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
