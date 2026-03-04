@echo off
setlocal EnableDelayedExpansion

:: ============================================================
::  OceanViewResort - Automated Build & Deployment Script
::  Target Environment: XAMPP Tomcat & LENOVO Maven
:: ============================================================

:: -------------------------------------------------------
:: SECTION 0 - ANSI COLOR SUPPORT
:: -------------------------------------------------------
for /f %%a in ('echo prompt $E ^| cmd') do set "ESC=%%a"
set "CLR_RESET=%ESC%[0m"
set "CLR_GREEN=%ESC%[92m"
set "CLR_YELLOW=%ESC%[93m"
set "CLR_RED=%ESC%[91m"
set "CLR_CYAN=%ESC%[96m"
set "CLR_WHITE=%ESC%[97m"
set "CLR_MAGENTA=%ESC%[95m"

:: -------------------------------------------------------
:: SECTION 1 - USER-CONFIGURABLE VARIABLES (XAMPP UPDATED)
:: -------------------------------------------------------
set "TOMCAT_HOME=C:\xampp\tomcat"
set "MAVEN_BIN_PATH=C:\Users\LENOVO\.maven\maven-3.9.12\bin"

:: Derived variables
set "PROJECT_DIR=%~dp0"
set "PROJECT_DIR=%PROJECT_DIR:~0,-1%"
set "ARTIFACT_ID=OceanViewResort"
set "VERSION=1.0-SNAPSHOT"
set "WAR_FILE=%ARTIFACT_ID%-%VERSION%.war"
set "WAR_SOURCE=%PROJECT_DIR%\target\%WAR_FILE%"
set "WEBAPPS_DIR=%TOMCAT_HOME%\webapps"
set "WAR_DEST=%WEBAPPS_DIR%\%WAR_FILE%"
set "EXPLODED_DIR=%WEBAPPS_DIR%\%ARTIFACT_ID%-%VERSION%"
set "TOMCAT_BIN=%TOMCAT_HOME%\bin"
set "APP_URL=http://localhost:8080/%ARTIFACT_ID%-%VERSION%/"

:: -------------------------------------------------------
:: BANNER
:: -------------------------------------------------------
echo.
echo %CLR_CYAN%================================================================%CLR_RESET%
echo %CLR_CYAN%        OceanViewResort ^| Build ^& Deployment Automation          %CLR_RESET%
echo %CLR_CYAN%================================================================%CLR_RESET%
echo %CLR_WHITE%  Project : %ARTIFACT_ID% v%VERSION%%CLR_RESET%
echo %CLR_WHITE%  Tomcat  : %TOMCAT_HOME% (XAMPP)%CLR_RESET%
echo %CLR_WHITE%  Maven   : %MAVEN_BIN_PATH%%CLR_RESET%
echo %CLR_CYAN%================================================================%CLR_RESET%
echo.

:: ================================================================
:: PHASE 1 — ENVIRONMENT SETUP
:: ================================================================
echo %CLR_MAGENTA%[PHASE 1]%CLR_RESET% %CLR_WHITE%Environment Setup%CLR_RESET%
echo %CLR_CYAN%----------------------------------------------------------------%CLR_RESET%

:: Add Maven bin to PATH for this session
set "PATH=%MAVEN_BIN_PATH%;%PATH%"

:: --- Check Java ---
echo    ^> Checking Java...
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo    %CLR_RED%[ERROR]%CLR_RESET% Java is not accessible.
    goto :ErrorExit
)
echo    %CLR_GREEN%[OK]%CLR_RESET%    Java is accessible.

:: --- Check Maven ---
echo    ^> Checking Maven...
call mvn -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo    %CLR_RED%[ERROR]%CLR_RESET% Maven is not accessible at: %MAVEN_BIN_PATH%
    goto :ErrorExit
)
echo    %CLR_GREEN%[OK]%CLR_RESET%    Maven is accessible.

