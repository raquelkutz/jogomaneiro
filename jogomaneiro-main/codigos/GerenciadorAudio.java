import javax.sound.sampled.*;

public class GerenciadorAudio {
    private static final int SAMPLE_RATE = 44100;
    private static final float MIN_DB = -35.0f;
    private static final float MAX_DB = -18.0f;

    private static Clip clipFundo;
    private static Clip clipCarregamento;

    public static void tocarMusicaFundo() {
        new Thread(() -> {
            try {
                final java.io.File audioFile = new java.io.File(JogoAudrey.resolvePath("bgm.wav"));
                if (audioFile.exists()) {
                    final AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                    clipFundo = AudioSystem.getClip();
                    clipFundo.open(audioStream);
                    atualizarVolumeMusica(Configuracoes.getInstance().getVolumeMusica());
                    clipFundo.loop(Clip.LOOP_CONTINUOUSLY);
                    clipFundo.start();
                } else {
                    System.err.println("Arquivo de musica nao encontrado: bgm.wav");
                }
            } catch (Exception e) {
                System.err.println("Erro ao tocar música de fundo: " + e.getMessage());
            }
        }).start();
    }

    private static float calcularVolumeDb(final int volume) {
        final int v = Math.max(0, Math.min(100, volume));
        return MIN_DB + ((MAX_DB - MIN_DB) * (v / 100.0f));
    }

    public static void atualizarVolumeMusica(final int volumeMusica) {
        ajustarVolumeClip(clipFundo, volumeMusica);
    }

    private static void ajustarVolumeClip(Clip clip, int volume) {
        if (clip != null && clip.isOpen()) {
            try {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                if (volume <= 0) {
                    gainControl.setValue(gainControl.getMinimum());
                } else {
                    gainControl.setValue(calcularVolumeDb(volume));
                }
            } catch (Exception e) {
                // Ignore if control not supported
            }
        }
    }

