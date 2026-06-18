import javax.sound.sampled.*;

public class GerenciadorAudio {
    private static final int SAMPLE_RATE = 44100;
    private static Clip clipFundo;

    public static void tocarMusicaFundo() {
        new Thread(() -> {
            try {
                java.io.File audioFile = new java.io.File("bgm.wav");
                if (audioFile.exists()) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                    clipFundo = AudioSystem.getClip();
                    clipFundo.open(audioStream);
                    atualizarVolumeMusica(Configuracoes.getInstance().getVolumeEfeitos());
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

    public static void atualizarVolumeMusica(int volumeEfeitos) {
        if (clipFundo != null && clipFundo.isOpen()) {
            try {
                FloatControl gainControl = (FloatControl) clipFundo.getControl(FloatControl.Type.MASTER_GAIN);
                if (volumeEfeitos <= 0) {
                    gainControl.setValue(gainControl.getMinimum());
                } else {
                    float min = -35.0f;
                    float max = -10.0f;
                    float db = min + ((max - min) * (volumeEfeitos / 100.0f));
                    gainControl.setValue(db);
                }
            } catch (Exception e) {
                // Ignore if control not supported
            }
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
