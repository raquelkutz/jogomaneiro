Set oShell = CreateObject("WScript.Shell")
Dim batPath
batPath = Left(WScript.ScriptFullName, InStrRev(WScript.ScriptFullName, "\")) & "rodar_jogo.bat"
oShell.Run "cmd /c """ & batPath & """", 1, False
