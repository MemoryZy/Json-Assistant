import argparse
from pathlib import Path
import json
import re

def load_donors(file_path):
    """从源仓库加载 donors.json"""
    with open(file_path, 'r', encoding='utf-8') as f:
        return json.load(f)

def update_support(output_path, start_marker, end_marker, content):
    """修改目标仓库的 Support.md"""
    target_file = Path(output_path)
    original = target_file.read_text(encoding='utf-8')

    # 修复正则表达式语法
    pattern = re.compile(
        rf'({re.escape(start_marker)})(.*?)({re.escape(end_marker)})',
        re.DOTALL
    )
    new_content = pattern.sub(f"{start_marker}\n{content}\n{end_marker}", original)

    target_file.write_text(new_content, encoding='utf-8')

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('--donors', required=True)
    parser.add_argument('--output', required=True)
    args = parser.parse_args()

    donors = sorted(
        load_donors(args.donors),
        key=lambda x: x['total'],
        reverse=True
    )

    # 生成表格
    table = [
        "| 名称 | 网站 | 附言 | 金额 |",
        "| ---- | ---- | ---- | ---- |"
    ]
    for donor in donors:
        row = [
            donor['name'],
            f"[{donor['website']}]({donor['website']})" if donor['website'] else "",
            donor['message'],
            f"{donor['total']:.2f} {donor.get('currency', 'CNY')}"
        ]
        table.append(f"| {' | '.join(row)} |")

    # 写入目标文件
    update_support(
        args.output,
        "<!-- DONORS_TABLE_ZH_START -->",
        "<!-- DONORS_TABLE_ZH_END -->",
        '\n'.join(table)
    )