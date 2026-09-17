// ═══════════════════════════════════════════════════════════════════
//  xp-system.js  —  Motor Central de XP do Hobby Quest
//  Inclua este arquivo em TODAS as páginas com:
//    <script src="xp-system.js"></script>
// ═══════════════════════════════════════════════════════════════════

const HQ = {};

// ─── CONFIGURAÇÃO CENTRAL ────────────────────────────────────────────────────
// Altere aqui para rebalancear o jogo sem tocar em outros arquivos
HQ.CFG = {
  XP_PER_LEVEL : 100,           // XP necessário para cada nível (flat)
  HOBBIES      : ['arte', 'leitura', 'fitness'],
  HOBBY_LABEL  : { arte: 'Arte', leitura: 'Leitura', fitness: 'Fitness' },
  HOBBY_EMOJI  : { arte: '🎨',  leitura: '📖',       fitness: '🏋️'    },
  HOBBY_COLOR  : { arte: '#f1a0c4', leitura: '#5b8a72', fitness: '#d35400' },
};

// ─── CONQUISTAS ───────────────────────────────────────────────────────────────
HQ.ACHIEVEMENTS = [
  {
    id   : 'first_task',
    label: '🥇 Primeiro Passo',
    desc : 'Concluiu sua primeira tarefa!',
    check: () => HQ.getCompletedTasks() + HQ.getCompletedCheckins() >= 1,
  },
  {
    id   : 'level2_any',
    label: '⬆️ Subindo de Nível',
    desc : 'Alcançou Nível 2 em qualquer hobby!',
    check: () => HQ.CFG.HOBBIES.some(h => HQ.getLevel(h) >= 2),
  },
  {
    id   : 'level3_arte',
    label: '🎨 Artista em Evolução',
    desc : 'Alcançou Nível 3 em Arte!',
    check: () => HQ.getLevel('arte') >= 3,
  },
  {
    id   : 'level3_leitura',
    label: '📖 Leitor Dedicado',
    desc : 'Alcançou Nível 3 em Leitura!',
    check: () => HQ.getLevel('leitura') >= 3,
  },
  {
    id   : 'level3_fitness',
    label: '🏋️ Atleta em Ascensão',
    desc : 'Alcançou Nível 3 em Fitness!',
    check: () => HQ.getLevel('fitness') >= 3,
  },
  {
    id   : 'streak3',
    label: '🔥 Sequência de Fogo',
    desc : '3 dias consecutivos de atividade!',
    check: () => HQ.getStreak() >= 3,
  },
  {
    id   : 'streak7',
    label: '⚡ Imparável',
    desc : '7 dias seguidos no Hobby Quest!',
    check: () => HQ.getStreak() >= 7,
  },
  {
    id   : 'checkin5',
    label: '✅ Check-in Dedicado',
    desc : 'Concluiu 5 desafios do check-in!',
    check: () => HQ.getCompletedCheckins() >= 5,
  },
  {
    id   : 'total_xp_300',
    label: '⭐ Acúmulo de Poder',
    desc : 'Acumulou 300 XP no total!',
    check: () => HQ.getTotalXP() >= 300,
  },
  {
    id   : 'total_xp_1000',
    label: '💎 Mestre do Quest',
    desc : 'Acumulou 1.000 XP no total!',
    check: () => HQ.getTotalXP() >= 1000,
  },
  {
    id   : 'all_hobbies_lv2',
    label: '🌟 Explorador Completo',
    desc : 'Evoluiu todos os 3 hobbies para Nível 2!',
    check: () => HQ.CFG.HOBBIES.every(h => HQ.getLevel(h) >= 2),
  },
];

// ─── ISOLAMENTO POR CONTA ─────────────────────────────────────────────────────
HQ.getUserEmail = () => localStorage.getItem('userEmail') || '';
HQ._suffix = () => {
  const email = HQ.getUserEmail();
  if (!email) return '';
  return '_' + email.replace(/[^a-zA-Z0-9]/g, '_');
};
HQ._k = (key) => key + HQ._suffix();

