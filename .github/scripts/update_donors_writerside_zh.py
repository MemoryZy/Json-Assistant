import json
import re
from pathlib import Path


def load_donors(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        return json.load(f)


def generate_md_table(donors, headers, columns):
    """智能处理 website 和 total 字段"""
    table = [
        f"| {' | '.join(headers)} |",
        f"| {' | '.join(['---'] * len(headers))} |"
    ]

    for donor in donors:
        row = []
        for col in columns:
            value = str(donor.get(col, ''))

            if col == 'website' and value.startswith(('http://', 'https://')):
                row.append(f"[{value}]({value})")
            elif col == 'total':
                total = f"{donor.get('total', 0):.2f}"
                currency = get_currency(donor)
                row.append(f"{total} {currency}")
            else:
                row.append(value)

        table.append(f"| {' | '.join(row)} |")

    return '\n'.join(table)


def update_support(file_path, start_marker, end_marker, content):
    """保持原有替换逻辑不变"""
    file = Path(file_path)
    original = file.read_text(encoding='utf-8')

    pattern = re.compile(
        rf'({re.escape(start_marker)})(.*?)({re.escape(end_marker)})',
        re.DOTALL
    )
    replacement = f"{start_marker}\n\n{content}\n\n{end_marker}"
    new_text = pattern.sub(replacement, original)

    file.write_text(new_text, encoding='utf-8')


def get_currency(donor):
    # 获取原始值
    raw = donor.get('currency')

    # 处理空值或非字符串类型
    if not isinstance(raw, str):
        return 'CNY'

    # 去除首尾空格
    cleaned = raw.strip()

    # 校验是否为3位大写字母
    if len(cleaned) == 3 and cleaned.isalpha() and cleaned.isupper():
        return cleaned
    else:
        return 'CNY'  # 默认值


if __name__ == "__main__":
    donors = sorted(
        load_donors(Path('donors.json')),
        key=lambda x: x['total'],
        reverse=True
    )

    configs = [
        {
            "file": "Writerside/topics/Support.md",
            "markers": ("<!-- DONORS_TABLE_ZH_START -->", "<!-- DONORS_TABLE_ZH_END -->"),
            "headers": ["名称", "网站", "附言", "金额"],
            "columns": ["name", "website", "message", "total"]
        }
    ]

    for cfg in configs:
        table = generate_md_table(donors, cfg["headers"], cfg["columns"])
        update_support(cfg["file"], *cfg["markers"], table)
