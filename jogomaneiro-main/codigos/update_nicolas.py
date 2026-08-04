import shutil
import os

src = r"C:\Users\raque\.gemini\antigravity-ide\brain\0c4b769e-638e-4453-8eef-6de7ce7d9209\media__1785545277308.png"
dest_dir = r"c:\Users\raque\Downloads\jogomaneiro-main\jogomaneiro-main"

targets = [
    "nico__1_-removebg-preview.png",
    "pixel_nicolas.png",
    "portrait_nicollas-removebg-preview.png"
]

if os.path.exists(src):
    for target in targets:
        dest_path = os.path.join(dest_dir, target)
        shutil.copy(src, dest_path)
        print(f"Substituído com sucesso: {target}")
else:
    print(f"Imagem de origem não encontrada: {src}")