// ─── STORAGE: XP por hobby ────────────────────────────────────────────────────
HQ.getXP  = (hobby) => Math.max(0, parseInt(localStorage.getItem(HQ._k('xp_' + hobby)) || '0', 10));
HQ.setXP  = (hobby, v) => {
  localStorage.setItem(HQ._k('xp_' + hobby), String(Math.max(0, Math.round(v))));
  // Mantém totalXP em sincronia como alias
  localStorage.setItem(HQ._k('totalXP'), String(HQ.getTotalXP()));
};

// ─── NÍVEIS ───────────────────────────────────────────────────────────────────
HQ.getLevel      = (hobby) => Math.max(1, Math.floor(HQ.getXP(hobby) / HQ.CFG.XP_PER_LEVEL) + 1);
HQ.getXPInLevel  = (hobby) => HQ.getXP(hobby) % HQ.CFG.XP_PER_LEVEL;
HQ.getXPPercent  = (hobby) => Math.min(100, Math.round((HQ.getXPInLevel(hobby) / HQ.CFG.XP_PER_LEVEL) * 100));
HQ.getTotalXP    = ()      => HQ.CFG.HOBBIES.reduce((s, h) => s + HQ.getXP(h), 0);
HQ.getXPToNext   = (hobby) => HQ.CFG.XP_PER_LEVEL - HQ.getXPInLevel(hobby);

// ─── STREAK ───────────────────────────────────────────────────────────────────
HQ.getStreak    = ()  => Math.max(0, parseInt(localStorage.getItem(HQ._k('hq_streak'))     || '0', 10));
HQ.setStreak    = (n) => localStorage.setItem(HQ._k('hq_streak'),     String(Math.max(0, n)));
HQ.getLastLogin = ()  => localStorage.getItem(HQ._k('hq_last_login')) || '';
HQ.setLastLogin = (d) => localStorage.setItem(HQ._k('hq_last_login'), d);

/**
 * Deve ser chamado ao abrir qualquer página após o login.
 * Incrementa o streak se hoje for um novo dia; reseta se houver brecha de mais de 1 dia.
 * @returns {number} streak atual
 */
HQ.updateStreak = () => {
  const today     = new Date().toISOString().slice(0, 10);
  const last      = HQ.getLastLogin();
  if (last === today) return HQ.getStreak(); // já visitou hoje

  const yesterday = new Date(Date.now() - 86400000).toISOString().slice(0, 10);
  const newStreak = (last === yesterday) ? HQ.getStreak() + 1 : 1;

  HQ.setStreak(newStreak);
  HQ.setLastLogin(today);
  HQ.checkAchievements();
  return newStreak;
};

// ─── CONQUISTAS ───────────────────────────────────────────────────────────────
HQ.getUnlockedAchievements = () => {
  try { return JSON.parse(localStorage.getItem(HQ._k('hq_achievements')) || '[]'); }
  catch { return []; }
};
HQ.saveAchievements = (arr) => localStorage.setItem(HQ._k('hq_achievements'), JSON.stringify(arr));

HQ.checkAchievements = () => {
  const unlocked = HQ.getUnlockedAchievements();
  const newOnes  = [];

  HQ.ACHIEVEMENTS.forEach(ach => {
    if (!unlocked.includes(ach.id) && ach.check()) {
      unlocked.push(ach.id);
      newOnes.push(ach);
    }
  });

  if (newOnes.length) {
    HQ.saveAchievements(unlocked);
    // Exibe toasts com delay para não sobrepor
    newOnes.forEach((ach, i) =>
      setTimeout(() => HQ._showAchievementToast(ach), i * 1200)
    );
  }

  return newOnes;
};

// ─── CONTADORES AUXILIARES ────────────────────────────────────────────────────
HQ.getCompletedCheckins = () => {
  try {
    const s = JSON.parse(localStorage.getItem(HQ._k('checkin_states')) || '{}');
    return Object.values(s).filter(v => v.state === 'success').length;
  } catch { return 0; }
};

