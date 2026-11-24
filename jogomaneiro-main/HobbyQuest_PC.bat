@echo off
title Hobby Quest - Aplicativo Desktop
color 0A

echo =======================================================
echo         HOBBY QUEST - Aplicativo Desktop PC
echo =======================================================
echo.
echo Iniciando o aplicativo desktop Hobby Quest...
echo.

cd /d "%~dp0"

REM Detecta o Java JDK 22 (ou usa o do PATH como fallback)
set "JAVA_EXE=javac"
set "JAVAW_EXE=javaw"

if exist "C:\Program Files\Java\jdk-22\bin\javac.exe" (
    set "JAVA_EXE=C:\Program Files\Java\jdk-22\bin\javac.exe"
    set "JAVAW_EXE=C:\Program Files\Java\jdk-22\bin\javaw.exe"
)

REM Compila os codigos Java
if not exist "bin" mkdir "bin"
"%JAVA_EXE%" -encoding UTF-8 -d bin -cp ".;codigos" codigos\*.java
if errorlevel 1 (
    echo.
    echo ERRO: nao foi possivel compilar o Hobby Quest. O jogo nao sera aberto com arquivos antigos.
    pause
    exit /b 1
)

REM Executa o aplicativo
start "" "%JAVAW_EXE%" -Xms256m -Xmx1024m -cp "bin;codigos;." JogoAudrey

exit
