/**
 * Jogo Audrey Engine - HTML5 Canvas Port 100% fiel ao JogoAudrey.java
 * Inclui Menu Inicial Fiel com Rabiscos Animados, Sistema Completo de Cutscene (Slides 1 a 12),
 * Easter Egg dos Armários 100% Responsivo, Sons Sintetizados e Interface Responsiva em Tela Cheia.
 */

class JogoAudreyEngine {
  constructor(canvasId) {
    this.canvas = document.getElementById(canvasId);
    this.ctx = this.canvas.getContext('2d');

    // Resolução virtual interna do Java original: 1000 x 750
    this.LARGURA = 1000;
    this.ALTURA = 750;

    // Estados do jogo (Fiel ao Java CardLayout)
    this.STATE = {
      MENU_PRINCIPAL: 0,
      MENU_SLOTS: 1,
      CUTSCENE: 3,
      JOGO: 4,
      SOBRE: 6
    };
    this.currentState = this.STATE.MENU_PRINCIPAL;
    this.slotAtual = 1;
    this.acaoSlot = 'NOVO';

    // Cutscene
    this.slideCutsceneAtual = 0;
    this.totalSlides = 12;

    // Animação do Menu (Rabiscos Flutuantes)
    this.animAngulo = 0;

    // Fontes
    this.FONT_FAMILY = '"CrayonHand", "Walter Turncoat", "Cabin Sketch", cursive, sans-serif';

    // Dimensões do personagem e NPCs (idêntico ao Java)
    this.AUDREY_LARGURA = 250;
    this.AUDREY_ALTURA = 475;
    this.ANDAR1_LARGURA = 207;
    this.ANDAR2_LARGURA = 217;
    this.NPC_LARGURA = 440;
    this.NPC_ALTURA = 500;
    this.SALA_NPC_LARGURA = 440;
    this.SALA_NPC_ALTURA = 500;
    this.SALA_RAQUEL_ALTURA = 510;
    this.SALA_NICOLAS_LARGURA = 340;
    this.SALA_NICOLAS_ALTURA = 540;
    this.SALA_CAMILA_ALTURA = 510;

    // Posições
    this.audreyX = 100;
    this.audreyY = 580;
    this.velX = 0;
    this.frameAtual = 0;
    this.contadorAnimacao = 0;
    this.olhandoDireita = true;
    this.estaMovendo = false;

    this.posArmarioX = 650;
    this.posNicolasXSalaAula = 500;
    this.posPortaX = 300;
    this.puertaSaidaX = 20;
    this.puertaSaidaLargura = 80;
    this.ultimaPosAoEntraSala = 100;
    this.cliquesArmarioEasterEgg = 0;
    this.easterEggAtivo = false;
    this.easterEggTimer = 0;

    // Estados de navegação
    this.indiceMapa = 0;
    this.menuAberto = false;

    this.temChave = false;
    this.armarioEstaAberto = false;
    this.estaEmDialogoNicolas = false;
    this.nicolasJaFoiEncontrado = false;
    this.mostrando_chave = false;
    this.contadorEfeitoChave = 0;
    this.inventarioAberto = false;
    this.temLivro = false;
    this.livroJoiFoiPego = false;
    this.mostrarObjetivos = false;
    this.diarioAberto = false;
    this.temDiario = false;
    this.temCadernoEsboco = false;
    this.temCronograma = false;

    // Hobbies / Níveis
    this.nivelLeitura = 1;
    this.nivelArte = 1;
    this.nivelFitness = 1;

    this.missaoLeituraAtiva = false;
    this.missaoArteAtiva = false;
    this.missaoFitnessAtiva = false;
    this.missaoLeituraConcluida = false;
    this.missaoArteConcluida = false;
    this.missaoFitnessConcluida = false;

    this.explorouCorredor = false;
    this.visitouGinasio = false;
    this.visitouCorredor2 = false;
    this.visitouCorredor3 = false;
    this.visitouCorredor4 = false;
    this.visitouBiblioteca = false;

    // Episódio 1 Flags
    this.ep1FalouNpc1 = false;
    this.ep1FalouNpc2 = false;
    this.ep1FalouNpc3 = false;
    this.ep1FalouGabiCorredor = false;
    this.ep1FalouIviCorredor = false;
    this.faseDialogoGabi = 0;
    this.faseDialogoIvi = 0;
    this.ep1InteragiuBiblioteca = false;
    this.ep1InteragiuMural = false;
    this.sala1Aberta = false;
    this.personagensNaBiblioteca = false;
    this.contadorTeleporte = -1;
    this.ep1FalouMaysa = false;

    // Diálogos & Opções
    this.faseDialogoNicolas = 0;
    this.selectedDialogueOption = 0;
    this.hoverOption = -1;
    this.textoDialogo = "";
    this.nomePersonagem = "";
    this.tamanhoTextoVisivel = 0;
    this.tamanhoTextoVisivelAcumulado = 0.0;
    this.textoDialogoAnterior = "";

    this.faseDialogoLeitura = 0;
    this.faseDialogoArte = 0;
    this.faseDialogoFitness = 0;

    // Diálogo Automático Sala de Aula
    this.dialogoSalaAutoIniciado = false;
    this.dialogoSalaAutoConcluido = false;
    this.faseDialogoSalaAuto = 0;
    this.aguardandoAvanceSalaAuto = false;

    // XP
    this.xp = 0;
    this.xpRaquelDado = false;
    this.xpNicolasDado = false;
    this.xpCamilaDado = false;
    this.xpGabiDado = false;
    this.xpIviDado = false;
    this.xpPendente = 0;
    this.temXpPendente = false;

    // Notificações
    this.mostrarNotificacaoMissao = false;
    this.contadorNotificacaoMissao = 0;
    this.NOTIFICACAO_DURACAO = 90;
    this.textoNotificacao = "";

    this.mostrarNotificacaoXP = false;
    this.contadorNotificacaoXP = 0;
    this.NOTIFICACAO_XP_DURACAO = 50;
    this.xpGanho = 0;

    this.mostrarAnimacaoRiscado = false;
    this.contadorAnimacaoRiscado = 0;
    this.textoMissaoRiscada = "";

    // Missão Solidão Urbana
    this.ep1_solidaoUrbanaAtiva = false;
    this.ep1_solidaoUrbanaConcluida = false;
    this.ep1_entregouDesenho = false;

    // Touch Controles
    this.touchEsquerdaPressionado = false;
    this.touchDireitaPressionado = false;

    this.touchBtnEsquerda = { x: 40, y: 595, width: 90, height: 90 };
    this.touchBtnDireita = { x: 155, y: 595, width: 90, height: 90 };
    this.touchBtnInteragir = { x: 845, y: 575, width: 110, height: 110 };
    this.touchBtnInventario = { x: 845, y: 16, width: 62, height: 40 };
    this.touchBtnDiario = { x: 775, y: 16, width: 62, height: 40 };
    this.touchBtnObjetivos = { x: 700, y: 16, width: 67, height: 40 };
    this.touchBtnMenu = { x: 915, y: 16, width: 62, height: 40 };
    this.touchBtnFechar = { x: 20, y: 16, width: 62, height: 40 };

    this.optBtnRect1 = null;
    this.optBtnRect2 = null;

    // Menu Pause buttons rects
    this.btnPauseResume = { x: 350, y: 190, width: 300, height: 46 };
    this.btnPauseSalvar = { x: 350, y: 245, width: 300, height: 46 };
    this.btnPauseDiario = { x: 350, y: 300, width: 300, height: 46 };
    this.btnPauseMissoes = { x: 350, y: 355, width: 300, height: 46 };
    this.btnPauseItens = { x: 350, y: 410, width: 300, height: 46 };
    this.btnPauseExit = { x: 350, y: 465, width: 300, height: 46 };

    // Menu Principal rects
    this.btnMenuNovo = { x: 85, y: 205, width: 320, height: 62 };
    this.btnMenuContinuar = { x: 85, y: 290, width: 320, height: 62 };
    this.btnMenuSobre = { x: 85, y: 375, width: 320, height: 62 };
    this.btnMenuSom = { x: 85, y: 460, width: 320, height: 52 };
    this.mousePos = { x: 0, y: 0 };
    this.particulasMenu = [];
    for (let i = 0; i < 28; i++) {
      this.particulasMenu.push({
        x: Math.random() * 1000,
        y: Math.random() * 750,
        radius: 1.2 + Math.random() * 2.6,
        vy: 0.25 + Math.random() * 0.45,
        vx: (Math.random() - 0.5) * 0.35,
        alpha: 0.2 + Math.random() * 0.65,
        color: Math.random() > 0.4 ? '#FFD764' : '#F1A0C4'
      });
    }

    // Menu Slots rects
    this.btnSlot1 = { x: 300, y: 220, width: 400, height: 68 };
    this.btnSlot2 = { x: 300, y: 305, width: 400, height: 68 };
    this.btnSlot3 = { x: 300, y: 390, width: 400, height: 68 };
    this.btnSlotVoltar = { x: 350, y: 500, width: 300, height: 55 };

    // Cutscene buttons rects
    this.btnCutscenePular = { x: 840, y: 25, width: 130, height: 42 };
    this.btnCutsceneAvancar = { x: 350, y: 660, width: 300, height: 55 };

    // Assets
    this.images = {};
    this.assetsLoaded = false;
    this.scale = 1;
    this.offsetX = 0;
    this.offsetY = 0;

    this.init();
  }

  sincronizarComHQ() {
    if (typeof HQ !== 'undefined') {
      this.nivelArte = HQ.getLevel('arte') || 1;
      this.nivelLeitura = HQ.getLevel('leitura') || 1;
      this.nivelFitness = HQ.getLevel('fitness') || 1;
      this.xp = HQ.getTotalXP() || 0;
    }
  }

  getSaveKey(slot) {
    const userSuffix = (typeof HQ !== 'undefined' && typeof HQ._suffix === 'function') ? HQ._suffix() : '';
    return 'hq_game_slot_' + slot + userSuffix;
  }

  getSlotInfo(slot) {
    try {
      const raw = localStorage.getItem(this.getSaveKey(slot));
      if (!raw) return null;
      return JSON.parse(raw);
    } catch (e) {
      return null;
    }
  }