HQ.getCompletedTasks = () => {
  try {
    const data = JSON.parse(localStorage.getItem(HQ._k('hobbyQuestTasks')) || '{}');
    let n = 0;
    Object.values(data).forEach(list => {
      if (Array.isArray(list)) n += list.filter(t => t.completed).length;
    });
    return n;
  } catch { return 0; }
};

// ─── PRINCIPAL: ADICIONAR XP ──────────────────────────────────────────────────
/**
 * Adiciona XP a um hobby, dispara toast, verifica level up e conquistas.
 * @param {string} hobby   'arte' | 'leitura' | 'fitness'
 * @param {number} amount  Quantidade positiva de XP
 * @param {string} reason  Texto curto exibido no toast (ex: "Tarefa concluída")
 */
HQ.addXP = (hobby, amount, reason) => {
  if (!amount || amount <= 0) return;
  const oldLevel = HQ.getLevel(hobby);
  HQ.setXP(hobby, HQ.getXP(hobby) + amount);
  const newLevel = HQ.getLevel(hobby);

  HQ._showXPToast('+' + amount + ' XP', reason || HQ.CFG.HOBBY_LABEL[hobby] || '');
  if (window.SFX) SFX.xpGain();

  if (newLevel > oldLevel) {
    setTimeout(() => HQ._showLevelUpModal(hobby, newLevel), 600);
  }

  HQ.checkAchievements();
  HQ._notifyUpdate(hobby, amount);
  return { oldLevel, newLevel, leveled: newLevel > oldLevel };
};

/**
 * Remove XP de um hobby (mínimo 0). Não exibe animação.
 */
HQ.removeXP = (hobby, amount) => {
  if (!amount || amount <= 0) return;
  HQ.setXP(hobby, HQ.getXP(hobby) - amount);
  HQ._notifyUpdate(hobby, -amount);
};

HQ._notifyUpdate = (hobby, delta) => {
  window.dispatchEvent(new CustomEvent('hq:xpUpdated', {
    detail: { hobby, delta, totalXP: HQ.getTotalXP() }
  }));
};

// ─── RESET COMPLETO (inclui conquistas) ────────────────────────────────────────
HQ.resetAll = () => {
  const keys = ['xp_arte', 'xp_leitura', 'xp_fitness', 'permanentXP_arte',
    'permanentXP_leitura', 'permanentXP_fitness', 'totalXP', 'hq_achievements',
    'hq_streak', 'hq_last_login', 'checkin_states', 'hobbyQuestTasks', 'profilePhotoUrl'];

  // Remove versões namespaced (por usuário) e globais (legado)
  keys.forEach(k => {
    localStorage.removeItem(k);
    localStorage.removeItem(HQ._k(k));
  });
};

// ─── MIGRAÇÃO DE DADOS LEGADOS (global → por usuário) ─────────────────────────
HQ._migrateLegacyData = () => {
  const suffix = HQ._suffix();
  if (!suffix) return;
  // Se já existem dados migrados para este usuário, não migra de novo
  if (localStorage.getItem(HQ._k('xp_arte')) !== null) return;

  const pairs = [
    ['xp_arte', 'xp_arte'],
    ['xp_leitura', 'xp_leitura'],
    ['xp_fitness', 'xp_fitness'],
    ['totalXP', 'totalXP'],
    ['hq_streak', 'hq_streak'],
    ['hq_last_login', 'hq_last_login'],
    ['hq_achievements', 'hq_achievements'],
    ['checkin_states', 'checkin_states'],
    ['hobbyQuestTasks', 'hobbyQuestTasks'],
    ['profilePhotoUrl', 'profilePhotoUrl'],
  ];
  pairs.forEach(([oldKey, newBase]) => {
    const val = localStorage.getItem(oldKey);
    if (val !== null) {
      localStorage.setItem(HQ._k(newBase), val);
    }
  });
};

