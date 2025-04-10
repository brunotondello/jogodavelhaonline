
import java.io.IOException;

public class Rede {
    private Servidor servidor;
    private Cliente cliente;
    private boolean ehServidor;

    public Rede(boolean ehServidor) {
        this.ehServidor = ehServidor;
    }

    public void iniciar(String host, int porta) throws IOException {
        if (ehServidor) {
            servidor = new Servidor();
            servidor.iniciarServidor(porta);
        } else {
            cliente = new Cliente();
            cliente.conectar(host, porta);
        }
    }

    public void enviar(String mensagem) {
        if (ehServidor) {
            System.out.println("Enviando: " + mensagem); // <- debug!

            servidor.enviar(mensagem);
        } else {
            cliente.enviar(mensagem);
        }
    }

    public String receber() throws IOException {
        return ehServidor ? servidor.receber() : cliente.receber();
    }

    public void fechar() throws IOException {
        if (ehServidor) {
            servidor.fechar();
        } else {
            cliente.fechar();
        }
    }
}
