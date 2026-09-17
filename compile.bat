@echo off
setlocal enabledelayedexpansion

echo ==================================================
echo Compiling Smart Library Management System...
echo ==================================================

if not exist bin mkdir bin

dir /s /b src\*.java > sources.txt

javac -encoding UTF-8 -cp "lib/sqlite-jdbc.jar" -d bin @sources.txt
set COMPILE_STATUS=%ERRORLEVEL%

if exist sources.txt del sources.txt

if %COMPILE_STATUS% EQU 0 (
    echo [SUCCESS] Compilation completed successfully into bin/
) else (
    echo [ERROR] Compilation failed with status %COMPILE_STATUS%
)

exit /b %COMPILE_STATUS%
