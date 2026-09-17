@echo off
chcp 65001 >nul 2>nul
title Hobby Quest - Aplicativo PC
color 0A

echo ═══════════════════════════════════════════════════
echo       ✨ HOBBY QUEST - Aplicativo para PC ✨
echo ═══════════════════════════════════════════════════
echo.

REM ─── Verifica Node.js ──────────────────────────────
where node >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERRO] Node.js nao encontrado!
    echo Instale em: https://nodejs.org/
    pause
    exit /b 1
)

REM ─── Verifica Java ─────────────────────────────────
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [AVISO] Java nao encontrado. O jogo nativo nao podera ser iniciado.
    echo Instale o JDK para habilitar o Jogo Audrey.
    echo.
)

REM ─── Compila o Jogo Java (se Java disponivel) ──────
where java >nul 2>nul
if %ERRORLEVEL% equ 0 (
    echo Compilando o Jogo Audrey...
    cd /d "%~dp0jogomaneiro-main"
    if not exist "bin" mkdir "bin"
    javac -encoding UTF-8 -d bin -cp ".;codigos" codigos/ServidorIntegradoJava.java codigos/JogoAudrey.java codigos/GerenciadorAudio.java 2>nul
    if %ERRORLEVEL% equ 0 (
        echo Compilacao do jogo OK!
    ) else (
        echo [AVISO] Falha na compilacao do jogo. O app web ainda funcionara.
    )
    cd /d "%~dp0"
    echo.
)

REM ─── Verifica se o servidor ja esta rodando ────────
echo Verificando servidor...
curl -s -o nul -w "%%{http_code}" http://localhost:3000/ > "%TEMP%\hq_status.txt" 2>nul
set /p STATUS=<"%TEMP%\hq_status.txt"
del "%TEMP%\hq_status.txt" 2>nul

if "%STATUS%"=="200" (
    echo Servidor ja esta rodando na porta 3000!
    goto ABRIR_APP
)

REM ─── Instala dependencias se necessario ────────────
if not exist "%~dp0novoprojeto_tcc\node_modules" (
    echo Instalando dependencias...
    cd /d "%~dp0novoprojeto_tcc"
    npm install >nul 2>nul
    cd /d "%~dp0"
)

REM ─── Inicia o servidor em background ───────────────
echo Iniciando servidor Hobby Quest...
start "" /b cmd /c "cd /d "%~dp0" && node servidor_tcc.js" >nul 2>nul

REM ─── Aguarda o servidor subir ──────────────────────
echo Aguardando servidor inicializar...
set TENTATIVAS=0
:ESPERAR
timeout /t 1 /nobreak >nul
set /a TENTATIVAS+=1
curl -s -o nul http://localhost:3000/ 2>nul
if %ERRORLEVEL% equ 0 goto ABRIR_APP
if %TENTATIVAS% geq 10 (
    echo [ERRO] O servidor nao iniciou em 10 segundos.
    echo Verifique se a porta 3000 esta livre.
    pause
    exit /b 1
)
goto ESPERAR

:ABRIR_APP
echo.
echo ═══════════════════════════════════════════════════
echo   Servidor OK! Abrindo Hobby Quest no PC...
echo ═══════════════════════════════════════════════════
echo.

REM ─── Detecta navegador e abre em modo app ──────────
REM Tenta Edge primeiro (melhor modo app no Windows)
where msedge >nul 2>nul
if %ERRORLEVEL% equ 0 (
    start "" msedge --app=http://localhost:3000/home.html --window-size=480,850
    goto FIM
)

REM Tenta Edge pelo caminho padrao
if exist "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" (
    start "" "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" --app=http://localhost:3000/home.html --window-size=480,850
    goto FIM
)

if exist "C:\Program Files\Microsoft\Edge\Application\msedge.exe" (
    start "" "C:\Program Files\Microsoft\Edge\Application\msedge.exe" --app=http://localhost:3000/home.html --window-size=480,850
    goto FIM
)

REM Tenta Chrome
where chrome >nul 2>nul
if %ERRORLEVEL% equ 0 (
    start "" chrome --app=http://localhost:3000/home.html --window-size=480,850
    goto FIM
)

REM Fallback: abre no navegador padrao
start http://localhost:3000/home.html

:FIM
echo.
echo Hobby Quest esta aberto! Voce pode fechar esta janela.
echo (O servidor continuara rodando em segundo plano)
echo.
echo Para encerrar completamente, feche a janela do app e execute:
echo   taskkill /f /im node.exe (para parar o servidor)
echo.
timeout /t 5 /nobreak >nul
exit
