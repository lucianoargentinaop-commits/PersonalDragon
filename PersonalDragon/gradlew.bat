@echo off
set DIR=%~dp0
set WRAPPER_JAR=%DIR%gradle\wrapper\gradle-wrapper.jar

if not exist "%WRAPPER_JAR%" (
  echo gradle-wrapper.jar no esta incluido. Descargalo con Gradle o usa un wrapper oficial.
  echo Recomendado: instala Gradle 8+ y ejecuta: gradle wrapper --gradle-version 8.8
  exit /b 1
)

java -jar "%WRAPPER_JAR%" %*
