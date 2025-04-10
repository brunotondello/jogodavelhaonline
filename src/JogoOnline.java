import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class JogoOnline extends JFrame {
    private Rede rede;
    private boolean minhaVez;
    private String simbolo, simboloAdversario;
    private String nomeJogador;
    private boolean solicitouReset = false;
    private boolean confirmouReset = false;
    private boolean recebeuPedidoReset = false;

    private JButton[] botoes = new JButton[9];
    private JLabel labelIdentidade, labelTurno, labelHistorico;
    private JButton botaoReset;

    private int vitoriasX = 0;
    private int vitoriasO = 0;
    private int empates = 0;

    // Chat
    private JTextArea areaChat;
    private JTextField campoMensagem;
    private JButton botaoEnviar;

    public JogoOnline(boolean ehServidor, String host, int porta, String nomeJogador) {
        super("Jogo da Velha Online");

        this.nomeJogador = nomeJogador;
        if (nomeJogador == null || nomeJogador.trim().isEmpty()) nomeJogador = "Jogador";

        simbolo = ehServidor ? "X" : "O";
        simboloAdversario = ehServidor ? "O" : "X";
        minhaVez = ehServidor;

        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 🎨 Cores modernas
        Color fundo = new Color(240, 248, 255); // AliceBlue
        Color corBotao = new Color(224, 255, 255); // LightCyan
        Color corTexto = new Color(25, 25, 112);  // MidnightBlue
        Color corBorda = new Color(176, 196, 222); // LightSteelBlue

        getContentPane().setBackground(fundo);

        // 🔝 Painel topo
        JPanel painelTopo = new JPanel(new GridLayout(3, 1));
        painelTopo.setBackground(fundo);

        labelIdentidade = new JLabel(nomeJogador + " (" + simbolo + ")", SwingConstants.CENTER);
        labelTurno = new JLabel("Vez de: " + (minhaVez ? "Você" : "Oponente"), SwingConstants.CENTER);
        labelHistorico = new JLabel(getTextoHistorico(), SwingConstants.CENTER);

        Font fonteInfo = new Font("Segoe UI", Font.BOLD, 14);
        labelIdentidade.setFont(fonteInfo);
        labelTurno.setFont(fonteInfo);
        labelHistorico.setFont(fonteInfo);

        labelIdentidade.setForeground(corTexto);
        labelTurno.setForeground(corTexto);
        labelHistorico.setForeground(corTexto);

        painelTopo.add(labelIdentidade);
        painelTopo.add(labelTurno);
        painelTopo.add(labelHistorico);
        add(painelTopo, BorderLayout.NORTH);

        // 🎮 Tabuleiro
        JPanel painelJogo = new JPanel(new GridLayout(3, 3));
        painelJogo.setBackground(fundo);

        Color corBotaoNormal = new Color(224, 255, 255);  // LightCyan
        Color corHover = new Color(200, 240, 255);        // Hover suave

        for (int i = 0; i < 9; i++) {
            final int pos = i;
            botoes[i] = new JButton("");
            botoes[i].setFont(new Font("Arial", Font.BOLD, 50));
            botoes[i].setBackground(corBotaoNormal);
            botoes[i].setFocusPainted(false);
            botoes[i].setBorder(BorderFactory.createLineBorder(new Color(176, 196, 222)));

            botoes[i].addActionListener(e -> fazerJogada(pos));

            // 🎨 Efeito Hover
            botoes[i].addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (botoes[pos].isEnabled() && botoes[pos].getText().isEmpty()) {
                        botoes[pos].setBackground(corHover);
                    }
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (botoes[pos].isEnabled() && botoes[pos].getText().isEmpty()) {
                        botoes[pos].setBackground(corBotaoNormal);
                    }
                }
            });

            painelJogo.add(botoes[i]);
        }
        add(painelJogo, BorderLayout.CENTER);

        // 🔁 Botão reinício
        botaoReset = new JButton("Jogar Novamente");
        botaoReset.setFont(new Font("Segoe UI", Font.BOLD, 13));
        botaoReset.setBackground(new Color(173, 216, 230)); // LightBlue
        botaoReset.setForeground(corTexto);
        botaoReset.setFocusPainted(false);

        botaoReset.addActionListener(e -> {
            if (!solicitouReset) {
                solicitouReset = true;
                rede.enviar("PEDIR_RESET");

                if (recebeuPedidoReset) {
                    rede.enviar("RESET_EXECUTAR");
                    executarReset();
                }
            }
        });
        add(botaoReset, BorderLayout.SOUTH);

        // 🔻 Painel do chat
        JPanel painelChat = new JPanel(new BorderLayout());
        areaChat = new JTextArea(6, 20);
        areaChat.setEditable(false);
        areaChat.setLineWrap(true);
        areaChat.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(areaChat);

        campoMensagem = new JTextField();
        botaoEnviar = new JButton("Enviar");

