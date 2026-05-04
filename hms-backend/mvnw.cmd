@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script, version 3.3.2
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set WRAPPER_DIR=%~dp0.mvn\wrapper
set WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar
set WRAPPER_PROPERTIES=%WRAPPER_DIR%\maven-wrapper.properties

if not exist "%WRAPPER_PROPERTIES%" (
  echo Maven Wrapper properties not found at "%WRAPPER_PROPERTIES%".
  exit /b 1
)

if not exist "%WRAPPER_JAR%" (
  echo Maven Wrapper jar not found at "%WRAPPER_JAR%".
  echo Downloading...
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$p = (Get-Content '%WRAPPER_PROPERTIES%' | Where-Object { $_ -match '^wrapperUrl=' }) -replace '^wrapperUrl=','';" ^
    "New-Item -ItemType Directory -Force -Path '%WRAPPER_DIR%' | Out-Null;" ^
    "Invoke-WebRequest -UseBasicParsing -Uri $p -OutFile '%WRAPPER_JAR%';"
  if errorlevel 1 exit /b 1
)

set MAVEN_PROJECTBASEDIR=%~dp0
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

set MAVEN_OPTS=%MAVEN_OPTS%

java %MAVEN_OPTS% -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*

endlocal