// ─── UI: INJETAR ESTILOS DAS ANIMAÇÕES ───────────────────────────────────────
HQ._injectStyles = () => {
  if (document.getElementById('hq-system-styles')) return;
  const s = document.createElement('style');
  s.id = 'hq-system-styles';
  s.textContent = `
    @keyframes hqLevelUpIn {
      0%   { opacity:0; transform:scale(0.5) rotate(-8deg); }
      70%  { transform:scale(1.08) rotate(2deg); }
      100% { opacity:1; transform:scale(1) rotate(0deg); }
    }
    @keyframes hqStarSpin {
      from { transform:rotate(0deg) scale(1); }
      50%  { transform:rotate(180deg) scale(1.3); }
      to   { transform:rotate(360deg) scale(1); }
    }
    @keyframes hqConfettiFall {
      0%   { transform:translateY(-10px) rotate(0deg); opacity:1; }
      100% { transform:translateY(70px) rotate(360deg); opacity:0; }
    }
    @keyframes hqXPToastIn {
      0%   { opacity:0; transform:translateX(-50%) translateY(-14px) scale(0.85); }
      60%  { transform:translateX(-50%) translateY(4px) scale(1.03); }
      100% { opacity:1; transform:translateX(-50%) translateY(0) scale(1); }
    }
    @keyframes hqAchToastIn {
      0%   { opacity:0; transform:translateX(-50%) translateY(20px); }
      100% { opacity:1; transform:translateX(-50%) translateY(0); }
    }
  `;
  document.head.appendChild(s);
};

// ─── UI: TOAST DE XP ─────────────────────────────────────────────────────────
// ─── UI: TOAST DE XP ─────────────────────────────────────────────────────────
HQ._showXPToast = (text, reason) => {
  HQ._injectStyles();
  // Remove toasts antigos para não empilhar
  document.querySelectorAll('.hq-xp-toast').forEach(t => t.remove());

  // 1. Feedback Háptico (Vibração em dispositivos touch/mobile)
  if (typeof navigator !== 'undefined' && typeof navigator.vibrate === 'function') {
    try { navigator.vibrate([50, 30, 50]); } catch (_) {}
  }

  // 2. Feedback Visual (Toast flutuante + Ícone)
  const el = document.createElement('div');
  el.className = 'hq-xp-toast';
  el.style.cssText = `
    position:fixed; top:72px; left:50%;
    transform:translateX(-50%) translateY(-14px);
    background:linear-gradient(135deg,#fce28b,#f7ca18);
    color:#333; padding:10px 24px; border-radius:999px;
    font-family:'Cabin Sketch',cursive; font-size:18px;
    box-shadow:0 6px 20px rgba(0,0,0,0.2); z-index:9999;
    display:flex; align-items:center; gap:8px;
    white-space:nowrap; pointer-events:none;
    animation:hqXPToastIn 0.4s cubic-bezier(0.34,1.56,0.64,1) forwards;
    border:2px solid rgba(255,255,255,0.5);
  `;
  el.innerHTML = `<span aria-hidden="true">⭐</span> <strong>${text}</strong>${reason ? `<span style="font-size:13px;opacity:0.65;margin-left:4px">${reason}</span>` : ''}`;
  document.body.appendChild(el);

  setTimeout(() => {
    el.style.transition = 'opacity 0.35s ease, transform 0.35s ease';
    el.style.opacity    = '0';
    el.style.transform  = 'translateX(-50%) translateY(-14px)';
    setTimeout(() => el.remove(), 380);
  }, 2000);
};

