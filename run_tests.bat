@echo off
setlocal
if exist out rmdir /s /q out
mkdir out
dir /s /b src\*.java > sources.txt
dir /s /b test\*.java >> sources.txt
javac -encoding UTF-8 -d out @sources.txt
if errorlevel 1 goto :error
del sources.txt
java -cp out ai.SelfTest
if errorlevel 1 goto :error
java -Djava.awt.headless=true -cp out ai.ui.ButtonFocusTest
if errorlevel 1 goto :error
pause
exit /b 0
:error
if exist sources.txt del sources.txt
pause
exit /b 1
