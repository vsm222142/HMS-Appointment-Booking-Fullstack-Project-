# HMS Backend Start Script
# Port 8080 automatically clear karta hai aur backend start karta hai

Write-Host "=== HMS Backend Starter ===" -ForegroundColor Cyan
Write-Host ""

# Step 1: Port 8080 par chalne wale sab processes band karo
Write-Host "Port 8080 check kar raha hoon..." -ForegroundColor Yellow
$portInfo = netstat -ano | Select-String ":8080 " | Select-String "LISTENING"
if ($portInfo) {
    $pids = $portInfo | ForEach-Object { ($_.ToString().Trim() -split "\s+")[-1] } | Sort-Object -Unique
    foreach ($p in $pids) {
        if ($p -match "^\d+$" -and $p -ne "0") {
            Write-Host "PID $p band kar raha hoon (port 8080)..." -ForegroundColor Red
            taskkill /PID $p /F 2>$null | Out-Null
        }
    }
    Write-Host "Port 8080 free kar diya!" -ForegroundColor Green
    Start-Sleep -Seconds 2
} else {
    Write-Host "Port 8080 already free hai." -ForegroundColor Green
}

Write-Host ""
Write-Host "Backend start ho raha hai..." -ForegroundColor Cyan
Write-Host "(Ctrl+C dabao band karne ke liye)" -ForegroundColor Gray
Write-Host ""

# Step 2: Backend start karo
.\mvnw.cmd spring-boot:run
