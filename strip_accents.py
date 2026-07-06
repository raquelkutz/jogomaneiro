import sys
import re
import unicodedata

file_path = "JogoAudrey.java"

with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

# Substituir manualmente os caracteres acentuados utf-8 para seus equivalentes ascii
def strip_accents(text):
    return ''.join(c for c in unicodedata.normalize('NFD', text)
                  if unicodedata.category(c) != 'Mn')

# Apenas substituir na parte de texto (ou em tudo, já que é pt-br)
content = strip_accents(content)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)

print("Accents stripped.")
