@echo off
title Hobby Quest - TCC + Jogo Audrey
color 0A
echo =======================================================
echo       HOBBY QUEST - TCC + JOGO AUDREY
echo =======================================================
echo.

REM ─── Verifica Java ──────────────────────────────────────────────────────
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERRO] Java nao encontrado! Instale o JDK.
    pause
    exit /b 1
)

REM ─── Compila o Jogo ─────────────────────────────────────────────────────
echo Compilando o Jogo Audrey...
cd /d "%~dp0jogomaneiro-main"
if not exist "bin" mkdir "bin"
javac -encoding UTF-8 -d bin -cp ".;codigos" codigos/ServidorIntegradoJava.java codigos/JogoAudrey.java codigos/GerenciadorAudio.java
if %ERRORLEVEL% neq 0 (
    echo [ERRO] Falha na compilacao.
    pause
    exit /b 1
)
echo Compilacao OK!
echo.

REM ─── Inicia o Jogo (que tambem sobe o servidor na porta 3000) ──────────
echo Iniciando o Jogo Audrey + Servidor na porta 3000...
echo A janela do jogo vai abrir em instantes!
echo.

REM Espera 3 segundos para o servidor subir e abre o Edge
start "" /b cmd /c "timeout /t 3 /nobreak >nul & start "" msedge http://localhost:3000/"

REM Roda o Java na mesma janela (visivel, com acesso ao desktop)
java -cp "bin;codigos;." JogoAudrey

echo.
echo O jogo foi encerrado.
pause
