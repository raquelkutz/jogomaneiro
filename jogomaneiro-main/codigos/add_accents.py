import sys
import re

file_path = "JogoAudrey.java"
with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

replacements = {
    r"\bCONFIGURACOES\b": "CONFIGURAÇÕES",
    r"\bINVENTARIO\b": "INVENTÁRIO",
    r"\bDIARIO\b": "DIÁRIO",
    r"\bDialogo\b": "Diálogo",
    r"\bDialogos\b": "Diálogos",
    r"\bInventario\b": "Inventário",
    r"\bDiario\b": "Diário",
    r"\bOpcoes\b": "Opções",
    r"\bOPCOES\b": "OPÇÕES",
    r"\bcapitulo\b": "capítulo",
    r"\bconcluido\b": "concluído",
    r"\bIncrivel\b": "Incrível",
    r"\besta\b": "está",
    r"\bEsta\b": "Está",
    r"\bVa\b": "Vá",
    r"\bne\?\b": "né?",
    r"\bai!\b": "aí!",
    r"\bexpressao\b": "expressão",
    r"\bpratica\b": "prática",
    r"\bdiaria\b": "diária",
    r"\besbocos\b": "esboços",
    r"\btraco\b": "traço",
    r"\bmagnifico\b": "magnífico",
    r"\bFaca\b": "Faça",
    r"\bParabens\b": "Parabéns",
    r"\bnao\b": "não",
    r"\bNao\b": "Não",
    r"\bvoce\b": "você",
    r"\bVoce\b": "Você",
    r"\bja\b": "já",
    r"\bJa\b": "Já",
    r"\bcoordenacao\b": "coordenação",
    r"\bArmario\b": "Armário",
    r"\barmario\b": "armário",
    r"\bNivel\b": "Nível",
    r"\bMissoes\b": "Missões",
    r"\bmissoes\b": "missões",
    r"\bproprias\b": "próprias",
    r"\bproxima\b": "próxima",
    r"\bso\b": "só",
    r"\bvoces\b": "vocês",
    r"\bVoces\b": "Vocês",
    r"Qualquer coisa e ": "Qualquer coisa é ",
    r"a chave de ouro e ": "a chave de ouro é ",
    r"a verdadeira expressao e ": "a verdadeira expressão é ",
    r"O segredo da verdadeira expressao e ": "O segredo da verdadeira expressão é ",
    r" e logo ali": " é logo ali"
}

for old, new in replacements.items():
    content = re.sub(old, new, content)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)

print("Accents added.")
