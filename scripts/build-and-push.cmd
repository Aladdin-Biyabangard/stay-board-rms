@echo off
setlocal EnableExtensions

set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

where pwsh >nul 2>nul
if %ERRORLEVEL% equ 0 (
  pwsh -NoProfile -File "%SCRIPT_DIR%\build-and-push.ps1" %*
  exit /b %ERRORLEVEL%
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%\build-and-push.ps1" %*
exit /b %ERRORLEVEL%
