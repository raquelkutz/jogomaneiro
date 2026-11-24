@echo off
title Jogo Audrey - Launcher
color 0A

cd /d "%~dp0"
if exist "jogomaneiro-main" cd "jogomaneiro-main"
if not exist "bin" mkdir "bin"
javac -encoding UTF-8 -d bin -cp ".;codigos" codigos/*.java
if errorlevel 1 (
	echo.
	echo ERRO: nao foi possivel compilar o Jogo Audrey. O jogo nao sera aberto com arquivos antigos.
	pause
	exit /b 1
)

start "" javaw -Xms256m -Xmx1024m -cp "bin;codigos;." JogoAudrey game
exit
