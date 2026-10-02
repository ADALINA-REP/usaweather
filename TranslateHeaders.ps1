$resPath = "d:\AKS PROJECTs\Skycast\app\src\main\res"

$translations = @{
    "values" = @{
        "health_environment" = "Health & Environment";
        "sport_outdoor" = "Sport & Outdoor";
        "solar_cycle" = "Solar Cycle";
        "moonrise_moonset" = "MOONRISE & MOONSET"
    };
    "values-ar" = @{ "health_environment" = "الصحة والبيئة"; "sport_outdoor" = "الرياضة والهواء الطلق"; "solar_cycle" = "الدورة الشمسية"; "moonrise_moonset" = "شروق وغروب القمر" };
    "values-bn" = @{ "health_environment" = "স্বাস্থ্য ও পরিবেশ"; "sport_outdoor" = "খেলাধুลา ও আউটডোর"; "solar_cycle" = "সৌর চক্র"; "moonrise_moonset" = "চাঁদ ওঠা ও অস্ত যাওয়া" };
    "values-de" = @{ "health_environment" = "Gesundheit & Umwelt"; "sport_outdoor" = "Sport & Outdoor"; "solar_cycle" = "Sonnenzyklus"; "moonrise_moonset" = "MOND-AUFGANG & UNTERGANG" };
    "values-el" = @{ "health_environment" = "Υγεία & Περιβάλλον"; "sport_outdoor" = "Σπορ & Ύπαιθρο"; "solar_cycle" = "Ηλιακός Κύκλος"; "moonrise_moonset" = "ΑΝΑΤΟΛΗ & ΔΥΣΗ ΣΕΛΗΝΗΣ" };
    "values-es" = @{ "health_environment" = "Salud y Medio Ambiente"; "sport_outdoor" = "Deporte y Aire Libre"; "solar_cycle" = "Ciclo Solar"; "moonrise_moonset" = "SALIDA Y PUESTA DE LUNA" };
    "values-fr" = @{ "health_environment" = "Santé & Environnement"; "sport_outdoor" = "Sport & Plein air"; "solar_cycle" = "Cycle Solaire"; "moonrise_moonset" = "LEVER & COUCHER DE LUNE" };
    "values-hi" = @{ "health_environment" = "स्वास्थ्य और पर्यावरण"; "sport_outdoor" = "खेल और आउटडोर"; "solar_cycle" = "सौर चक्र"; "moonrise_moonset" = "चंद्रोदय और चंद्रास्त" };
    "values-it" = @{ "health_environment" = "Salute e Ambiente"; "sport_outdoor" = "Sport e Outdoor"; "solar_cycle" = "Ciclo Solare"; "moonrise_moonset" = "SORGE E TRAMONTA LA LUNA" };
    "values-ja" = @{ "health_environment" = "健康と環境"; "sport_outdoor" = "スポーツとアウトドア"; "solar_cycle" = "ソーラーサイクル"; "moonrise_moonset" = "月の出と月の入り" };
    "values-ko" = @{ "health_environment" = "건강과 환경"; "sport_outdoor" = "스포츠 및 야외 활동"; "solar_cycle" = "태양 주기"; "moonrise_moonset" = "월출과 월몰" };
    "values-nl" = @{ "health_environment" = "Gezondheid & Milieu"; "sport_outdoor" = "Sport & Outdoor"; "solar_cycle" = "Zonnecyclus"; "moonrise_moonset" = "MAANSOPKOMST & ONDERGANG" };
    "values-pl" = @{ "health_environment" = "Zdrowie i Środowisko"; "sport_outdoor" = "Sport i Rekreacja"; "solar_cycle" = "Cykl Słoneczny"; "moonrise_moonset" = "WSCHÓD I ZACHÓD KSIĘŻYCA" };
    "values-pt" = @{ "health_environment" = "Saúde e Meio Ambiente"; "sport_outdoor" = "Esporte e Atividades ao Ar Livre"; "solar_cycle" = "Ciclo Solar"; "moonrise_moonset" = "NASCER E PÔR DA LUA" };
    "values-ru" = @{ "health_environment" = "Здоровье и Окружающая Среда"; "sport_outdoor" = "Спорт и Отдых"; "solar_cycle" = "Солнечный Цикл"; "moonrise_moonset" = "ВОСХОД И ЗАХОД ЛУНЫ" };
    "values-sv" = @{ "health_environment" = "Hälsa & Miljö"; "sport_outdoor" = "Sport & Utomhus"; "solar_cycle" = "Solcykel"; "moonrise_moonset" = "MÅNUPPGÅNG & MÅNNEDGÅNG" };
    "values-th" = @{ "health_environment" = "สุขภาพและสิ่งแวดล้อม"; "sport_outdoor" = "กีฬาและกิจกรรมกลางแจ้ง"; "solar_cycle" = "วัฏจักรสุริยะ"; "moonrise_moonset" = "พระจันทร์ขึ้นและตก" };
    "values-tr" = @{ "health_environment" = "Sağlık ve Çevre"; "sport_outdoor" = "Spor ve Dış Mekan"; "solar_cycle" = "Güneş Döngüsü"; "moonrise_moonset" = "AY DOĞUMU VE BATIMI" };
    "values-vi" = @{ "health_environment" = "Sức khỏe & Môi trường"; "sport_outdoor" = "Thể thao & Ngoài trời"; "solar_cycle" = "Chu kỳ Mặt trời"; "moonrise_moonset" = "TRĂNG MỌC & TRĂNG LẶN" };
    "values-zh" = @{ "health_environment" = "健康与环境"; "sport_outdoor" = "运动与户外"; "solar_cycle" = "太阳周期"; "moonrise_moonset" = "月出与月落" }
}

foreach ($folder in $translations.Keys) {
    $stringsFile = Join-Path $resPath $folder "strings.xml"
    if (Test-Path $stringsFile) {
        $content = Get-Content $stringsFile -Raw -Encoding utf8
        $newLines = @()
        $items = $translations[$folder]
        foreach ($key in $items.Keys) {
            if ($content -notlike "*name=`"$key`"*") {
                $val = $items[$key].Replace("&", "&amp;")
                $newLines += "  <string name=`"$key`">$val</string>"
            }
        }
        if ($newLines.Count -gt 0) {
            $addText = ($newLines -join "`n") + "`n"
            $content = $content.Replace("</resources>", "$addText</resources>")
            Set-Content $stringsFile $content -Encoding utf8
            Write-Host "Updated $folder"
        }
    }
}
