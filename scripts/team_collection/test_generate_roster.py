import tempfile
import unittest
from pathlib import Path
from xml.sax.saxutils import escape
from zipfile import ZipFile
from generate_roster import generate, quote, read_roster


def fixture(path, duplicate=False, formula=False):
    ns = 'http://schemas.openxmlformats.org/spreadsheetml/2006/main'
    rel = 'http://schemas.openxmlformats.org/officeDocument/2006/relationships'
    titles = ['赛道一名单', '赛道二名单', '赛道三名单']
    with ZipFile(path, 'w') as z:
        sheets = ''.join(f'<sheet name="{title}" sheetId="{i}" r:id="r{i}"/>' for i, title in enumerate(titles, 1))
        z.writestr('xl/workbook.xml', f'<workbook xmlns="{ns}" xmlns:r="{rel}"><sheets>{sheets}</sheets></workbook>')
        z.writestr('xl/_rels/workbook.xml.rels', '<Relationships>' + ''.join(f'<Relationship Id="r{i}" Target="worksheets/sheet{i}.xml"/>' for i in range(1,4)) + '</Relationships>')
        for i in range(1,4):
            data = [('A1','队伍ID'),('B1','姓名1'),('C1','教师1'),('D1','奖项'),('A2',f'TEAM_{1 if duplicate else i}'),('B2',"测试'学生\\"),('C2','禁止导入的教师'),('D2','一等奖')]
            rows = []
            for row in [1,2]:
                cells = ''.join(f'<c r="{cell}" t="inlineStr"><is><t>{escape(value)}</t></is>{"<f>1+1</f>" if formula and cell=="B2" else ""}</c>' for cell,value in data if cell.endswith(str(row)))
                rows.append(f'<row r="{row}">{cells}</row>')
            z.writestr(f'xl/worksheets/sheet{i}.xml', f'<worksheet xmlns="{ns}"><sheetData>{"".join(rows)}</sheetData></worksheet>')


class RosterTests(unittest.TestCase):
    def test_only_student_columns_and_deterministic_safe_sql(self):
        with tempfile.TemporaryDirectory() as directory:
            source = Path(directory)/'source.xlsx'; fixture(source)
            out = Path(directory)/'out'
            result = generate(source,out,'SYNTHETIC')
            self.assertEqual((result['teams'],result['student_cells']),(3,3))
            sql = (out/'01_source.sql').read_bytes()
            self.assertNotIn(quote('禁止导入的教师').encode(),sql)
            self.assertIn(quote("测试'学生\\").encode(),sql)
            self.assertNotIn(b'v1_team_collection_member',sql)
            generate(source,out,'SYNTHETIC')
            self.assertEqual(sql,(out/'01_source.sql').read_bytes())

    def test_duplicate_teams_are_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            source=Path(directory)/'source.xlsx'; fixture(source,duplicate=True)
            with self.assertRaises(ValueError): read_roster(source)

    def test_formulas_are_rejected_in_authorization_input(self):
        with tempfile.TemporaryDirectory() as directory:
            source=Path(directory)/'source.xlsx'; fixture(source,formula=True)
            with self.assertRaises(ValueError): read_roster(source)

    def test_collection_code_cannot_inject_sql(self):
        with self.assertRaises(ValueError): generate(Path('unused'),Path('unused'),"';DROP TABLE x")


if __name__ == '__main__': unittest.main()
