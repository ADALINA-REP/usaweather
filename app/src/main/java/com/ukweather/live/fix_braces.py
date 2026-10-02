import os

path = r'd:\AKS PROJECTs\Skycast\app\src\main\java\com\skycast\weather\radar\ui\screens\home\HomeScreen.kt'

with open(path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
skip_next = False
for i, line in enumerate(lines):
    if skip_next:
        skip_next = False
        continue
        
    # Find the specific sequence of closing braces in DayDetails
    if i > 1130 and i < 1140 and line.strip() == '}' and lines[i+1].strip() == '}':
        # Check if we have triple }
        if lines[i+2].strip() == '}':
             # We found the problem area
             new_lines.append(line)
             new_lines.append(lines[i+1])
             # Skip the third one
             skip_next = True 
             continue

    new_lines.append(line)

with open(path, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)

print("Done")
