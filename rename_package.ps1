$targetPath = "d:\AKS PROJECTs\UK Weather Live\app\src\main\java"
Get-ChildItem -Path $targetPath -Recurse -Filter "*.kt" | ForEach-Object {
    $content = Get-Content $_.FullName
    $newContent = $content -replace "com\.ukweatherlive", "com.ukweatherlive.forecastradar"
    $newContent | Set-Content $_.FullName
    Write-Host "Updated: $($_.Name)"
}
