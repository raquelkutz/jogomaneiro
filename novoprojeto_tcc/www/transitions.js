/**
 * transitions.js — Sistema global de transições suaves para Hobby Quest
 * Adiciona: fade-in na entrada, fade-out + loading screen na saída
 */

(function () {
  /* ─── 1. Injetar estilos globais ─── */
  const css = `
    /* Animação de entrada da página */
    @keyframes hq-fadeIn {
      from { opacity: 0; transform: translateY(12px); }
      to   { opacity: 1; transform: translateY(0); }
    }

    /* Overlay de saída */
    #hq-page-overlay {
      position: fixed;
      inset: 0;
      z-index: 9999;
      pointer-events: none;
      opacity: 0;
      transition: opacity 0.28s ease;
      background: var(--bg-color, #eef2f5);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 16px;
    }
    #hq-page-overlay.visible {
      opacity: 1;
      pointer-events: all;
    }

    /* Loading spinner estilo caderno */
    .hq-spinner {
      width: 52px;
      height: 52px;
      border: 5px solid #d4e9d5;
      border-top-color: #67a760;
      border-radius: 50%;
      animation: hq-spin 0.75s linear infinite;
    }
    @keyframes hq-spin {
      to { transform: rotate(360deg); }
    }

    .hq-loading-text {
      font-family: 'Cabin Sketch', cursive;
      font-size: 20px;
      color: #67a760;
      letter-spacing: 2px;
      animation: hq-pulse-text 1.2s ease-in-out infinite;
    }
    @keyframes hq-pulse-text {
      0%, 100% { opacity: 1; }
      50%       { opacity: 0.4; }
    }

    /* Entrada da página */
    body.hq-enter {
      animation: hq-fadeIn 0.35s cubic-bezier(0.22, 1, 0.36, 1) forwards;
    }
  `;
  const styleEl = document.createElement('style');
  styleEl.textContent = css;
  document.head.appendChild(styleEl);

  /* ─── 2. Criar overlay de loading ─── */
  const overlay = document.createElement('div');
  overlay.id = 'hq-page-overlay';
  overlay.innerHTML = `
    <div class="hq-spinner"></div>
    <span class="hq-loading-text" id="hq-loading-label">CARREGANDO...</span>
  `;
  document.body.appendChild(overlay);

  /* ─── 3. Animação de ENTRADA (fade-in ao carregar a página) ─── */
  document.addEventListener('DOMContentLoaded', () => {
    document.body.classList.add('hq-enter');
  });

  /* ─── 4. Função global de navegação com loading screen ─── */
  window.hqNavigate = function (url, label) {
    if (window._hqNavigating) return;
    window._hqNavigating = true;

    const loadingLabel = document.getElementById('hq-loading-label');
    if (loadingLabel && label) loadingLabel.textContent = label;

    overlay.classList.add('visible');

    // Aguarda o overlay aparecer antes de redirecionar
    setTimeout(() => {
      window.location.href = url;
    }, 320);
  };

  /* ─── 5. Interceptar todos os links <a> e data-href ─── */
  document.addEventListener('DOMContentLoaded', () => {

    // Links <a href="...">
    document.querySelectorAll('a[href]').forEach(link => {
      const href = link.getAttribute('href');
      // Ignora âncoras, externos, e links que já têm onclick
      if (!href || href.startsWith('#') || href.startsWith('http') || href.startsWith('mailto')) return;

      link.addEventListener('click', (e) => {
        e.preventDefault();
        hqNavigate(href, getLabelForPage(href));
      });
    });

    // Elementos com data-href (hobby cards, action cards, etc.)
    document.querySelectorAll('[data-href]').forEach(el => {
      const href = el.getAttribute('data-href');
      if (!href) return;

      el.style.cursor = 'pointer';
      el.addEventListener('click', (e) => {
        e.preventDefault();
        hqNavigate(href, getLabelForPage(href));
      });
    });
  });

  /* ─── 6. Mapeamento de labels por página ─── */
  function getLabelForPage(url) {
    const map = {
      'home.html':       'HOME...',
      'meta.html':       'OBJETIVOS...',
      'perfil.html':     'PERFIL...',
      'progresso.html':  'DESEMPENHO...',
      'ajuda.html':      'AJUDA...',
      'arte.html':       'ARTE...',
      'leitura.html':    'LEITURA...',
      'fitness.html':    'FITNESS...',
      'niveis.html':     'NÍVEIS...',
      'conquistas.html': 'CONQUISTAS...',
      'desafios.html':   'DESAFIOS...',
      'jogo.html':       'JOGO...',
      'index.html':      'SAINDO...',
    };
    for (const [key, label] of Object.entries(map)) {
      if (url.includes(key)) return label;
    }
    return 'CARREGANDO...';
  }

})();
