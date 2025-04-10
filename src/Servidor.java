import java.io.*;
import java.net.*;

public class Servidor {
    private ServerSocket serverSocket;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void iniciarServidor(int porta) throws IOException {
        serverSocket = new ServerSocket(porta);
        System.out.println("Servidor aguardando conexão na porta " + porta + "...");
        socket = serverSocket.accept();
        System.out.println("Cliente conectado!");

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
        serverSocket.close();
    }
}
