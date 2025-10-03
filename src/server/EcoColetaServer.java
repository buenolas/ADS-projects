package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EcoColetaServer {
    private static final int PORT = 12345;
    private List<PontoColeta> pontosColeta;

    public EcoColetaServer() {
        PontoColeta.resetIdCounter(); // Resetar o contador de IDs ao iniciar o servidor
        pontosColeta = new ArrayList<>();
        // Adicionar alguns pontos de coleta de exemplo
        pontosColeta.add(new PontoColeta("Ponto Central", "Rua Principal, 123", Arrays.asList("Plástico", "Papel")));
        pontosColeta.add(new PontoColeta("Ponto Leste", "Av. Leste, 456", Arrays.asList("Papel", "Vidro")));
        pontosColeta.add(new PontoColeta("Ponto Oeste", "Travessa Oeste, 789", Arrays.asList("Vidro", "Metal")));
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor EcoColeta iniciado na porta " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());

                String command = (String) in.readObject();
                String userRole = (String) in.readObject(); // RNF3: Recebe o papel do usuário

                switch (command) {
                    case "ADICIONAR": // RF1
                        if ("ADMIN".equalsIgnoreCase(userRole)) {
                            PontoColeta novoPonto = (PontoColeta) in.readObject();
                            pontosColeta.add(novoPonto);
                            out.writeObject("Ponto de coleta adicionado com sucesso! ID: " + novoPonto.getId());
                            System.out.println("Novo ponto adicionado: " + novoPonto.getNome() + " (ID: " + novoPonto.getId() + ")");
                        } else {
                            out.writeObject("Acesso negado. Apenas administradores podem adicionar pontos.");
                        }
                        break;
                    case "LISTAR": // RF2
                        out.writeObject(pontosColeta);
                        System.out.println("Lista de pontos enviada ao cliente.");
                        break;
                    case "BUSCAR_RESIDUO": // RF3
                        String tipoResiduoBusca = (String) in.readObject();
                        List<PontoColeta> resultadosBusca = pontosColeta.stream()
                                .filter(p -> p.aceitaResiduo(tipoResiduoBusca))
                                .collect(Collectors.toList());
                        out.writeObject(resultadosBusca);
                        System.out.println("Resultados da busca por resíduo \'" + tipoResiduoBusca + "\' enviada ao cliente.");
                        break;
                    case "ATUALIZAR": // RF4
                        if ("ADMIN".equalsIgnoreCase(userRole)) {
                            PontoColeta pontoAtualizado = (PontoColeta) in.readObject();
                            boolean encontrado = false;
                            for (int i = 0; i < pontosColeta.size(); i++) {
                                if (pontosColeta.get(i).getId() == pontoAtualizado.getId()) {
                                    pontosColeta.set(i, pontoAtualizado);
                                    encontrado = true;
                                    break;
                                }
                            }
                            if (encontrado) {
                                out.writeObject("Ponto de coleta ID " + pontoAtualizado.getId() + " atualizado com sucesso!");
                                System.out.println("Ponto atualizado: " + pontoAtualizado.getNome() + " (ID: " + pontoAtualizado.getId() + ")");
                            } else {
                                out.writeObject("Ponto de coleta ID " + pontoAtualizado.getId() + " não encontrado.");
                            }
                        } else {
                            out.writeObject("Acesso negado. Apenas administradores podem atualizar pontos.");
                        }
                        break;
                    case "EXCLUIR": // RF5
                        if ("ADMIN".equalsIgnoreCase(userRole)) {
                            Long idExcluir = (Long) in.readObject();
                            boolean removido = pontosColeta.removeIf(p -> p.getId() == idExcluir);
                            if (removido) {
                                out.writeObject("Ponto de coleta ID " + idExcluir + " removido com sucesso!");
                                System.out.println("Ponto removido: ID " + idExcluir);
                            } else {
                                out.writeObject("Ponto de coleta ID " + idExcluir + " não encontrado.");
                            }
                        } else {
                            out.writeObject("Acesso negado. Apenas administradores podem excluir pontos.");
                        }
                        break;
                    default:
                        out.writeObject("Comando desconhecido.");
                        break;
                }

            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Erro na comunicação com o cliente: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Erro ao fechar o socket do cliente: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        new EcoColetaServer().start();
    }
}

