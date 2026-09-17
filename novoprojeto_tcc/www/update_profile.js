const fs = require('fs');

const replacement = `    <!-- ===== PROFILE OVERLAY ===== -->
    <div class="profile-overlay" id="profileOverlay" onclick="closeProfile(event)">
      <div class="profile-panel" onclick="event.stopPropagation()">
        <div class="profile-handle"></div>

        <!-- Polaroid Avatar -->
        <div style="background: white; padding: 10px 10px 30px 10px; border-radius: 4px; box-shadow: 2px 4px 10px rgba(0,0,0,0.15); width: fit-content; margin: 0 auto 20px; transform: rotate(-3deg); position: relative;">
          <!-- Washi tape -->
          <div style="position: absolute; top: -10px; left: 50%; transform: translateX(-50%) rotate(5deg); width: 60px; height: 20px; background: rgba(168,216,234,0.7); box-shadow: 1px 1px 2px rgba(0,0,0,0.1);"></div>
          
          <div style="width: 90px; height: 90px; background: #eee; display: flex; align-items: center; justify-content: center; overflow: hidden; border: 1px solid #ddd;">
            <svg width="50" height="50" viewBox="0 0 24 24" fill="#aaa"><circle cx="12" cy="8" r="5"/><path d="M3 21c0-4.97 4.03-9 9-9s9 4.03 9 9"/></svg>
          </div>
          <div style="font-family: 'Caveat', cursive; font-size: 20px; color: #333; text-align: center; position: absolute; bottom: 5px; width: 100%; left: 0;">Eu!</div>
        </div>

        <h2 style="font-family: 'Cabin Sketch', cursive; font-size: 36px; color: #333; text-align: center; margin: 0 0 5px; letter-spacing: 2px;">PERFIL</h2>
        <p style="font-family: 'Caveat', cursive; font-size: 22px; color: #888; text-align: center; margin: 0 0 25px;">Aventureiro(a) Criativo(a) ✨</p>
        
        <!-- Info Tags -->
        <div style="display: flex; flex-direction: column; gap: 12px; margin-bottom: 24px; width: 100%;">
          <!-- Name Tag -->
          <div style="background: #fdf5c9; border: 2px dashed #d4b85c; border-radius: 8px; padding: 12px 20px; display: flex; justify-content: space-between; align-items: center; transform: rotate(1deg);">
            <span style="font-family: 'Walter Turncoat', cursive; font-size: 14px; color: #997a3e;">NOME:</span>
            <span style="font-family: 'Cabin Sketch', cursive; font-size: 22px; color: #d946ef;">Aventureiro</span>
          </div>
          <!-- Email Tag -->
          <div style="background: #e6f7ff; border: 2px dashed #9bcdeb; border-radius: 8px; padding: 12px 20px; display: flex; justify-content: space-between; align-items: center; transform: rotate(-1deg);">
            <span style="font-family: 'Walter Turncoat', cursive; font-size: 14px; color: #5c8ca8;">EMAIL:</span>
            <span style="font-family: 'Cabin Sketch', cursive; font-size: 20px; color: #3b82f6;">usuario@email.com</span>
          </div>
        </div>

        <div class="profile-stats">
          <div class="stat-badge" style="background: #ffe5e5; border: 1px dashed #ffb3b3;"><span class="stat-number" style="color: #ff6b6b;">3</span><span class="stat-label">Hobbies</span></div>
          <div class="stat-badge" style="background: #e5ffe5; border: 1px dashed #b3ffb3;"><span class="stat-number" style="color: #4cd137;">7</span><span class="stat-label">Desafios</span></div>
          <div class="stat-badge" style="background: #fffae5; border: 1px dashed #ffeeb3;"><span class="stat-number" style="color: #fbc531;">⭐ 2</span><span class="stat-label">Conquistas</span></div>
        </div>

        <!-- Achievements Section -->
        <div style="background-color: #fce28b; border-radius: 16px; padding: 16px; margin-bottom: 14px; box-shadow: 2px 3px 8px rgba(0,0,0,0.1); width: 100%;">
          <div style="display: flex; align-items: center; justify-content: center; gap: 8px; margin-bottom: 12px;">
            <span style="font-size: 24px;">🏆</span>
            <span style="font-family: 'Cabin Sketch', cursive; font-size: 18px; color: #333; letter-spacing: 1px;">CONQUISTAS</span>
          </div>
          <ul style="list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 8px;">
            <li style="background: rgba(255,255,255,0.5); padding: 8px 12px; border-radius: 10px; display: flex; align-items: center; gap: 10px; border: 1px dashed #d4b85c;">
              <span style="font-size: 20px;">🥇</span>
              <span style="font-family: 'Caveat', cursive; font-size: 18px; color: #444;">Leu 1 livro completo!</span>
            </li>
            <li style="background: rgba(255,255,255,0.5); padding: 8px 12px; border-radius: 10px; display: flex; align-items: center; gap: 10px; border: 1px dashed #d4b85c;">
              <span style="font-size: 20px;">🌟</span>
              <span style="font-family: 'Caveat', cursive; font-size: 18px; color: #444;">Subiu de nível em Artes!</span>
            </li>
            <li style="background: rgba(255,255,255,0.5); padding: 8px 12px; border-radius: 10px; display: flex; align-items: center; gap: 10px; border: 1px dashed #d4b85c;">
              <span style="font-size: 20px;">🔥</span>
              <span style="font-family: 'Caveat', cursive; font-size: 18px; color: #444;">7 dias seguidos de Fitness!</span>
            </li>
          </ul>
        </div>

        <div class="profile-actions" style="grid-template-columns: 1fr;">
          <div class="profile-action-card story" style="background: repeating-linear-gradient(45deg, #a8d8ea, #a8d8ea 10px, #9acae0 10px, #9acae0 20px);" onclick="alert('Seu progresso na história!')">
            <span class="action-icon" style="background: white; border-radius: 50%; padding: 10px; width: 50px; height: 50px; display: flex; align-items: center; justify-content: center; margin: 0 auto;">📖</span>
            <span class="action-title" style="color: white; text-shadow: 1px 1px 0 #555; font-size: 20px;">HISTÓRIA</span>
            <span class="action-desc" style="color: white; font-weight: bold; text-shadow: 1px 1px 2px rgba(0,0,0,0.2);">Progresso na jornada</span>
          </div>
        </div>

        <!-- Logout Button -->
        <button class="profile-logout-btn" style="background: #fff0f0; border: 2px dashed #ff4757; color: #ff4757; margin-top: 10px; box-shadow: 2px 2px 0px #ff4757;" onclick="handleLogout()">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#ff4757" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
          <span style="color: #ff4757;">SAIR DA CONTA</span>
        </button>
      </div>
    </div>`;

const files = ['home.html', 'meta.html', 'progresso.html', 'ajuda.html'];

for (const file of files) {
  let content = fs.readFileSync(file, 'utf8');
  const startIndex = content.indexOf('<!-- ===== PROFILE OVERLAY ===== -->');
  const endIndex = content.indexOf('<!-- Bottom Navigation -->');
  
  if (startIndex !== -1 && endIndex !== -1) {
    const newContent = content.substring(0, startIndex) + replacement + '\n\n    ' + content.substring(endIndex);
    fs.writeFileSync(file, newContent);
    console.log('Updated ' + file);
  } else {
    console.log('Could not find boundaries in ' + file);
  }
}
