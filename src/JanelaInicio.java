import javax.swing.*;
import java.awt.*;

public class JanelaInicio extends JFrame {
    private JRadioButton servidorRadio;
    private JRadioButton clienteRadio;
    private JTextField campoIP;
    private JTextField campoPorta;
    private JTextField campoNome;
    private JButton botaoIniciar;

    public JanelaInicio() {

        try {
            ImageIcon icone = new ImageIcon(getClass().getResource("/recursos/icone.png"));
            setIconImage(icone.getImage());
        } catch (Exception e) {
            System.out.println("Ícone não encontrado");
        }

        setTitle("Configuração da Conexão");
        setSize(350, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2));

        servidorRadio = new JRadioButton("Servidor");
        clienteRadio = new JRadioButton("Cliente");
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(servidorRadio);
        grupo.add(clienteRadio);

        campoIP = new JTextField();
        campoPorta = new JTextField("12345");
        campoNome = new JTextField();

        botaoIniciar = new JButton("Iniciar");

        add(new JLabel("Modo:"));
        JPanel painelModo = new JPanel();
        painelModo.add(servidorRadio);
        painelModo.add(clienteRadio);
        add(painelModo);

        add(new JLabel("Seu nome:"));
        add(campoNome);

        add(new JLabel("IP do servidor:"));
        add(campoIP);

        add(new JLabel("Porta:"));
        add(campoPorta);

        add(new JLabel(""));
        add(botaoIniciar);

        //campoIP.setEnabled(false); // desativa IP por padrão

        servidorRadio.addActionListener(e -> campoIP.setEnabled(true));
        clienteRadio.addActionListener(e -> campoIP.setEnabled(true));

        botaoIniciar.addActionListener(e -> iniciarJogo());

        setVisible(true);
    }

    private void iniciarJogo() {
        boolean ehServidor = servidorRadio.isSelected();
        String nome = campoNome.getText().trim();
        String ip = campoIP.getText().trim();
        String portaTexto = campoPorta.getText().trim();

        if (!ehServidor && !clienteRadio.isSelected()) {
            JOptionPane.showMessageDialog(this, "Escolha se é Cliente ou Servidor.");
            return;
        }

        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe seu nome.");
            return;
        }

        int porta;
        try {
            porta = Integer.parseInt(portaTexto);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Porta inválida.");
            return;
        }

        if (!ehServidor && ip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o IP do servidor.");
            return;
        }

        dispose(); // fecha a janela

        SwingUtilities.invokeLater(() -> {
            JogoOnline jogo = new JogoOnline(ehServidor, ip, porta, nome);
            jogo.setVisible(true);
        });
    }

    public static void main(String[] args) {
        new JanelaInicio();
    }
}
