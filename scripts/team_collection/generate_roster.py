#!/usr/bin/env python3
"""Read an XLSX roster; emit V1 staging SQL only. No DB access and no workbook writes."""
import argparse
import hashlib
import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path
from zipfile import ZipFile

NS = {"x": "http://schemas.openxmlformats.org/spreadsheetml/2006/main"}
REL = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"


def quote(value):
    # Hex literals preserve Unicode/apostrophes/backslashes regardless of SQL mode.
    return "CONVERT(0x" + str(value).encode("utf-8").hex() + " USING utf8mb4)"


def read_roster(path):
    digest = hashlib.sha256(Path(path).read_bytes()).hexdigest()
    teams, students, sheets = set(), [], []
    with ZipFile(path) as archive:
        strings = []
        if "xl/sharedStrings.xml" in archive.namelist():
            for si in ET.fromstring(archive.read("xl/sharedStrings.xml")):
                strings.append("".join(si.itertext()))
        relations = {r.attrib["Id"]: r.attrib["Target"] for r in ET.fromstring(archive.read("xl/_rels/workbook.xml.rels"))}
        for sheet in ET.fromstring(archive.read("xl/workbook.xml")).findall("x:sheets/x:sheet", NS):
            title = sheet.attrib["name"]
            if title not in {"赛道一名单", "赛道二名单", "赛道三名单"}:
                continue
            target = relations[sheet.attrib[f"{{{REL}}}id"]]
            target = target.lstrip("/") if target.startswith("/") else "xl/" + target
            rows = []
            for row in ET.fromstring(archive.read(target)).findall("x:sheetData/x:row", NS):
                values = {}
                for cell in row.findall("x:c", NS):
                    if cell.find("x:f", NS) is not None:
                        raise ValueError(f"Formula in roster: {title}!{cell.attrib['r']}")
                    raw = cell.findtext("x:v", "", NS)
                    kind = cell.attrib.get("t")
                    if kind == "s": raw = strings[int(raw)]
                    elif kind == "inlineStr": raw = "".join(cell.find("x:is", NS).itertext())
                    values[re.sub(r"\d", "", cell.attrib["r"])] = raw.strip()
                rows.append((int(row.attrib["r"]), values))
            if not rows or rows[0][1].get("A") != "队伍ID":
                raise ValueError(f"Unexpected header: {title}")
            header = rows[0][1]
            name_columns = [c for c, label in header.items() if re.fullmatch(r"姓名[1-6]", label)]
            award_columns = [c for c, label in header.items() if label == "奖项"]
            if not name_columns or not award_columns: raise ValueError(f"Missing columns: {title}")
            team_count, student_count = 0, 0
            for number, values in rows[1:]:
                if not any(values.values()): continue
                team = values.get("A", "")
                if not re.fullmatch(r"[A-Za-z0-9_-]{1,100}", team) or team in teams:
                    raise ValueError(f"Missing/duplicate/invalid team: {title}!A{number}")
                awards = {values.get(c, "") for c in award_columns}
                if len(awards) != 1 or next(iter(awards)) not in {"一等奖", "二等奖"}:
                    raise ValueError(f"Unexpected/conflicting awards: {title}:{number}")
                names = [(c, values[c]) for c in name_columns if values.get(c)]
                if not names: raise ValueError(f"No students: {title}:{number}")
                teams.add(team)
                team_count += 1
                for column, name in names:
                    if len(name) > 100: raise ValueError("Student name too long")
                    students.append((digest, title, number, column, team, name, next(iter(awards))))
                    student_count += 1
            sheets.append({"sheet": title, "teams": team_count, "student_cells": student_count})
    if len(sheets) != 3: raise ValueError("Expected all three roster sheets")
    return students, {"sha256": digest, "teams": len(teams), "student_cells": len(students), "sheets": sheets}


def generate(source, output, collection):
    if not re.fullmatch(r"[A-Za-z0-9_-]{1,80}", collection): raise ValueError("Invalid collection code")
    students, summary = read_roster(source)
    output.mkdir(parents=True, exist_ok=True)
    lines = ["-- Generated source staging ONLY. Does not authorize any user or enable any team.",
             "-- Re-run keeps existing identical source positions; never resets active handling state.",
             "SET NAMES utf8mb4;", "START TRANSACTION;"]
    for student in students:
        values = [quote(collection)] + [str(x) if isinstance(x, int) else quote(x) for x in student]
        lines.append("INSERT INTO v1_team_collection_source(collection_code,source_sha256,source_sheet,source_row,source_column,team_code,student_name,award_level) VALUES(" + ",".join(values) + ") ON DUPLICATE KEY UPDATE source_sha256=VALUES(source_sha256);")
    lines.append("COMMIT;")
    (output / "01_source.sql").write_text("\n".join(lines) + "\n", encoding="utf-8")
    summary["collection_code"] = collection
    (output / "source-summary.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return summary


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("source", type=Path)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--collection", required=True)
    args = parser.parse_args()
    print(json.dumps(generate(args.source, args.output, args.collection), ensure_ascii=False))
