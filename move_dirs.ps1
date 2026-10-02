$basePath = "d:\AKS PROJECTs\UK Weather Live\app\src\main\java\com\ukweatherlive"
$targetPath = "d:\AKS PROJECTs\UK Weather Live\app\src\main\java\com\ukweatherlive\forecastradar"

if (-not (Test-Path $targetPath)) {
    New-Item -ItemType Directory -Path $targetPath -Force
}

Get-ChildItem -Path $basePath | Where-Object { $_.Name -ne "forecastradar" } | ForEach-Object {
    Move-Item -Path $_.FullName -Destination $targetPath -Force
}