// ─── UI: TOAST DE CONQUISTA ───────────────────────────────────────────────────
HQ._showAchievementToast = (ach) => {
  HQ._injectStyles();

  // 1. Feedback Háptico
  if (typeof navigator !== 'undefined' && typeof navigator.vibrate === 'function') {
    try { navigator.vibrate([100, 50, 100, 50, 150]); } catch (_) {}
  }

  // 2. Audio SFX
  if (window.SFX) SFX.achievement();

  // 3. Toast Visual
  const el = document.createElement('div');
  el.className = 'hq-ach-toast';
  el.style.cssText = `
    position:fixed; bottom:88px; left:50%;
    transform:translateX(-50%) translateY(20px);
    background:linear-gradient(135deg,#fffef5,#fdf5c9);
    border:2px dashed #d4b85c; color:#333; padding:14px 24px;
    border-radius:18px; font-family:'Cabin Sketch',cursive; font-size:15px;
    box-shadow:0 8px 28px rgba(0,0,0,0.16); z-index:9998; text-align:center;
    max-width:280px; width:90%;
    animation:hqAchToastIn 0.4s ease forwards;
  `;
  el.innerHTML = `
    <div style="font-size:22px;margin-bottom:4px"><span aria-hidden="true">🏆</span> CONQUISTA!</div>
    <div style="font-size:17px;font-weight:bold;line-height:1.3">${ach.label}</div>
    <div style="font-family:'Caveat',cursive;font-size:15px;color:#777;margin-top:5px">${ach.desc}</div>
  `;
  document.body.appendChild(el);

  setTimeout(() => {
    el.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
    el.style.opacity    = '0';
    el.style.transform  = 'translateX(-50%) translateY(20px)';
    setTimeout(() => el.remove(), 420);
  }, 3800);
};

// ─── UI: MODAL DE LEVEL UP ────────────────────────────────────────────────────
HQ._showLevelUpModal = (hobby, newLevel) => {
  // 1. Feedback Háptico
  if (typeof navigator !== 'undefined' && typeof navigator.vibrate === 'function') {
    try { navigator.vibrate([100, 50, 100, 50, 200]); } catch (_) {}
  }

  // 2. Audio SFX
  if (window.SFX) SFX.levelUp();

  HQ._injectStyles();
  document.querySelectorAll('.hq-levelup-overlay').forEach(m => m.remove());

  const emoji = HQ.CFG.HOBBY_EMOJI[hobby] || '⭐';
  const label = HQ.CFG.HOBBY_LABEL[hobby] || hobby;
  const color = HQ.CFG.HOBBY_COLOR[hobby] || '#e07a5f';

  // Confetti
  const confettiColors = ['#e07a5f','#f1c97a','#9dd4a0','#a8d8ea','#f1a0c4','#a88bc5'];
  let confettiHTML = '';
  for (let i = 0; i < 20; i++) {
    const c = confettiColors[i % confettiColors.length];
    const left = Math.random() * 90 + 5;
    const delay = (Math.random() * 0.8).toFixed(2);
    const size = (Math.random() * 8 + 6).toFixed(0);
    confettiHTML += `<div style="position:absolute;left:${left}%;width:${size}px;height:${size}px;background:${c};border-radius:2px;animation:hqConfettiFall ${(1.2 + Math.random()).toFixed(1)}s ${delay}s ease-in forwards;"></div>`;
  }

  const overlay = document.createElement('div');
  overlay.className = 'hq-levelup-overlay';
  overlay.style.cssText = `
    position:fixed; inset:0; background:rgba(0,0,0,0.6);
    backdrop-filter:blur(6px); z-index:9997;
    display:flex; align-items:center; justify-content:center; padding:20px;
  `;
  overlay.innerHTML = `
    <div style="
      background:#fdf5c9;
      background-image:repeating-linear-gradient(transparent,transparent 27px,rgba(0,0,0,0.04) 27px,rgba(0,0,0,0.04) 28px);
      border-radius:12px; padding:38px 28px 28px; text-align:center;
      max-width:320px; width:100%;
      box-shadow:4px 6px 24px rgba(0,0,0,0.25),0 0 0 3px #f0e08a;
      position:relative; overflow:hidden;
      animation:hqLevelUpIn 0.6s cubic-bezier(0.34,1.56,0.64,1) forwards;
    ">
      <!-- Fita washi -->
      <div style="position:absolute;top:-14px;left:50%;transform:translateX(-50%) rotate(-2deg);width:80px;height:26px;background:rgba(168,216,234,0.8);box-shadow:1px 1px 3px rgba(0,0,0,0.12)"></div>
      <!-- Confetti -->
      <div style="position:absolute;top:10px;left:0;width:100%;height:80px;overflow:hidden;pointer-events:none">${confettiHTML}</div>
      <!-- Conteúdo -->
      <div style="font-size:54px;display:block;margin:10px auto 6px;animation:hqStarSpin 1.2s ease-out forwards">${emoji}</div>
      <div style="font-family:'Cabin Sketch',cursive;font-size:30px;color:#e07a5f;letter-spacing:2px;margin:6px 0 3px">SUBIU DE NÍVEL!</div>
      <div style="font-family:'Caveat',cursive;font-size:20px;color:#888;margin:0 0 4px">em ${label}</div>
      <div style="display:inline-block;background:#fff;border:3px dashed ${color};border-radius:50px;padding:10px 32px;margin:14px 0 22px;box-shadow:2px 3px 0 rgba(0,0,0,0.08)">
        <span style="font-family:'Walter Turncoat',cursive;font-size:20px;color:#555">Nível </span>
        <span style="font-family:'Cabin Sketch',cursive;font-size:32px;color:${color};font-weight:bold">${newLevel}</span>
      </div>
      <button class="hq-levelup-close" style="
        width:100%; background:linear-gradient(135deg,#c7f0d8,#9dd4a0);
        color:#1a6633; border:2px dashed rgba(30,130,70,0.22);
        border-radius:255px 15px 225px 15px/15px 225px 15px 255px;
        padding:14px; font-family:'Cabin Sketch',cursive; font-size:18px;
        cursor:pointer; box-shadow:2px 3px 8px rgba(0,0,0,0.12); letter-spacing:1px;
      ">🎉 Continuar!</button>
    </div>
  `;

  overlay.querySelector('.hq-levelup-close').onclick = () => overlay.remove();
  overlay.addEventListener('click', e => { if (e.target === overlay) overlay.remove(); });
  document.body.appendChild(overlay);
};

