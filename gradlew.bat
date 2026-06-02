@rem Gradle wrapper bootstrap script (Windows)
@echo off
setlocal

set APP_HOME=%~dp0
set WRAPPER_JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar

if not exist "%WRAPPER_JAR%" (
    echo Downloading Gradle wrapper JAR...
    mkdir "%APP_HOME%gradle\wrapper" 2>nul
    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/gradle/gradle/raw/v8.1.1/gradle/wrapper/gradle-wrapper.jar' -OutFile '%WRAPPER_JAR%'"
    echo Gradle wrapper JAR downloaded.
)

java -jar "%WRAPPER_JAR%" %*
