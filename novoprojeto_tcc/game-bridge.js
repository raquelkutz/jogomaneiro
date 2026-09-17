// ═══════════════════════════════════════════════════════════════════
//  game-bridge.js  —  Camada de Comunicação: Hobby Quest ↔ Jogo Java
//
//  Como usar:
//    <script src="game-bridge.js"></script>
//
//  Esta camada:
//    1. Exporta e importa dados do HobbyQuest para o jogo Java
//    2. Recebe eventos do jogo (XP ganho, missão concluída, etc.)
//    3. Usa dados simulados enquanto o jogo Java não estiver integrado
//    4. Expõe GameBridge.* como API pública para o jogo Java chamar
// ═══════════════════════════════════════════════════════════════════

const GameBridge = (() => {

  // ─── CONFIGURAÇÃO ─────────────────────────────────────────────────────────
  const CFG = {
    /** Versão do protocolo de comunicação. Incremente ao mudar a API. */
    PROTOCOL_VERSION : '1.0.0',
    /** Timeout em ms para aguardar o jogo carregar antes de exibir erro. */
    LOAD_TIMEOUT_MS  : 15000,
    /** Nome do iframe/container onde o jogo será hospedado. */
    GAME_FRAME_ID    : 'game-frame',
    /** Prefixo dos eventos CustomEvent disparados pelo bridge. */
    EVENT_PREFIX     : 'hqgame:',
  };

  // ─── ESTADO INTERNO ────────────────────────────────────────────────────────
  let _state = {
    initialized : false,
    gameLoaded  : false,
    gameReady   : false,
    sessionStart: null,
  };

  // ─── DADOS SIMULADOS (usados até o jogo Java ser integrado) ───────────────
  /**
   * Retorna o snapshot atual do jogador no Hobby Quest.
   * Quando o jogo Java estiver integrado, estes dados serão enviados ao jogo.
   * @returns {Object} Payload completo do jogador
   */
  const _buildPlayerPayload = () => {
    // Lê dados do sistema HQ se disponível
    const hasHQ = typeof HQ !== 'undefined';
    return {
      protocolVersion : CFG.PROTOCOL_VERSION,
      timestamp       : new Date().toISOString(),
      player: {
        name   : localStorage.getItem('userName') || 'Aventureiro',
        email  : localStorage.getItem('userEmail') || '',
        streak : hasHQ ? HQ.getStreak() : 0,
      },
      hobbies: {
        arte    : hasHQ ? { xp: HQ.getXP('arte'),    level: HQ.getLevel('arte')    } : { xp: 0, level: 1 },
        leitura : hasHQ ? { xp: HQ.getXP('leitura'), level: HQ.getLevel('leitura') } : { xp: 0, level: 1 },
        fitness : hasHQ ? { xp: HQ.getXP('fitness'), level: HQ.getLevel('fitness') } : { xp: 0, level: 1 },
      },
      totalXP        : hasHQ ? HQ.getTotalXP() : 0,
      achievements   : hasHQ ? HQ.getUnlockedAchievements() : [],
      completedTasks : hasHQ ? HQ.getCompletedTasks() : 0,
    };
  };

  // ─── DADOS DE SIMULAÇÃO (fallback para testes sem jogo real) ──────────────
  const _simulatedGameEvents = [
    { type: 'MISSION_COMPLETE', data: { missionId: 'sim_001', xpReward: 25, hobby: 'arte',    label: 'Primeiro Esboço'    } },
    { type: 'MISSION_COMPLETE', data: { missionId: 'sim_002', xpReward: 30, hobby: 'leitura', label: 'Capítulo Lido'       } },
    { type: 'MISSION_COMPLETE', data: { missionId: 'sim_003', xpReward: 20, hobby: 'fitness', label: 'Corrida no Mundo'    } },
    { type: 'COIN_EARNED',      data: { amount: 10, source: 'exploration'                                                 } },
    { type: 'LEVEL_UP_GAME',   data: { newLevel: 2, message: 'Parabéns! Nível 2 no jogo!'                               } },
  ];

  // ─── EVENTOS PÚBLICOS ──────────────────────────────────────────────────────

  /**
   * Dispara um CustomEvent no window para que a UI possa reagir.
   * @param {string} eventName  Nome sem prefixo (ex: 'ready', 'xpEarned')
   * @param {Object} detail     Payload do evento
   */
  const _emit = (eventName, detail = {}) => {
    window.dispatchEvent(new CustomEvent(CFG.EVENT_PREFIX + eventName, { detail }));
  };

  // ─── HANDLERS DE EVENTOS VINDOS DO JOGO ───────────────────────────────────

  /**
   * Recebe uma missão concluída no jogo e aplica XP no HobbyQuest.
   * Deve ser chamado pelo jogo Java via postMessage ou diretamente.
   * @param {Object} missionData  { missionId, xpReward, hobby, label }
   */
  const onMissionComplete = (missionData) => {
    console.log('[GameBridge] Missão concluída:', missionData);
    if (typeof HQ !== 'undefined' && missionData.xpReward > 0) {
      HQ.addXP(missionData.hobby || 'arte', missionData.xpReward, missionData.label || 'Missão do Jogo');
    }
    _emit('missionComplete', missionData);
  };

  /**
   * Recebe moedas ganhas no jogo.
   * @param {Object} coinData  { amount, source }
   */
  const onCoinEarned = (coinData) => {
    console.log('[GameBridge] Moedas ganhas:', coinData);
    // Armazena moedas no localStorage para uso futuro
    const current = parseInt(localStorage.getItem('hq_coins') || '0', 10);
    localStorage.setItem('hq_coins', String(current + (coinData.amount || 0)));
    _emit('coinEarned', coinData);
  };

  /**
   * Recebe notificação de level up do jogo.
   * @param {Object} levelData  { newLevel, message }
   */
  const onGameLevelUp = (levelData) => {
    console.log('[GameBridge] Level up no jogo:', levelData);
    _emit('gameLevelUp', levelData);
  };

  /**
   * Roteador central de mensagens vindas do jogo Java via postMessage.
   * Adicione novos 'case' conforme novos eventos forem definidos.
   * @param {MessageEvent} event
   */
  const _handlePostMessage = (event) => {
    // Segurança: valide a origin quando souber a URL do jogo
    // if (event.origin !== 'http://localhost:8080') return;

    const { type, data } = event.data || {};
    if (!type) return;

    switch (type) {
      case 'GAME_READY'       : _onGameReady(); break;
      case 'MISSION_COMPLETE' : onMissionComplete(data); break;
      case 'COIN_EARNED'      : onCoinEarned(data); break;
      case 'LEVEL_UP_GAME'    : onGameLevelUp(data); break;
      default:
        console.warn('[GameBridge] Evento desconhecido:', type, data);
    }
  };

  /** Chamado quando o jogo sinaliza que está pronto para receber dados. */
  const _onGameReady = () => {
    _state.gameReady = true;
    console.log('[GameBridge] Jogo pronto! Enviando payload do jogador...');
    sendPlayerData();
    _emit('ready');
  };

  // ─── API PÚBLICA: ENVIO DE DADOS PARA O JOGO ──────────────────────────────

  /**
   * Envia os dados atuais do jogador para o jogo Java via postMessage.
   * O jogo Java deve escutar window.addEventListener('message', ...).
   */
  const sendPlayerData = () => {
    const payload = _buildPlayerPayload();
    const frame = document.getElementById(CFG.GAME_FRAME_ID);
    if (frame && frame.contentWindow) {
      frame.contentWindow.postMessage({ type: 'PLAYER_DATA', data: payload }, '*');
      console.log('[GameBridge] Dados enviados ao jogo:', payload);
    }
    return payload; // Retorna para uso em testes
  };

  // ─── SIMULAÇÃO DE EVENTOS (apenas para teste sem jogo real) ───────────────

  /**
   * Simula eventos do jogo para testar a integração sem o jogo Java.
   * Remove esta função quando o jogo real for integrado.
   * @param {number} index  Índice do evento simulado (0–4)
   */
  const simulateGameEvent = (index = 0) => {
    const event = _simulatedGameEvents[index % _simulatedGameEvents.length];
    console.log('[GameBridge] SIMULAÇÃO de evento:', event);
    _handlePostMessage({ data: event });
  };

  /**
   * Roda todos os eventos simulados em sequência com delay.
   * Útil para demonstrar a integração antes do jogo Java estar pronto.
   */
  const runSimulation = () => {
    console.log('[GameBridge] Iniciando simulação completa...');
    _simulatedGameEvents.forEach((event, i) => {
      setTimeout(() => {
        console.log(`[GameBridge] Simulando evento ${i + 1}/${_simulatedGameEvents.length}:`, event.type);
        _handlePostMessage({ data: event });
      }, i * 2500);
    });
  };

  // ─── INICIALIZAÇÃO ─────────────────────────────────────────────────────────

  /**
   * Inicializa o bridge. Deve ser chamado uma vez por página.
   * Registra listener de postMessage e atualiza estado.
   */
  const init = () => {
    if (_state.initialized) return;
    _state.initialized = true;
    _state.sessionStart = new Date().toISOString();

    // Escuta mensagens do jogo Java (via iframe ou janela filho)
    window.addEventListener('message', _handlePostMessage);

    // Conecta ao Server-Sent Events (SSE) do servidor local para sincronização em tempo real com JogoAudrey.java
    try {
      if (typeof EventSource !== 'undefined') {
        const sse = new EventSource('/api/eventos');
        sse.onmessage = (e) => {
          try {
            const parsed = JSON.parse(e.data);
            if (!parsed || !parsed.type) return;
            console.log('[GameBridge SSE] Evento recebido do jogo Java:', parsed);

            if (parsed.type === 'MISSION_COMPLETE') {
              const missionData = parsed.data || parsed;
              onMissionComplete(missionData);
            } else if (parsed.type === 'COIN_EARNED') {
              const coinData = parsed.data || parsed;
              onCoinEarned(coinData);
            } else if (parsed.type === 'LEVEL_UP_GAME') {
              const levelData = parsed.data || parsed;
              onGameLevelUp(levelData);
            } else if (parsed.type === 'GAME_STARTED') {
              _emit('gameStarted', parsed);
            } else if (parsed.type === 'GAME_CLOSED') {
              _emit('gameClosed', parsed);
            }
          } catch (err) {
            console.warn('[GameBridge SSE] Erro ao processar evento:', err);
          }
        };
        sse.onerror = () => {
          // Servidor pode estar desconectado temporariamente
        };
      }
    } catch (e) {
      console.log('[GameBridge SSE] Modo offline/sem SSE:', e);
    }

    console.log(`[GameBridge v${CFG.PROTOCOL_VERSION}] Inicializado em ${_state.sessionStart}`);
    _emit('initialized', { version: CFG.PROTOCOL_VERSION });
  };

  /** Remove listeners ao sair da tela do jogo. */
  const destroy = () => {
    window.removeEventListener('message', _handlePostMessage);
    _state.initialized = false;
    _state.gameLoaded  = false;
    _state.gameReady   = false;
    console.log('[GameBridge] Destruído. Listeners removidos.');
  };

  /** Retorna o estado atual do bridge (readonly). */
  const getState = () => ({ ..._state });

  /** Retorna o payload atual do jogador sem enviar ao jogo. */
  const getPlayerData = () => _buildPlayerPayload();

  // ─── EXPOSIÇÃO DA API ──────────────────────────────────────────────────────
  return {
    init,
    destroy,
    getState,
    getPlayerData,
    sendPlayerData,
    onMissionComplete,
    onCoinEarned,
    onGameLevelUp,
    simulateGameEvent,
    runSimulation,
    CFG,
  };

})();

// Auto-inicializa quando o script é carregado
if (typeof document !== 'undefined') {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => GameBridge.init());
  } else {
    GameBridge.init();
  }
}