// Envio ao clicar ou apertar Enter
        botaoEnviar.addActionListener(e -> enviarMensagem());
        campoMensagem.addActionListener(e -> enviarMensagem());

        JPanel painelInput = new JPanel(new BorderLayout());
        painelInput.add(campoMensagem, BorderLayout.CENTER);
        painelInput.add(botaoEnviar, BorderLayout.EAST);

        painelChat.add(scroll, BorderLayout.CENTER);
        painelChat.add(painelInput, BorderLayout.SOUTH);
        add(painelChat, BorderLayout.EAST); // Você pode mudar para SOUTH se quiser embaixo do tabuleiro

        // 🌐 Conexão
        try {
            rede = new Rede(ehServidor);
            rede.iniciar(host, porta);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao iniciar conexão!");
            System.exit(1);
        }

        // 📡 Escuta rede
        new Thread(() -> {
            while (true) {
                try {
                    String msg = rede.receber();
                    if (msg == null) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "O oponente se desconectou.", "Desconectado", JOptionPane.WARNING_MESSAGE);
                            dispose(); // Fecha a janela de forma segura
                            System.exit(0); // Opcional: encerra todo o processo
                        });
                        break;
                    }

                    if (msg.equals("PEDIR_RESET")) {
                        recebeuPedidoReset = true;
                        int opcao = JOptionPane.showConfirmDialog(this, "O oponente quer reiniciar. Aceitar?", "Reinício", JOptionPane.YES_NO_OPTION);
                        if (opcao == JOptionPane.YES_OPTION) {
                            confirmouReset = true;
                            rede.enviar("RESET_CONFIRMADO");

                            if (solicitouReset) {
                                executarReset();
                            }
                        }
                        continue;
                    }

                    if (msg.equals("RESET_CONFIRMADO")) {
                        confirmouReset = true;
                        if (solicitouReset) {
                            rede.enviar("RESET_EXECUTAR");
                            executarReset();
                        }
                        continue;
                    }

                    if (msg.equals("RESET_EXECUTAR")) {
                        executarReset();
                        continue;
                    }

                    if (msg.startsWith("CHAT:")) {
                        String conteudo = msg.substring(5); // remove o "CHAT:"
                        SwingUtilities.invokeLater(() -> mostrarMensagemChat(conteudo));
                        continue;
                    }

                    int pos = Integer.parseInt(msg);
                    SwingUtilities.invokeLater(() -> {
                        botoes[pos].setText(simboloAdversario);
                        botoes[pos].setForeground(simboloAdversario.equals("X") ? new Color(0, 120, 215) : new Color(220, 50, 47));
                        botoes[pos].setEnabled(false);
                        minhaVez = true;
                        labelTurno.setText("Vez de: Você");

                        if (verificarVitoria(simboloAdversario)) {
                            JOptionPane.showMessageDialog(this, "Você perdeu!");
                            vitoriaAdversario();
                            desabilitarBotoes();
                        } else if (verificarEmpate()) {
                            JOptionPane.showMessageDialog(this, "Empate!");
                            empate();
                            desabilitarBotoes();
                        }
                    });

                } catch (IOException | NumberFormatException e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "O oponente se desconectou da partida.",
                                "Desconectado",
                                JOptionPane.WARNING_MESSAGE);
                        dispose(); // Fecha só a janela
                        System.exit(0); // Encerra o processo todo (opcional)
                    });
                    break;
                }
            }
        }).start();
    }

    private void fazerJogada(int pos) {
        if (!minhaVez || !botoes[pos].isEnabled()) return;

        botoes[pos].setText(simbolo);
        botoes[pos].setForeground(simbolo.equals("X") ? new Color(0, 120, 215) : new Color(220, 50, 47));
        botoes[pos].setEnabled(false);
        rede.enviar(String.valueOf(pos));
        minhaVez = false;
        labelTurno.setText("Vez de: Oponente");

        if (verificarVitoria(simbolo)) {
            JOptionPane.showMessageDialog(this, "Você venceu!");
            vitoriaJogador();
            desabilitarBotoes();
        } else if (verificarEmpate()) {
            JOptionPane.showMessageDialog(this, "Empate!");
            empate();
            desabilitarBotoes();
        }
    }

    private void executarReset() {
        reiniciarJogo();
        solicitouReset = false;
        confirmouReset = false;
        recebeuPedidoReset = false;
    }

    private void reiniciarJogo() {
        for (JButton b : botoes) {
            b.setText("");
            b.setEnabled(true);
            b.setBackground(new Color(224, 255, 255)); // LightCyan
        }

        minhaVez = simbolo.equals("X");
        labelTurno.setText("Vez de: " + (minhaVez ? "Você" : "Oponente"));
    }

    private boolean verificarVitoria(String s) {
        int[][] win = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6}
        };
        for (int[] c : win) {
            if (botoes[c[0]].getText().equals(s) &&
                    botoes[c[1]].getText().equals(s) &&
                    botoes[c[2]].getText().equals(s)) return true;
        }
        return false;
    }

    private boolean verificarEmpate() {
        for (JButton b : botoes) {
            if (b.getText().isEmpty()) return false;
        }
        return true;
    }

    private void desabilitarBotoes() {
        for (JButton b : botoes) b.setEnabled(false);
    }

    private void atualizarHistorico() {
        String txt = "Vitórias " + nomeJogador + " (" + simbolo + "): " +
                (simbolo.equals("X") ? vitoriasX : vitoriasO) +
                " | Oponente (" + simboloAdversario + "): " +
                (simboloAdversario.equals("X") ? vitoriasX : vitoriasO) +
                " | Empates: " + empates;
        labelHistorico.setText(txt);
    }

    private String getTextoHistorico() {
        return "Vitórias " + nomeJogador + " (" + simbolo + "): 0 | Oponente (" + simboloAdversario + "): 0 | Empates: 0";
    }

    private void vitoriaJogador() {
        if (simbolo.equals("X")) vitoriasX++; else vitoriasO++;
        atualizarHistorico();
    }

    private void vitoriaAdversario() {
        if (simboloAdversario.equals("X")) vitoriasX++; else vitoriasO++;
        atualizarHistorico();
    }

    private void empate() {
        empates++;
        atualizarHistorico();
    }

    private void enviarMensagem() {
        String texto = campoMensagem.getText().trim();
        if (!texto.isEmpty()) {
            String mensagem = "CHAT:" + nomeJogador + ": " + texto;
            rede.enviar(mensagem);
            mostrarMensagemChat("Você: " + texto);
            campoMensagem.setText("");
        }
    }

    private void mostrarMensagemChat(String msg) {
        areaChat.append(msg + "\n");
        areaChat.setCaretPosition(areaChat.getDocument().getLength());
    }
}