  salvarEstado(slot = this.slotAtual) {
    try {
      this.slotAtual = slot;
      this.sincronizarComHQ();
      const state = {
        slot: slot,
        timestamp: Date.now(),
        dataHora: new Date().toLocaleDateString('pt-BR') + ' ' + new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }),
        indiceMapa: this.indiceMapa,
        audreyX: this.audreyX,
        audreyY: this.audreyY,
        olhandoDireita: this.olhandoDireita,
        temChave: this.temChave,
        armarioEstaAberto: this.armarioEstaAberto,
        nicolasJaFoiEncontrado: this.nicolasJaFoiEncontrado,
        livroJoiFoiPego: this.livroJoiFoiPego,
        temLivro: this.temLivro,
        faseDialogoNicolas: this.faseDialogoNicolas,
        temDiario: this.temDiario,
        temCadernoEsboco: this.temCadernoEsboco,
        temCronograma: this.temCronograma,
        explorouCorredor: this.explorouCorredor,
        ep1FalouNpc1: this.ep1FalouNpc1,
        ep1FalouNpc2: this.ep1FalouNpc2,
        ep1FalouNpc3: this.ep1FalouNpc3,
        ep1FalouGabiCorredor: this.ep1FalouGabiCorredor,
        ep1FalouIviCorredor: this.ep1FalouIviCorredor,
        faseDialogoGabi: this.faseDialogoGabi,
        faseDialogoIvi: this.faseDialogoIvi,
        ep1InteragiuBiblioteca: this.ep1InteragiuBiblioteca,
        ep1InteragiuMural: this.ep1InteragiuMural,
        sala1Aberta: this.sala1Aberta,
        faseDialogoLeitura: this.faseDialogoLeitura,
        faseDialogoArte: this.faseDialogoArte,
        faseDialogoFitness: this.faseDialogoFitness,
        dialogoSalaAutoIniciado: this.dialogoSalaAutoIniciado,
        dialogoSalaAutoConcluido: this.dialogoSalaAutoConcluido,
        faseDialogoSalaAuto: this.faseDialogoSalaAuto,
        ep1_solidaoUrbanaAtiva: this.ep1_solidaoUrbanaAtiva,
        ep1_solidaoUrbanaConcluida: this.ep1_solidaoUrbanaConcluida,
        ep1_entregouDesenho: this.ep1_entregouDesenho,
        xp: this.xp,
        xpRaquelDado: this.xpRaquelDado,
        xpNicolasDado: this.xpNicolasDado,
        xpCamilaDado: this.xpCamilaDado,
        nivelArte: this.nivelArte,
        nivelLeitura: this.nivelLeitura,
        nivelFitness: this.nivelFitness
      };
      localStorage.setItem(this.getSaveKey(slot), JSON.stringify(state));
      this.textoNotificacao = `Jogo salvo com sucesso no Slot ${slot}!`;
      this.mostrarNotificacaoMissao = true;
      this.contadorNotificacaoMissao = 0;
      if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomColeta();
      return true;
    } catch (e) {
      console.error('Erro ao salvar jogo:', e);
      return false;
    }
  }

  carregarEstado(slot = this.slotAtual) {
    try {
      const state = this.getSlotInfo(slot);
      if (!state) return false;
      this.slotAtual = slot;
      this.indiceMapa = state.indiceMapa !== undefined ? state.indiceMapa : 0;
      this.audreyX = state.audreyX !== undefined ? state.audreyX : 100;
      this.audreyY = state.audreyY !== undefined ? state.audreyY : 580;
      this.olhandoDireita = state.olhandoDireita !== undefined ? state.olhandoDireita : true;
      this.temChave = !!state.temChave;
      this.armarioEstaAberto = !!state.armarioEstaAberto;
      this.nicolasJaFoiEncontrado = !!state.nicolasJaFoiEncontrado;
      this.livroJoiFoiPego = !!state.livroJoiFoiPego;
      this.temLivro = !!state.temLivro;
      this.faseDialogoNicolas = state.faseDialogoNicolas || 0;
      this.temDiario = !!state.temDiario;
      this.temCadernoEsboco = !!state.temCadernoEsboco;
      this.temCronograma = !!state.temCronograma;
      this.explorouCorredor = !!state.explorouCorredor;
      this.ep1FalouNpc1 = !!state.ep1FalouNpc1;
      this.ep1FalouNpc2 = !!state.ep1FalouNpc2;
      this.ep1FalouNpc3 = !!state.ep1FalouNpc3;
      this.ep1FalouGabiCorredor = !!state.ep1FalouGabiCorredor;
      this.ep1FalouIviCorredor = !!state.ep1FalouIviCorredor;
      this.faseDialogoGabi = state.faseDialogoGabi || 0;
      this.faseDialogoIvi = state.faseDialogoIvi || 0;
      this.ep1InteragiuBiblioteca = !!state.ep1InteragiuBiblioteca;
      this.ep1InteragiuMural = !!state.ep1InteragiuMural;
      this.sala1Aberta = !!state.sala1Aberta;
      this.faseDialogoLeitura = state.faseDialogoLeitura || 0;
      this.faseDialogoArte = state.faseDialogoArte || 0;
      this.faseDialogoFitness = state.faseDialogoFitness || 0;
      this.dialogoSalaAutoIniciado = !!state.dialogoSalaAutoIniciado;
      this.dialogoSalaAutoConcluido = !!state.dialogoSalaAutoConcluido;
      this.faseDialogoSalaAuto = state.faseDialogoSalaAuto || 0;
      this.ep1_solidaoUrbanaAtiva = !!state.ep1_solidaoUrbanaAtiva;
      this.ep1_solidaoUrbanaConcluida = !!state.ep1_solidaoUrbanaConcluida;
      this.ep1_entregouDesenho = !!state.ep1_entregouDesenho;
      this.xpRaquelDado = !!state.xpRaquelDado;
      this.xpNicolasDado = !!state.xpNicolasDado;
      this.xpCamilaDado = !!state.xpCamilaDado;

      this.sincronizarComHQ();
      this.textoDialogo = "";
      this.nomePersonagem = "";
      this.estaEmDialogoNicolas = false;
      this.inventarioAberto = false;
      this.mostrarObjetivos = false;
      this.diarioAberto = false;
      this.menuAberto = false;
      this.currentState = this.STATE.JOGO;

      this.textoNotificacao = `Slot ${slot} carregado!`;
      this.mostrarNotificacaoMissao = true;
      this.contadorNotificacaoMissao = 0;
      return true;
    } catch (e) {
      console.error('Erro ao carregar jogo:', e);
      return false;
    }
  }

  async init() {
    this.setupEvents();
    this.resizeCanvas();
    window.addEventListener('resize', () => this.resizeCanvas());

    this.sincronizarComHQ();

    await this.loadAllAssets();
    this.assetsLoaded = true;
    window.dispatchEvent(new CustomEvent('hqgame:ready'));

    // Loop principal (60 FPS ~ 16ms)
    setInterval(() => {
      this.actionPerformed();
    }, 16);
  }

  resizeCanvas() {
    const container = this.canvas.parentElement || document.body;
    const cw = container.clientWidth || window.innerWidth;
    const ch = container.clientHeight || window.innerHeight;

    this.canvas.width = cw;
    this.canvas.height = ch;

    this.scale = Math.min(cw / this.LARGURA, ch / this.ALTURA);
    this.offsetX = Math.floor((cw - this.LARGURA * this.scale) / 2);
    this.offsetY = Math.floor((ch - this.ALTURA * this.scale) / 2);

    this.ctx.imageSmoothingEnabled = true;
    this.ctx.imageSmoothingQuality = 'high';
  }

  async loadAllAssets() {
    const assetList = {
      banner: 'imagens/banner_menu.jpg',
      titulo: 'imagens/titulo.png',

      // Cutscene Slides 1..12
      slide1: 'imagens/slide1.png',
      slide2: 'imagens/slide2.png',
      slide3: 'imagens/slide3.png',
      slide4: 'imagens/slide4.png',
      slide5: 'imagens/slide5.png',
      slide6: 'imagens/slide6.png',
      slide7: 'imagens/slide7.png',
      slide8: 'imagens/slide8.png',
      slide9: 'imagens/slide9.png',
      slide10: 'imagens/slide10.png',
      slide11: 'imagens/slide11.png',
      slide12: 'imagens/slide12.png',

      corredor1: 'imagens/corredor1.png',
      corredor2: 'imagens/corredor2.png',
      corredor3: 'imagens/corredor3.png',
      ginasio: 'imagens/ginasio.jpg',
      biblioteca: 'imagens/bibliotecasala4.png',
      salaAula: 'imagens/sala de aula 1.png',
      armarioAberto: 'imagens/imagemarmario.png',

      audreyParada: 'imagens/parada-removebg-preview (1).png',
      audreyAndar1: 'imagens/andarum.png',
      audreyAndar2: 'imagens/andar02-removebg-preview.png',

      nicolas: 'imagens/nico__1_-removebg-preview-Photoroom.png',
      raquel: 'imagens/pixil-frame-0__4_-removebg-preview-Photoroom.png',
      camila: 'imagens/a_full_body_drawing_of_the_female_character_from_data_image_image_12_showing-removebg-preview-removebg-preview-Photoroom (1).png',
      gabi: 'imagens/gabi_personagem-removebg-preview.png',
      gabiCorredor: 'imagens/pixil-frame-0-Photoroom (1).png',
      ivi: 'imagens/1000115243-removebg-preview.png',
      victor: 'imagens/victor_personagem.png',
      maysa: 'imagens/maysa.png',

      portraitAudrey: 'imagens/pixil-frame-0 (5)-Photoroom.png',
      portraitNicollas: 'imagens/nicollas_caixa_dialogo.png',
      portraitGabi: 'imagens/gabi caixa de dialogo-Photoroom.png',
      portraitIvi: 'imagens/ivi_portrait.png',
      portraitRaquel: 'imagens/quel_pixel_art_modified (1)-Photoroom (1).png',
      portraitNicolas: 'imagens/portrait_nicollas-removebg-preview.png',
      portraitCamila: 'imagens/camiz_pixel_art (1)-Photoroom (1).png',
      portraitVictor: 'imagens/victor_portrait.png',
      portraitMaysa: 'imagens/maysa_portrait.png',

      chave: 'imagens/chave.png',
      livro: 'imagens/livro.png',
      iconeInteragir: 'imagens/icone_interagir.png'
    };

    const promises = Object.entries(assetList).map(([key, src]) => {
      return new Promise((resolve) => {
        const img = new Image();
        img.onload = () => { this.images[key] = img; resolve(); };
        img.onerror = () => {
          console.warn(`[Asset] Não foi possível carregar ${src}`);
          this.images[key] = null;
          resolve();
        };
        img.src = src;
      });
    });

    await Promise.all(promises);
  }

  // ─── CONVERSÃO DE COORDENADAS EXATAS (0..1000, 0..750) ─────────────
  getGameCoordinates(e) {
    const rect = this.canvas.getBoundingClientRect();
    const clientX = e.clientX !== undefined ? e.clientX : (e.touches && e.touches[0] ? e.touches[0].clientX : (e.changedTouches && e.changedTouches[0] ? e.changedTouches[0].clientX : 0));
    const clientY = e.clientY !== undefined ? e.clientY : (e.touches && e.touches[0] ? e.touches[0].clientY : (e.changedTouches && e.changedTouches[0] ? e.changedTouches[0].clientY : 0));

    const canvasX = clientX - rect.left;
    const canvasY = clientY - rect.top;

    const realX = (canvasX - this.offsetX) / this.scale;
    const realY = (canvasY - this.offsetY) / this.scale;
    return { x: Math.floor(realX), y: Math.floor(realY) };
  }

  // ─── TECLADO E MOUSE / TOUCH ─────────────────────────────────────
  setupEvents() {
    window.addEventListener('keydown', (e) => this.keyPressed(e));
    window.addEventListener('keyup', (e) => this.keyReleased(e));

    const handlePointerDown = (e) => {
      this.mousePressed(e);
    };

    const handlePointerUp = (e) => {
      this.mouseReleased(e);
    };

    const handlePointerMove = (e) => {
      this.mouseMoved(e);
    };

    this.canvas.addEventListener('mousedown', handlePointerDown);
    this.canvas.addEventListener('mouseup', handlePointerUp);
    this.canvas.addEventListener('mousemove', handlePointerMove);

    this.canvas.addEventListener('touchstart', (e) => {
      if (e.cancelable) e.preventDefault();
      handlePointerDown(e);
    }, { passive: false });

    this.canvas.addEventListener('touchend', (e) => {
      if (e.cancelable) e.preventDefault();
      handlePointerUp(e);
    }, { passive: false });

    this.canvas.addEventListener('touchmove', (e) => {
      if (e.cancelable) e.preventDefault();
      handlePointerMove(e);
    }, { passive: false });
  }

  keyPressed(e) {
    const code = e.keyCode || e.which;

    if (code === 27) { // ESC: Menu em jogo / voltar
      if (this.currentState === this.STATE.JOGO) {
        this.menuAberto = !this.menuAberto;
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
      } else if (this.currentState === this.STATE.MENU_SLOTS || this.currentState === this.STATE.SOBRE) {
        this.currentState = this.STATE.MENU_PRINCIPAL;
      } else if (this.currentState === this.STATE.CUTSCENE) {
        this.iniciarJogo();
      }
      return;
    }

    if (this.currentState === this.STATE.CUTSCENE) {
      if (code === 32 || code === 13 || code === 39) { // Espaço, Enter, Direita: Avançar cutscene
        this.avancarCutscene();
      }
      return;
    }

    if (this.currentState !== this.STATE.JOGO) return;

    if (this.menuAberto) return;
    if (this.easterEggAtivo) {
      if (code === 81 || code === 27 || code === 32) this.easterEggAtivo = false;
      return;
    }

    if (code === 66 || code === 73) { // B ou I: Inventário
      this.inventarioAberto = !this.inventarioAberto;
      if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
      return;
    }

    if (this.faseDialogoNicolas === 100 && this.tamanhoTextoVisivel >= this.textoDialogo.length) {
      if (code === 38 || code === 87 || code === 65 || code === 37) {
        this.selectedDialogueOption = 0;
        return;
      }
      if (code === 40 || code === 83 || code === 68 || code === 39) {
        this.selectedDialogueOption = 1;
        return;
      }
    }

    if (code === 77) { // M: Objetivos
      this.mostrarObjetivos = !this.mostrarObjetivos;
      if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
      return;
    }

    if (code === 68 || code === 39) { // D ou Direita
      this.moverDireita();
    }

    if (code === 65 || code === 37) { // A ou Esquerda
      this.moverEsquerda();
    }

    if (code === 74) { // J: Diário
      if (!this.temDiario) {
        this.textoNotificacao = "Pegue o diário em seu armário antes!";
        this.mostrarNotificacaoMissao = true;
        this.contadorNotificacaoMissao = 0;
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomErro();
      } else {
        this.diarioAberto = !this.diarioAberto;
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
      }
      return;
    }

    const isFalarKey = (code === 70 || code === 32 || code === 13 || code === 69);

    if (isFalarKey && (this.estaEmDialogoNicolas || this.aguardandoAvanceSalaAuto || this.textoDialogo.length > 0)) {
      this.avancarDialogo();
      return;
    }

    if (code === 69) { // E: Interagir
      this.executarInteracao();
    }

    if (code === 81) { // Q: Fechar
      if (this.armarioEstaAberto) {
        this.armarioEstaAberto = false;
      }
      this.entregarXpPendente();
      this.textoDialogo = "";
      this.nomePersonagem = "";
      this.estaEmDialogoNicolas = false;
      if (window.GerenciadorAudio) window.GerenciadorAudio.pararTodasAsVozes();
    }
  }

  keyReleased(e) {
    const code = e.keyCode || e.which;
    if (code === 68 || code === 39 || code === 65 || code === 37) {
      this.pararMovimento();
    }
  }

  // ─── LÓGICA DE MOVIMENTO E ATUALIZAÇÃO ─────────────────────────
  moverEsquerda() {
    const emDialogoForcado = this.aguardandoAvanceSalaAuto || this.faseDialogoNicolas >= 100;
    if (!emDialogoForcado && !this.inventarioAberto && !this.diarioAberto && !this.armarioEstaAberto && !this.menuAberto && this.currentState === this.STATE.JOGO) {
      this.velX = (this.indiceMapa === 2) ? -9 : -6;
      this.olhandoDireita = false;
      this.estaMovendo = true;
      if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomPassos();
    }
  }

  moverDireita() {
    const emDialogoForcado = this.aguardandoAvanceSalaAuto || this.faseDialogoNicolas >= 100;
    if (!emDialogoForcado && !this.inventarioAberto && !this.diarioAberto && !this.armarioEstaAberto && !this.menuAberto && this.currentState === this.STATE.JOGO) {
      this.velX = (this.indiceMapa === 2) ? 9 : 6;
      this.olhandoDireita = true;
      this.estaMovendo = true;
      if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomPassos();
    }
  }

  pararMovimento() {
    this.velX = 0;
    this.estaMovendo = false;
    if (window.GerenciadorAudio) window.GerenciadorAudio.pararSomPassos();
  }

  avancarCutscene() {
    if (this.slideCutsceneAtual < this.totalSlides - 1) {
      this.slideCutsceneAtual++;
      if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
    } else {
      this.iniciarJogo();
    }
  }

  iniciarJogo() {
    this.currentState = this.STATE.JOGO;
    this.indiceMapa = 0;
    this.audreyX = 100;
    if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomAbrirPorta();
  }

  actionPerformed() {
    this.animAngulo += 0.05;

    if (this.currentState !== this.STATE.JOGO) {
      this.render();
      return;
    }

    if (this.touchEsquerdaPressionado) {
      this.moverEsquerda();
    } else if (this.touchDireitaPressionado) {
      this.moverDireita();
    }

    if (this.menuAberto || this.easterEggAtivo) {
      if (this.easterEggAtivo) this.easterEggTimer++;
      this.render();
      return;
    }

    if (this.textoDialogo.length > 0) {
      if (this.textoDialogo !== this.textoDialogoAnterior) {
        this.textoDialogoAnterior = this.textoDialogo;
        this.tamanhoTextoVisivel = 0;
        this.tamanhoTextoVisivelAcumulado = 0.0;
        this.contadorTypewriter = 0;
      }

      if (this.tamanhoTextoVisivel < this.textoDialogo.length) {
        this.tamanhoTextoVisivelAcumulado += 0.35;
        const novoTamanho = Math.floor(this.tamanhoTextoVisivelAcumulado);
        if (novoTamanho > this.tamanhoTextoVisivel) {
          this.tamanhoTextoVisivel = Math.min(this.textoDialogo.length, novoTamanho);
          const charAtual = this.textoDialogo.charAt(this.tamanhoTextoVisivel - 1);
          if (charAtual !== ' ' && charAtual !== '.' && charAtual !== ',' && charAtual !== '!') {
            if (window.GerenciadorAudio) window.GerenciadorAudio.tocarVoz(this.nomePersonagem);
          }
        }
      } else {
        if (window.GerenciadorAudio) window.GerenciadorAudio.pararTodasAsVozes();
      }
    } else {
      if (this.textoDialogoAnterior.length > 0) {
        this.textoDialogoAnterior = "";
        this.tamanhoTextoVisivel = 0;
        if (window.GerenciadorAudio) window.GerenciadorAudio.pararTodasAsVozes();
      }
    }

    if (this.mostrando_chave) {
      this.contadorEfeitoChave++;
      if (this.contadorEfeitoChave >= 60) {
        this.mostrando_chave = false;
        this.contadorEfeitoChave = 0;
      }
    }

    if (this.mostrarNotificacaoMissao) {
      this.contadorNotificacaoMissao++;
      if (this.contadorNotificacaoMissao >= this.NOTIFICACAO_DURACAO) {
        this.mostrarNotificacaoMissao = false;
        this.contadorNotificacaoMissao = 0;
      }
    }

    if (this.mostrarNotificacaoXP) {
      this.contadorNotificacaoXP++;
      if (this.contadorNotificacaoXP >= this.NOTIFICACAO_XP_DURACAO) {
        this.mostrarNotificacaoXP = false;
        this.contadorNotificacaoXP = 0;
      }
    }

    if (this.mostrarAnimacaoRiscado) {
      this.contadorAnimacaoRiscado++;
      if (this.contadorAnimacaoRiscado >= 50) {
        this.mostrarAnimacaoRiscado = false;
        this.contadorAnimacaoRiscado = 0;
      }
    }

    if (this.contadorTeleporte >= 0 && this.indiceMapa === 2) {
      this.contadorTeleporte++;
      if (this.contadorTeleporte >= 600 && !this.personagensNaBiblioteca) {
        this.personagensNaBiblioteca = true;
        this.contadorTeleporte = -1;
      }
    }

    if (!this.armarioEstaAberto) {
      this.audreyX += this.velX;

      if (this.indiceMapa === 3 || this.indiceMapa === 4 || this.indiceMapa === 5 || this.indiceMapa === 6) {
        this.explorouCorredor = true;
      }

      if (this.indiceMapa === 0) {
        this.visitouGinasio = true;
        if (this.audreyX < 0) this.audreyX = 0;
        if (this.audreyX > this.LARGURA) {
          this.indiceMapa = 1;
          this.audreyX = 0;
          this.textoDialogo = "";
          this.nomePersonagem = "";
          this.estaEmDialogoNicolas = false;
          this.faseDialogoNicolas = 0;
          if (!this.sala1Aberta) {
            this.sala1Aberta = true;
            if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomSinalEscolar();
          }
        }
      } else if (this.indiceMapa === 1) {
        if (this.audreyX < 0) {
          this.audreyX = 0;
          if (this.velX < 0) {
            this.indiceMapa = 0;
            this.audreyX = this.LARGURA - this.AUDREY_LARGURA;
            this.textoDialogo = "";
            this.nomePersonagem = "";
            this.estaEmDialogoNicolas = false;
            this.faseDialogoNicolas = 0;
          }
        }
        if (this.audreyX + this.AUDREY_LARGURA > this.LARGURA) {
          this.indiceMapa = 3;
          this.audreyX = 0;
          this.textoDialogo = "";
          this.nomePersonagem = "";
          this.estaEmDialogoNicolas = false;
        }
      } else if (this.indiceMapa === 3) {
        this.visitouCorredor2 = true;
        if (this.audreyX < 0) {
          this.audreyX = 0;
          if (this.velX < 0) {
            this.indiceMapa = 1;
            this.audreyX = this.LARGURA - this.AUDREY_LARGURA;
            this.textoDialogo = "";
            this.nomePersonagem = "";
          }
        }
        if (this.audreyX + this.AUDREY_LARGURA > this.LARGURA) {
          this.indiceMapa = 4;
          this.audreyX = 0;
          this.textoDialogo = "";
          this.nomePersonagem = "";
        }
      } else if (this.indiceMapa === 4) {
        this.visitouCorredor3 = true;
        if (this.audreyX < 0) {
          this.audreyX = 0;
          if (this.velX < 0) {
            this.indiceMapa = 3;
            this.audreyX = this.LARGURA - this.AUDREY_LARGURA;
            this.textoDialogo = "";
            this.nomePersonagem = "";
          }
        }
        if (this.audreyX + this.AUDREY_LARGURA > this.LARGURA) {
          this.indiceMapa = 5;
          this.audreyX = 0;
          this.textoDialogo = "";
          this.nomePersonagem = "";
        }
      } else if (this.indiceMapa === 5) {
        this.visitouCorredor4 = true;
        if (this.audreyX < 0) {
          this.audreyX = 0;
          if (this.velX < 0) {
            this.indiceMapa = 4;
            this.audreyX = this.LARGURA - this.AUDREY_LARGURA;
            this.textoDialogo = "";
            this.nomePersonagem = "";
          }
        }
        if (this.audreyX + this.AUDREY_LARGURA > this.LARGURA) {
          this.audreyX = this.LARGURA - this.AUDREY_LARGURA;
        }
      } else if (this.indiceMapa === 2) {
        if (this.audreyX < 0) this.audreyX = 0;
        if (this.audreyX + this.AUDREY_LARGURA > this.LARGURA) {
          this.audreyX = this.LARGURA - this.AUDREY_LARGURA;
        }

        if (!this.dialogoSalaAutoIniciado) {
          this.dialogoSalaAutoIniciado = true;
          this.faseDialogoSalaAuto = 1;
          this.aguardandoAvanceSalaAuto = true;
          this.estaEmDialogoNicolas = true;
          this.nomePersonagem = "Audrey";
          this.textoDialogo = "— Oi, com licença... Sou a Audrey, aluna nova.";
          if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();

          if (!this.xpRaquelDado && !this.xpNicolasDado && !this.xpCamilaDado) {
            this.xpRaquelDado = true;
            this.xpNicolasDado = true;
            this.xpCamilaDado = true;
            this.adicionarXpPendente(3);
          }
        }

        if (this.dialogoSalaAutoConcluido && !this.estaEmDialogoNicolas) {
          if (Math.abs(this.audreyX - 250) > 220 && Math.abs(this.audreyX - 390) > 220 && Math.abs(this.audreyX - 530) > 220) {
            this.textoDialogo = "";
            this.nomePersonagem = "";
          }
        }
      } else if (this.indiceMapa === 6) {
        if (this.audreyX < 0) this.audreyX = 0;
        if (this.audreyX + this.AUDREY_LARGURA > this.LARGURA) {
          this.audreyX = this.LARGURA - this.AUDREY_LARGURA;
        }
      }

      if (this.indiceMapa === 0 && Math.abs(this.audreyX - this.posArmarioX) > 200) {
        this.textoDialogo = "";
        this.nomePersonagem = "";
      }

      if (this.estaMovendo) {
        this.contadorAnimacao++;
        if (this.contadorAnimacao > 10) {
          this.frameAtual = (this.frameAtual === 0) ? 1 : 0;
          this.contadorAnimacao = 0;
        }
      } else {
        this.frameAtual = 0;
      }
    }

    this.render();
  }

  // ─── DIÁLOGOS E INTERAÇÕES ─────────────────────────────────────
  avancarDialogo() {
    if (this.textoDialogo.length > 0 && this.tamanhoTextoVisivel < this.textoDialogo.length) {
      this.tamanhoTextoVisivel = this.textoDialogo.length;
      this.tamanhoTextoVisivelAcumulado = this.textoDialogo.length;
      if (window.GerenciadorAudio) window.GerenciadorAudio.pararTodasAsVozes();
      return;
    }

    if (this.faseDialogoNicolas === 100 && this.tamanhoTextoVisivel >= this.textoDialogo.length) {
      if (this.selectedDialogueOption === 0) {
        if (window.mostrarImportadorReal) window.mostrarImportadorReal();
      } else {
        this.estaEmDialogoNicolas = false;
        this.textoDialogo = "";
        this.nomePersonagem = "";
        this.faseDialogoNicolas = 0;
        this.ep1_entregouDesenho = false;
      }
      return;
    }

    if (this.indiceMapa === 2) {
      if (this.aguardandoAvanceSalaAuto && !this.dialogoSalaAutoConcluido) {
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
        this.faseDialogoSalaAuto++;
        this.tamanhoTextoVisivel = 0;
        this.tamanhoTextoVisivelAcumulado = 0.0;
        switch (this.faseDialogoSalaAuto) {
          case 1:
            this.nomePersonagem = "Audrey";
            this.textoDialogo = "— Oi, com licença... Sou a Audrey, aluna nova.";
            break;
          case 2:
            this.nomePersonagem = "Raquel";
            this.textoDialogo = "— Ah, oi! Seja bem-vinda! Eu sou a Raquel. Estávamos, justamente, discutindo sobre o projeto do festival cultural.";
            break;
          case 3:
            this.nomePersonagem = "Nicollas";
            this.textoDialogo = "— Salve, Audrey! Sou o Nicollas. Cara, a Raquel quer ir pelo caminho mais clássico, mas, eu acho, que a gente devia meter uma parada mais urbana... tipo, um grafite expressionista.";
            break;
          case 4:
            this.nomePersonagem = "Nicollas";
            this.textoDialogo = "— Arte tem que ter impacto, sabe? Sentimento, puro sentimento bruto!";
            break;
          case 5:
            this.nomePersonagem = "Camila";
            this.textoDialogo = "— Sou a Camila. E, o Nicollas, esquece que o expressionismo é, justamente, sobre distorcer a realidade para expressar as emoções. Não precisa ser só grafite.";
            break;
          case 6:
            this.nomePersonagem = "Camila";
            this.textoDialogo = "— A arte ganha vida, de verdade, quando o conceito é forte. E você, Audrey? Curte alguma vertente?";
            break;
          case 7:
            this.nomePersonagem = "Audrey";
            this.textoDialogo = "— Eu gosto bastante de desenhar, na verdade. Para mim, a arte ajuda a colocar para fora coisas... coisas que as palavras não dão conta.";
            break;
          case 8:
            this.nomePersonagem = "Raquel";
            this.textoDialogo = "— Sério? Que incrível! Olha, a gente está precisando de ajuda, e de ideias novas para o grupo. Que tal um teste rápido, bem rápido, para ver o seu estilo?";
            break;
          case 9:
            this.nomePersonagem = "Nicollas";
            this.textoDialogo = "— Boa! Vamos ver, então, o seu nível artístico. Quero ver como você interpreta o tema: 'O Sentimento da Solidão Urbana'.";
            break;
          case 10:
            this.nomePersonagem = "Nicollas";
            this.textoDialogo = "— Pode ser algo tecnológico, um cenário bem vazio, ou, quem sabe, só alguém na multidão.";
            break;
          case 11:
            this.nomePersonagem = "Camila";
            this.textoDialogo = "— Mostra para a gente, Audrey, do que você é capaz. Estamos, de verdade, bem curiosos!";
            break;
          default:
            this.dialogoSalaAutoConcluido = true;
            this.aguardandoAvanceSalaAuto = false;
            this.estaEmDialogoNicolas = false;
            this.textoDialogo = "";
            this.nomePersonagem = "";
            this.ep1_solidaoUrbanaAtiva = true;
            this.entregarXpPendente();
            this.textoNotificacao = "Missão adicionada ao Diário! Pressione [J] para abrir o diário";
            this.mostrarNotificacaoMissao = true;
            this.contadorNotificacaoMissao = 0;
            break;
        }
        return;
      }

      if (this.dialogoSalaAutoConcluido) {
        this.estaEmDialogoNicolas = true;
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();

        if (this.ep1_solidaoUrbanaAtiva && !this.ep1_solidaoUrbanaConcluida && !this.ep1_entregouDesenho) {
          this.ep1_entregouDesenho = true;
          this.faseDialogoNicolas = 100;
          this.nomePersonagem = "Audrey";
          this.textoDialogo = " ";
          this.tamanhoTextoVisivel = 0;
          this.tamanhoTextoVisivelAcumulado = 0.0;
        } else if (this.faseDialogoNicolas === 101) {
          this.nomePersonagem = "Raquel";
          this.textoDialogo = "— Perfeito! Você passou no teste com folga, Audrey. Agora, você é, oficialmente, a mente criativa do nosso grupo. Pronta para os próximos desafios?";
          this.faseDialogoNicolas = 102;
          this.tamanhoTextoVisivel = 0;
          this.tamanhoTextoVisivelAcumulado = 0.0;
        } else if (this.faseDialogoNicolas === 102) {
          this.estaEmDialogoNicolas = false;
          this.textoDialogo = "";
          this.nomePersonagem = "";
          this.faseDialogoNicolas = 0;
          this.ganharXP(10);
        } else {
          this.tamanhoTextoVisivel = 0;
          this.tamanhoTextoVisivelAcumulado = 0.0;
          if (Math.abs(this.audreyX - 250) < 150) {
            this.nomePersonagem = "Raquel";
            this.textoDialogo = this.ep1_entregouDesenho ? "— Pronta para os próximos desafios?" : "— Como está ficando o esboço?";
          } else if (Math.abs(this.audreyX - 390) < 150) {
            this.nomePersonagem = "Nicollas";
            this.textoDialogo = this.ep1_entregouDesenho ? "— Você desenha muito bem!" : "— Mal posso esperar para ver seu desenho!";
          } else if (Math.abs(this.audreyX - 530) < 150) {
            this.nomePersonagem = "Camila";
            this.textoDialogo = this.ep1_entregouDesenho ? "— Que traço sensacional!" : "— Como está ficando o esboço?";
          }
        }
      }
    } else {
      this.estaEmDialogoNicolas = false;
      this.textoDialogo = "";
      this.nomePersonagem = "";
      if (window.GerenciadorAudio) window.GerenciadorAudio.pararTodasAsVozes();
    }
  }

  executarInteracao() {
    if (this.armarioEstaAberto && !this.livroJoiFoiPego) {
      if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomColeta();
      this.temLivro = true;
      this.livroJoiFoiPego = true;
      return;
    }

    if (this.indiceMapa === 2 && this.estaProximoDaPuerta()) {
      if (this.estaEmDialogoNicolas || this.textoDialogo.length > 0) return;
      if (this.ep1_solidaoUrbanaAtiva && !this.ep1_entregouDesenho) {
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomErro();
        this.estaEmDialogoNicolas = true;
        this.nomePersonagem = "Audrey";
        this.textoDialogo = "Não posso sair sem antes entregar o desenho para o pessoal.";
        return;
      }
      if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomAbrirPorta();
      this.indiceMapa = 1;
      this.audreyX = this.ultimaPosAoEntraSala;
      this.textoDialogo = "";
      this.nomePersonagem = "";
      this.estaEmDialogoNicolas = false;
      this.faseDialogoNicolas = 0;
      return;
    }

    if (this.indiceMapa === 1 && Math.abs(this.audreyX - this.posPortaX) < 150) {
      if (!this.sala1Aberta) {
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomErro();
        this.estaEmDialogoNicolas = true;
        this.nomePersonagem = "Audrey";
        this.textoDialogo = "Ainda preciso conhecer o lugar e falar com o pessoal antes de entrar.";
        return;
      }
      if (!this.explorouCorredor) {
        this.nomePersonagem = "Audrey";
        this.textoDialogo = "Acho melhor eu ir explorar um pouco a escola antes de entrar na sala de aula.";
        this.estaEmDialogoNicolas = true;
      } else {
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomAbrirPorta();
        this.ultimaPosAoEntraSala = this.audreyX;
        this.indiceMapa = 2;
        this.audreyX = 100;
        this.textoDialogo = "";
        this.nomePersonagem = "";
        this.estaEmDialogoNicolas = false;
        this.faseDialogoNicolas = 0;
      }
      return;
    }

    if (this.indiceMapa === 1 && !this.personagensNaBiblioteca && Math.abs(this.audreyX - 480) < 110 && this.textoDialogo.length === 0) {
      if (!this.ep1FalouGabiCorredor) {
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
        this.estaEmDialogoNicolas = true;
        this.nomePersonagem = "Gabi";
        this.textoDialogo = "Oi! Você é a aluna nova, né? Eu sou a Gabi e essa é a Ivi! A sala de aula é bem ali, pode ir lá tranquila.";
        this.ep1FalouGabiCorredor = true;
        this.ep1FalouNpc2 = true;
        this.adicionarXpPendente(1);
        this.checarObjetivosEp1();
        return;
      } else {
        this.estaEmDialogoNicolas = true;
        this.nomePersonagem = "Gabi";
        this.textoDialogo = "A Ivi e eu estamos conversando aqui no corredor. Qualquer coisa é só chamar!";
        return;
      }
    }

    if (this.indiceMapa === 1 && !this.personagensNaBiblioteca && Math.abs(this.audreyX - 630) < 110 && this.textoDialogo.length === 0) {
      if (!this.ep1FalouIviCorredor) {
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
        this.estaEmDialogoNicolas = true;
        this.nomePersonagem = "Ivi";
        this.textoDialogo = "Oi! Você deve ser a Audrey, né? Eu sou a Ivi! Seja muito bem-vinda à escola!";
        this.ep1FalouIviCorredor = true;
        this.ep1FalouNpc3 = true;
        this.adicionarXpPendente(1);
        this.checarObjetivosEp1();
        return;
      } else {
        this.estaEmDialogoNicolas = true;
        this.nomePersonagem = "Ivi";
        this.textoDialogo = "Estamos aproveitando o tempinho antes da aula começar. Pode explorar à vontade!";
        return;
      }
    }

    if (this.indiceMapa === 2 && Math.abs(this.audreyX - this.posNicolasXSalaAula) < 120 && this.textoDialogo.length === 0) {
      this.avancarDialogo();
      return;
    }

    if (this.indiceMapa === 6 && Math.abs(this.audreyX - 50) < 150) {
      if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomAbrirPorta();
      this.entregarXpPendente();
      this.indiceMapa = 3;
      this.audreyX = 750;
      return;
    }

    if (this.indiceMapa === 3 && Math.abs(this.audreyX - 750) < 150) {
      if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomAbrirPorta();
      this.indiceMapa = 6;
      this.audreyX = 100;
      this.visitouBiblioteca = true;
      if (!this.ep1InteragiuBiblioteca) {
        this.ep1InteragiuBiblioteca = true;
        this.checarObjetivosEp1();
      }
      return;
    }

    if (this.indiceMapa === 6 && this.personagensNaBiblioteca && Math.abs(this.audreyX - 550) < 150 && this.textoDialogo.length === 0) {
      if (!this.ep1FalouNpc2) {
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
        this.estaEmDialogoNicolas = true;
        this.nomePersonagem = "Gabi";
        this.textoDialogo = "Ah, você veio! A Ivi e eu estamos aqui na biblioteca agora. A sala de aula é logo ali!";
        this.ep1FalouNpc2 = true;
        this.adicionarXpPendente(1);
        this.checarObjetivosEp1();
        return;
      } else {
        this.estaEmDialogoNicolas = true;
        this.nomePersonagem = "Gabi";
        this.textoDialogo = "Estamos aqui na biblioteca estudando. Qualquer coisa é só chamar!";
        return;
      }
    }

    if (this.indiceMapa === 6 && this.personagensNaBiblioteca && Math.abs(this.audreyX - 300) < 150 && this.textoDialogo.length === 0) {
      if (!this.ep1FalouNpc3) {
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
        this.estaEmDialogoNicolas = true;
        this.nomePersonagem = "Ivi";
        this.textoDialogo = "Oi Audrey! A Gabi e eu viemos pra biblioteca estudar. Já foi na sala de aula?";
        this.ep1FalouNpc3 = true;
        this.adicionarXpPendente(1);
        this.checarObjetivosEp1();
        return;
      } else {
        this.estaEmDialogoNicolas = true;
        this.nomePersonagem = "Ivi";
        this.textoDialogo = "Tem uns livros muito bons aqui! Vem conferir depois.";
        return;
      }
    }

    if (this.indiceMapa === 6 && Math.abs(this.audreyX - 480) < 150 && this.textoDialogo.length === 0) {
      if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
      this.estaEmDialogoNicolas = true;
      this.nomePersonagem = "Maysa";
      if (!this.ep1FalouMaysa) {
        this.textoDialogo = "Oi! Você é a Audrey, né? Eu sou a Maysa. Adoro passar o tempo aqui na biblioteca lendo e estudando.";
        this.ep1FalouMaysa = true;
        this.adicionarXpPendente(1);
        this.checarObjetivosEp1();
      } else {
        this.textoDialogo = "Se precisar de ajuda para encontrar algum livro ou dica de leitura, é só me chamar!";
      }
      return;
    }

    if (this.indiceMapa === 4 && Math.abs(this.audreyX - 600) < 150 && this.textoDialogo.length === 0) {
      if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
      this.estaEmDialogoNicolas = true;
      this.nomePersonagem = "Victor";
      if (!this.missaoFitnessAtiva) {
        this.textoDialogo = "E aí, Audrey! Bem-vinda ao ginásio! Eu sou o Victor, sou do time de futebol da escola!";
        this.missaoFitnessAtiva = true;
        this.adicionarXpPendente(1);
        this.checarObjetivosEp1();
      } else if (!this.missaoFitnessConcluida) {
        this.textoDialogo = "O treino hoje tá ótimo! Se quiser participar de algum esporte, vê as metas no seu diário!";
        this.adicionarXpPendente(1);
      } else {
        this.textoDialogo = "Valeu, Audrey! Sempre que quiser jogar uma pelada depois das aulas, pode aparecer aqui!";
      }
      return;
    }

    if (this.indiceMapa === 0 && Math.abs(this.audreyX - 250) < 100 && this.textoDialogo.length === 0) {
      if (!this.ep1InteragiuMural) {
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
        this.ep1InteragiuMural = true;
        this.estaEmDialogoNicolas = true;
        this.nomePersonagem = "Audrey";
        this.textoDialogo = "Um mural de avisos da escola. 'Festival Cultural em breve!'";
        this.checarObjetivosEp1();
        return;
      }
    }

    if (this.indiceMapa === 3 && Math.abs(this.audreyX - 350) < 150 && this.textoDialogo.length === 0) {
      if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomErro();
      this.nomePersonagem = "Audrey";
      this.textoDialogo = "A porta está trancada. Parece ser a sala da coordenação.";
      return;
    }

    if (this.indiceMapa === 0 && Math.abs(this.audreyX - this.posArmarioX) < 150 && this.textoDialogo.length === 0) {
      this.nomePersonagem = "Audrey";
      if (!this.temDiario) {
        if (!this.armarioEstaAberto) this.armarioEstaAberto = true;
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomColeta();
        this.temDiario = true;
        this.textoDialogo = "Achei meu diário! Agora posso riscar as missões nele e organizar meu dia.";
        this.textoNotificacao = "Você pegou o Diário!";
        this.mostrarNotificacaoMissao = true;
        this.contadorNotificacaoMissao = 0;
        return;
      } else if (this.temChave) {
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomArmario();
        this.textoDialogo = "Destrancado! Vamos ver o que tem aqui...";
        this.armarioEstaAberto = true;
        this.temChave = false;
      } else if (!this.armarioEstaAberto && !this.livroJoiFoiPego) {
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomErro();
        if (this.missaoLeituraAtiva) {
          this.textoDialogo = "Está trancado... o garoto com o livro me deu a chave, mas ainda não encontrei!";
        } else {
          this.textoDialogo = "Está trancado. Não tenho a chave...";
        }
      } else {
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomArmario();
        this.textoDialogo = "Vamos ver o que tem aqui...";
        this.armarioEstaAberto = true;
      }
      this.estaEmDialogoNicolas = false;
    }
  }

  estaProximoDaPuerta() {
    return this.indiceMapa === 2 &&
      this.audreyX < this.puertaSaidaX + this.puertaSaidaLargura + 50 &&
      this.audreyX + this.AUDREY_LARGURA > this.puertaSaidaX - 50;
  }

  checarObjetivosEp1() {
    if (!this.sala1Aberta && this.ep1FalouNpc2 && this.ep1FalouNpc3) {
      this.sala1Aberta = true;
      if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomSinalEscolar();
      this.estaEmDialogoNicolas = true;
      this.nomePersonagem = "Sistema";
      this.textoDialogo = "Agora que você conheceu a Gabi e a Ivi, a porta da Sala 1 está aberta! Pressione [E] para entrar.";
    }
  }

  ganharXP(quantidade, hobby = 'arte', label) {
    this.xp += quantidade;
    this.xpGanho = quantidade;
    this.mostrarNotificacaoXP = true;
    this.contadorNotificacaoXP = 0;
    const reasonText = label || `Ganhou ${quantidade} XP em Audrey`;

    // Atualiza o HQ (Motor Central do App) diretamente no localStorage do navegador
    if (typeof HQ !== 'undefined' && typeof HQ.addXP === 'function') {
      HQ.addXP(hobby, quantidade, reasonText);
    }

    this.enviarEventoParaApp("XP_EARNED", hobby, quantidade, reasonText);
  }

  adicionarXpPendente(quantidade) {
    this.xpPendente += quantidade;
    this.temXpPendente = true;
  }

  entregarXpPendente() {
    if (this.temXpPendente && this.xpPendente > 0) {
      this.ganharXP(this.xpPendente, "arte", "Exploração da Escola");
      this.xpPendente = 0;
      this.temXpPendente = false;
    }
  }

  continuarDialogoDesenho() {
    this.faseDialogoNicolas = 101;
    this.estaEmDialogoNicolas = true;
    this.nomePersonagem = "Nicollas";
    this.textoDialogo = "— Ficou irado! Essa perspectiva, cara, deu um peso enorme para o desenho. Você tem, de fato, muita técnica.";
    this.tamanhoTextoVisivel = 0;
    this.tamanhoTextoVisivelAcumulado = 0.0;
    if (window.GerenciadorAudio) window.GerenciadorAudio.tocarVozNicolas();

    this.ep1_solidaoUrbanaConcluida = true;
    this.mostrarAnimacaoRiscado = true;
    this.contadorAnimacaoRiscado = 0;
    this.textoMissaoRiscada = "Desenhar 'A Solidão Urbana'";
    if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomColeta();

    if (typeof HQ !== 'undefined' && typeof HQ.addXP === 'function') {
      HQ.addXP("arte", 25, "Desenhar 'A Solidão Urbana'");
    }
    this.enviarEventoParaApp("MISSION_COMPLETE", "arte", 25, "Desenhar 'A Solidão Urbana'");
  }

  enviarEventoParaApp(tipo, hobby, xp, label) {
    const eventId = Date.now() + '_' + Math.floor(Math.random() * 100000);
    fetch('http://127.0.0.1:3000/api/evento-jogo', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        type: tipo,
        data: {
          hobby: hobby || 'arte',
          xpReward: xp,
          amount: xp,
          label: label || 'Jogo Audrey',
          source: 'web-engine',
          eventId: eventId
        }
      })
    }).catch(() => {});
  }

  // ─── RENDERIZAÇÃO CENTRALIZADA SEM CORTES / ESMAGAMENTO ──────────
  render() {
    const ctx = this.ctx;

    // Fundo preto total na tela inteira
    ctx.fillStyle = '#000000';
    ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

    ctx.save();
    ctx.translate(this.offsetX, this.offsetY);
    ctx.scale(this.scale, this.scale);

    // Borda do canvas virtual 1000x750
    ctx.beginPath();
    ctx.rect(0, 0, this.LARGURA, this.ALTURA);
    ctx.clip();

    if (this.currentState === this.STATE.MENU_PRINCIPAL) {
      this.renderMenuPrincipal();
    } else if (this.currentState === this.STATE.MENU_SLOTS) {
      this.renderMenuSlots();
    } else if (this.currentState === this.STATE.CUTSCENE) {
      this.renderCutscene();
    } else if (this.currentState === this.STATE.SOBRE) {
      this.renderSobre();
    } else {
      // STATE.JOGO
      if (this.easterEggAtivo) {
        this.renderEasterEgg();
      } else if (!this.armarioEstaAberto) {
        this.renderCenario();
        this.renderNPCs();
        this.renderAudrey();
        this.renderInterface();

        if (this.inventarioAberto) {
          this.renderInventario();
        } else if (this.mostrarObjetivos) {
          this.renderObjetivos();
        }

        if (this.mostrando_chave) {
          this.renderEfeitoChave();
        }
      } else {
        ctx.fillStyle = '#000000';
        ctx.fillRect(0, 0, this.LARGURA, this.ALTURA);

        const imgArmario = this.images.armarioAberto;
        if (imgArmario) {
          const lw = 800;
          const lh = 600;
          const lx = (this.LARGURA - lw) / 2;
          const ly = (this.ALTURA - 150 - lh) / 2;
          ctx.drawImage(imgArmario, lx, ly, lw, lh);
        }
        this.renderInterface();

        if (this.inventarioAberto) {
          this.renderInventario();
        } else if (this.mostrarObjetivos) {
          this.renderObjetivos();
        }
      }

      if (this.diarioAberto) {
        this.renderDiario();
      }

      if (this.menuAberto) {
        this.renderMenuEmJogo();
      }

      if (this.mostrarNotificacaoMissao) {
        this.renderNotificacao(this.textoNotificacao, '#50A050', '#C8FFC8', true);
      }

      if (this.mostrarNotificacaoXP) {
        this.renderNotificacao(`+${this.xpGanho} XP ganho!`, '#B4640A', '#FFDC64', false);
      }

      if (this.mostrarAnimacaoRiscado) {
        this.renderAnimacaoRiscado();
      }

      this.renderControlesTouch();
    }

    ctx.restore();
  }

  // ─── TELA DO MENU PRINCIPAL (ESTILO DIÁRIO/QUARTO ACONCHEGANTE) ─────────
  renderMenuPrincipal() {
    const ctx = this.ctx;
    this.animAngulo += 0.025;

    // 1. Fundo Banner Pixel Art da Personagem no Quarto
    if (this.images.banner) {
      ctx.drawImage(this.images.banner, 0, 0, this.LARGURA, this.ALTURA);
    } else {
      ctx.fillStyle = '#1C143D';
      ctx.fillRect(0, 0, this.LARGURA, this.ALTURA);
    }

    // Gradient sutil no lado esquerdo para legibilidade impecável dos botões sem cobrir a Audrey
    let vignGrad = ctx.createLinearGradient(0, 0, 520, 0);
    vignGrad.addColorStop(0, 'rgba(12, 8, 24, 0.78)');
    vignGrad.addColorStop(0.7, 'rgba(12, 8, 24, 0.42)');
    vignGrad.addColorStop(1, 'rgba(12, 8, 24, 0.0)');
    ctx.fillStyle = vignGrad;
    ctx.fillRect(0, 0, 540, this.ALTURA);

    // 2. Partículas Mágicas Flutuantes no Ambiente (Poeira estelar / faíscas quentes)
    this.renderParticulasMenu();

    // 3. Rabiscos Decorativos Flutuantes
    this.desenharRabiscosMenu();

    // 4. FAIXA DE TÍTULO PRINCIPAL "HOBBY QUEST" (ESTILO CADERNO / WASHI TAPE NO TOPO ESQUERDO)
    const titleW = 380;
    const titleH = 100;
    const titleX = 55;
    const titleY = 40;

    ctx.save();
    // Sombra macia do título
    ctx.fillStyle = 'rgba(0, 0, 0, 0.45)';
    ctx.beginPath();
    ctx.roundRect(titleX + 4, titleY + 6, titleW, titleH, 20);
    ctx.fill();

    // Fundo do título em papel creme artesanal aconchegante
    ctx.fillStyle = '#FDF5C9';
    ctx.beginPath();
    ctx.roundRect(titleX, titleY, titleW, titleH, 20);
    ctx.fill();
    ctx.strokeStyle = '#E2B740';
    ctx.lineWidth = 2.5;
    ctx.stroke();

    // Fita Washi Tape Rosa no topo do papel
    ctx.fillStyle = 'rgba(241, 160, 196, 0.92)';
    ctx.fillRect(titleX + titleW / 2 - 60, titleY - 10, 120, 20);
    ctx.strokeStyle = 'rgba(215, 120, 160, 0.5)';
    ctx.lineWidth = 1;
    ctx.strokeRect(titleX + titleW / 2 - 60, titleY - 10, 120, 20);

    // Texto do Título principal
    ctx.font = `bold 44px ${this.FONT_FAMILY}`;
    ctx.fillStyle = '#E07A5F';
    ctx.textAlign = 'center';
    ctx.fillText("HOBBY QUEST", titleX + titleW / 2, titleY + 54);

    // Subtítulo delicado
    ctx.font = `bold 16px ${this.FONT_FAMILY}`;
    ctx.fillStyle = '#5B8A72';
    ctx.fillText("✨ A JORNADA DE AUDREY • FOQUE & EVOLUA ✨", titleX + titleW / 2, titleY + 84);
    ctx.restore();

    // 5. PAINEL DE BOTÕES DE MENU (LADO ESQUERDO - TOTALMENTE EQUILIBRADO)
    const cardW = 380;
    const cardH = 510;
    const cardX = 55;
    const cardY = 170;

    ctx.save();
    // Sombra do painel
    ctx.fillStyle = 'rgba(0, 0, 0, 0.50)';
    ctx.beginPath();
    ctx.roundRect(cardX + 4, cardY + 6, cardW, cardH, 24);
    ctx.fill();

    // Fundo glassmorphism escuro e aconchegante
    ctx.fillStyle = 'rgba(20, 14, 34, 0.84)';
    ctx.beginPath();
    ctx.roundRect(cardX, cardY, cardW, cardH, 24);
    ctx.fill();
    ctx.strokeStyle = 'rgba(241, 201, 122, 0.45)';
    ctx.lineWidth = 2;
    ctx.stroke();

    // Atualiza posições exatas dos botões para casar com o clique e o hover
    this.btnMenuNovo = { x: cardX + 30, y: cardY + 30, width: cardW - 60, height: 62 };
    this.btnMenuContinuar = { x: cardX + 30, y: cardY + 115, width: cardW - 60, height: 62 };
    this.btnMenuSobre = { x: cardX + 30, y: cardY + 200, width: cardW - 60, height: 62 };
    this.btnMenuSom = { x: cardX + 30, y: cardY + 285, width: cardW - 60, height: 54 };

    // Desenha os botões estilizados
    this.renderBotaoMenuPrincipal(this.btnMenuNovo, "🎮 NOVO JOGO", '#40B070', '#C7F0D8');
    this.renderBotaoMenuPrincipal(this.btnMenuContinuar, "▶ CONTINUAR", '#E2B740', '#FCE28B');
    this.renderBotaoMenuPrincipal(this.btnMenuSobre, "ℹ️ SOBRE & CONTROLES", '#9169A5', '#A8D8EA');

    // Botão de Áudio
    const somAtivo = window.GerenciadorAudio ? window.GerenciadorAudio.somAtivo : true;
    this.renderBotaoMenuPrincipal(
      this.btnMenuSom,
      somAtivo ? "🔊 EFEITOS SONOROS: ON" : "🔇 EFEITOS SONOROS: OFF",
      somAtivo ? '#558CAB' : '#CD4B4B',
      '#D0E8F5'
    );

    // Rodapé informativo no painel de menu
    ctx.font = `bold 14px ${this.FONT_FAMILY}`;
    ctx.fillStyle = 'rgba(241, 201, 122, 0.90)';
    ctx.textAlign = 'center';
    ctx.fillText("⭐ Progresso conectado ao App", cardX + cardW / 2, cardY + 440);
    ctx.font = `12px sans-serif`;
    ctx.fillStyle = 'rgba(200, 180, 230, 0.65)';
    ctx.fillText("v2.5 • TCC Hobby Quest", cardX + cardW / 2, cardY + 468);

    ctx.restore();
  }

  renderBotaoMenuPrincipal(btn, label, corBorda, corDestaque) {
    const ctx = this.ctx;
    const { x, y, width: w, height: h } = btn;
    const isHovered = this.mousePos && this.pointInRect(this.mousePos, btn);

    ctx.save();

    // Animação de deslocamento no hover
    const offsetX = isHovered ? 5 : 0;
    const drawX = x + offsetX;

    // Sombra do botão
    ctx.fillStyle = 'rgba(0, 0, 0, 0.40)';
    ctx.beginPath();
    ctx.roundRect(drawX + 3, y + 5, w, h, 16);
    ctx.fill();

    // Fundo do botão (brilha no hover)
    ctx.fillStyle = isHovered ? 'rgba(54, 40, 80, 0.95)' : 'rgba(32, 24, 48, 0.90)';
    ctx.beginPath();
    ctx.roundRect(drawX, y, w, h, 16);
    ctx.fill();

    // Borda temática com Glow Effect no hover
    ctx.strokeStyle = isHovered ? corDestaque : corBorda;
    ctx.lineWidth = isHovered ? 3.2 : 2.2;
    if (isHovered) {
      ctx.shadowColor = corBorda;
      ctx.shadowBlur = 12;
    }
    ctx.stroke();
    ctx.shadowBlur = 0;

    // Brilho superior em gradiente
    let shine = ctx.createLinearGradient(drawX, y, drawX, y + h / 2);
    shine.addColorStop(0, isHovered ? 'rgba(255, 255, 255, 0.18)' : 'rgba(255, 255, 255, 0.08)');
    shine.addColorStop(1, 'rgba(255, 255, 255, 0.0)');
    ctx.fillStyle = shine;
    ctx.beginPath();
    ctx.roundRect(drawX + 2, y + 2, w - 4, h / 2 - 2, 14);
    ctx.fill();

    // Texto com a fonte CrayonHand
    ctx.font = `bold 20px ${this.FONT_FAMILY}`;
    ctx.textAlign = 'center';

    // Sombra do texto
    ctx.fillStyle = 'rgba(0, 0, 0, 0.85)';
    ctx.fillText(label, drawX + w / 2 + 1, y + h / 2 + 7);

    // Texto principal
    ctx.fillStyle = isHovered ? '#FFFFFF' : '#F2ECFA';
    ctx.fillText(label, drawX + w / 2, y + h / 2 + 6);

    // Seta / Indicador no hover
    if (isHovered) {
      ctx.fillStyle = corDestaque;
      ctx.font = `bold 18px sans-serif`;
      ctx.fillText('➤', drawX + w - 24, y + h / 2 + 6);
    }

    ctx.restore();
  }

  renderParticulasMenu() {
    const ctx = this.ctx;
    if (!this.particulasMenu) return;

    this.particulasMenu.forEach(p => {
      p.y -= p.vy;
      p.x += Math.sin(p.y * 0.02) * p.vx;

      if (p.y < -10) {
        p.y = this.ALTURA + 10;
        p.x = Math.random() * this.LARGURA;
      }

      ctx.save();
      ctx.globalAlpha = p.alpha;
      ctx.fillStyle = p.color;
      ctx.beginPath();
      ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
      ctx.fill();
      ctx.restore();
    });
  }

  desenharRabiscosMenu() {
    const ctx = this.ctx;
    const a = this.animAngulo;
    const w = this.LARGURA;
    const h = this.ALTURA;

    ctx.save();
    ctx.lineWidth = 2.5;

    // Flor (topo esquerdo)
    const fx = Math.floor(w * 0.09);
    const fy = Math.floor(h * 0.13 + Math.sin(a) * 6);
    ctx.strokeStyle = 'rgba(255, 175, 200, 0.7)';
    ctx.beginPath();
    ctx.arc(fx, fy, 14, 0, Math.PI * 2);
    ctx.stroke();

    // Estrela 4 pontos (topo direito)
    const ex = Math.floor(w * 0.88 + Math.sin(a * 0.7) * 5);
    const ey = Math.floor(h * 0.10 + Math.cos(a * 0.8) * 5);
    ctx.strokeStyle = 'rgba(255, 215, 100, 0.8)';
    ctx.beginPath();
    ctx.moveTo(ex, ey - 14); ctx.lineTo(ex, ey + 14);
    ctx.moveTo(ex - 14, ey); ctx.lineTo(ex + 14, ey);
    ctx.stroke();

    // Coração Pulsando (direita meio)
    const pulse = 1.0 + Math.sin(a * 1.2) * 0.15;
    const hx = Math.floor(w * 0.87 + Math.sin(a * 0.9) * 4);
    const hy = Math.floor(h * 0.50 + Math.cos(a) * 5);
    ctx.fillStyle = 'rgba(255, 155, 190, 0.7)';
    ctx.beginPath();
    ctx.arc(hx - 8 * pulse, hy - 6 * pulse, 8 * pulse, 0, Math.PI * 2);
    ctx.arc(hx + 8 * pulse, hy - 6 * pulse, 8 * pulse, 0, Math.PI * 2);
    ctx.fill();

    // Nuvem (esquerda meio)
    const nx = Math.floor(w * 0.05 + Math.sin(a * 0.5) * 3);
    const ny = Math.floor(h * 0.42 + Math.cos(a * 0.6) * 4);
    ctx.strokeStyle = 'rgba(170, 205, 255, 0.7)';
    ctx.beginPath();
    ctx.arc(nx, ny, 12, 0, Math.PI * 2);
    ctx.arc(nx + 14, ny - 4, 16, 0, Math.PI * 2);
    ctx.arc(nx + 30, ny, 12, 0, Math.PI * 2);
    ctx.stroke();

    ctx.restore();
  }

  // ─── TELA DE CUTSCENE (SLIDES 1 A 12) ───────────────────────────
  renderCutscene() {
    const ctx = this.ctx;
    ctx.fillStyle = '#140E28';
    ctx.fillRect(0, 0, this.LARGURA, this.ALTURA);

    const slideKey = `slide${this.slideCutsceneAtual + 1}`;
    const slideImg = this.images[slideKey];

    const frameW = 860;
    const frameH = 540;
    const frameX = (this.LARGURA - frameW) / 2;
    const frameY = 80;

    // Moldura Quadrinho / Paper Frame
    ctx.fillStyle = 'rgba(0, 0, 0, 0.5)';
    ctx.beginPath();
    ctx.roundRect(frameX + 6, frameY + 6, frameW, frameH, 20);
    ctx.fill();

    ctx.fillStyle = '#2A2244';
    ctx.beginPath();
    ctx.roundRect(frameX, frameY, frameW, frameH, 20);
    ctx.fill();

    ctx.strokeStyle = '#FFE064';
    ctx.lineWidth = 3;
    ctx.stroke();

    if (slideImg) {
      ctx.save();
      ctx.beginPath();
      ctx.roundRect(frameX + 10, frameY + 10, frameW - 20, frameH - 20, 14);
      ctx.clip();
      ctx.drawImage(slideImg, frameX + 10, frameY + 10, frameW - 20, frameH - 20);
      ctx.restore();
    } else {
      ctx.font = `bold 28px ${this.FONT_FAMILY}`;
      ctx.fillStyle = '#FFDC64';
      ctx.textAlign = 'center';
      ctx.fillText(`CENA ${this.slideCutsceneAtual + 1} DE ${this.totalSlides}`, this.LARGURA / 2, frameY + frameH / 2);
    }

    // Botão PULAR
    this.renderBotaoMenu(this.btnCutscenePular, "PULAR ➔", '#827D91');

    // Progresso por Bolinhas (Dots)
    const dotCenterY = 640;
    const startX = (this.LARGURA - (this.totalSlides * 18)) / 2;
    for (let i = 0; i < this.totalSlides; i++) {
      ctx.fillStyle = (i === this.slideCutsceneAtual) ? '#FFDC64' : 'rgba(255, 255, 255, 0.3)';
      ctx.beginPath();
      ctx.arc(startX + i * 18, dotCenterY, (i === this.slideCutsceneAtual) ? 7 : 5, 0, Math.PI * 2);
      ctx.fill();
    }

    // Botão AVANÇAR / COMEÇAR AVENTURA
    const labelBtn = (this.slideCutsceneAtual === this.totalSlides - 1) ? "✨ COMEÇAR AVENTURA! ➔" : "PRÓXIMO SLIDE ➔";
    const corBtn = (this.slideCutsceneAtual === this.totalSlides - 1) ? '#40B070' : '#FFDC50';
    this.renderBotaoMenu(this.btnCutsceneAvancar, labelBtn, corBtn);
  }

  renderMenuSlots() {
    const ctx = this.ctx;

    if (this.images.banner) {
      ctx.drawImage(this.images.banner, 0, 0, this.LARGURA, this.ALTURA);
    } else {
      ctx.fillStyle = '#1C143D';
      ctx.fillRect(0, 0, this.LARGURA, this.ALTURA);
    }

    ctx.fillStyle = 'rgba(10, 8, 20, 0.70)';
    ctx.fillRect(0, 0, this.LARGURA, this.ALTURA);

    ctx.font = `bold 36px ${this.FONT_FAMILY}`;
    ctx.fillStyle = '#FFD764';
    ctx.textAlign = 'center';
    ctx.fillText(this.acaoSlot === 'NOVO' ? "SELECIONE UM SLOT PARA NOVO JOGO" : "SELECIONE UM SLOT PARA CONTINUAR", this.LARGURA / 2, 140);

    for (let s = 1; s <= 3; s++) {
      const btn = s === 1 ? this.btnSlot1 : (s === 2 ? this.btnSlot2 : this.btnSlot3);
      const info = this.getSlotInfo(s);
      let label = `SLOT ${s} - [NOVO JOGO / VAZIO]`;
      let cor = '#558CAB';
      if (info) {
        label = `SLOT ${s} - SALVO (${info.dataHora || 'Jogo Salvo'}) - XP: ${info.xp || 0}`;
        cor = '#40B070';
      }
      this.renderBotaoMenu(btn, label, cor);
    }
    this.renderBotaoMenu(this.btnSlotVoltar, "⬅ VOLTAR AO MENU", '#CD4B4B');
  }

  renderSobre() {
    const ctx = this.ctx;
    if (this.images.banner) {
      ctx.drawImage(this.images.banner, 0, 0, this.LARGURA, this.ALTURA);
    } else {
      ctx.fillStyle = '#1C143D';
      ctx.fillRect(0, 0, this.LARGURA, this.ALTURA);
    }

    ctx.fillStyle = 'rgba(12, 8, 24, 0.88)';
    ctx.fillRect(0, 0, this.LARGURA, this.ALTURA);

    const cardW = 740;
    const cardH = 500;
    const cardX = (this.LARGURA - cardW) / 2;
    const cardY = 120;

    ctx.save();
    ctx.fillStyle = 'rgba(24, 16, 42, 0.92)';
    ctx.beginPath();
    ctx.roundRect(cardX, cardY, cardW, cardH, 24);
    ctx.fill();
    ctx.strokeStyle = '#E2B740';
    ctx.lineWidth = 2.5;
    ctx.stroke();

    ctx.font = `bold 32px ${this.FONT_FAMILY}`;
    ctx.fillStyle = '#FFD764';
    ctx.textAlign = 'center';
    ctx.fillText("✨ SOBRE O JOGO AUDREY & HOBBY QUEST ✨", this.LARGURA / 2, cardY + 65);

    ctx.strokeStyle = 'rgba(241, 201, 122, 0.4)';
    ctx.lineWidth = 1.5;
    ctx.beginPath();
    ctx.moveTo(cardX + 40, cardY + 90);
    ctx.lineTo(cardX + cardW - 40, cardY + 90);
    ctx.stroke();

    ctx.font = `20px ${this.FONT_FAMILY}`;
    ctx.fillStyle = '#F5F0FA';
    ctx.fillText("Desenvolvido como TCC para explorar hobbies e aprendizado na vida real.", this.LARGURA / 2, cardY + 145);
    ctx.fillText("Ajude a Audrey a explorar a escola, fazer novos amigos e concluir missões!", this.LARGURA / 2, cardY + 195);
    ctx.fillText("Controles: Teclas A/D (Mover), E (Interagir), J (Diário), B (Inventário).", this.LARGURA / 2, cardY + 245);
    ctx.fillText("⭐ Totalmente conectado em tempo real com o seu XP no App!", this.LARGURA / 2, cardY + 295);

    this.renderBotaoMenu({ x: (this.LARGURA - 280) / 2, y: cardY + 380, width: 280, height: 60 }, "⬅ VOLTAR AO MENU", '#40B070');
    ctx.restore();
  }

  renderMenuEmJogo() {
    const ctx = this.ctx;
    ctx.fillStyle = 'rgba(10, 8, 20, 0.88)';
    ctx.fillRect(0, 0, this.LARGURA, this.ALTURA);

    ctx.font = `bold 38px ${this.FONT_FAMILY}`;
    ctx.fillStyle = '#FFD764';
    ctx.textAlign = 'center';
    ctx.fillText("PAUSA - HOBBY QUEST", this.LARGURA / 2, 130);

    ctx.strokeStyle = '#645082';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(250, 155);
    ctx.lineTo(750, 155);
    ctx.stroke();

    this.renderBotaoMenu(this.btnPauseResume, "▶ CONTINUAR JOGO", '#40B070');
    this.renderBotaoMenu(this.btnPauseSalvar, "💾 SALVAR PROGRESSO", '#E2B740');
    this.renderBotaoMenu(this.btnPauseDiario, "📖 DIÁRIO DE HOBBIES", '#9169A5');
    this.renderBotaoMenu(this.btnPauseMissoes, "🎯 MISSÕES & OBJETIVOS", '#558CAB');
    this.renderBotaoMenu(this.btnPauseItens, "🎒 INVENTÁRIO DE ITENS", '#7D9B6E');
    this.renderBotaoMenu(this.btnPauseExit, "🏠 MENU PRINCIPAL", '#CD4B4B');
  }

  renderBotaoMenu(btn, label, cor) {
    const ctx = this.ctx;
    ctx.fillStyle = 'rgba(30, 24, 45, 0.9)';
    ctx.beginPath();
    ctx.roundRect(btn.x, btn.y, btn.width, btn.height, 14);
    ctx.fill();

    ctx.strokeStyle = cor;
    ctx.lineWidth = 2.5;
    ctx.stroke();

    ctx.font = `bold 18px ${this.FONT_FAMILY}`;
    ctx.fillStyle = '#F8F5FC';
    ctx.textAlign = 'center';
    ctx.fillText(label, btn.x + btn.width / 2, btn.y + btn.height / 2 + 6);
  }

  init3DRaycaster() {
    this.p3dX = 2.5;
    this.p3dY = 2.5;
    this.p3dAngle = 0;
    this.p3dCoins = 0;
    this.p3dCoinsList = [
      { x: 3.5, y: 2.5, collected: false },
      { x: 5.5, y: 2.5, collected: false },
      { x: 6.5, y: 4.5, collected: false },
      { x: 4.5, y: 6.5, collected: false },
      { x: 2.5, y: 6.5, collected: false },
      { x: 7.5, y: 7.5, collected: false },
      { x: 8.5, y: 2.5, collected: false }
    ];
    this.p3dMap = [
      [1,1,1,1,1,1,1,1,1,1],
      [1,0,0,0,1,0,0,0,0,1],
      [1,0,1,0,1,0,1,1,0,1],
      [1,0,1,0,0,0,0,1,0,1],
      [1,0,1,1,2,1,0,1,0,1],
      [1,0,0,0,0,1,0,0,0,1],
      [1,0,1,1,0,1,1,1,0,1],
      [1,0,0,1,0,0,0,1,0,1],
      [1,0,0,0,0,1,0,0,0,1],
      [1,1,1,1,1,1,1,1,1,1]
    ];
  }

  update3DRaycaster() {
    if (!this.easterEggAtivo) return;
    this.easterEggTimer++;

    let moveSpeed = 0.06;
    let rotSpeed = 0.045;

    if (this.keys[87] || this.keys[38] || this.touchEsquerdaPressionado) {
      let nextX = this.p3dX + Math.cos(this.p3dAngle) * moveSpeed;
      let nextY = this.p3dY + Math.sin(this.p3dAngle) * moveSpeed;
      if (this.canWalk3D(nextX, this.p3dY)) this.p3dX = nextX;
      if (this.canWalk3D(this.p3dX, nextY)) this.p3dY = nextY;
    }
    if (this.keys[83] || this.keys[40] || this.touchDireitaPressionado) {
      let nextX = this.p3dX - Math.cos(this.p3dAngle) * moveSpeed;
      let nextY = this.p3dY - Math.sin(this.p3dAngle) * moveSpeed;
      if (this.canWalk3D(nextX, this.p3dY)) this.p3dX = nextX;
      if (this.canWalk3D(this.p3dX, nextY)) this.p3dY = nextY;
    }
    if (this.keys[65] || this.keys[37]) {
      this.p3dAngle -= rotSpeed;
    }
    if (this.keys[68] || this.keys[39]) {
      this.p3dAngle += rotSpeed;
    }

    if (this.p3dCoinsList) {
      this.p3dCoinsList.forEach(c => {
        if (!c.collected) {
          let dist = Math.hypot(this.p3dX - c.x, this.p3dY - c.y);
          if (dist < 0.6) {
            c.collected = true;
            this.p3dCoins++;
            if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomColeta();
          }
        }
      });
    }
  }

  canWalk3D(x, y) {
    let mx = Math.floor(x);
    let my = Math.floor(y);
    if (!this.p3dMap || my < 0 || my >= this.p3dMap.length || mx < 0 || mx >= this.p3dMap[0].length) return false;
    return this.p3dMap[my][mx] !== 1;
  }

  renderEasterEgg() {
    this.update3DRaycaster();

    const ctx = this.ctx;
    const W = this.LARGURA;
    const H = this.ALTURA;
    const halfH = H / 2;

    // 1. Teto com gradiente escuro e iluminação de masmorra
    let skyGrad = ctx.createLinearGradient(0, 0, 0, halfH);
    skyGrad.addColorStop(0, '#090514');
    skyGrad.addColorStop(1, '#1E1235');
    ctx.fillStyle = skyGrad;
    ctx.fillRect(0, 0, W, halfH);

    // 2. Chão 3D com linhas de perspectiva animadas
    let floorGrad = ctx.createLinearGradient(0, halfH, 0, H);
    floorGrad.addColorStop(0, '#120B24');
    floorGrad.addColorStop(1, '#341D59');
    ctx.fillStyle = floorGrad;
    ctx.fillRect(0, halfH, W, halfH);

    // Linhas de grade 3D no chão
    ctx.strokeStyle = 'rgba(170, 130, 220, 0.22)';
    ctx.lineWidth = 1.5;
    for (let i = -12; i <= 12; i++) {
      ctx.beginPath();
      ctx.moveTo(W / 2 + i * 35, halfH);
      ctx.lineTo(W / 2 + i * 190, H);
      ctx.stroke();
    }
    for (let y = halfH; y < H; y += Math.max(3, (y - halfH) * 0.22)) {
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(W, y);
      ctx.stroke();
    }

    // 3. RAYCASTING DAS PAREDES 3D
    const numRays = 130;
    const rayWidth = W / numRays;
    const fov = Math.PI / 3;

    for (let i = 0; i < numRays; i++) {
      let rayAngle = (this.p3dAngle - fov / 2) + (i / numRays) * fov;
      let distance = 0;
      let hitWall = 0;
      let cosA = Math.cos(rayAngle);
      let sinA = Math.sin(rayAngle);

      while (!hitWall && distance < 16) {
        distance += 0.05;
        let testX = Math.floor(this.p3dX + cosA * distance);
        let testY = Math.floor(this.p3dY + sinA * distance);

        if (testY < 0 || testY >= this.p3dMap.length || testX < 0 || testX >= this.p3dMap[0].length) {
          hitWall = 1;
          distance = 16;
        } else if (this.p3dMap[testY][testX] > 0) {
          hitWall = this.p3dMap[testY][testX];
        }
      }

      let correctedDist = distance * Math.cos(rayAngle - this.p3dAngle);
      let wallHeight = Math.min(H, (H / Math.max(0.1, correctedDist)));
      let wallTop = halfH - wallHeight / 2;

      let shade = Math.max(0.15, 1 - correctedDist / 11);
      let rCol = Math.floor(hitWall === 2 ? 255 * shade : 150 * shade);
      let gCol = Math.floor(hitWall === 2 ? 215 * shade : 85 * shade);
      let bCol = Math.floor(hitWall === 2 ? 100 * shade : 225 * shade);

      ctx.fillStyle = `rgb(${rCol}, ${gCol}, ${bCol})`;
      ctx.fillRect(i * rayWidth, wallTop, rayWidth + 1, wallHeight);

      // Linha neon no topo/base da parede
      ctx.fillStyle = hitWall === 2 ? 'rgba(255, 220, 100, 0.85)' : 'rgba(210, 160, 255, 0.65)';
      ctx.fillRect(i * rayWidth, wallTop, rayWidth + 1, 3);
      ctx.fillRect(i * rayWidth, wallTop + wallHeight - 3, rayWidth + 1, 3);
    }

    // 4. MOEDAS 3D FLUTUANTES
    if (this.p3dCoinsList) {
      this.p3dCoinsList.forEach(c => {
        if (!c.collected) {
          let dx = c.x - this.p3dX;
          let dy = c.y - this.p3dY;
          let spriteAngle = Math.atan2(dy, dx) - this.p3dAngle;

          while (spriteAngle < -Math.PI) spriteAngle += Math.PI * 2;
          while (spriteAngle > Math.PI) spriteAngle -= Math.PI * 2;

          if (Math.abs(spriteAngle) < fov) {
            let dist = Math.hypot(dx, dy);
            let screenX = (W / 2) + Math.tan(spriteAngle) * (W / 2);
            let size = Math.min(110, (H / Math.max(0.5, dist)) * 0.4);
            let screenY = halfH + (Math.sin(this.easterEggTimer * 0.1) * 8);

            ctx.font = `${Math.floor(size)}px cursive, sans-serif`;
            ctx.textAlign = 'center';
            ctx.fillText("🪙", screenX, screenY + size / 3);
          }
        }
      });
    }

    // 5. BANNER HUD SUPERIOR
    ctx.fillStyle = 'rgba(20, 14, 34, 0.88)';
    ctx.beginPath();
    ctx.roundRect(W / 2 - 280, 18, 560, 52, 16);
    ctx.fill();
    ctx.strokeStyle = '#FFD764';
    ctx.lineWidth = 2.5;
    ctx.stroke();

    ctx.font = `bold 22px ${this.FONT_FAMILY}`;
    ctx.fillStyle = '#FFD764';
    ctx.textAlign = 'center';
    ctx.fillText("★ LABIRINTO SECRETO 3D DOS ARMÁRIOS ★", W / 2, 52);

    // Badge +50 XP Conectado
    ctx.fillStyle = 'rgba(255, 220, 100, 0.95)';
    ctx.beginPath();
    ctx.roundRect(24, 18, 240, 48, 14);
    ctx.fill();
    ctx.font = `bold 16px ${this.FONT_FAMILY}`;
    ctx.fillStyle = '#1C1424';
    ctx.textAlign = 'left';
    ctx.fillText("🏆 +50 XP NO APP!", 40, 46);

    // Moedas Coletadas
    ctx.font = `bold 18px ${this.FONT_FAMILY}`;
    ctx.fillStyle = '#FFFFFF';
    ctx.fillText(`🪙 Moedas: ${this.p3dCoins}/${this.p3dCoinsList ? this.p3dCoinsList.length : 7}`, 40, 92);

    // 6. MINIMAPA 3D NO CANTO SUPERIOR DIREITO
    const mapSize = 110;
    const mapX = W - mapSize - 20;
    const mapY = 18;
    ctx.fillStyle = 'rgba(10, 6, 20, 0.88)';
    ctx.beginPath();
    ctx.roundRect(mapX, mapY, mapSize, mapSize, 12);
    ctx.fill();
    ctx.strokeStyle = 'rgba(255, 215, 100, 0.8)';
    ctx.lineWidth = 2;
    ctx.stroke();

    const scaleM = mapSize / 10;
    for (let r = 0; r < 10; r++) {
      for (let c = 0; c < 10; c++) {
        if (this.p3dMap[r][c] === 1) {
          ctx.fillStyle = '#9B78CD';
          ctx.fillRect(mapX + c * scaleM, mapY + r * scaleM, scaleM - 1, scaleM - 1);
        }
      }
    }
    // Jogador no minimapa
    ctx.fillStyle = '#FFD764';
    ctx.beginPath();
    ctx.arc(mapX + this.p3dX * scaleM, mapY + this.p3dY * scaleM, 4, 0, Math.PI * 2);
    ctx.fill();

    ctx.strokeStyle = '#FF3264';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(mapX + this.p3dX * scaleM, mapY + this.p3dY * scaleM);
    ctx.lineTo(mapX + this.p3dX * scaleM + Math.cos(this.p3dAngle) * 10, mapY + this.p3dY * scaleM + Math.sin(this.p3dAngle) * 10);
    ctx.stroke();

    // 7. BOTÃO SAIR / RETORNAR
    const btnExit = { x: W / 2 - 150, y: H - 72, width: 300, height: 48 };
    this.renderBotaoSuaveComIcone(btnExit, "SAIR DO LABIRINTO [ESC]", false, '#CD4B4B', 'fechar');
  }

  renderCenario() {
    const ctx = this.ctx;
    let bg = null;

    if (this.indiceMapa === 0) bg = this.images.corredor1;
    else if (this.indiceMapa === 1) bg = this.images.corredor2;
    else if (this.indiceMapa === 2) bg = this.images.salaAula;
    else if (this.indiceMapa === 3) bg = this.images.corredor3;
    else if (this.indiceMapa === 4) bg = this.images.ginasio;
    else if (this.indiceMapa === 5) bg = this.images.corredor1;
    else if (this.indiceMapa === 6) bg = this.images.biblioteca;

    if (bg) {
      ctx.drawImage(bg, 0, 0, this.LARGURA, this.ALTURA);
    }

    // --- OVERLAY DESTACADO DO ARMÁRIO SECRETO (100% FIEL AO JAVA) ---
    if (this.indiceMapa === 1) {
      // Corredor 2: Armário Secreto no Canto Esquerdo (boxX=50, boxY=150, boxW=200, boxH=220)
      const boxX = 50, boxY = 150, boxW = 200, boxH = 220;
      const alphaRed = (this.cliquesArmarioEasterEgg > 0) ? 'rgba(255, 0, 0, 0.65)' : 'rgba(255, 0, 0, 0.45)';

      ctx.save();
      ctx.fillStyle = alphaRed;
      ctx.fillRect(boxX, boxY, boxW, boxH);

      ctx.strokeStyle = '#FFFFFF';
      ctx.lineWidth = 3;
      ctx.strokeRect(boxX + 1, boxY + 1, boxW - 2, boxH - 2);

      ctx.strokeStyle = '#FF0000';
      ctx.lineWidth = 2;
      ctx.strokeRect(boxX + 3, boxY + 3, boxW - 6, boxH - 6);

      ctx.font = `bold 15px ${this.FONT_FAMILY}`;
      ctx.textAlign = 'center';
      const txtCorredor2 = (this.cliquesArmarioEasterEgg > 0)
        ? `★ SECRETO: ${this.cliquesArmarioEasterEgg}/3 CLIQUES!`
        : "★ ARMÁRIO SECRETO (CLIQUE 3X) ★";

      ctx.fillStyle = '#000000';
      ctx.fillText(txtCorredor2, boxX + boxW / 2 + 1, boxY + boxH / 2 + 1);

      ctx.fillStyle = '#FFDC32';
      ctx.fillText(txtCorredor2, boxX + boxW / 2, boxY + boxH / 2);
      ctx.restore();
    } else if (this.indiceMapa === 0 && Math.abs(this.audreyX - this.posArmarioX) < 250) {
      // Corredor 1: Área do armário
      const boxX = 500, boxY = 50, boxW = 300, boxH = 400;
      const alphaRed = (this.cliquesArmarioEasterEgg > 0) ? 'rgba(255, 0, 0, 0.65)' : 'rgba(255, 0, 0, 0.35)';

      ctx.save();
      ctx.fillStyle = alphaRed;
      ctx.fillRect(boxX, boxY, boxW, boxH);

      ctx.strokeStyle = '#FFFFFF';
      ctx.lineWidth = 3;
      ctx.strokeRect(boxX + 1, boxY + 1, boxW - 2, boxH - 2);

      ctx.strokeStyle = '#FF0000';
      ctx.lineWidth = 2;
      ctx.strokeRect(boxX + 3, boxY + 3, boxW - 6, boxH - 6);

      ctx.font = `bold 15px ${this.FONT_FAMILY}`;
      ctx.textAlign = 'center';
      const txtC1 = (this.cliquesArmarioEasterEgg > 0)
        ? `★ SECRETO: ${this.cliquesArmarioEasterEgg}/3 CLIQUES!`
        : "★ ENTRADA SECRETA: CLIQUE 3X ★";

      ctx.fillStyle = '#000000';
      ctx.fillText(txtC1, boxX + boxW / 2 + 1, boxY + boxH / 2 + 1);

      ctx.fillStyle = '#FFDC32';
      ctx.fillText(txtC1, boxX + boxW / 2, boxY + boxH / 2);
      ctx.restore();
    }
  }

  renderSombraChao(cx, groundY, largura) {
    const ctx = this.ctx;
    ctx.fillStyle = 'rgba(0, 0, 0, 0.27)';
    ctx.beginPath();
    ctx.ellipse(cx, groundY - 12, largura / 2, 12, 0, 0, Math.PI * 2);
    ctx.fill();
  }

  renderLabel(text, x, y, width) {
    const ctx = this.ctx;
    ctx.fillStyle = 'rgba(253, 246, 227, 0.86)';
    ctx.beginPath();
    ctx.roundRect(x, y, width, 30, 12);
    ctx.fill();
    ctx.strokeStyle = '#D2B48C';
    ctx.lineWidth = 2;
    ctx.stroke();

    ctx.font = `bold 14px ${this.FONT_FAMILY}`;
    ctx.fillStyle = '#785064';
    ctx.textAlign = 'center';
    ctx.fillText(text, x + width / 2, y + 20);
  }

  renderNPCs() {
    const ctx = this.ctx;
    const npcBaseY = this.audreyY + 150;

    if (this.indiceMapa === 1 && !this.personagensNaBiblioteca) {
      const gabiImg = this.images.gabiCorredor || this.images.gabi;
      if (gabiImg) {
        const gx = 480;
        this.renderSombraChao(gx + this.NPC_LARGURA / 2, npcBaseY - 55, this.NPC_LARGURA * 0.6);
        ctx.save();
        ctx.translate(gx + this.NPC_LARGURA, npcBaseY - this.NPC_ALTURA);
        ctx.scale(-1, 1);
        ctx.drawImage(gabiImg, 0, 0, this.NPC_LARGURA, this.NPC_ALTURA);
        ctx.restore();
        this.renderLabel('Gabi', gx + (this.NPC_LARGURA - 100) / 2, npcBaseY - this.NPC_ALTURA - 35, 100);
      }

      if (this.images.ivi) {
        const ix = 630;
        this.renderSombraChao(ix + this.NPC_LARGURA / 2, npcBaseY - 55, this.NPC_LARGURA * 0.6);
        ctx.save();
        ctx.translate(ix + this.NPC_LARGURA, npcBaseY - this.NPC_ALTURA);
        ctx.scale(-1, 1);
        ctx.drawImage(this.images.ivi, 0, 0, this.NPC_LARGURA, this.NPC_ALTURA);
        ctx.restore();
        this.renderLabel('Ivi', ix + (this.NPC_LARGURA - 80) / 2, npcBaseY - this.NPC_ALTURA - 35, 80);
      }
    }

    if (this.indiceMapa === 2) {
      if (this.images.raquel) {
        this.renderSombraChao(200 + this.SALA_NPC_LARGURA / 2, npcBaseY - 55, this.SALA_NPC_LARGURA * 0.6);
        ctx.save();
        ctx.translate(200 + this.SALA_NPC_LARGURA, npcBaseY - this.SALA_RAQUEL_ALTURA);
        ctx.scale(-1, 1);
        ctx.drawImage(this.images.raquel, 0, 0, this.SALA_NPC_LARGURA, this.SALA_RAQUEL_ALTURA);
        ctx.restore();
        this.renderLabel('Raquel', 200 + (this.SALA_NPC_LARGURA - 110) / 2, npcBaseY - this.SALA_RAQUEL_ALTURA - 35, 110);
      }

      if (this.images.nicolas) {
        this.renderSombraChao(340 + this.SALA_NICOLAS_LARGURA / 2, npcBaseY - 55, this.SALA_NICOLAS_LARGURA * 0.6);
        ctx.save();
        ctx.translate(340 + this.SALA_NICOLAS_LARGURA, npcBaseY - this.SALA_NICOLAS_ALTURA);
        ctx.scale(-1, 1);
        ctx.drawImage(this.images.nicolas, 0, 0, this.SALA_NICOLAS_LARGURA, this.SALA_NICOLAS_ALTURA);
        ctx.restore();
        this.renderLabel('Nicollas', 340 + (this.SALA_NICOLAS_LARGURA - 120) / 2, npcBaseY - this.SALA_NICOLAS_ALTURA - 35, 120);
      }

      if (this.images.camila) {
        this.renderSombraChao(480 + this.SALA_NPC_LARGURA / 2, npcBaseY - 55, this.SALA_NPC_LARGURA * 0.6);
        ctx.save();
        ctx.translate(480 + this.SALA_NPC_LARGURA, npcBaseY - this.SALA_CAMILA_ALTURA);
        ctx.scale(-1, 1);
        ctx.drawImage(this.images.camila, 0, 0, this.SALA_NPC_LARGURA, this.SALA_CAMILA_ALTURA);
        ctx.restore();
        this.renderLabel('Camila', 480 + (this.SALA_NPC_LARGURA - 110) / 2, npcBaseY - this.SALA_CAMILA_ALTURA - 35, 110);
      }
    }

    if (this.indiceMapa === 6) {
      if (this.personagensNaBiblioteca) {
        if (this.images.gabi) {
          const gx = 500;
          this.renderSombraChao(gx + this.SALA_NPC_LARGURA / 2, npcBaseY - 55, this.SALA_NPC_LARGURA * 0.6);
          ctx.save();
          ctx.translate(gx + this.SALA_NPC_LARGURA, npcBaseY - this.SALA_RAQUEL_ALTURA);
          ctx.scale(-1, 1);
          ctx.drawImage(this.images.gabi, 0, 0, this.SALA_NPC_LARGURA, this.SALA_RAQUEL_ALTURA);
          ctx.restore();
          this.renderLabel('Gabi', gx + (this.SALA_NPC_LARGURA - 100) / 2, npcBaseY - this.SALA_RAQUEL_ALTURA - 35, 100);
        }
        if (this.images.ivi) {
          const ix = 250;
          this.renderSombraChao(ix + this.SALA_NPC_LARGURA / 2, npcBaseY - 55, this.SALA_NPC_LARGURA * 0.6);
          ctx.save();
          ctx.translate(ix + this.SALA_NPC_LARGURA, npcBaseY - this.SALA_RAQUEL_ALTURA);
          ctx.scale(-1, 1);
          ctx.drawImage(this.images.ivi, 0, 0, this.SALA_NPC_LARGURA, this.SALA_RAQUEL_ALTURA);
          ctx.restore();
          this.renderLabel('Ivi', ix + (this.SALA_NPC_LARGURA - 80) / 2, npcBaseY - this.SALA_RAQUEL_ALTURA - 35, 80);
        }
      }
      if (this.images.maysa) {
        const mx = 480;
        const mw = Math.floor(this.SALA_NPC_LARGURA * 0.85);
        const mh = Math.floor(this.SALA_RAQUEL_ALTURA * 0.9);
        this.renderSombraChao(mx + mw / 2, npcBaseY - 30, mw * 0.6);
        ctx.drawImage(this.images.maysa, mx, npcBaseY - mh, mw, mh);
        this.renderLabel('Maysa', mx + (mw - 100) / 2, npcBaseY - mh - 35, 100);
      }
    }

    if (this.indiceMapa === 4 && this.images.victor) {
      const gx = 600;
      this.renderSombraChao(gx + this.NPC_LARGURA / 2, npcBaseY - 55, this.NPC_LARGURA * 0.6);
      ctx.save();
      ctx.translate(gx + this.NPC_LARGURA, npcBaseY - this.NPC_ALTURA);
      ctx.scale(-1, 1);
      ctx.drawImage(this.images.victor, 0, 0, this.NPC_LARGURA, this.NPC_ALTURA);
      ctx.restore();
      this.renderLabel('Victor', gx + (this.NPC_LARGURA - 100) / 2, npcBaseY - this.NPC_ALTURA - 35, 100);
    }
  }

  renderAudrey() {
    const ctx = this.ctx;
    let drawY = this.audreyY - this.AUDREY_ALTURA + 150;
    let img = this.images.audreyParada;

    this.renderSombraChao(this.audreyX + this.AUDREY_LARGURA / 2, this.audreyY + 95, this.AUDREY_LARGURA * 0.55);

    if (this.estaMovendo) {
      img = (this.frameAtual === 0) ? this.images.audreyAndar1 : this.images.audreyAndar2;
    }

    if (img) {
      ctx.save();
      if (!this.olhandoDireita) {
        ctx.translate(this.audreyX + this.AUDREY_LARGURA, drawY);
        ctx.scale(-1, 1);
        ctx.drawImage(img, 0, 0, this.AUDREY_LARGURA, this.AUDREY_ALTURA);
      } else {
        ctx.drawImage(img, this.audreyX, drawY, this.AUDREY_LARGURA, this.AUDREY_ALTURA);
      }
      ctx.restore();
    }
  }

  renderInterface() {
    const ctx = this.ctx;

    if (!this.armarioEstaAberto && this.textoDialogo.length === 0) {
      ctx.font = `bold 18px ${this.FONT_FAMILY}`;
      ctx.textAlign = 'left';

      if (this.indiceMapa === 0 && Math.abs(this.audreyX - this.posArmarioX) < 150) {
        this.renderTextoComSombra("Pressione [E] para abrir", this.audreyX - 10, this.audreyY - 430, '#A078C8');
      }

      if (this.indiceMapa === 1 && !this.personagensNaBiblioteca) {
        if (Math.abs(this.audreyX - this.posPortaX) < 150 && Math.abs(this.audreyX - 480) >= 120 && Math.abs(this.audreyX - 630) >= 120) {
          if (this.explorouCorredor) {
            this.renderTextoComSombra("Pressione [E] para entrar", this.audreyX - 10, this.audreyY - 430, '#A078C8');
          } else {
            this.renderTextoComSombra("Vou explorar o final do corredor primeiro...", this.audreyX - 10, this.audreyY - 430, '#8C64B4');
          }
        }
        if (Math.abs(this.audreyX - 480) < 100 && Math.abs(this.audreyX - this.posPortaX) >= 150) {
          this.renderTextoComSombra("Pressione [E] para falar com Gabi", this.audreyX - 10, this.audreyY - 430, '#A078C8');
        }
        if (Math.abs(this.audreyX - 630) < 100 && Math.abs(this.audreyX - this.posPortaX) >= 150) {
          this.renderTextoComSombra("Pressione [E] para falar com Ivi", this.audreyX - 10, this.audreyY - 430, '#A078C8');
        }
      }

      if (this.indiceMapa === 2 && Math.abs(this.audreyX - this.posNicolasXSalaAula) < 120) {
        this.renderTextoComSombra("Pressione [F] para falar", this.audreyX - 10, this.audreyY - 430, '#A078C8');
      }

      if (this.indiceMapa === 2 && this.estaProximoDaPuerta()) {
        this.renderTextoComSombra("Pressione [E] para sair", this.audreyX - 10, this.audreyY - 430, '#A078C8');
      }

      if (this.indiceMapa === 3) {
        if (Math.abs(this.audreyX - 350) < 150) {
          this.renderTextoComSombra("Pressione [E] para abrir", this.audreyX - 10, this.audreyY - 430, '#A078C8');
        } else if (Math.abs(this.audreyX - 750) < 150) {
          this.renderTextoComSombra("Pressione [E] para entrar na biblioteca", this.audreyX - 10, this.audreyY - 430, '#A078C8');
        }
      }

      if (this.indiceMapa === 6 && Math.abs(this.audreyX - 50) < 150) {
        this.renderTextoComSombra("Pressione [E] para sair da biblioteca", this.audreyX - 10, this.audreyY - 430, '#A078C8');
      }

      if (this.indiceMapa === 6) {
        if (this.personagensNaBiblioteca) {
          if (Math.abs(this.audreyX - 550) < 150) {
            this.renderTextoComSombra("Pressione [E] para falar com Gabi", this.audreyX - 10, this.audreyY - 430, '#A078C8');
          } else if (Math.abs(this.audreyX - 300) < 150) {
            this.renderTextoComSombra("Pressione [E] para falar com Ivi", this.audreyX - 10, this.audreyY - 430, '#A078C8');
          }
        }
        if (Math.abs(this.audreyX - 480) < 150) {
          this.renderTextoComSombra("Pressione [E] para falar com Maysa", this.audreyX - 10, this.audreyY - 430, '#A078C8');
        }
      }

      if (this.indiceMapa === 4 && Math.abs(this.audreyX - 600) < 150 && this.textoDialogo.length === 0) {
        this.renderTextoComSombra("Pressione [E] para falar com Victor", this.audreyX - 10, this.audreyY - 430, '#A078C8');
      }
    }

    if (this.textoDialogo.length > 0 || this.armarioEstaAberto) {
      const caixaW = 860;
      const caixaH = 130;
      const caixaX = (this.LARGURA - caixaW) / 2;
      const caixaY = this.ALTURA - caixaH - 30;

      let portraitImg = null;
      if (this.nomePersonagem === "Audrey") portraitImg = this.images.portraitAudrey;
      else if (this.nomePersonagem === "Nicollas" || this.nomePersonagem === "Nicolas") portraitImg = this.images.portraitNicollas || this.images.portraitNicolas;
      else if (this.nomePersonagem === "Raquel") portraitImg = this.images.portraitRaquel;
      else if (this.nomePersonagem === "Camila") portraitImg = this.images.portraitCamila;
      else if (this.nomePersonagem === "Gabi") portraitImg = this.images.portraitGabi;
      else if (this.nomePersonagem === "Ivi") portraitImg = this.images.portraitIvi;
      else if (this.nomePersonagem === "Victor") portraitImg = this.images.portraitVictor;
      else if (this.nomePersonagem === "Maysa") portraitImg = this.images.portraitMaysa;

      let textXOffset = caixaX + 30;

      if (portraitImg) {
        const polaroidW = 160;
        const polaroidH = 190;
        const polaroidX = caixaX + 15;
        const polaroidY = caixaY - 130;

        ctx.fillStyle = 'rgba(0, 0, 0, 0.43)';
        ctx.beginPath();
        ctx.roundRect(polaroidX + 5, polaroidY + 5, polaroidW, polaroidH, 12);
        ctx.fill();

        ctx.fillStyle = '#AF9FAF';
        ctx.beginPath();
        ctx.roundRect(polaroidX, polaroidY, polaroidW, polaroidH, 12);
        ctx.fill();
        ctx.strokeStyle = '#281E2D';
        ctx.lineWidth = 2.5;
        ctx.stroke();

        const fotoX = polaroidX + 10;
        const fotoY = polaroidY + 10;
        const fotoW = polaroidW - 20;
        const fotoH = polaroidH - 35;

        ctx.fillStyle = '#1E1623';
        ctx.fillRect(fotoX, fotoY, fotoW, fotoH);

        ctx.save();
        ctx.beginPath();
        ctx.rect(fotoX, fotoY, fotoW, fotoH);
        ctx.clip();
        ctx.drawImage(portraitImg, fotoX, fotoY, fotoW, fotoH);
        ctx.restore();

        ctx.strokeStyle = '#3C3041';
        ctx.lineWidth = 1.5;
        ctx.strokeRect(fotoX, fotoY, fotoW, fotoH);

        textXOffset = polaroidX + polaroidW + 25;
      }

      ctx.fillStyle = 'rgba(0, 0, 0, 0.51)';
      ctx.beginPath();
      ctx.roundRect(caixaX + 4, caixaY + 4, caixaW, caixaH, 16);
      ctx.fill();

      ctx.fillStyle = 'rgba(24, 18, 28, 0.94)';
      ctx.beginPath();
      ctx.roundRect(caixaX, caixaY, caixaW, caixaH, 16);
      ctx.fill();

      ctx.strokeStyle = 'rgba(180, 165, 195, 0.86)';
      ctx.lineWidth = 2.5;
      ctx.stroke();

      if (this.nomePersonagem.length > 0 && !this.armarioEstaAberto) {
        const nomeUpper = this.nomePersonagem.toUpperCase();
        ctx.font = `bold 15px ${this.FONT_FAMILY}`;
        const tagW = Math.max(100, ctx.measureText(nomeUpper).width + 30);
        const tagH = 26;
        const tagX = textXOffset;
        const tagY = caixaY - 14;

        ctx.fillStyle = 'rgba(15, 12, 20, 0.98)';
        ctx.beginPath();
        ctx.roundRect(tagX, tagY, tagW, tagH, 8);
        ctx.fill();

        ctx.strokeStyle = '#D2C3E1';
        ctx.lineWidth = 2;
        ctx.stroke();

        ctx.fillStyle = '#F5F0FA';
        ctx.textAlign = 'center';
        ctx.fillText(nomeUpper, tagX + tagW / 2, tagY + 18);
      }

      ctx.font = `bold 18px ${this.FONT_FAMILY}`;
      ctx.textAlign = 'left';

      let msg = this.armarioEstaAberto ? "Pressione 'Q' para fechar o armário." : this.textoDialogo;
      if (!this.armarioEstaAberto && this.textoDialogo.length > 0) {
        msg = msg.substring(0, Math.min(msg.length, this.tamanhoTextoVisivel));
        if (this.tamanhoTextoVisivel < this.textoDialogo.length || Math.floor(Date.now() / 400) % 2 === 0) {
          msg += " █";
        }
      }

      if (this.faseDialogoNicolas === 100 && this.tamanhoTextoVisivel >= this.textoDialogo.length) {
        msg = "";
      }

      ctx.fillStyle = '#F0EBF5';
      const maxWidth = caixaX + caixaW - textXOffset - 40;
      this.renderTextoQuebrado(msg, textXOffset, caixaY + 45, maxWidth);

      if (!this.armarioEstaAberto && this.textoDialogo.length > 0) {
        if (this.faseDialogoNicolas === 100 && this.tamanhoTextoVisivel >= this.textoDialogo.length) {
          const opt1 = "🖼️  Já terminei! (Enviar imagem)";
          const opt2 = "⏳  Ainda vou fazer o desenho.";

          const optX = textXOffset;
          const btnW = caixaX + caixaW - optX - 25;
          const btnH = 28;

          this.optBtnRect1 = { x: optX, y: caixaY + 35, width: btnW, height: btnH };
          this.optBtnRect2 = { x: optX, y: caixaY + 70, width: btnW, height: btnH };

          const active1 = this.hoverOption === 0 || this.selectedDialogueOption === 0;
          ctx.fillStyle = active1 ? 'rgba(40, 160, 90, 0.86)' : 'rgba(25, 20, 45, 0.71)';
          ctx.beginPath();
          ctx.roundRect(this.optBtnRect1.x, this.optBtnRect1.y, btnW, btnH, 12);
          ctx.fill();
          ctx.strokeStyle = active1 ? '#8CFFB4' : '#645A82';
          ctx.lineWidth = active1 ? 2 : 1.2;
          ctx.stroke();
          ctx.fillStyle = active1 ? '#DCFFE6' : '#AAA0BE';
          ctx.fillText((active1 ? "▶ " : "  ") + opt1, this.optBtnRect1.x + 12, this.optBtnRect1.y + 19);

          const active2 = this.hoverOption === 1 || this.selectedDialogueOption === 1;
          ctx.fillStyle = active2 ? 'rgba(160, 110, 20, 0.86)' : 'rgba(25, 20, 45, 0.71)';
          ctx.beginPath();
          ctx.roundRect(this.optBtnRect2.x, this.optBtnRect2.y, btnW, btnH, 12);
          ctx.fill();
          ctx.strokeStyle = active2 ? '#FFD764' : '#645A82';
          ctx.lineWidth = active2 ? 2 : 1.2;
          ctx.stroke();
          ctx.fillStyle = active2 ? '#FFF0B4' : '#AAA0BE';
          ctx.fillText((active2 ? "▶ " : "  ") + opt2, this.optBtnRect2.x + 12, this.optBtnRect2.y + 19);
        } else {
          this.optBtnRect1 = null;
          this.optBtnRect2 = null;
          const arrowX = caixaX + caixaW / 2;
          const arrowY = caixaY + caixaH - 8;
          ctx.fillStyle = '#DCEBF5';
          ctx.beginPath();
          ctx.moveTo(arrowX - 8, arrowY - 8);
          ctx.lineTo(arrowX + 8, arrowY - 8);
          ctx.lineTo(arrowX, arrowY);
          ctx.closePath();
          ctx.fill();
        }
      }
    }
  }

  renderTextoQuebrado(texto, x, y, maxWidth) {
    const ctx = this.ctx;
    const palavras = texto.split(" ");
    let linhaAtual = "";
    let linhaY = y;

    for (let i = 0; i < palavras.length; i++) {
      const palavra = palavras[i];
      const teste = linhaAtual + palavra + " ";
      if (ctx.measureText(teste).width < maxWidth) {
        linhaAtual = teste;
      } else {
        ctx.fillText(linhaAtual, x, linhaY);
        linhaAtual = palavra + " ";
        linhaY += 22;
      }
    }
    ctx.fillText(linhaAtual, x, linhaY);
  }

  renderTextoComSombra(t, x, y, c) {
    const ctx = this.ctx;
    ctx.font = `bold 18px ${this.FONT_FAMILY}`;
    ctx.fillStyle = '#000000';
    ctx.fillText(t, x + 2, y + 2);
    ctx.fillStyle = c;
    ctx.fillText(t, x, y);
  }

  renderObjetivos() {
    const ctx = this.ctx;
    const ox = 20;
    const oy = 20;
    const ow = 200;
    const oh = 180;

    ctx.fillStyle = 'rgba(100, 100, 100, 0.78)';
    ctx.beginPath();
    ctx.roundRect(ox, oy, ow, oh, 10);
    ctx.fill();
    ctx.strokeStyle = '#C86464';
    ctx.lineWidth = 3;
    ctx.stroke();

    ctx.font = `bold 14px ${this.FONT_FAMILY}`;
    ctx.fillStyle = '#FF9696';
    ctx.textAlign = 'left';
    ctx.fillText("OBJETIVOS", ox + 30, oy + 25);

    ctx.strokeStyle = '#C86464';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(ox + 10, oy + 35);
    ctx.lineTo(ox + ow - 10, oy + 35);
    ctx.stroke();

    let yOffset = oy + 60;
    ctx.font = `11px ${this.FONT_FAMILY}`;

    ctx.fillStyle = this.explorouCorredor ? '#64FF64' : '#FFC864';
    ctx.fillText((this.explorouCorredor ? "✓ " : "○ ") + "Explorar corredor", ox + 15, yOffset);
    yOffset += 25;

    ctx.fillStyle = this.temChave ? '#64FF64' : '#FFC864';
    ctx.fillText((this.temChave ? "✓ " : "○ ") + "Pegar chave", ox + 15, yOffset);
    yOffset += 25;

    ctx.fillStyle = this.livroJoiFoiPego ? '#64FF64' : '#FFC864';
    ctx.fillText((this.livroJoiFoiPego ? "✓ " : "○ ") + "Abrir armário", ox + 15, yOffset);
    yOffset += 25;

    ctx.fillStyle = this.temLivro ? '#64FF64' : '#FFC864';
    ctx.fillText((this.temLivro ? "✓ " : "○ ") + "Pegar livro", ox + 15, yOffset);
    yOffset += 25;

    if (this.missaoLeituraAtiva || this.missaoArteAtiva || this.missaoFitnessAtiva) {
      ctx.fillStyle = '#FFC864';
      ctx.fillText("○ Abrir o Diário (J)", ox + 15, yOffset);
    }
  }

  renderEfeitoChave() {
    const ctx = this.ctx;
    const x = this.LARGURA - 120;
    const y = 100;

    const alpha = 1.0 - (this.contadorEfeitoChave / 60.0);
    if (alpha > 0) {
      ctx.save();
      ctx.globalAlpha = alpha;
      ctx.font = `bold 24px ${this.FONT_FAMILY}`;
      ctx.fillStyle = '#FFD700';
      ctx.fillText("COLETADO!", x - 70, y + 100);
      ctx.restore();
    }
  }

  renderNotificacao(texto, corFundo, corTexto, noTopo) {
    const ctx = this.ctx;
    ctx.font = `bold 20px ${this.FONT_FAMILY}`;
    const notifW = Math.max(400, ctx.measureText(texto).width + 90);
    const notifH = 52;

    const notifX = noTopo ? (this.LARGURA - notifW) / 2 : this.LARGURA - notifW - 30;
    const notifY = noTopo ? 20 : this.ALTURA - notifH - 30;

    ctx.fillStyle = corFundo;
    ctx.beginPath();
    ctx.roundRect(notifX, notifY, notifW, notifH, 18);
    ctx.fill();

    ctx.strokeStyle = corTexto;
    ctx.lineWidth = 2;
    ctx.stroke();

    ctx.fillStyle = corTexto;
    ctx.textAlign = 'left';
    ctx.fillText("✓  " + texto, notifX + 20, notifY + notifH / 2 + 7);
  }

  renderAnimacaoRiscado() {
    const ctx = this.ctx;
    ctx.font = `bold 22px ${this.FONT_FAMILY}`;
    const width = ctx.measureText(this.textoMissaoRiscada).width + 40;
    const height = 50;
    const x = this.LARGURA - width - 20;
    const y = 80;

    ctx.fillStyle = 'rgba(255, 250, 240, 0.86)';
    ctx.beginPath();
    ctx.roundRect(x, y, width, height, 10);
    ctx.fill();

    ctx.strokeStyle = '#C8B496';
    ctx.lineWidth = 2;
    ctx.stroke();

    ctx.fillStyle = '#643232';
    ctx.fillText(this.textoMissaoRiscada, x + 20, y + 32);

    if (this.contadorAnimacaoRiscado > 10) {
      const progressoRisco = Math.min(20, this.contadorAnimacaoRiscado - 10);
      const proporcao = progressoRisco / 20.0;
      const endX = x + 10 + Math.floor((width - 20) * proporcao);

      ctx.strokeStyle = 'rgba(200, 30, 30, 0.78)';
      ctx.lineWidth = 4;
      ctx.beginPath();
      ctx.moveTo(x + 10, y + height / 2);
      ctx.lineTo(endX, y + height / 2);
      ctx.stroke();
    }
  }

  renderInventario() {
    const ctx = this.ctx;
    ctx.fillStyle = 'rgba(0, 0, 0, 0.82)';
    ctx.fillRect(0, 0, this.LARGURA, this.ALTURA);

    const caixaX = 150;
    const caixaY = 50;
    const caixaLargura = this.LARGURA - 300;
    const caixaAltura = this.ALTURA - 100;

    ctx.fillStyle = '#2A2648';
    ctx.beginPath();
    ctx.roundRect(caixaX, caixaY, caixaLargura, caixaAltura, 30);
    ctx.fill();

    ctx.strokeStyle = '#FFDC50';
    ctx.lineWidth = 2.5;
    ctx.stroke();

    ctx.font = `bold 36px ${this.FONT_FAMILY}`;
    ctx.fillStyle = '#FFE15A';
    ctx.textAlign = 'center';
    ctx.fillText("INVENTÁRIO", caixaX + caixaLargura / 2, caixaY + 55);

    let itemCount = 0;
    if (this.temChave) itemCount++;
    if (this.temLivro) itemCount++;
    if (this.temCadernoEsboco) itemCount++;
    if (this.temCronograma) itemCount++;

    let itemY = caixaY + 130;

    if (this.temChave) {
      this.renderCartaoItem(caixaX, caixaLargura, itemY, "Chave do Armário", "Obtida de: Garoto com livro", this.images.chave);
      itemY += 90;
    }
    if (this.temLivro) {
      this.renderCartaoItem(caixaX, caixaLargura, itemY, "Livro Misterioso", "Encontrado no armário", this.images.livro);
      itemY += 90;
    }
    if (this.temCadernoEsboco) {
      this.renderCartaoItem(caixaX, caixaLargura, itemY, "Caderno de Esboços", "Obtido de: Líder Arte", null);
      itemY += 90;
    }
    if (this.temCronograma) {
      this.renderCartaoItem(caixaX, caixaLargura, itemY, "Cronograma de Treinos", "Obtido de: Líder Fitness", null);
      itemY += 90;
    }

    if (itemCount === 0) {
      ctx.font = `22px ${this.FONT_FAMILY}`;
      ctx.fillStyle = '#DC7878';
      ctx.fillText("Inventário vazio", caixaX + caixaLargura / 2, caixaY + caixaAltura / 2);
    }
  }

  renderCartaoItem(caixaX, caixaLargura, itemY, titulo, desc, img) {
    const ctx = this.ctx;
    const cartaoX = caixaX + 30;
    const cartaoY = itemY - 42;
    const cartaoLargura = caixaLargura - 60;
    const cartaoAltura = 78;

    ctx.fillStyle = 'rgba(255, 255, 255, 0.05)';
    ctx.beginPath();
    ctx.roundRect(cartaoX, cartaoY, cartaoLargura, cartaoAltura, 18);
    ctx.fill();

    ctx.strokeStyle = 'rgba(255, 215, 0, 0.2)';
    ctx.lineWidth = 1.5;
    ctx.stroke();

    const slotX = cartaoX + 16;
    const slotY = cartaoY + 13;
    const slotTam = 52;

    ctx.fillStyle = 'rgba(0, 0, 0, 0.39)';
    ctx.beginPath();
    ctx.roundRect(slotX, slotY, slotTam, slotTam, 14);
    ctx.fill();

    if (img) {
      ctx.drawImage(img, slotX + 4, slotY + 4, slotTam - 8, slotTam - 8);
    }

    ctx.font = `bold 20px ${this.FONT_FAMILY}`;
    ctx.fillStyle = '#FFF5D2';
    ctx.textAlign = 'left';
    ctx.fillText(titulo, cartaoX + 85, cartaoY + 32);

    ctx.font = `14px ${this.FONT_FAMILY}`;
    ctx.fillStyle = '#BEBCD2';
    ctx.fillText(desc, cartaoX + 85, cartaoY + 58);
  }

  renderDiario() {
    const ctx = this.ctx;
    ctx.fillStyle = 'rgba(0, 0, 0, 0.6)';
    ctx.fillRect(0, 0, this.LARGURA, this.ALTURA);

    const diarioX = 80;
    const diarioY = 30;
    const diarioW = 840;
    const diarioH = 680;
    const meioX = diarioX + diarioW / 2;

    ctx.fillStyle = '#654321';
    ctx.beginPath();
    ctx.roundRect(diarioX - 4, diarioY - 4, diarioW + 8, diarioH + 8, 24);
    ctx.fill();

    ctx.fillStyle = '#FFF8EB';
    ctx.beginPath();
    ctx.roundRect(diarioX, diarioY, diarioW, diarioH, 20);
    ctx.fill();

    ctx.strokeStyle = '#8B5A2B';
    ctx.lineWidth = 3;
    ctx.stroke();

    ctx.strokeStyle = '#784B1E';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(meioX, diarioY + 10);
    ctx.lineTo(meioX, diarioY + diarioH - 10);
    ctx.stroke();

    // Esquerda
    ctx.font = `bold 32px ${this.FONT_FAMILY}`;
    ctx.fillStyle = '#654321';
    ctx.textAlign = 'left';
    ctx.fillText("Meu Diário", diarioX + 70, diarioY + 50);

    let textX = diarioX + 40;
    let textY = diarioY + 120;

    this.renderSecaoDiario(textX, textY, "Arte", this.nivelArte, this.ep1_solidaoUrbanaAtiva, this.ep1_solidaoUrbanaConcluida, "Desenhar 'A Solidão Urbana'");
    textY += 120;

    this.renderSecaoDiario(textX, textY, "Fitness", this.nivelFitness, this.missaoFitnessAtiva, this.missaoFitnessConcluida, "12 Polichinelos no Ginásio");
    textY += 120;

    this.renderSecaoDiario(textX, textY, "Leitura", this.nivelLeitura, this.missaoLeituraAtiva, this.missaoLeituraConcluida, "Ler livro na Biblioteca");

    // Direita
    ctx.font = `bold 26px ${this.FONT_FAMILY}`;
    ctx.fillStyle = '#654321';
    ctx.fillText("Progresso Geral", meioX + 40, diarioY + 50);

    ctx.font = `14px ${this.FONT_FAMILY}`;
    ctx.fillStyle = '#503C28';
    ctx.fillText(`XP Total: ${this.xp}`, meioX + 40, diarioY + 100);
    ctx.fillText(`Nível Arte: ${this.nivelArte}`, meioX + 40, diarioY + 130);
    ctx.fillText(`Nível Fitness: ${this.nivelFitness}`, meioX + 40, diarioY + 160);
    ctx.fillText(`Nível Leitura: ${this.nivelLeitura}`, meioX + 40, diarioY + 190);
  }

  renderSecaoDiario(x, y, titulo, nivel, ativa, concluida, textoMissao) {
    const ctx = this.ctx;
    ctx.font = `bold 18px ${this.FONT_FAMILY}`;
    ctx.fillStyle = '#654321';
    ctx.fillText(`${titulo} (Nível ${nivel}/3)`, x, y);

    ctx.font = `14px ${this.FONT_FAMILY}`;
    if (concluida) {
      ctx.fillStyle = '#228B22';
      ctx.fillText(`✓ ${textoMissao} (Concluído!)`, x + 15, y + 25);
    } else if (ativa) {
      ctx.fillStyle = '#B41E1E';
      ctx.fillText(`▸ ${textoMissao}`, x + 15, y + 25);
    } else {
      ctx.fillStyle = '#644628';
      ctx.fillText(`▸ Fale com os alunos no corredor`, x + 15, y + 25);
    }
  }

  renderControlesTouch() {
    const ctx = this.ctx;
    if (this.armarioEstaAberto || this.inventarioAberto || this.diarioAberto) {
      this.renderBotaoSuaveComIcone(this.touchBtnFechar, "FECHAR", this.touchFecharPressionado, '#CD4B4B', 'fechar');
      return;
    }

    // 1. Fundo pílula suave atrás dos botões direcionais
    ctx.fillStyle = 'rgba(0, 0, 0, 0.45)';
    ctx.beginPath();
    ctx.roundRect(
      this.touchBtnEsquerda.x - 8,
      this.touchBtnEsquerda.y - 8,
      (this.touchBtnDireita.x + this.touchBtnDireita.width) - this.touchBtnEsquerda.x + 16,
      this.touchBtnEsquerda.height + 16,
      32
    );
    ctx.fill();

    // 2. Botões de Movimento
    this.renderBotaoSuaveComIcone(this.touchBtnEsquerda, "", this.touchEsquerdaPressionado, '#9B78CD', 'seta_esquerda');
    this.renderBotaoSuaveComIcone(this.touchBtnDireita, "", this.touchDireitaPressionado, '#9B78CD', 'seta_direita');

    // 3. Botão de Ação / Interação à Direita
    let labelAcao = "INTERAGIR";
    let tipoIcone = "maozinha";
    let corAcao = '#3CA569';
    if (this.textoDialogo.length > 0 || this.aguardandoAvanceSalaAuto || this.estaEmDialogoNicolas) {
      labelAcao = "AVANÇAR";
      tipoIcone = "balao";
      corAcao = '#468CDC';
    }
    this.renderBotaoSuaveComIcone(this.touchBtnInteragir, labelAcao, this.touchInteragirPressionado, corAcao, tipoIcone);

    // 4. Barra Superior de Atalhos Rápidos
    this.renderBotaoSuaveComIcone(this.touchBtnMenu, "MENU", this.touchMenuPressionado, '#827D91', 'menu');
    this.renderBotaoSuaveComIcone(this.touchBtnInventario, "ITENS", this.touchInventarioPressionado || this.inventarioAberto, '#7D9B6E', 'itens');
    this.renderBotaoSuaveComIcone(this.touchBtnDiario, "DIÁRIO", this.touchDiarioPressionado || this.diarioAberto, '#9169A5', 'diario');
    this.renderBotaoSuaveComIcone(this.touchBtnObjetivos, "MISSÕES", this.touchObjetivosPressionado || this.mostrarObjetivos, '#558CAB', 'missoes');

    if (this.textoDialogo.length > 0) {
      this.renderBotaoSuaveComIcone(this.touchBtnFechar, "FECHAR", this.touchFecharPressionado, '#C34B4B', 'fechar');
    }
  }

  renderBotaoSuaveComIcone(btn, label, pressionado, corTemaHex, tipoIcone) {
    const ctx = this.ctx;
    const { x, y, width: w, height: h } = btn;
    const arc = btn.arc || (w > 80 ? 24 : 16);

    ctx.save();

    // 1. Sombra suave inferior
    ctx.fillStyle = 'rgba(0, 0, 0, 0.22)';
    ctx.beginPath();
    ctx.roundRect(x + 2, y + 4, w, h, arc);
    ctx.fill();

    // 2. Fundo Glassmorphism
    if (pressionado) {
      ctx.fillStyle = corTemaHex;
      ctx.beginPath();
      ctx.roundRect(x, y + 2, w, h, arc);
      ctx.fill();

      // Brilho superior
      ctx.fillStyle = 'rgba(255, 255, 255, 0.43)';
      ctx.beginPath();
      ctx.roundRect(x + 4, y + 5, w - 8, Math.max(2, (h - 8) / 2), Math.max(1, arc - 4));
      ctx.fill();
    } else {
      ctx.fillStyle = 'rgba(28, 20, 36, 0.71)';
      ctx.beginPath();
      ctx.roundRect(x, y, w, h, arc);
      ctx.fill();

      // Reflexo superior
      ctx.fillStyle = 'rgba(255, 255, 255, 0.11)';
      ctx.beginPath();
      ctx.roundRect(x + 2, y + 2, w - 4, Math.max(2, (h - 4) / 2), Math.max(1, arc - 2));
      ctx.fill();
    }

    // 3. Borda suave arredondada
    ctx.strokeStyle = pressionado ? '#FFFFFF' : corTemaHex;
    ctx.lineWidth = pressionado ? 2.8 : 2.0;
    ctx.beginPath();
    ctx.roundRect(x, y + (pressionado ? 2 : 0), w, h, arc);
    ctx.stroke();

    // 4. Ícone Vetorial Específico
    const cx = x + w / 2;
    const cy = (!label || label.length === 0) ? (y + h / 2) : (y + h / 2 - 6);
    const yOff = pressionado ? 2 : 0;
    const cyEff = cy + yOff;

    if (tipoIcone === 'seta_esquerda') {
      this.drawIconSeta(cx, cyEff, 32, -1, 0, pressionado);
    } else if (tipoIcone === 'seta_direita') {
      this.drawIconSeta(cx, cyEff, 32, 1, 0, pressionado);
    } else if (tipoIcone === 'maozinha') {
      this.drawIconMaozinha(cx, cyEff, 42, pressionado);
    } else if (tipoIcone === 'balao') {
      this.drawIconBalao(cx, cyEff, 42, pressionado);
    } else if (tipoIcone === 'menu') {
      this.drawIconMenu(cx, cyEff - 2, 18, pressionado);
    } else if (tipoIcone === 'itens') {
      this.drawIconMochila(cx, cyEff - 2, 18, pressionado);
    } else if (tipoIcone === 'diario') {
      this.drawIconDiario(cx, cyEff - 2, 18, pressionado);
    } else if (tipoIcone === 'missoes') {
      this.drawIconAlvo(cx, cyEff - 2, 18, pressionado);
    } else if (tipoIcone === 'fechar') {
      this.drawIconFechar(cx, cyEff - 2, 18, pressionado);
    }

    // 5. Label de Texto
    if (label && label.length > 0) {
      ctx.font = `bold 12px ${this.FONT_FAMILY}`;
      ctx.textAlign = 'center';
      const tyTxt = y + h - 8 + (pressionado ? 2 : 0);

      // Sombra do texto
      ctx.fillStyle = 'rgba(0, 0, 0, 0.65)';
      ctx.fillText(label, cx + 1, tyTxt + 1);

      ctx.fillStyle = '#F8F5FC';
      ctx.fillText(label, cx, tyTxt);
    }

    ctx.restore();
  }

  drawIconSeta(cx, cy, size, dx, dy, pressionado) {
    const ctx = this.ctx;
    const r = size / 2;
    ctx.save();
    ctx.beginPath();
    if (dx === -1) {
      ctx.moveTo(cx + r * 0.5, cy - r * 0.65);
      ctx.lineTo(cx - r * 0.55, cy);
      ctx.lineTo(cx + r * 0.5, cy + r * 0.65);
      ctx.lineTo(cx + r * 0.2, cy);
    } else {
      ctx.moveTo(cx - r * 0.5, cy - r * 0.65);
      ctx.lineTo(cx + r * 0.55, cy);
      ctx.lineTo(cx - r * 0.5, cy + r * 0.65);
      ctx.lineTo(cx - r * 0.2, cy);
    }
    ctx.closePath();
    ctx.fillStyle = '#FFFFFF';
    ctx.fill();
    ctx.strokeStyle = 'rgba(60, 45, 75, 0.8)';
    ctx.lineWidth = 1.8;
    ctx.stroke();
    ctx.restore();
  }

  drawIconMaozinha(cx, cy, size, pressionado) {
    const ctx = this.ctx;
    const r = size / 2;
    ctx.save();
    ctx.fillStyle = '#FFF8F0';
    ctx.beginPath();
    ctx.roundRect(cx - r / 2, cy - r / 6, r, r * 0.85, r / 3);
    ctx.roundRect(cx - r * 0.82, cy + r / 12, r * 0.5, r * 0.42, r / 4);
    ctx.roundRect(cx - r * 0.48, cy - r * 0.82, r * 0.26, r * 0.78, r / 5);
    ctx.roundRect(cx - r * 0.18, cy - r * 0.92, r * 0.26, r * 0.84, r / 5);
    ctx.roundRect(cx + r * 0.12, cy - r * 0.82, r * 0.26, r * 0.78, r / 5);
    ctx.roundRect(cx + r * 0.42, cy - r * 0.62, r * 0.26, r * 0.62, r / 5);
    ctx.fill();

    ctx.strokeStyle = '#2D1E37';
    ctx.lineWidth = 2.2;
    ctx.stroke();
    ctx.restore();
  }

  drawIconBalao(cx, cy, size, pressionado) {
    const ctx = this.ctx;
    const r = size / 2;
    ctx.save();
    ctx.fillStyle = '#FFFFFF';
    ctx.beginPath();
    ctx.roundRect(cx - r * 0.7, cy - r * 0.6, r * 1.4, r * 0.95, r / 2);
    ctx.fill();

    ctx.beginPath();
    ctx.moveTo(cx - r * 0.3, cy + r * 0.35);
    ctx.lineTo(cx - r * 0.55, cy + r * 0.7);
    ctx.lineTo(cx, cy + r * 0.35);
    ctx.closePath();
    ctx.fill();

    ctx.strokeStyle = '#2D1E37';
    ctx.lineWidth = 2.2;
    ctx.stroke();

    ctx.fillStyle = '#2D1E37';
    const dot = Math.max(3, Math.floor(size / 10));
    ctx.beginPath();
    ctx.arc(cx - r * 0.35, cy - 2, dot / 2, 0, Math.PI * 2);
    ctx.arc(cx, cy - 2, dot / 2, 0, Math.PI * 2);
    ctx.arc(cx + r * 0.35, cy - 2, dot / 2, 0, Math.PI * 2);
    ctx.fill();
    ctx.restore();
  }

  drawIconMenu(cx, cy, size, pressionado) {
    const ctx = this.ctx;
    const r = size / 2;
    ctx.save();
    ctx.fillStyle = '#FFFFFF';
    ctx.beginPath();
    ctx.roundRect(cx - r + 1, cy - r, r * 0.6, r * 2, 3);
    ctx.roundRect(cx + r / 3, cy - r, r * 0.6, r * 2, 3);
    ctx.fill();
    ctx.restore();
  }

  drawIconMochila(cx, cy, size, pressionado) {
    const ctx = this.ctx;
    const r = size / 2;
    ctx.save();
    ctx.fillStyle = '#FFFFFF';
    ctx.beginPath();
    ctx.roundRect(cx - r * 0.8, cy - r * 0.6, r * 1.6, r * 1.4, 5);
    ctx.fill();

    ctx.strokeStyle = '#FFFFFF';
    ctx.lineWidth = 2.0;
    ctx.beginPath();
    ctx.arc(cx, cy - r * 0.6, r * 0.35, Math.PI, 0);
    ctx.stroke();

    ctx.strokeStyle = '#32283C';
    ctx.beginPath();
    ctx.moveTo(cx - r * 0.8, cy);
    ctx.lineTo(cx + r * 0.8, cy);
    ctx.stroke();
    ctx.restore();
  }

  drawIconDiario(cx, cy, size, pressionado) {
    const ctx = this.ctx;
    const r = size / 2;
    ctx.save();
    ctx.fillStyle = '#8B5A2B';
    ctx.beginPath();
    ctx.roundRect(cx - r * 0.78, cy - r * 0.92, r * 1.56, r * 1.84, 6);
    ctx.fill();

    ctx.fillStyle = '#FFFAF0';
    ctx.beginPath();
    ctx.roundRect(cx - r * 0.68, cy - r * 0.85, r * 1.36, r * 1.7, 4);
    ctx.fill();

    ctx.strokeStyle = '#784B1E';
    ctx.lineWidth = 1.2;
    ctx.beginPath();
    ctx.moveTo(cx, cy - r * 0.82);
    ctx.lineTo(cx, cy + r * 0.82);
    ctx.stroke();

    ctx.strokeStyle = 'rgba(100, 80, 60, 0.7)';
    ctx.lineWidth = 1.2;
    ctx.beginPath();
    ctx.moveTo(cx - r * 0.55, cy - r * 0.45); ctx.lineTo(cx - r * 0.1, cy - r * 0.45);
    ctx.moveTo(cx - r * 0.55, cy - r * 0.15); ctx.lineTo(cx - r * 0.15, cy - r * 0.15);
    ctx.moveTo(cx - r * 0.55, cy + r * 0.15); ctx.lineTo(cx - r * 0.2, cy + r * 0.15);

    ctx.moveTo(cx + r * 0.1, cy - r * 0.45); ctx.lineTo(cx + r * 0.55, cy - r * 0.45);
    ctx.moveTo(cx + r * 0.1, cy - r * 0.15); ctx.lineTo(cx + r * 0.5, cy - r * 0.15);
    ctx.moveTo(cx + r * 0.1, cy + r * 0.15); ctx.lineTo(cx + r * 0.4, cy + r * 0.15);
    ctx.stroke();
    ctx.restore();
  }

  drawIconAlvo(cx, cy, size, pressionado) {
    const ctx = this.ctx;
    const r = size / 2;
    ctx.save();
    ctx.strokeStyle = '#FFFFFF';
    ctx.lineWidth = 1.8;
    ctx.beginPath();
    ctx.arc(cx, cy, r - 1, 0, Math.PI * 2);
    ctx.stroke();

    ctx.fillStyle = '#FFFFFF';
    ctx.beginPath();
    ctx.arc(cx, cy, r / 3, 0, Math.PI * 2);
    ctx.fill();
    ctx.restore();
  }

  drawIconFechar(cx, cy, size, pressionado) {
    const ctx = this.ctx;
    const r = size / 2;
    ctx.save();
    ctx.strokeStyle = '#FFFFFF';
    ctx.lineWidth = 2.2;
    ctx.beginPath();
    ctx.moveTo(cx - r / 2, cy - r / 2); ctx.lineTo(cx + r / 2, cy + r / 2);
    ctx.moveTo(cx + r / 2, cy - r / 2); ctx.lineTo(cx - r / 2, cy + r / 2);
    ctx.stroke();
    ctx.restore();
  }

  // ─── TOUCH & MOUSE CLICKS CONVERTIDOS PARA COORDENADAS VIRTUAIS EXATAS ───
  mousePressed(e) {
    const p = this.getGameCoordinates(e);

    // 1. NAVEGAÇÃO NO MENU PRINCIPAL (MENU INICIAL FIEL AO JAVA)
    if (this.currentState === this.STATE.MENU_PRINCIPAL) {
      if (this.pointInRect(p, this.btnMenuNovo)) {
        this.acaoSlot = 'NOVO';
        this.slideCutsceneAtual = 0;
        this.currentState = this.STATE.CUTSCENE;
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
        return;
      }
      if (this.pointInRect(p, this.btnMenuContinuar)) {
        this.acaoSlot = 'CONTINUAR';
        this.currentState = this.STATE.MENU_SLOTS;
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
        return;
      }
      if (this.pointInRect(p, this.btnMenuSobre)) {
        this.currentState = this.STATE.SOBRE;
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
        return;
      }
      if (this.btnMenuSom && this.pointInRect(p, this.btnMenuSom)) {
        if (window.GerenciadorAudio) {
          window.GerenciadorAudio.somAtivo = !window.GerenciadorAudio.somAtivo;
          if (window.GerenciadorAudio.somAtivo) {
            window.GerenciadorAudio.tocarSomDialogo();
          }
        }
        return;
      }
      return;
    }

    // 2. NAVEGAÇÃO NA CUTSCENE
    if (this.currentState === this.STATE.CUTSCENE) {
      if (this.pointInRect(p, this.btnCutscenePular)) {
        this.iniciarJogo();
        return;
      }
      if (this.pointInRect(p, this.btnCutsceneAvancar)) {
        this.avancarCutscene();
        return;
      }
      this.avancarCutscene();
      return;
    }

    // 3. NAVEGAÇÃO NO MENU DE SLOTS
    if (this.currentState === this.STATE.MENU_SLOTS) {
      let clickedSlot = null;
      if (this.pointInRect(p, this.btnSlot1)) clickedSlot = 1;
      else if (this.pointInRect(p, this.btnSlot2)) clickedSlot = 2;
      else if (this.pointInRect(p, this.btnSlot3)) clickedSlot = 3;

      if (clickedSlot !== null) {
        this.slotAtual = clickedSlot;
        if (this.acaoSlot === 'NOVO') {
          this.slideCutsceneAtual = 0;
          this.currentState = this.STATE.CUTSCENE;
          this.salvarEstado(clickedSlot);
        } else {
          const ok = this.carregarEstado(clickedSlot);
          if (!ok) {
            // Slot vazio: inicia nova jornada nele
            this.slideCutsceneAtual = 0;
            this.currentState = this.STATE.CUTSCENE;
            this.salvarEstado(clickedSlot);
          }
        }
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomAbrirPorta();
        return;
      }
      if (this.pointInRect(p, this.btnSlotVoltar)) {
        this.currentState = this.STATE.MENU_PRINCIPAL;
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
        return;
      }
      return;
    }

    // 4. TELA SOBRE
    if (this.currentState === this.STATE.SOBRE) {
      this.currentState = this.STATE.MENU_PRINCIPAL;
      if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
      return;
    }

    // 5. MENU DE PAUSA EM JOGO
    if (this.menuAberto) {
      if (this.pointInRect(p, this.btnPauseResume)) {
        this.menuAberto = false;
        return;
      }
      if (this.pointInRect(p, this.btnPauseSalvar)) {
        this.salvarEstado(this.slotAtual);
        this.menuAberto = false;
        return;
      }
      if (this.pointInRect(p, this.btnPauseDiario)) {
        this.menuAberto = false;
        this.diarioAberto = true;
        return;
      }
      if (this.pointInRect(p, this.btnPauseMissoes)) {
        this.menuAberto = false;
        this.mostrarObjetivos = true;
        return;
      }
      if (this.pointInRect(p, this.btnPauseItens)) {
        this.menuAberto = false;
        this.inventarioAberto = true;
        return;
      }
      if (this.pointInRect(p, this.btnPauseExit)) {
        this.salvarEstado(this.slotAtual);
        this.menuAberto = false;
        this.currentState = this.STATE.MENU_PRINCIPAL;
        return;
      }
      return;
    }

    if (this.easterEggAtivo) {
      this.easterEggAtivo = false;
      return;
    }

    // Clique Easter Egg nos armários (Corredor 2, Corredor 1 ou Armário Aberto)
    let ehArmario = false;
    if (this.armarioEstaAberto) {
      ehArmario = true;
    } else if (this.indiceMapa === 1) {
      // Corredor 2 (boxX=50, boxY=150, boxW=200, boxH=220)
      if (p.x >= 50 && p.x <= 250 && p.y >= 150 && p.y <= 370) ehArmario = true;
    } else if (this.indiceMapa === 0 && Math.abs(this.audreyX - this.posArmarioX) < 250) {
      // Corredor 1
      if (p.x >= 500 && p.x <= 800 && p.y >= 50 && p.y <= 450) ehArmario = true;
    }

    if (ehArmario) {
      this.cliquesArmarioEasterEgg++;
      if (this.cliquesArmarioEasterEgg >= 3) {
        this.cliquesArmarioEasterEgg = 0;
        this.easterEggAtivo = true;
        this.easterEggTimer = 0;
        this.init3DRaycaster();
        this.ganharXP(50, 'arte', 'Easter Egg dos Armários');
        if (window.GerenciadorAudio) {
          if (window.SFX && window.SFX.achievement) window.SFX.achievement();
          else window.GerenciadorAudio.tocarSomColeta();
        }
        return;
      }
    } else {
      this.cliquesArmarioEasterEgg = 0;
    }

    if (this.pointInRect(p, this.touchBtnMenu)) {
      this.menuAberto = !this.menuAberto;
      if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
      return;
    }

    if (this.pointInRect(p, this.touchBtnInventario)) {
      this.inventarioAberto = !this.inventarioAberto;
      if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
      return;
    }
    if (this.pointInRect(p, this.touchBtnDiario)) {
      if (!this.temDiario) {
        this.textoNotificacao = "Pegue o diário em seu armário antes!";
        this.mostrarNotificacaoMissao = true;
        this.contadorNotificacaoMissao = 0;
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomErro();
      } else {
        this.diarioAberto = !this.diarioAberto;
        if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
      }
      return;
    }
    if (this.pointInRect(p, this.touchBtnObjetivos)) {
      this.mostrarObjetivos = !this.mostrarObjetivos;
      if (window.GerenciadorAudio) window.GerenciadorAudio.tocarSomDialogo();
      return;
    }
    if (this.pointInRect(p, this.touchBtnFechar)) {
      if (this.armarioEstaAberto) this.armarioEstaAberto = false;
      if (this.inventarioAberto) this.inventarioAberto = false;
      if (this.diarioAberto) this.diarioAberto = false;
      if (this.menuAberto) this.menuAberto = false;
      this.textoDialogo = "";
      this.nomePersonagem = "";
      this.estaEmDialogoNicolas = false;
      if (window.GerenciadorAudio) window.GerenciadorAudio.pararTodasAsVozes();
      return;
    }

    if (this.pointInRect(p, this.touchBtnEsquerda)) {
      this.touchEsquerdaPressionado = true;
      this.moverEsquerda();
      return;
    }
    if (this.pointInRect(p, this.touchBtnDireita)) {
      this.touchDireitaPressionado = true;
      this.moverDireita();
      return;
    }
    if (this.pointInRect(p, this.touchBtnInteragir)) {
      if (this.estaEmDialogoNicolas || this.aguardandoAvanceSalaAuto || this.textoDialogo.length > 0) {
        this.avancarDialogo();
      } else {
        this.executarInteracao();
      }
      return;
    }

    if (this.faseDialogoNicolas === 100 && this.tamanhoTextoVisivel >= this.textoDialogo.length) {
      if (this.optBtnRect1 && this.pointInRect(p, this.optBtnRect1)) {
        this.selectedDialogueOption = 0;
        if (window.mostrarImportadorReal) window.mostrarImportadorReal();
        return;
      } else if (this.optBtnRect2 && this.pointInRect(p, this.optBtnRect2)) {
        this.selectedDialogueOption = 1;
        this.estaEmDialogoNicolas = false;
        this.textoDialogo = "";
        this.nomePersonagem = "";
        this.faseDialogoNicolas = 0;
        this.ep1_entregouDesenho = false;
        return;
      }
    }
  }

  mouseReleased() {
    this.touchEsquerdaPressionado = false;
    this.touchDireitaPressionado = false;
    this.pararMovimento();
  }

  mouseMoved(e) {
    if (this.faseDialogoNicolas === 100 && this.tamanhoTextoVisivel >= this.textoDialogo.length) {
      const p = this.getGameCoordinates(e);
      if (this.optBtnRect1 && this.pointInRect(p, this.optBtnRect1)) this.hoverOption = 0;
      else if (this.optBtnRect2 && this.pointInRect(p, this.optBtnRect2)) this.hoverOption = 1;
      else this.hoverOption = -1;
    }
  }

  pointInRect(p, rect) {
    return p.x >= rect.x && p.x <= rect.x + rect.width && p.y >= rect.y && p.y <= rect.y + rect.height;
  }
}

// Inicializar quando a página for carregada
window.addEventListener('load', () => {
  if (document.getElementById('gameCanvas')) {
    window.jogoAudrey = new JogoAudreyEngine('gameCanvas');
  }
});
