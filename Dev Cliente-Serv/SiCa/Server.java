import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 65432;
    private static final String SERVER_FILES_DIR = "server_files";
    private static final int MAX_THREADS = 10;

    public static void main(String[] args) {
        // Garante que o diretório de arquivos do servidor exista
        File directory = new File(SERVER_FILES_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Cria um pool de threads para lidar com múltiplos clientes
        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[ESCUTANDO] Servidor escutando em 127.0.0.1:" + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[CONEXÃO] " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + " conectado.");
                // Submete a tarefa de lidar com o cliente a uma thread do pool
                pool.execute(new ClientHandler(clientSocket, SERVER_FILES_DIR));
            }
        } catch (IOException e) {
            System.err.println("[ERRO] Erro ao iniciar o servidor: " + e.getMessage());
        } finally {
            // Desliga o pool de threads quando o servidor for encerrado
            pool.shutdown();
        }
    }
}