    public static void tocarMusicaCarregamento() {
        new Thread(() -> {
            try {
                java.io.File audioFile = new java.io.File(JogoAudrey.resolvePath("carregamento.wav"));
                if (audioFile.exists()) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                    clipCarregamento = AudioSystem.getClip();
                    clipCarregamento.open(audioStream);
                    try {
                        FloatControl gainControl = (FloatControl) clipCarregamento.getControl(FloatControl.Type.MASTER_GAIN);
                        int vol = Configuracoes.getInstance().getVolumeEfeitos();
                        if (vol <= 0) {
                            gainControl.setValue(gainControl.getMinimum());
                        } else {
                            gainControl.setValue(Math.max(gainControl.getMinimum(), calcularVolumeDb(vol) - 6.0f));
                        }
                    } catch (Exception e) {
                    }
                    clipCarregamento.addLineListener(ev -> {
                        if (ev.getType() == LineEvent.Type.STOP) {
                            clipCarregamento.close();
                        }
                    });
                    clipCarregamento.start();
                } else {
                    System.err.println("Arquivo de musica nao encontrado: carregamento.wav");
                }
            } catch (Exception e) {
                System.err.println("Erro ao tocar música de carregamento: " + e.getMessage());
            }
        }).start();
    }

    public static void pararMusicaCarregamento() {
        if (clipCarregamento != null) {
            if (clipCarregamento.isOpen()) {
                if (clipCarregamento.isRunning()) {
                    clipCarregamento.stop();
                }
                clipCarregamento.close();
            }
            clipCarregamento = null;
        }
    }

    public static void pausarMusicaFundo() {
        if (clipFundo != null && clipFundo.isOpen() && clipFundo.isRunning()) {
            clipFundo.stop();
        }
    }

    public static void retomarMusicaFundo() {
        if (clipFundo != null && clipFundo.isOpen() && !clipFundo.isRunning()) {
            clipFundo.loop(Clip.LOOP_CONTINUOUSLY);
            clipFundo.start();
        }
    }

    public static void tocarSomSinalEscolar() {
        new Thread(() -> {
            try {
                // Toca notas simulando um sino de escola
                tocarNota(800, 400);
                Thread.sleep(100);
                tocarNota(800, 800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    // Som de play - Estrelas mágicas
    public static void tocarSomPlay() {
        new Thread(() -> {
            try {
                // Efeito mágico com notas altas e tintilar
                tocarNotaComRuido(1200, 100, 0.3f);
                Thread.sleep(80);
                tocarNotaComRuido(1600, 120, 0.25f);
                Thread.sleep(100);
                tocarNotaComRuido(2000, 150, 0.2f);
                Thread.sleep(80);
                tocarNotaComRuido(1800, 100, 0.25f);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    // Som de porta de madeira
    public static void tocarSomPortaMadeira() {
        new Thread(() -> {
            try {
                // Efeito de creaking de madeira - frequências mais baixas e som áspero
                tocarSomAspero(150, 250);
                Thread.sleep(200);
                tocarSomAspero(180, 300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    // Som real de abertura de porta
    public static void tocarSomAbrirPorta() {
        tocarWav("abrir_porta.wav", -12f);
    }

    // Passos ao caminhar (cache em memória para 0 I/O de disco no movimento)
    private static volatile Clip clipPassos;
    private static final Object LOCK_PASSOS = new Object();

    private static void carregarClipPassos() {
        if (clipPassos != null && clipPassos.isOpen()) return;
        try {
            java.io.File f = new java.io.File(JogoAudrey.resolvePath("passos_concreto.wav"));
            if (f.exists()) {
                AudioInputStream in = AudioSystem.getAudioInputStream(f);
                clipPassos = AudioSystem.getClip();
                clipPassos.open(in);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar passos: " + e.getMessage());
        }
    }

    public static void tocarSomPassos() {
        synchronized (LOCK_PASSOS) {
            carregarClipPassos();
            if (clipPassos != null && clipPassos.isOpen() && !clipPassos.isRunning()) {
                clipPassos.setFramePosition(0);
                clipPassos.loop(Clip.LOOP_CONTINUOUSLY);
                clipPassos.start();
            }
        }
    }

    public static void pararSomPassos() {
        synchronized (LOCK_PASSOS) {
            if (clipPassos != null && clipPassos.isOpen() && clipPassos.isRunning()) {
                clipPassos.stop();
            }
        }
    }

    // Voz do personagem Nicolás (tocada quando ele fala, estilo Undertale)
    private static volatile Clip clipVozNicolas;
    private static volatile boolean tocandoVozNicolas = false;
    private static volatile Thread threadVozNicolas = null;
    private static final Object LOCK_VOZ = new Object();

    private static void logVoz(String msg) {
        System.out.println("[VOZ] " + msg);
    }

    private static void carregarClipVozNicolas() {
        if (clipVozNicolas != null && clipVozNicolas.isOpen()) {
            return;
        }
        try {
            java.io.File f = new java.io.File(JogoAudrey.resolvePath("vozdopersonagemnicollas.wav"));
            if (!f.exists()) {
                f = new java.io.File(JogoAudrey.resolvePath("voz_nicolas.wav"));
            }
            if (!f.exists()) {
                logVoz("arquivo NAO existe");
                return;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(f);
            clipVozNicolas = AudioSystem.getClip();
            clipVozNicolas.open(audioStream);
            logVoz("Clip de voz Nicolas carregado com sucesso!");
        } catch (Exception e) {
            logVoz("Erro ao carregar clip voz: " + e.getMessage());
        }
    }

    public static void tocarVozNicolas() {
        synchronized (LOCK_VOZ) {
            if (tocandoVozNicolas && threadVozNicolas != null && threadVozNicolas.isAlive()) {
                return;
            }
            tocandoVozNicolas = true;
            if (threadVozNicolas != null && threadVozNicolas.isAlive()) {
                threadVozNicolas.interrupt();
            }
            threadVozNicolas = new Thread(() -> {
                carregarClipVozNicolas();
                if (clipVozNicolas == null || !clipVozNicolas.isOpen()) {
                    synchronized (LOCK_VOZ) {
                        tocandoVozNicolas = false;
                    }
                    return;
                }
                while (tocandoVozNicolas && !Thread.currentThread().isInterrupted()) {
                    try {
                        clipVozNicolas.stop();
                        clipVozNicolas.setFramePosition(0);

                        try {
                            FloatControl gain = (FloatControl) clipVozNicolas.getControl(FloatControl.Type.MASTER_GAIN);
                            int vol = Configuracoes.getInstance().getVolumeEfeitos();
                            if (vol <= 0) {
                                gain.setValue(gain.getMinimum());
                            } else {
                                float maxDb = -10.0f;
                                float minDb = -35.0f;
                                float valorDb = minDb + (maxDb - minDb) * (vol / 100.0f);
                                gain.setValue(valorDb);
                            }
                        } catch (Exception ignorado) {
                        }

                        clipVozNicolas.start();
                        Thread.sleep(75);
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        logVoz("Erro ao tocar blip: " + e.getMessage());
                    }
                }
            });
            threadVozNicolas.start();
        }
    }

    public static void pararVozNicolas() {
        synchronized (LOCK_VOZ) {
            tocandoVozNicolas = false;
            if (threadVozNicolas != null) {
                threadVozNicolas.interrupt();
                threadVozNicolas = null;
            }
            if (clipVozNicolas != null && clipVozNicolas.isRunning()) {
                clipVozNicolas.stop();
            }
        }
    }

    // Voz da personagem Raquel (tocada quando ela fala, estilo Undertale)
    private static volatile Clip clipVozRaquel;
    private static volatile boolean tocandoVozRaquel = false;
    private static volatile Thread threadVozRaquel = null;
    private static final Object LOCK_VOZ_RAQUEL = new Object();

    private static void carregarClipVozRaquel() {
        if (clipVozRaquel != null && clipVozRaquel.isOpen()) {
            return;
        }
        try {
            java.io.File f = null;

            // 1. Busca dinâmica na pasta audios/ e na pasta raiz do jogo
            String[] pastasParaBuscar = {"audios", "."};
            for (String pastaNome : pastasParaBuscar) {
                java.io.File pasta = new java.io.File(JogoAudrey.resolvePath(pastaNome));
                if (pasta.isDirectory()) {
                    java.io.File[] arquivos = pasta.listFiles();
                    if (arquivos != null) {
                        for (java.io.File arq : arquivos) {
                            String nome = arq.getName().toLowerCase();
                            if (nome.endsWith(".wav") || nome.endsWith(".mp3") || nome.endsWith(".ogg")) {
                                if (nome.contains("quel") || nome.contains("raquel")) {
                                    f = arq;
                                    logVoz("Voz da Raquel encontrada por busca dinamica: " + arq.getAbsolutePath());
                                    break;
                                }
                            }
                        }
                    }
                }
                if (f != null && f.exists()) break;
            }

            // 2. Se não encontrou por busca dinâmica, tenta caminhos exatos
            if (f == null || !f.exists()) {
                String[] arquivosPossiveis = {
                    "voz_raquel.wav",
                    "vozdopersonagemraquel.wav",
                    "\u00C1udio-de-\u00B0Quel\u2606.wav"
                };
                for (String arq : arquivosPossiveis) {
                    java.io.File temp = new java.io.File(JogoAudrey.resolvePath(arq));
                    if (temp.exists()) {
                        f = temp;
                        logVoz("Voz da Raquel encontrada por nome exato: " + temp.getAbsolutePath());
                        break;
                    }
                }
            }

            if (f == null || !f.exists()) {
                logVoz("Arquivo de voz da Raquel nao encontrado. Coloque um .wav com 'quel' no nome na pasta audios/");
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(f);
            clipVozRaquel = AudioSystem.getClip();
            clipVozRaquel.open(audioStream);
            logVoz("Clip de voz Raquel (" + f.getName() + ") carregado com sucesso!");
        } catch (Exception e) {
            logVoz("Erro ao carregar voz da Raquel (" + e.getMessage() + ")");
        }
    }

    public static void tocarVozRaquel() {
        synchronized (LOCK_VOZ_RAQUEL) {
            if (tocandoVozRaquel && threadVozRaquel != null && threadVozRaquel.isAlive()) {
                return;
            }
            tocandoVozRaquel = true;
            if (threadVozRaquel != null && threadVozRaquel.isAlive()) {
                threadVozRaquel.interrupt();
            }
            threadVozRaquel = new Thread(() -> {
                carregarClipVozRaquel();
                if (clipVozRaquel == null || !clipVozRaquel.isOpen()) {
                    synchronized (LOCK_VOZ_RAQUEL) {
                        tocandoVozRaquel = false;
                    }
                    return;
                }
                while (tocandoVozRaquel && !Thread.currentThread().isInterrupted()) {
                    try {
                        clipVozRaquel.stop();
                        clipVozRaquel.setFramePosition(0);

                        try {
                            FloatControl gain = (FloatControl) clipVozRaquel.getControl(FloatControl.Type.MASTER_GAIN);
                            int vol = Configuracoes.getInstance().getVolumeEfeitos();
                            if (vol <= 0) {
                                gain.setValue(gain.getMinimum());
                            } else {
                                float maxDb = -10.0f;
                                float minDb = -35.0f;
                                float valorDb = minDb + (maxDb - minDb) * (vol / 100.0f);
                                gain.setValue(valorDb);
                            }
                        } catch (Exception ignorado) {
                        }

                        clipVozRaquel.start();
                        Thread.sleep(75);
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        logVoz("Erro ao tocar blip Raquel: " + e.getMessage());
                    }
                }
            });
            threadVozRaquel.start();
        }
    }

    public static void pararVozRaquel() {
        synchronized (LOCK_VOZ_RAQUEL) {
            tocandoVozRaquel = false;
            if (threadVozRaquel != null) {
                threadVozRaquel.interrupt();
                threadVozRaquel = null;
            }
            if (clipVozRaquel != null && clipVozRaquel.isRunning()) {
                clipVozRaquel.stop();
            }
        }
    }

    // Voz da personagem Audrey (tocada quando ela fala, estilo Undertale)
    private static volatile Clip clipVozAudrey;
    private static volatile boolean tocandoVozAudrey = false;
    private static volatile Thread threadVozAudrey = null;
    private static final Object LOCK_VOZ_AUDREY = new Object();

    private static void carregarClipVozAudrey() {
        if (clipVozAudrey != null && clipVozAudrey.isOpen()) {
            return;
        }
        try {
            java.io.File f = new java.io.File(JogoAudrey.resolvePath("voz-audrey.wav"));
            if (!f.exists()) {
                logVoz("Arquivo de voz da Audrey nao encontrado: voz-audrey.wav");
                return;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(f);
            clipVozAudrey = AudioSystem.getClip();
            clipVozAudrey.open(audioStream);
            logVoz("Clip de voz Audrey carregado com sucesso!");
        } catch (Exception e) {
            logVoz("Erro ao carregar voz da Audrey: " + e.getMessage());
        }
    }

    public static void tocarVozAudrey() {
        synchronized (LOCK_VOZ_AUDREY) {
            if (tocandoVozAudrey && threadVozAudrey != null && threadVozAudrey.isAlive()) {
                return;
            }
            tocandoVozAudrey = true;
            if (threadVozAudrey != null && threadVozAudrey.isAlive()) {
                threadVozAudrey.interrupt();
            }
            threadVozAudrey = new Thread(() -> {
                carregarClipVozAudrey();
                if (clipVozAudrey == null || !clipVozAudrey.isOpen()) {
                    synchronized (LOCK_VOZ_AUDREY) {
                        tocandoVozAudrey = false;
                    }
                    return;
                }
                while (tocandoVozAudrey && !Thread.currentThread().isInterrupted()) {
                    try {
                        clipVozAudrey.stop();
                        clipVozAudrey.setFramePosition(0);

                        try {
                            FloatControl gain = (FloatControl) clipVozAudrey.getControl(FloatControl.Type.MASTER_GAIN);
                            int vol = Configuracoes.getInstance().getVolumeEfeitos();
                            if (vol <= 0) {
                                gain.setValue(gain.getMinimum());
                            } else {
                                float maxDb = -10.0f;
                                float minDb = -35.0f;
                                float valorDb = minDb + (maxDb - minDb) * (vol / 100.0f);
                                gain.setValue(valorDb);
                            }
                        } catch (Exception ignorado) {
                        }

                        clipVozAudrey.start();
                        Thread.sleep(75);
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        logVoz("Erro ao tocar blip Audrey: " + e.getMessage());
                    }
                }
            });
            threadVozAudrey.start();
        }
    }

    public static void pararVozAudrey() {
        synchronized (LOCK_VOZ_AUDREY) {
            tocandoVozAudrey = false;
            if (threadVozAudrey != null) {
                threadVozAudrey.interrupt();
                threadVozAudrey = null;
            }
            if (clipVozAudrey != null && clipVozAudrey.isRunning()) {
                clipVozAudrey.stop();
            }
        }
    }

    // Voz da personagem Camila (tocada quando ela fala, estilo Undertale)
    private static volatile Clip clipVozCamila;
    private static volatile boolean tocandoVozCamila = false;
    private static volatile Thread threadVozCamila = null;
    private static final Object LOCK_VOZ_CAMILA = new Object();

    private static void carregarClipVozCamila() {
        if (clipVozCamila != null && clipVozCamila.isOpen()) return;
        try {
            java.io.File f = new java.io.File(JogoAudrey.resolvePath("voz-camila.wav"));
            if (!f.exists()) return;
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(f);
            clipVozCamila = AudioSystem.getClip();
            clipVozCamila.open(audioStream);
        } catch (Exception e) {
            logVoz("Erro ao carregar voz da Camila: " + e.getMessage());
        }
    }

    public static void tocarVozCamila() {
        synchronized (LOCK_VOZ_CAMILA) {
            if (tocandoVozCamila && threadVozCamila != null && threadVozCamila.isAlive()) return;
            tocandoVozCamila = true;
            if (threadVozCamila != null && threadVozCamila.isAlive()) threadVozCamila.interrupt();
            threadVozCamila = new Thread(() -> {
                carregarClipVozCamila();
                if (clipVozCamila == null || !clipVozCamila.isOpen()) {
                    synchronized (LOCK_VOZ_CAMILA) { tocandoVozCamila = false; }
                    return;
                }
                while (tocandoVozCamila && !Thread.currentThread().isInterrupted()) {
                    try {
                        clipVozCamila.stop();
                        clipVozCamila.setFramePosition(0);
                        try {
                            FloatControl gain = (FloatControl) clipVozCamila.getControl(FloatControl.Type.MASTER_GAIN);
                            int vol = Configuracoes.getInstance().getVolumeEfeitos();
                            if (vol <= 0) gain.setValue(gain.getMinimum());
                            else {
                                float maxDb = -10.0f;
                                float minDb = -35.0f;
                                gain.setValue(minDb + (maxDb - minDb) * (vol / 100.0f));
                            }
                        } catch (Exception ignorado) {}
                        clipVozCamila.start();
                        Thread.sleep(75);
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        logVoz("Erro ao tocar blip Camila: " + e.getMessage());
                    }
                }
            });
            threadVozCamila.start();
        }
    }

    public static void pararVozCamila() {
        synchronized (LOCK_VOZ_CAMILA) {
            tocandoVozCamila = false;
            if (threadVozCamila != null) {
                threadVozCamila.interrupt();
                threadVozCamila = null;
            }
            if (clipVozCamila != null && clipVozCamila.isRunning()) clipVozCamila.stop();
        }
    }

    // Voz da personagem Gabi (tocada quando ela fala, estilo Undertale)
    private static volatile Clip clipVozGabi;
    private static volatile boolean tocandoVozGabi = false;
    private static volatile Thread threadVozGabi = null;
    private static final Object LOCK_VOZ_GABI = new Object();

    private static void carregarClipVozGabi() {
        if (clipVozGabi != null && clipVozGabi.isOpen()) return;
        try {
            java.io.File f = new java.io.File(JogoAudrey.resolvePath("voz-gabi.wav"));
            if (!f.exists()) return;
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(f);
            clipVozGabi = AudioSystem.getClip();
            clipVozGabi.open(audioStream);
        } catch (Exception e) {
            logVoz("Erro ao carregar voz da Gabi: " + e.getMessage());
        }
    }

    public static void tocarVozGabi() {
        synchronized (LOCK_VOZ_GABI) {
            if (tocandoVozGabi && threadVozGabi != null && threadVozGabi.isAlive()) return;
            tocandoVozGabi = true;
            if (threadVozGabi != null && threadVozGabi.isAlive()) threadVozGabi.interrupt();
            threadVozGabi = new Thread(() -> {
                carregarClipVozGabi();
                if (clipVozGabi == null || !clipVozGabi.isOpen()) {
                    synchronized (LOCK_VOZ_GABI) { tocandoVozGabi = false; }
                    return;
                }
                while (tocandoVozGabi && !Thread.currentThread().isInterrupted()) {
                    try {
                        clipVozGabi.stop();
                        clipVozGabi.setFramePosition(0);
                        try {
                            FloatControl gain = (FloatControl) clipVozGabi.getControl(FloatControl.Type.MASTER_GAIN);
                            int vol = Configuracoes.getInstance().getVolumeEfeitos();
                            if (vol <= 0) gain.setValue(gain.getMinimum());
                            else {
                                float maxDb = -10.0f;
                                float minDb = -35.0f;
                                gain.setValue(minDb + (maxDb - minDb) * (vol / 100.0f));
                            }
                        } catch (Exception ignorado) {}
                        clipVozGabi.start();
                        Thread.sleep(75);
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        logVoz("Erro ao tocar blip Gabi: " + e.getMessage());
                    }
                }
            });
            threadVozGabi.start();
        }
    }

    public static void pararVozGabi() {
        synchronized (LOCK_VOZ_GABI) {
            tocandoVozGabi = false;
            if (threadVozGabi != null) {
                threadVozGabi.interrupt();
                threadVozGabi = null;
            }
            if (clipVozGabi != null && clipVozGabi.isRunning()) clipVozGabi.stop();
        }
    }

    // Voz da personagem Ivi (tocada quando ela fala, estilo Undertale)
    private static volatile Clip clipVozIvi;
    private static volatile boolean tocandoVozIvi = false;
    private static volatile Thread threadVozIvi = null;
    private static final Object LOCK_VOZ_IVI = new Object();

    private static void carregarClipVozIvi() {
        if (clipVozIvi != null && clipVozIvi.isOpen()) return;
        try {
            java.io.File f = new java.io.File(JogoAudrey.resolvePath("voz-ivi.wav"));
            if (!f.exists()) return;
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(f);
            clipVozIvi = AudioSystem.getClip();
            clipVozIvi.open(audioStream);
        } catch (Exception e) {
            logVoz("Erro ao carregar voz da Ivi: " + e.getMessage());
        }
    }

    public static void tocarVozIvi() {
        synchronized (LOCK_VOZ_IVI) {
            if (tocandoVozIvi && threadVozIvi != null && threadVozIvi.isAlive()) return;
            tocandoVozIvi = true;
            if (threadVozIvi != null && threadVozIvi.isAlive()) threadVozIvi.interrupt();
            threadVozIvi = new Thread(() -> {
                carregarClipVozIvi();
                if (clipVozIvi == null || !clipVozIvi.isOpen()) {
                    synchronized (LOCK_VOZ_IVI) { tocandoVozIvi = false; }
                    return;
                }
                while (tocandoVozIvi && !Thread.currentThread().isInterrupted()) {
                    try {
                        clipVozIvi.stop();
                        clipVozIvi.setFramePosition(0);
                        try {
                            FloatControl gain = (FloatControl) clipVozIvi.getControl(FloatControl.Type.MASTER_GAIN);
                            int vol = Configuracoes.getInstance().getVolumeEfeitos();
                            if (vol <= 0) gain.setValue(gain.getMinimum());
                            else {
                                float maxDb = -10.0f;
                                float minDb = -35.0f;
                                gain.setValue(minDb + (maxDb - minDb) * (vol / 100.0f));
                            }
                        } catch (Exception ignorado) {}
                        clipVozIvi.start();
                        Thread.sleep(75);
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        logVoz("Erro ao tocar blip Ivi: " + e.getMessage());
                    }
                }
            });
            threadVozIvi.start();
        }
    }

    public static void pararVozIvi() {
        synchronized (LOCK_VOZ_IVI) {
            tocandoVozIvi = false;
            if (threadVozIvi != null) {
                threadVozIvi.interrupt();
                threadVozIvi = null;
            }
            if (clipVozIvi != null && clipVozIvi.isRunning()) clipVozIvi.stop();
        }
    }

    // Voz da personagem Maysa (tocada quando ela fala, estilo Undertale)
    private static volatile Clip clipVozMaysa;
    private static volatile boolean tocandoVozMaysa = false;
    private static volatile Thread threadVozMaysa = null;
    private static final Object LOCK_VOZ_MAYSA = new Object();

    private static void carregarClipVozMaysa() {
        if (clipVozMaysa != null && clipVozMaysa.isOpen()) return;
        try {
            java.io.File f = new java.io.File(JogoAudrey.resolvePath("voz-maysa.wav"));
            if (!f.exists()) return;
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(f);
            clipVozMaysa = AudioSystem.getClip();
            clipVozMaysa.open(audioStream);
        } catch (Exception e) {
            logVoz("Erro ao carregar voz da Maysa: " + e.getMessage());
        }
    }

    public static void tocarVozMaysa() {
        synchronized (LOCK_VOZ_MAYSA) {
            if (tocandoVozMaysa && threadVozMaysa != null && threadVozMaysa.isAlive()) return;
            tocandoVozMaysa = true;
            if (threadVozMaysa != null && threadVozMaysa.isAlive()) threadVozMaysa.interrupt();
            threadVozMaysa = new Thread(() -> {
                carregarClipVozMaysa();
                if (clipVozMaysa == null || !clipVozMaysa.isOpen()) {
                    synchronized (LOCK_VOZ_MAYSA) { tocandoVozMaysa = false; }
                    return;
                }
                while (tocandoVozMaysa && !Thread.currentThread().isInterrupted()) {
                    try {
                        clipVozMaysa.stop();
                        clipVozMaysa.setFramePosition(0);
                        try {
                            FloatControl gain = (FloatControl) clipVozMaysa.getControl(FloatControl.Type.MASTER_GAIN);
                            int vol = Configuracoes.getInstance().getVolumeEfeitos();
                            if (vol <= 0) gain.setValue(gain.getMinimum());
                            else {
                                float maxDb = -10.0f;
                                float minDb = -35.0f;
                                gain.setValue(minDb + (maxDb - minDb) * (vol / 100.0f));
                            }
                        } catch (Exception ignorado) {}
                        clipVozMaysa.start();
                        Thread.sleep(75);
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        logVoz("Erro ao tocar blip Maysa: " + e.getMessage());
                    }
                }
            });
            threadVozMaysa.start();
        }
    }

    public static void pararVozMaysa() {
        synchronized (LOCK_VOZ_MAYSA) {
            tocandoVozMaysa = false;
            if (threadVozMaysa != null) {
                threadVozMaysa.interrupt();
                threadVozMaysa = null;
            }
            if (clipVozMaysa != null && clipVozMaysa.isRunning()) clipVozMaysa.stop();
        }
    }

    // Voz da personagem Victor (tocada quando ela fala, estilo Undertale)
    private static volatile Clip clipVozVictor;
    private static volatile boolean tocandoVozVictor = false;
    private static volatile Thread threadVozVictor = null;
    private static final Object LOCK_VOZ_VICTOR = new Object();

    private static void carregarClipVozVictor() {
        if (clipVozVictor != null && clipVozVictor.isOpen()) return;
        try {
            java.io.File f = new java.io.File(JogoAudrey.resolvePath("voz-victor.wav"));
            if (!f.exists()) return;
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(f);
            clipVozVictor = AudioSystem.getClip();
            clipVozVictor.open(audioStream);
        } catch (Exception e) {
            logVoz("Erro ao carregar voz do Victor: " + e.getMessage());
        }
    }

    public static void tocarVozVictor() {
        synchronized (LOCK_VOZ_VICTOR) {
            if (tocandoVozVictor && threadVozVictor != null && threadVozVictor.isAlive()) return;
            tocandoVozVictor = true;
            if (threadVozVictor != null && threadVozVictor.isAlive()) threadVozVictor.interrupt();
            threadVozVictor = new Thread(() -> {
                carregarClipVozVictor();
                if (clipVozVictor == null || !clipVozVictor.isOpen()) {
                    synchronized (LOCK_VOZ_VICTOR) { tocandoVozVictor = false; }
                    return;
                }
                while (tocandoVozVictor && !Thread.currentThread().isInterrupted()) {
                    try {
                        clipVozVictor.stop();
                        clipVozVictor.setFramePosition(0);
                        try {
                            FloatControl gain = (FloatControl) clipVozVictor.getControl(FloatControl.Type.MASTER_GAIN);
                            int vol = Configuracoes.getInstance().getVolumeEfeitos();
                            if (vol <= 0) gain.setValue(gain.getMinimum());
                            else {
                                float maxDb = -10.0f;
                                float minDb = -35.0f;
                                gain.setValue(minDb + (maxDb - minDb) * (vol / 100.0f));
                            }
                        } catch (Exception ignorado) {}
                        clipVozVictor.start();
                        Thread.sleep(75);
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        logVoz("Erro ao tocar blip Victor: " + e.getMessage());
                    }
                }
            });
            threadVozVictor.start();
        }
    }

    public static void pararVozVictor() {
        synchronized (LOCK_VOZ_VICTOR) {
            tocandoVozVictor = false;
            if (threadVozVictor != null) {
                threadVozVictor.interrupt();
                threadVozVictor = null;
            }
            if (clipVozVictor != null && clipVozVictor.isRunning()) clipVozVictor.stop();
        }
    }

    public static void pararTodasAsVozes() {
        pararVozNicolas();
        pararVozRaquel();
        pararVozAudrey();
        pararVozCamila();
        pararVozGabi();
        pararVozIvi();
        pararVozMaysa();
        pararVozVictor();
    }

    public static void tocarVoz(String personagem) {
        pararTodasAsVozes();
        if ("Nicollas".equalsIgnoreCase(personagem)) {
            tocarVozNicolas();
        } else if ("Raquel".equalsIgnoreCase(personagem)) {
            tocarVozRaquel();
        } else if ("Audrey".equalsIgnoreCase(personagem)) {
            tocarVozAudrey();
        } else if ("Camila".equalsIgnoreCase(personagem)) {
            tocarVozCamila();
        } else if ("Gabi".equalsIgnoreCase(personagem)) {
            tocarVozGabi();
        } else if ("Ivi".equalsIgnoreCase(personagem)) {
            tocarVozIvi();
        } else if ("Maysa".equalsIgnoreCase(personagem)) {
            tocarVozMaysa();
        } else if ("Victor".equalsIgnoreCase(personagem)) {
            tocarVozVictor();
        }
    }

    // Toca um arquivo .wav uma única vez
    private static void tocarWav(String caminho, float ganhoDb) {
        new Thread(() -> {
            try {
                java.io.File f = new java.io.File(JogoAudrey.resolvePath(caminho));
                if (!f.exists()) {
                    return;
                }
                AudioInputStream in = AudioSystem.getAudioInputStream(f);
                Clip clip = AudioSystem.getClip();
                clip.open(in);
                try {
                    FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    int vol = Configuracoes.getInstance().getVolumeEfeitos();
                    if (vol <= 0) {
                        gain.setValue(gain.getMinimum());
                    } else {
                        float base = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), ganhoDb));
                        gain.setValue(base + (gain.getMinimum() - base) * (1.0f - vol / 100.0f));
                    }
                } catch (Exception ignorado) {
                }
                clip.addLineListener(ev -> {
                    if (ev.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                clip.start();
            } catch (Exception e) {
                System.err.println("Erro ao tocar " + caminho + ": " + e.getMessage());
            }
        }).start();
    }

    // Som de coleta (item coletado)
    public static void tocarSomColeta() {
        new Thread(() -> {
            try {
                // Som alegre - tintilar de vidro/sino
                tocarNotaComRuido(900, 120, 0.4f);
                Thread.sleep(80);
                tocarNotaComRuido(1400, 150, 0.35f);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    // Som de diálogo/interação
    public static void tocarSomDialogo() {
        new Thread(() -> {
            try {
                tocarNota(500, 100);
                Thread.sleep(50);
                tocarNota(600, 100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    // Som de abertura de armário
    public static void tocarSomArmario() {
        new Thread(() -> {
            try {
                tocarNota(400, 200);
                Thread.sleep(100);
                tocarNota(600, 300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    // Som de sucesso/objetivo completado
    public static void tocarSomSucesso() {
        new Thread(() -> {
            try {
                tocarNota(800, 150);
                Thread.sleep(100);
                tocarNota(1000, 150);
                Thread.sleep(100);
                tocarNota(1200, 300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    // Som de erro/ação inválida
    public static void tocarSomErro() {
        new Thread(() -> {
            try {
                tocarNota(300, 100);
                Thread.sleep(50);
                tocarNota(200, 200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    // Método auxiliar para tocar uma nota simples
    private static void tocarNota(int frequencia, int duracao) {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            int numSamples = (SAMPLE_RATE * duracao) / 1000;
            byte[] buffer = new byte[numSamples * 2];

            double volMult = (Configuracoes.getInstance().getVolumeEfeitos() / 100.0) * 0.15; // Reduzido para ficar mais baixo que a música
            for (int i = 0; i < numSamples; i++) {
                double angle = 2.0 * Math.PI * frequencia * i / SAMPLE_RATE;
                // Amplitude com fade-out para evitar clicks
                double amplitude = 32767.0 * Math.sin(angle) * volMult;
                double envelope = Math.max(0, 1.0 - (double) i / (numSamples * 0.1));
                short sample = (short) (amplitude * envelope);

                buffer[i * 2] = (byte) (sample & 0xFF);
                buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
            }

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            line.write(buffer, 0, buffer.length);
            line.drain();
            line.close();
        } catch (LineUnavailableException e) {
            // Falha silenciosa se áudio não disponível
        }
    }

    // Som com ruído - para efeitos mais realistas
    private static void tocarNotaComRuido(int frequencia, int duracao, float nivelRuido) {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            int numSamples = (SAMPLE_RATE * duracao) / 1000;
            byte[] buffer = new byte[numSamples * 2];

            double volMult = (Configuracoes.getInstance().getVolumeEfeitos() / 100.0) * 0.15; // Reduzido para ficar mais baixo que a música
            for (int i = 0; i < numSamples; i++) {
                double angle = 2.0 * Math.PI * frequencia * i / SAMPLE_RATE;
                double nota = Math.sin(angle);
                double ruido = (Math.random() - 0.5) * 2;
                
                double amplitude = 32767.0 * ((nota * (1 - nivelRuido)) + (ruido * nivelRuido)) * volMult;
                double envelope = Math.max(0, 1.0 - (double) i / (numSamples * 0.15));
                short sample = (short) (amplitude * envelope);

                buffer[i * 2] = (byte) (sample & 0xFF);
                buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
            }

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            line.write(buffer, 0, buffer.length);
            line.drain();
            line.close();
        } catch (LineUnavailableException e) {
            // Falha silenciosa
        }
    }

    // Som áspero de madeira - creaking
    private static void tocarSomAspero(int frequencia, int duracao) {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            int numSamples = (SAMPLE_RATE * duracao) / 1000;
            byte[] buffer = new byte[numSamples * 2];

            double volMult = (Configuracoes.getInstance().getVolumeEfeitos() / 100.0) * 0.15; // Reduzido para ficar mais baixo que a música
            for (int i = 0; i < numSamples; i++) {
                // Onda dente de serra com muita variação para som de madeira
                double dente = 2.0 * ((double) i / SAMPLE_RATE * frequencia - Math.floor((double) i / SAMPLE_RATE * frequencia + 0.5));
                double amplitude = 32767.0 * dente * volMult;
                
                // Envelope mais longo para creaking
                double envelope = Math.max(0, 1.0 - (double) i / (numSamples * 0.3));
                short sample = (short) (amplitude * envelope);

                buffer[i * 2] = (byte) (sample & 0xFF);
                buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
            }

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            line.write(buffer, 0, buffer.length);
            line.drain();
            line.close();
        } catch (LineUnavailableException e) {
            // Falha silenciosa
        }
    }
}
