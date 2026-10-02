$resPath = "d:\AKS PROJECTs\Skycast\app\src\main\res"

$translations = @{
    "values" = "SkyCast: Accuracy in every drop.";
    "values-ar" = "سكاي كاست: الدقة في كل قطرة.";
    "values-bn" = "স্কাইকাস্ট: প্রতি ফোঁটায় নির্ভুলতা।";
    "values-de" = "SkyCast: Präzision in jedem Tropfen.";
    "values-el" = "SkyCast: Ακρίβεια σε κάθε σταγόνα.";
    "values-es" = "SkyCast: Precisión en cada gota.";
    "values-fr" = "SkyCast : La précision dans chaque goutte.";
    "values-hi" = "स्काईकास्ट: हर बूंद में सटीकता।";
    "values-it" = "SkyCast: Precisione in ogni goccia.";
    "values-ja" = "SkyCast：一滴一滴に正確さを。";
    "values-ko" = "스카이캐스트: 모든 빗방울에 담긴 정확성.";
    "values-nl" = "SkyCast: Nauwkeurigheid in elke druppel.";
    "values-pl" = "SkyCast: Dokładność w każdej kropli.";
    "values-pt" = "SkyCast: Precisão em cada gota.";
    "values-ru" = "SkyCast: Точность в каждой капле.";
    "values-sv" = "SkyCast: Noggrannhet i varje droppe.";
    "values-th" = "สกายแคสต์: ความแม่นยำในทุกหยาดหยด";
    "values-tr" = "SkyCast: Her damlada doğruluk.";
    "values-vi" = "SkyCast: Độ chính xác trong từng giọt nước.";
    "values-zh" = "SkyCast：每一滴都精准。"
}

foreach ($folder in $translations.Keys) {
    $stringsFile = Join-Path $resPath $folder "strings.xml"
    if (Test-Path $stringsFile) {
        $content = Get-Content $stringsFile -Raw -Encoding utf8
        $val = $translations[$folder].Replace("&", "&amp;")
        
        if ($content -notlike "*name=`"app_slogan`"*") {
            $addText = "`n  <string name=`"app_slogan`">$val</string>`n"
            $content = $content.Replace("</resources>", "$addText</resources>")
            Set-Content $stringsFile $content -Encoding utf8
            Write-Host "Added app_slogan to $folder"
        } else {
            # Update existing if needed (regex replace)
            $content = $content -replace '<string name="app_slogan">.*?</string>', "<string name=`"app_slogan`">$val</string>"
            Set-Content $stringsFile $content -Encoding utf8
            Write-Host "Updated app_slogan in $folder"
        }
    }
}
