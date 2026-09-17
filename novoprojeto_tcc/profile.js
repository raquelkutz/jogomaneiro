// Profile photo, name and email logic for all pages containing the profile overlay
document.addEventListener('DOMContentLoaded', () => {

  // ---- Foto de perfil ----
  const avatars = document.querySelectorAll('.profile-avatar');
  avatars.forEach(avatar => {
    avatar.style.cursor = 'pointer';
    avatar.title = 'Alterar foto de perfil';
    avatar.style.backgroundSize = 'cover';
    avatar.style.backgroundPosition = 'center';

    const savedPhoto = localStorage.getItem(HQ._k('profilePhotoUrl'));
    if (savedPhoto) {
      avatar.style.backgroundImage = `url(${savedPhoto})`;
      const svg = avatar.querySelector('svg');
      if (svg) svg.style.display = 'none';
    }

    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    fileInput.accept = 'image/*';
    fileInput.style.display = 'none';
    avatar.appendChild(fileInput);

    avatar.addEventListener('click', () => fileInput.click());

    fileInput.addEventListener('change', (e) => {
      const file = e.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (event) => {
          const photoUrl = event.target.result;
          avatar.style.backgroundImage = `url(${photoUrl})`;
          const svg = avatar.querySelector('svg');
          if (svg) svg.style.display = 'none';
          localStorage.setItem(HQ._k('profilePhotoUrl'), photoUrl);
          avatars.forEach(otherAvatar => {
            if (otherAvatar !== avatar) {
              otherAvatar.style.backgroundImage = `url(${photoUrl})`;
              const otherSvg = otherAvatar.querySelector('svg');
              if (otherSvg) otherSvg.style.display = 'none';
            }
          });
        };
        reader.readAsDataURL(file);
      }
    });
  });

  // ---- Nome editável ----
  const nameDisplay = document.getElementById('profileNameDisplay');
  if (nameDisplay) {
    // Carrega nome salvo
    const savedName = localStorage.getItem('userName') || 'Aventureiro';
    nameDisplay.textContent = savedName;

    // Indica que é clicável
    nameDisplay.style.cursor = 'pointer';
    nameDisplay.title = 'Clique para editar seu nome';

    // Ícone de lápis ao lado
    const pencilIcon = document.createElement('span');
    pencilIcon.textContent = ' ✏️';
    pencilIcon.style.fontSize = '14px';
    nameDisplay.appendChild(pencilIcon);

    nameDisplay.addEventListener('click', () => {
      const currentName = localStorage.getItem('userName') || 'Aventureiro';
      const input = document.createElement('input');
      input.type = 'text';
      input.value = currentName;
      input.maxLength = 24;
      input.style.cssText = `
        font-family: 'Cabin Sketch', cursive;
        font-size: 20px;
        color: #d946ef;
        background: transparent;
        border: none;
        border-bottom: 2px dashed #d946ef;
        outline: none;
        width: 140px;
        text-align: left;
      `;

      // Substitui o span pelo input
      nameDisplay.replaceWith(input);
      input.focus();
      input.select();

      const saveAndRestore = () => {
        const newName = input.value.trim() || 'Aventureiro';
        localStorage.setItem('userName', newName);

        // Restaura o span com o novo nome
        nameDisplay.childNodes.forEach(n => { if (n !== pencilIcon) n.remove(); });
        nameDisplay.textContent = newName;
        nameDisplay.appendChild(pencilIcon);
        input.replaceWith(nameDisplay);
      };

      input.addEventListener('blur', saveAndRestore);
      input.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') input.blur();
        if (e.key === 'Escape') { input.value = currentName; input.blur(); }
      });
    });
  }

  // ---- Email do login ----
  const emailDisplay = document.getElementById('profileEmailDisplay');
  if (emailDisplay) {
    const savedEmail = localStorage.getItem('userEmail') || 'usuario@email.com';
    emailDisplay.textContent = savedEmail;
  }
});
