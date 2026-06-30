@echo off
set JAVA_HOME=C:\Program Files\Android\openjdk\jdk-21.0.8
set ANDROID_HOME=C:\Users\user\AppData\Local\Android\Sdk
set PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\platform-tools;%PATH%
cd /d C:\PayBus\android
call gradlew.bat clean assembleDebug
