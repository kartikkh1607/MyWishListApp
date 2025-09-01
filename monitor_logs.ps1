# Log Monitoring Script for MyWishListApp
# This script monitors device logs in real-time to catch crashes

param(
    [string]$DeviceId = "",
    [switch]$ClearFirst = $false
)

Write-Host "🔍 Starting log monitoring for MyWishListApp..." -ForegroundColor Green

# Set up ADB path
$env:PATH += ";E:\Android\platform-tools"

# Check for connected devices
$devices = & adb devices | Select-String -Pattern "device$"
if ($devices.Count -le 1) {
    Write-Host "❌ No devices connected. Please connect your device with USB debugging enabled." -ForegroundColor Red
    exit 1
}

# Get device ID if not specified
if (-not $DeviceId) {
    $deviceList = & adb devices | Select-String -Pattern "\tdevice$"
    if ($deviceList.Count -gt 1) {
        Write-Host "Multiple devices found:" -ForegroundColor Yellow
        $deviceList | ForEach-Object { Write-Host "  $($_.Line.Split("`t")[0])" -ForegroundColor Cyan }
        $DeviceId = Read-Host "Enter device ID (or press Enter for first device)"
        if (-not $DeviceId) {
            $DeviceId = $deviceList[0].Line.Split("`t")[0]
        }
    } else {
        $DeviceId = $deviceList[0].Line.Split("`t")[0]
    }
}

Write-Host "📱 Using device: $DeviceId" -ForegroundColor Cyan

# Clear logs if requested
if ($ClearFirst) {
    Write-Host "🧹 Clearing existing logs..." -ForegroundColor Yellow
    & adb -s $DeviceId logcat -c
}

Write-Host "📋 Monitoring logs for crashes and app activity..." -ForegroundColor Green
Write-Host "Press Ctrl+C to stop monitoring" -ForegroundColor Yellow
Write-Host "---" -ForegroundColor Gray

# Monitor logs in real-time
try {
    & adb -s $DeviceId logcat | Where-Object { 
        $_ -match "MyWishListApp|WishListApp|MainActivity|Graph|AndroidRuntime|FATAL|Exception|Error.*mywishlistapp" 
    } | ForEach-Object {
        $timestamp = Get-Date -Format "HH:mm:ss"
        
        # Color-code log levels
        if ($_ -match "FATAL|AndroidRuntime") {
            Write-Host "[$timestamp] " -NoNewline -ForegroundColor Gray
            Write-Host $_ -ForegroundColor Red
        }
        elseif ($_ -match "ERROR|Exception") {
            Write-Host "[$timestamp] " -NoNewline -ForegroundColor Gray
            Write-Host $_ -ForegroundColor DarkRed
        }
        elseif ($_ -match "WARN") {
            Write-Host "[$timestamp] " -NoNewline -ForegroundColor Gray
            Write-Host $_ -ForegroundColor Yellow
        }
        elseif ($_ -match "DEBUG") {
            Write-Host "[$timestamp] " -NoNewline -ForegroundColor Gray
            Write-Host $_ -ForegroundColor Green
        }
        else {
            Write-Host "[$timestamp] " -NoNewline -ForegroundColor Gray
            Write-Host $_ -ForegroundColor White
        }
    }
}
catch {
    Write-Host "❌ Error monitoring logs: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "🔍 Log monitoring stopped." -ForegroundColor Gray
