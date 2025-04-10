
import java.io.*;
import java.net.*;

public class Cliente {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void conectar(String host, int porta) throws IOException {
        socket = new Socket(host, porta);
        System.out.println("Conectado ao servidor em " + host + ":" + porta);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void enviar(String mensagem) {
        out.println(mensagem);
    }

    public String receber() throws IOException {
        return in.readLine();
    }

    public void fechar() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}
