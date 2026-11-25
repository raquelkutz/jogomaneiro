Set WshShell = CreateObject("WScript.Shell")
strPath = "C:\Users\aluno\Downloads\jogomaneiro-main\jogomaneiro-main"
WshShell.CurrentDirectory = strPath

' Executa com javaw (sem abrir tela preta do terminal)
strJavaW = "C:\Program Files\Java\jdk-22\bin\javaw.exe"
strCmd = """" & strJavaW & """ -Xms256m -Xmx1024m -Dfile.encoding=UTF-8 -cp ""bin;codigos;."" JogoAudrey"

WshShell.Run strCmd, 0, False
