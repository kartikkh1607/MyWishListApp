# Debug Build Script for MyWishListApp
# This script builds a debug version with enhanced logging

Write-Host "Starting debug build for MyWishListApp..." -ForegroundColor Green

# Set environment variables
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_SDK_ROOT = "E:\Android"
$env:PATH += ";E:\Android\platform-tools"

# Clean previous builds
Write-Host "Cleaning previous builds..." -ForegroundColor Yellow
& ./gradlew clean

# Build debug APK
Write-Host "Building debug APK with enhanced logging..." -ForegroundColor Yellow
& ./gradlew assembleDebug --stacktrace --info

# Check if build was successful
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Debug build completed successfully!" -ForegroundColor Green
    
    # Find the APK file
    $apkPath = Get-ChildItem -Path "app\build\outputs\apk\debug\" -Filter "*.apk" | Select-Object -First 1
    if ($apkPath) {
        Write-Host "📱 APK Location: $($apkPath.FullName)" -ForegroundColor Cyan
        
        # Install on device if connected
        $devices = & adb devices | Select-String -Pattern "device$"
        if ($devices.Count -gt 1) {  # More than just the header line
            Write-Host "📲 Installing APK on connected device..." -ForegroundColor Yellow
            & adb install -r $apkPath.FullName
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "✅ APK installed successfully!" -ForegroundColor Green
                Write-Host "🔍 To monitor logs, run: adb logcat -s WishListApp MainActivity Graph" -ForegroundColor Cyan
            } else {
                Write-Host "❌ Failed to install APK" -ForegroundColor Red
            }
        } else {
            Write-Host "⚠️ No device connected. Connect your device and run 'adb install -r $($apkPath.FullName)'" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "❌ Build failed!" -ForegroundColor Red
    Write-Host "Check the build output above for errors." -ForegroundColor Yellow
}

Write-Host "Done!" -ForegroundColor Green
