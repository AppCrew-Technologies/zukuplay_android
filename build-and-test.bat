@echo off
echo ========================================
echo Zukuplay Build and Test Script
echo ========================================
echo.

echo [1/6] Cleaning project...
call gradlew clean
if %errorlevel% neq 0 (
    echo ❌ Clean failed!
    pause
    exit /b 1
)
echo ✅ Clean completed

echo.
echo [2/6] Building debug version...
call gradlew assembleDebug
if %errorlevel% neq 0 (
    echo ❌ Debug build failed!
    pause
    exit /b 1
)
echo ✅ Debug build completed

echo.
echo [3/6] Building release version...
call gradlew assembleRelease
if %errorlevel% neq 0 (
    echo ❌ Release build failed!
    pause
    exit /b 1
)
echo ✅ Release build completed

echo.
echo [4/6] Building release bundle...
call gradlew bundleRelease
if %errorlevel% neq 0 (
    echo ❌ Bundle build failed!
    pause
    exit /b 1
)
echo ✅ Bundle build completed

echo.
echo [5/6] Running tests...
call gradlew test
if %errorlevel% neq 0 (
    echo ❌ Tests failed!
    pause
    exit /b 1
)
echo ✅ Tests completed

echo.
echo [6/6] Build summary...
echo.
echo 📱 Debug APK: app\build\outputs\apk\debug\app-debug.apk
echo 📱 Release APK: app\build\outputs\apk\release\app-release.apk
echo 📦 Release Bundle: app\build\outputs\bundle\release\app-release.aab
echo.
echo ========================================
echo 🎉 All builds completed successfully!
echo ========================================
echo.
echo Next steps:
echo 1. Test debug APK on device
echo 2. Test release APK on device  
echo 3. Test release bundle installation
echo 4. Submit bundle to Play Store
echo.
pause
