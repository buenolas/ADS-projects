import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String serverFilesDir;
    private DataInputStream in;
    private DataOutputStream out;

    public ClientHandler(Socket socket, String serverFilesDir) {
        this.clientSocket = socket;
        this.serverFilesDir = serverFilesDir;
        try {
            // Inicializa os streams de entrada e saída para comunicação com o cliente
            this.in = new DataInputStream(clientSocket.getInputStream());
            this.out = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.err.println("[ERRO] Erro ao obter streams do cliente: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Lê o comando enviado pelo cliente
                String commandLine = in.readUTF(); // Espera uma linha de comando
                String[] parts = commandLine.split(" ");
                String command = parts[0];

                String filename = null;
                long filesize = 0;

                if (parts.length > 1) {
                    filename = parts[1];
                }
                if (parts.length > 2) {
                    try {
                        filesize = Long.parseLong(parts[2]);
                    } catch (NumberFormatException e) {
                        System.err.println("[ERRO] Tamanho de arquivo inválido recebido: " + parts[2]);
                        sendResponse("ERROR Tamanho de arquivo inválido.");
                        continue;
                    }
                }

                System.out.println(String.format("[REQUISIÇÃO de %s:%d] Comando: %s, Arquivo: %s",
                        clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(), command, filename));

                switch (command) {
                    case "UPLOAD":
                        handleUpload(filename, filesize);
                        break;
                    case "LIST":
                        handleList();
                        break;
                    case "DOWNLOAD":
                        handleDownload(filename);
                        break;
                    default:
                        sendResponse("ERROR Comando desconhecido.");
                        break;
                }
            }
        } catch (EOFException e) {
            // Cliente desconectou normalmente
            System.out.println(String.format("[DESCONEXÃO] %s:%d desconectado.", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort()));
        } catch (IOException e) {
            System.err.println(String.format("[ERRO] %s:%d: %s", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(), e.getMessage()));
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("[ERRO] Erro ao fechar socket do cliente: " + e.getMessage());
            }
        }
    }

    private void handleUpload(String filename, long filesize) throws IOException {
        File file = new File(serverFilesDir, filename);
        try {
            sendResponse("OK"); // Confirma que está pronto para receber o arquivo

            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                long bytesReadTotal = 0;
                int bytesRead;
                while (bytesReadTotal < filesize && (bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, filesize - bytesReadTotal))) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    bytesReadTotal += bytesRead;
                }
                if (bytesReadTotal < filesize) {
                    throw new IOException("Conexão perdida durante o upload.");
                }
            }
            System.out.println("[UPLOAD] Arquivo " + filename + " recebido e salvo em " + file.getAbsolutePath());
        } catch (IOException e) {
            sendResponse("ERROR Erro ao receber arquivo: " + e.getMessage());
            System.err.println("[ERRO UPLOAD] " + e.getMessage());
            if (file.exists()) {
                file.delete(); // Remove arquivo parcial em caso de erro
            }
        }
    }

    private void handleList() throws IOException {
        File dir = new File(serverFilesDir);
        String[] files = dir.list();
        if (files == null) {
            files = new String[0];
        }
        // Converte a lista de arquivos para uma string separada por ';' para enviar ao cliente
        String fileList = String.join(";", files);
        sendResponse("OK " + fileList);
        System.out.println("[LIST] Lista de arquivos enviada: " + Arrays.asList(files));
    }

    private void handleDownload(String filename) throws IOException {
        File file = new File(serverFilesDir, filename);
        if (file.exists() && file.isFile()) {
            try {
                long filesize = file.length();
                // Envia o cabeçalho com o nome e tamanho do arquivo
                sendResponse("OK " + filename + " " + filesize);

                // Envia o arquivo em chunks
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                System.out.println("[DOWNLOAD] Arquivo " + filename + " enviado com sucesso.");
            } catch (IOException e) {
                System.err.println("[ERRO DOWNLOAD] " + e.getMessage());
                sendResponse("ERROR Erro ao enviar arquivo: " + e.getMessage());
            }
        } else {
            sendResponse("ERROR Arquivo " + filename + " não encontrado no servidor.");
            System.err.println("[DOWNLOAD] Arquivo " + filename + " não encontrado.");
        }
    }

    // Envia uma resposta de string para o cliente
    private void sendResponse(String response) throws IOException {
        out.writeUTF(response);
        out.flush();
    }
}


