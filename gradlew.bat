@echo off
setlocal
where gradle >nul 2>nul
if %errorlevel%==0 (
  gradle %*
  exit /b %errorlevel%
)
set VERSION=8.9
set BASE=%~dp0.gradle-local
set GH=%BASE%\gradle-%VERSION%
set ZIP=%BASE%\gradle-%VERSION%-bin.zip
if not exist "%BASE%" mkdir "%BASE%"
if not exist "%GH%\bin\gradle.bat" (
  if not exist "%ZIP%" powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-8.9-bin.zip' -OutFile '%ZIP%'"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%ZIP%' -DestinationPath '%BASE%' -Force"
)
call "%GH%\bin\gradle.bat" %*
exit /b %errorlevel%