// ─── UTILITÁRIO: atualizar elementos de barra na página atual ─────────────────
/**
 * Atualiza os elementos de exibição de nível/XP de um hobby na página.
 * Chave dos IDs esperados:  [prefix]-level, [prefix]-xp-fill, [prefix]-xp-label
 * Ex: hobby='arte', prefix='arte' → IDs: arte-level-circle, arte-xp-fill, arte-xp-text
 */
HQ.updateHobbyUI = (hobby, opts) => {
  const xp      = HQ.getXP(hobby);
  const level   = HQ.getLevel(hobby);
  const pct     = HQ.getXPPercent(hobby);
  const inLevel = HQ.getXPInLevel(hobby);
  const toNext  = HQ.getXPToNext(hobby);

  const ids = opts || {};
  const get = (id) => id ? document.getElementById(id) : null;

  // Genéricos: tenta IDs padrão se não forem fornecidos
  const lvlEl   = get(ids.level)   || get(hobby + '-level-circle') || get('level-text-' + hobby) || get('level-' + hobby);
  const fillEl  = get(ids.fill)    || get(hobby + '-xp-fill')      || get('bar-' + hobby);
  const labelEl = get(ids.label)   || get('xp-label-' + hobby)     || get(hobby + '-xp-text');

  if (lvlEl)   lvlEl.textContent  = level + '.LV';
  if (fillEl)  fillEl.style.width = pct + '%';
  if (labelEl) labelEl.textContent = inLevel + '/' + HQ.CFG.XP_PER_LEVEL + ' XP';

  return { xp, level, pct, inLevel, toNext };
};

// Atualiza todos os hobbies de uma vez
HQ.updateAllHobbyUI = () => {
  HQ.CFG.HOBBIES.forEach(h => HQ.updateHobbyUI(h));
};

// ─── AUTO-INIT ────────────────────────────────────────────────────────────────
// Injeta estilos assim que o script carrega (sem aguardar DOMContentLoaded)
if (typeof document !== 'undefined') {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      HQ._injectStyles();
      HQ._migrateLegacyData();
    });
  } else {
    HQ._injectStyles();
    HQ._migrateLegacyData();
  }
}
