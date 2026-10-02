import os

project_dir = r"d:\AKS PROJECTs\Skycast\app\src\main\java"
old_import = "import com.ukweatherlive.forecastradar.R"
new_import = "import com.skycast.Localweather.liveForecast.radar.R"

count = 0
for root, dirs, files in os.walk(project_dir):
    for file in files:
        if file.endswith(".kt") or file.endswith(".java"):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            if old_import in content:
                content = content.replace(old_import, new_import)
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                count += 1
                print(f"Updated: {filepath}")

print(f"Update complete. Modified {count} files.")
