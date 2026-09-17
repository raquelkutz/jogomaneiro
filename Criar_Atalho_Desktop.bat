@echo off
chcp 65001 >nul 2>nul
echo Criando atalho do Hobby Quest na Area de Trabalho...

REM Cria o script VBS temporario para gerar o atalho
echo Set oWS = WScript.CreateObject("WScript.Shell") > "%TEMP%\criar_atalho_hq.vbs"
echo sLinkFile = oWS.SpecialFolders("Desktop") ^& "\Hobby Quest.lnk" >> "%TEMP%\criar_atalho_hq.vbs"
echo Set oLink = oWS.CreateShortcut(sLinkFile) >> "%TEMP%\criar_atalho_hq.vbs"
echo oLink.TargetPath = "%~dp0HobbyQuest_PC.bat" >> "%TEMP%\criar_atalho_hq.vbs"
echo oLink.WorkingDirectory = "%~dp0" >> "%TEMP%\criar_atalho_hq.vbs"
echo oLink.Description = "Hobby Quest - Aplicativo PC" >> "%TEMP%\criar_atalho_hq.vbs"
echo oLink.WindowStyle = 7 >> "%TEMP%\criar_atalho_hq.vbs"
echo oLink.Save >> "%TEMP%\criar_atalho_hq.vbs"

cscript //nologo "%TEMP%\criar_atalho_hq.vbs"
del "%TEMP%\criar_atalho_hq.vbs" 2>nul

echo.
echo ✨ Atalho "Hobby Quest" criado na sua Area de Trabalho!
echo Clique duas vezes nele para abrir o aplicativo.
echo.
pause
