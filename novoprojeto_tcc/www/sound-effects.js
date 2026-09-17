let _sfxCtx = null;
function _sfxGetCtx() {
  if (!_sfxCtx) {
    _sfxCtx = new (window.AudioContext || window.webkitAudioContext)();
  }
  if (_sfxCtx && _sfxCtx.state === 'suspended') {
    _sfxCtx.resume();
  }
  return _sfxCtx;
}

// Auto-resume AudioContext on first user gesture
window.addEventListener('click', () => _sfxGetCtx(), { once: true });
window.addEventListener('keydown', () => _sfxGetCtx(), { once: true });
window.addEventListener('touchstart', () => _sfxGetCtx(), { once: true });

function _sfxPlay(freq, duration, type, volume) {
  try {
    const ctx = _sfxGetCtx();
    if (!ctx) return;
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = type || 'sine';
    osc.frequency.setValueAtTime(freq, ctx.currentTime);
    gain.gain.setValueAtTime(volume || 0.15, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + duration);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + duration);
  } catch (_) {}
}

function _sfxPlayMulti(notes, type, volume) {
  notes.forEach(([freq, delay, dur]) => {
    setTimeout(() => _sfxPlay(freq, dur || 0.12, type, volume), delay * 1000);
  });
}

let _musicTimer = null;

window.SFX = {
  click() {
    _sfxPlay(800, 0.06, 'square', 0.05);
  },
  xpGain() {
    _sfxPlay(880, 0.1, 'sine', 0.12);
    setTimeout(() => _sfxPlay(1100, 0.1, 'sine', 0.10), 80);
  },
  levelUp() {
    _sfxPlayMulti([
      [1319, 0, 0.06],
      [1568, 0.06, 0.06],
      [1760, 0.12, 0.06],
      [2093, 0.18, 0.06],
      [2637, 0.24, 0.08],
      [2093, 0.32, 0.05],
      [2637, 0.37, 0.05],
      [3136, 0.42, 0.3],
    ], 'sine', 0.08);
  },
  achievement() {
    _sfxPlayMulti([
      [660, 0, 0.12],
      [880, 0.1, 0.12],
      [1100, 0.2, 0.25],
    ], 'sine', 0.13);
  },
  success() {
    _sfxPlay(660, 0.08, 'sine', 0.10);
    setTimeout(() => _sfxPlay(880, 0.12, 'sine', 0.10), 70);
  },
  error() {
    _sfxPlay(300, 0.15, 'sawtooth', 0.08);
    setTimeout(() => _sfxPlay(200, 0.2, 'sawtooth', 0.08), 120);
  },
  nav() {
    _sfxPlay(660, 0.06, 'sine', 0.06);
    setTimeout(() => _sfxPlay(880, 0.06, 'sine', 0.05), 50);
  },
  hobbyArte() {
    _sfxPlay(880, 0.08, 'sine', 0.08);
    setTimeout(() => _sfxPlay(1100, 0.08, 'sine', 0.07), 60);
  },
  hobbyLeitura() {
    this.hobbyArte();
  },
  hobbyFitness() {
    this.hobbyArte();
  },
  journey() {
    _sfxPlayMulti([
      [523, 0, 0.15],
      [659, 0.1, 0.15],
      [784, 0.2, 0.15],
      [1047, 0.3, 0.4],
    ], 'sine', 0.18);
  }
};

// ─── GERENCIADOR DE ÁUDIO FIEL AO JAVA (WEB AUDIO SYNTHESIZER) ──────────────
let _passosInterval = null;
let _vozTimer = null;

window.GerenciadorAudio = {
  tocarSomColeta() {
    SFX.levelUp();
  },
  tocarSomDialogo() {
    _sfxPlay(520, 0.05, 'triangle', 0.08);
  },
  tocarSomPassos() {
    if (_passosInterval) return;
    _passosInterval = setInterval(() => {
      _sfxPlay(180 + Math.random() * 40, 0.03, 'sine', 0.03);
    }, 280);
  },
  pararSomPassos() {
    if (_passosInterval) {
      clearInterval(_passosInterval);
      _passosInterval = null;
    }
  },
  tocarSomAbrirPorta() {
    _sfxPlayMulti([
      [220, 0, 0.1],
      [330, 0.08, 0.12],
      [440, 0.16, 0.2]
    ], 'sine', 0.09);
  },
  tocarSomSinalEscolar() {
    _sfxPlayMulti([
      [880, 0, 0.25],
      [1108, 0.25, 0.25],
      [1318, 0.5, 0.5]
    ], 'triangle', 0.12);
  },
  tocarSomErro() {
    SFX.error();
  },
  tocarSomArmario() {
    _sfxPlayMulti([
      [300, 0, 0.08],
      [250, 0.07, 0.12]
    ], 'sawtooth', 0.06);
  },
  tocarVoz(nomePersonagem) {
    let baseFreq = 400;
    if (nomePersonagem === "Audrey") baseFreq = 520;
    else if (nomePersonagem === "Nicollas" || nomePersonagem === "Nicolas") baseFreq = 280;
    else if (nomePersonagem === "Raquel") baseFreq = 480;
    else if (nomePersonagem === "Camila") baseFreq = 540;
    else if (nomePersonagem === "Gabi") baseFreq = 580;
    else if (nomePersonagem === "Ivi") baseFreq = 500;
    else if (nomePersonagem === "Victor") baseFreq = 220;
    else if (nomePersonagem === "Maysa") baseFreq = 460;

    const pitchVar = (Math.random() - 0.5) * 60;
    _sfxPlay(baseFreq + pitchVar, 0.04, 'triangle', 0.05);
  },
  tocarVozNicolas() {
    this.tocarVoz("Nicollas");
  },
  pararTodasAsVozes() {
    if (_vozTimer) {
      clearTimeout(_vozTimer);
      _vozTimer = null;
    }
  }
};

document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.back-btn, .nav-item').forEach(el => {
    el.addEventListener('click', (e) => {
      if (window.SFX) SFX.nav();
      if (el.tagName === 'A' && el.getAttribute('href') && !el.getAttribute('onclick')) {
        e.preventDefault();
        const href = el.getAttribute('href');
        setTimeout(() => { window.location.href = href; }, 120);
      }
    });
  });
});
