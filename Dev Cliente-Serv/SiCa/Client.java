import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 65432;
    private static final String CLIENT_FILES_DIR = "client_files";

    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;

    public Client() {
        // Garante que o diretório de arquivos do cliente exista
        File directory = new File(CLIENT_FILES_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public void start() {
        try {
            clientSocket = new Socket(HOST, PORT);
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            System.out.println("Conectado ao servidor em " + HOST + ":" + PORT);

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("\n--- MENU SiCA ---");
                System.out.println("1. Enviar arquivo (UPLOAD)");
                System.out.println("2. Listar arquivos no servidor (LIST)");
                System.out.println("3. Baixar arquivo (DOWNLOAD)");
                System.out.println("4. Sair");
                System.out.print("Escolha uma opção: ");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        System.out.print("Digite o caminho completo do arquivo para enviar: ");
                        String filepath = scanner.nextLine();
                        File fileToUpload = new File(filepath);
                        if (fileToUpload.exists() && fileToUpload.isFile()) {
                            uploadFile(fileToUpload);
                        } else {
                            System.out.println("Arquivo não encontrado.");
                        }
                        break;
                    case "2":
                        listFiles();
                        break;
                    case "3":
                        System.out.print("Digite o nome do arquivo para baixar: ");
                        String filenameToDownload = scanner.nextLine();
                        downloadFile(filenameToDownload);
                        break;
                    case "4":
                        System.out.println("Saindo...");
                        return;
                    default:
                        System.out.println("Opção inválida. Tente novamente.");
                }
            }
        } catch (IOException e) {
            System.err.println("Não foi possível conectar ao servidor em " + HOST + ":" + PORT + ". Verifique se o servidor está em execução.");
            System.err.println("Erro: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                    System.out.println("Conexão com o servidor fechada.");
                }
            } catch (IOException e) {
                System.err.println("Erro ao fechar o socket do cliente: " + e.getMessage());
            }
        }
    }

    private void sendCommand(String command) throws IOException {
        out.writeUTF(command);
        out.flush();
    }

    private String readResponse() throws IOException {
        return in.readUTF();
    }

    private void uploadFile(File file) throws IOException {
        String filename = file.getName();
        long filesize = file.length();

        // Envia o comando de UPLOAD com os metadados do arquivo
        sendCommand("UPLOAD " + filename + " " + filesize);

        // Espera a confirmação do servidor
        String response = readResponse();

        if (response.startsWith("OK")) {
            System.out.println("[UPLOAD] Servidor pronto para receber. Enviando " + filename + "...");
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }
            System.out.println("[UPLOAD] Arquivo " + filename + " enviado com sucesso.");
        } else {
            System.out.println("[ERRO UPLOAD] Servidor retornou erro: " + response.substring(6)); // Remove "ERROR "
        }
    }

    private void listFiles() throws IOException {
        sendCommand("LIST");

        String response = readResponse();

        if (response.startsWith("OK")) {
            System.out.println("[LISTA DE ARQUIVOS NO SERVIDOR]");
            String fileListString = response.substring(3); // Remove "OK "
            if (!fileListString.isEmpty()) {
                String[] files = fileListString.split(";");
                for (String f : files) {
                    System.out.println("- " + f);
                }
            } else {
                System.out.println("Nenhum arquivo encontrado.");
            }
        } else {
            System.out.println("[ERRO LIST] Servidor retornou erro: " + response.substring(6)); // Remove "ERROR "
        }
    }

    private void downloadFile(String filename) throws IOException {
        sendCommand("DOWNLOAD " + filename);

        String response = readResponse();

        if (response.startsWith("OK")) {
            String[] parts = response.split(" ");
            if (parts.length < 3) {
                System.out.println("[ERRO DOWNLOAD] Resposta inválida do servidor.");
                return;
            }
            String receivedFilename = parts[1];
            long filesize = Long.parseLong(parts[2]);
            File file = new File(CLIENT_FILES_DIR, receivedFilename);

            System.out.println("[DOWNLOAD] Baixando " + receivedFilename + " (" + filesize + " bytes)...");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                long bytesReceived = 0;
                int bytesRead;
                while (bytesReceived < filesize && (bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, filesize - bytesReceived))) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    bytesReceived += bytesRead;
                }
                if (bytesReceived < filesize) {
                    throw new IOException("Conexão perdida durante o download.");
                }
            }
            System.out.println("[DOWNLOAD] Arquivo " + receivedFilename + " baixado com sucesso em " + file.getAbsolutePath());
        } else {
            System.out.println("[ERRO DOWNLOAD] Servidor retornou erro: " + response.substring(6)); // Remove "ERROR "
        }
    }

    public static void main(String[] args) {
        new Client().start();
    }
}