:: --- Check Tomcat (XAMPP) ---
echo    ^> Checking Tomcat...
if not exist "%TOMCAT_HOME%" (
    echo    %CLR_RED%[ERROR]%CLR_RESET% XAMPP Tomcat directory not found at: %TOMCAT_HOME%
    goto :ErrorExit
)
echo    %CLR_GREEN%[OK]%CLR_RESET%    XAMPP Tomcat found.
echo.

:: ================================================================
:: PHASE 2 — BUILD
:: ================================================================
echo %CLR_MAGENTA%[PHASE 2]%CLR_RESET% %CLR_WHITE%Maven Build%CLR_RESET%
echo %CLR_CYAN%----------------------------------------------------------------%CLR_RESET%
cd /d "%PROJECT_DIR%"
echo    ^> Running clean package...
call mvn clean package -DskipTests

if %ERRORLEVEL% neq 0 (
    echo.
    echo    %CLR_RED%[BUILD FAILED]%CLR_RESET% Maven build encountered an error.
    goto :ErrorExit
)

if not exist "%WAR_SOURCE%" (
    echo    %CLR_RED%[ERROR]%CLR_RESET% WAR file not found. Check if pom.xml has ^<packaging^>war^</packaging^>.
    goto :ErrorExit
)
echo.
echo    %CLR_GREEN%[OK]%CLR_RESET%    WAR file created: %WAR_FILE%
echo.

:: ================================================================
:: PHASE 3 — CLEAN DEPLOYMENT
:: ================================================================
echo %CLR_MAGENTA%[PHASE 3]%CLR_RESET% %CLR_WHITE%Cleaning Old Artifacts%CLR_RESET%
echo %CLR_CYAN%----------------------------------------------------------------%CLR_RESET%

if exist "%WAR_DEST%" (
    echo    ^> Deleting old WAR file...
    del /f /q "%WAR_DEST%"
)
if exist "%EXPLODED_DIR%" (
    echo    ^> Removing old exploded folder...
    rd /s /q "%EXPLODED_DIR%"
)
echo    %CLR_GREEN%[OK]%CLR_RESET%    Deployment area cleaned.
echo.

:: ================================================================
:: PHASE 4 — DEPLOY & START
:: ================================================================
echo %CLR_MAGENTA%[PHASE 4]%CLR_RESET% %CLR_WHITE%Deploy ^& Start Server%CLR_RESET%
echo %CLR_CYAN%----------------------------------------------------------------%CLR_RESET%

echo    ^> Copying WAR to XAMPP webapps...
copy /y "%WAR_SOURCE%" "%WAR_DEST%" >nul
if %ERRORLEVEL% neq 0 (
    echo    %CLR_RED%[ERROR]%CLR_RESET% Failed to copy WAR. Check folder permissions.
    goto :ErrorExit
)

:: Check if port 8080 is active
netstat -ano | findstr ":8080" | findstr "LISTENING" >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo    %CLR_YELLOW%[INFO]%CLR_RESET%  Tomcat is already running. It will auto-deploy the new WAR.
) else (
    echo    ^> Starting XAMPP Tomcat...
    start "Tomcat Server" /d "%TOMCAT_BIN%" cmd /c startup.bat
)

echo.
echo    ^> Waiting 10 seconds for XAMPP to deploy the app...
timeout /t 10 /nobreak >nul
echo.

:: ================================================================
:: PHASE 5 — LAUNCH
:: ================================================================
echo %CLR_MAGENTA%[PHASE 5]%CLR_RESET% %CLR_WHITE%Launch Browser%CLR_RESET%
echo %CLR_CYAN%----------------------------------------------------------------%CLR_RESET%
start "" "%APP_URL%"
echo    %CLR_GREEN%[OK]%CLR_RESET%    Opening %APP_URL%
echo.

echo %CLR_GREEN%================================================================%CLR_RESET%
echo %CLR_GREEN%          DEPLOYMENT SUCCESSFUL!                                %CLR_RESET%
echo %CLR_GREEN%================================================================%CLR_RESET%
echo.
pause
goto :EOF

:ErrorExit
echo.
echo %CLR_RED%[FAILED] Deployment aborted. Check the steps above.%CLR_RESET%
echo.
pause
exit /b 1