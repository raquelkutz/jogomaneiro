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
                                float maxDb = gain.getMaximum();
                                float minDb = -18.0f;
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
                                float maxDb = gain.getMaximum();
                                float minDb = -18.0f;
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
