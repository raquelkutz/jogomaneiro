import sys

file_path = "JogoAudrey.java"

with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

# Substituições diretas com a string exata, que é muito mais seguro
replacements = {
    "CONFIGURACOES": "CONFIGURAÇÕES",
    "INVENTARIO": "INVENTÁRIO",
    "DIARIO": "DIÁRIO",
    "Dialogo": "Diálogo",
    "Dialogos": "Diálogos",
    "Inventario": "Inventário",
    "Diario": "Diário",
    "Opcoes": "Opções",
    "OPCOES": "OPÇÕES",
    "captulo": "capítulo",
    "concludo": "concluído",
    "Incrvel": "Incrível",
    "est\ufffd": "está",
    "V\ufffd ": "Vá ",
    " \ufffd ": " é ",
    "n\ufffd?": "né?",
    "a\ufffd!": "aí!",
    "expresso": "expressão",
    "prtica": "prática",
    "diria": "diária",
    "esboos": "esboços",
    "trao": "traço",
    "magnfico": "magnífico",
    "Faa": "Faça",
    "Parabns": "Parabéns",
    "n\ufffdo": "não",
    "N\ufffdo": "Não",
    "voc\ufffd": "você",
    "Voc\ufffd": "Você",
    "j\ufffd": "já",
    "J\ufffd": "Já",
    "coordena\ufffdo": "coordenação",
    "Arm\ufffdrio": "Armário",
    "arm\ufffdrio": "armário",
    "Di\ufffdrio": "Diário",
    "N\ufffdvel": "Nível",
    "Miss\ufffdes": "Missões",
    "miss\ufffdes": "missões",
    "pr\ufffdprias": "próprias",
    "pr\ufffdxima": "próxima",
    " s\ufffd ": " só ",
    "cap\ufffdtulo": "capítulo",
    "conclu\ufffddo": "concluído",
    "Incr\ufffdvel": "Incrível",
    "express\ufffdo": "expressão",
    "pr\ufffdtica": "prática",
    "di\ufffdria": "diária",
    "esbo\ufffdos": "esboços",
    "tra\ufffdo": "traço",
    "magn\ufffdfico": "magnífico",
    "Fa\ufffda": "Faça",
    "Parab\ufffdns": "Parabéns",
    " voc\ufffdes ": " vocês ",
    "Voc\ufffdes ": "Vocês "
}

for old, new in replacements.items():
    content = content.replace(old, new)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)

print("Accents fixed via string replace.")
