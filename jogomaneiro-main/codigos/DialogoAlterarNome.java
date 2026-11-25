import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Diálogo modal Scrapbook / Bullet-Journal para alterar o nome de usuário.
 * Totalmente integrado com a identidade visual do jogo:
 * tons pastéis, bordas arredondadas, fita washi tape e estrelinhas.
 */
public class DialogoAlterarNome extends JDialog {

    private String novoNome = null;
    private JTextField txtEntrada;
    private boolean confirmado = false;

    public DialogoAlterarNome(JFrame parent, String nomeAtual, Font fontTitulo, Font fontCabin) {
        super(parent, "Alterar Nome de Aventureiro", true);
        setUndecorated(true);
        setSize(380, 220);
        setLocationRelativeTo(parent);
        setBackground(new Color(0, 0, 0, 0));

        JPanel painel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Sombra suave multicamadas
                g2.setColor(new Color(0, 0, 0, 25));
                g2.fillRoundRect(6, 6, w - 12, h - 12, 28, 28);
                g2.setColor(new Color(0, 0, 0, 15));
                g2.fillRoundRect(3, 3, w - 6, h - 6, 28, 28);

                // Fundo creme suave estilo caderno/scrapbook
                GradientPaint bg = new GradientPaint(0, 0, new Color(255, 252, 240), 0, h, new Color(252, 246, 225));
                g2.setPaint(bg);
                g2.fillRoundRect(0, 0, w - 8, h - 8, 26, 26);

                // Borda dourada suave
                g2.setColor(new Color(230, 200, 120));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(0, 0, w - 8, h - 8, 26, 26);

                // Costura interna tracejada
                g2.setColor(new Color(215, 190, 120, 140));
                g2.setStroke(new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{4, 4}, 0));
                g2.drawRoundRect(6, 6, w - 20, h - 20, 20, 20);

                // Washi tape decorativa no topo
                g2.setColor(new Color(255, 205, 215, 210));
                g2.fillRect((w - 90) / 2, 0, 90, 14);
                g2.setColor(new Color(255, 255, 255, 80));
                g2.setStroke(new BasicStroke(0.6f));
                for (int i = 0; i < 90; i += 6) {
                    g2.drawLine((w - 90) / 2 + i, 0, (w - 90) / 2 + i + 3, 14);
                }

                // Estrelinhas cintilantes nos cantos
                g2.setColor(new Color(255, 210, 70));
                g2.fillOval(18, 22, 6, 6);
                g2.fillOval(w - 32, 22, 6, 6);

                g2.dispose();
            }
        };
        painel.setOpaque(false);

        // Título estilizado
        JLabel lblTitulo = new JLabel("✦ Digite seu novo nome ✦", SwingConstants.CENTER);
        lblTitulo.setFont(fontCabin != null ? fontCabin.deriveFont(Font.BOLD, 17f) : new Font("SansSerif", Font.BOLD, 17));
        lblTitulo.setForeground(new Color(75, 60, 30));
        lblTitulo.setBounds(10, 24, 355, 28);
        painel.add(lblTitulo);

        // Subtítulo descritivo
        JLabel lblSub = new JLabel("Como quer ser chamado em sua jornada?", SwingConstants.CENTER);
        lblSub.setFont(fontCabin != null ? fontCabin.deriveFont(Font.ITALIC, 12f) : new Font("SansSerif", Font.PLAIN, 12));
        lblSub.setForeground(new Color(130, 115, 90));
        lblSub.setBounds(10, 52, 355, 20);
        painel.add(lblSub);

        // Campo de entrada estilizado
        txtEntrada = new JTextField(nomeAtual != null ? nomeAtual : "") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(210, 190, 140));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        txtEntrada.setFont(fontCabin != null ? fontCabin.deriveFont(Font.BOLD, 15f) : new Font("SansSerif", Font.BOLD, 15));
        txtEntrada.setForeground(new Color(50, 45, 35));
        txtEntrada.setCaretColor(new Color(110, 80, 160));
        txtEntrada.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        txtEntrada.setOpaque(false);
        txtEntrada.setBounds(35, 82, 305, 36);
        txtEntrada.selectAll();
        painel.add(txtEntrada);

        // Botão Salvar / Confirmar
        JButton btnSalvar = new JButton("Confirmar") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(255, 220, 110), 0, getHeight(), new Color(245, 185, 75));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(205, 150, 45));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(new Color(65, 45, 15));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnSalvar.setFont(fontCabin != null ? fontCabin.deriveFont(Font.BOLD, 14f) : new Font("SansSerif", Font.BOLD, 14));
        btnSalvar.setContentAreaFilled(false);
        btnSalvar.setBorderPainted(false);
        btnSalvar.setFocusPainted(false);
        btnSalvar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalvar.setBounds(195, 138, 145, 38);
        btnSalvar.addActionListener(e -> acaoConfirmar());
        painel.add(btnSalvar);

        // Botão Cancelar
        JButton btnCancelar = new JButton("Cancelar") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(242, 240, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(205, 200, 190));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(new Color(110, 105, 95));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnCancelar.setFont(fontCabin != null ? fontCabin.deriveFont(Font.BOLD, 14f) : new Font("SansSerif", Font.BOLD, 14));
        btnCancelar.setContentAreaFilled(false);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.setBounds(35, 138, 145, 38);
        btnCancelar.addActionListener(e -> dispose());
        painel.add(btnCancelar);

        // Pressionar ENTER confirma e ESC cancela
        txtEntrada.addActionListener(e -> acaoConfirmar());
        painel.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        setContentPane(painel);
    }

    private void acaoConfirmar() {
        String t = txtEntrada.getText().trim();
        if (!t.isEmpty()) {
            novoNome = t;
            confirmado = true;
            dispose();
        }
    }

    public static String exibir(JFrame parent, String nomeAtual, Font fontTitulo, Font fontCabin) {
        DialogoAlterarNome diag = new DialogoAlterarNome(parent, nomeAtual, fontTitulo, fontCabin);
        diag.setVisible(true);
        return diag.confirmado ? diag.novoNome : null;
    }
}
