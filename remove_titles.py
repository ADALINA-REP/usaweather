import sys

file_path = r"d:\AKS PROJECTs\UK Weather Live\app\src\main\java\com\ukweatherlive\ui\screens\home\HomeScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if 'SectionTitle("Hourly Trend")' in line:
        continue
    if 'SectionTitle("Air Quality Index")' in line:
        continue
    new_lines.append(line)

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)
