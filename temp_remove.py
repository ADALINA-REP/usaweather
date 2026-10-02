import sys

file_path = r"d:\AKS PROJECTs\UK Weather Live\app\src\main\java\com\ukweatherlive\ui\screens\home\HomeScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Spacer cleanup (indices 300 and 301 are duplicate spacers)
del lines[301]

# Activity Insights removal
# SectionTitle starts at 321 (index 320 before previous deletion, now 320-1=319)
# GlassCard ends at 411 (index 410 before previous deletion, now 410-1=409)
# So delete from 319 to 410 inclusive
del lines[319:410]

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(lines)
