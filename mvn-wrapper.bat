@echo off
REM Maven Wrapper for Windows
REM Use this to build without having Maven installed globally
REM Run: mvn-wrapper.bat clean package

set MAVEN_VERSION=3.9.2

if not exist "maven-%MAVEN_VERSION%" (
    echo 🔧 Downloading Maven %MAVEN_VERSION%...
    powershell -Command "(New-Object Net.WebClient).DownloadFile('https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip', 'maven.zip')"
    powershell -Command "Expand-Archive -Path maven.zip -DestinationPath ."
    ren apache-maven-%MAVEN_VERSION% maven-%MAVEN_VERSION%
    del maven.zip
)

REM Run Maven with passed arguments
.\maven-%MAVEN_VERSION%\bin\mvn.cmd %*
