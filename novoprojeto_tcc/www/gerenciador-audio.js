// ═══════════════════════════════════════════════════════════════════
//  gerenciador-audio.js — Sistema de Áudio Fiel do Jogo Audrey
//  Reproduz sons originais em WAV e sintetiza efeitos sonoros via Web Audio API.
// ═══════════════════════════════════════════════════════════════════

const GerenciadorAudio = (() => {
  let somAtivo = true;
  let audioCtx = null;
  const audioCache = {};
  let passosAudio = null;
  let vozesAtivas = [];

  // Inicializa contexto de áudio em interação do usuário
  function getAudioContext() {
    if (!audioCtx) {
      const AudioContextClass = window.AudioContext || window.webkitAudioContext;
      if (AudioContextClass) {
        audioCtx = new AudioContextClass();
      }
    }
    if (audioCtx && audioCtx.state === 'suspended') {
      audioCtx.resume();
    }
    return audioCtx;
  }

  // Toca um arquivo de áudio HTML5
  function playWav(path, volume = 0.7, loop = false) {
    if (!somAtivo) return null;
    try {
      getAudioContext();
      const sound = new Audio(path);
      sound.volume = Math.max(0, Math.min(1, volume));
      sound.loop = loop;
      const promise = sound.play();
      if (promise !== undefined) {
        promise.catch(() => {
          // Bloqueio de autoplay ignorado até interação
        });
      }
      return sound;
    } catch (e) {
      return null;
    }
  }

  // Sintetizador para bips de diálogo, coletas e erros via Web Audio
  function playBeep(frequency, durationMs, type = 'sine', gainVal = 0.15) {
    if (!somAtivo) return;
    try {
      const ctx = getAudioContext();
      if (!ctx) return;
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = type;
      osc.frequency.setValueAtTime(frequency, ctx.currentTime);
      gain.gain.setValueAtTime(gainVal, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + (durationMs / 1000));
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start();
      osc.stop(ctx.currentTime + (durationMs / 1000));
    } catch (_) {}
  }

  return {
    get somAtivo() { return somAtivo; },
    set somAtivo(v) { somAtivo = !!v; },

    tocarSomPassos() {
      if (!somAtivo) return;
      if (!passosAudio) {
        passosAudio = playWav('audios/passos_concreto.wav', 0.5, true);
      } else if (passosAudio.paused) {
        passosAudio.play().catch(() => {});
      }
    },

    pararSomPassos() {
      if (passosAudio) {
        passosAudio.pause();
        passosAudio.currentTime = 0;
      }
    },

    tocarSomAbrirPorta() {
      playWav('audios/abrir_porta.wav', 0.85);
    },

    tocarSomArmario() {
      playWav('audios/abrir_porta.wav', 0.75);
    },

    tocarSomColeta() {
      // Tom brilhante de conquista
      playBeep(587.33, 100, 'triangle', 0.2); // D5
      setTimeout(() => playBeep(880, 180, 'triangle', 0.22), 90); // A5
    },

    tocarSomDialogo() {
      playBeep(440, 50, 'sine', 0.1);
    },

    tocarSomErro() {
      playBeep(220, 150, 'sawtooth', 0.2);
    },

    tocarSomSinalEscolar() {
      playBeep(800, 350, 'sine', 0.25);
      setTimeout(() => playBeep(800, 600, 'sine', 0.25), 400);
    },

    pararTodasAsVozes() {
      vozesAtivas.forEach(v => {
        try {
          v.pause();
          v.currentTime = 0;
        } catch (_) {}
      });
      vozesAtivas = [];
    },

    tocarVozNicolas() {
      this.pararTodasAsVozes();
      const v = playWav('audios/voz_nicolas.wav', 0.85);
      if (v) vozesAtivas.push(v);
    },

    tocarVoz(personagem) {
      this.pararTodasAsVozes();
      const p = (personagem || '').toLowerCase().trim();
      let path = null;
      if (p === 'nicollas' || p === 'nicolas') path = 'audios/voz_nicolas.wav';
      else if (p === 'audrey') path = 'audios/voz-audrey.wav';
      else if (p === 'camila') path = 'audios/voz-camila.wav';
      else if (p === 'gabi') path = 'audios/voz-gabi.wav';
      else if (p === 'ivi') path = 'audios/voz-ivi.wav';
      else if (p === 'maysa') path = 'audios/voz-maysa.wav';
      else if (p === 'victor') path = 'audios/voz-victor.wav';
      else if (p === 'raquel') path = 'audios/voz-audrey.wav';

      if (path) {
        const sound = playWav(path, 0.85);
        if (sound) vozesAtivas.push(sound);
      } else {
        this.tocarSomDialogo();
      }
    }
  };
})();

window.GerenciadorAudio = GerenciadorAudio;
