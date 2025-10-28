@echo off
echo Generating Release Keystore for NextPlayer...
echo.

"C:\Program Files\Java\jdk-23\bin\keytool.exe" -genkey -v -keystore release-key.keystore -alias nextplayer-release -keyalg RSA -keysize 2048 -validity 10000

echo.
echo Keystore generation completed!
echo The file "release-key.keystore" has been created in your project directory.
echo.
echo IMPORTANT: Remember the passwords you entered!
echo You will need them for ALL future app updates.
echo.
pause 