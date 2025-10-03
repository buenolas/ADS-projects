package client;

import server.PontoColeta;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class EcoColetaClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static String userRole = "CIDADAO"; // RNF3: Papel padrão do usuário

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n--- Menu EcoColeta ---");
            System.out.println("1. Adicionar Ponto de Coleta (Admin)");
            System.out.println("2. Listar Pontos de Coleta");
            System.out.println("3. Buscar Pontos por Tipo de Resíduo");
            System.out.println("4. Atualizar Ponto de Coleta (Admin)");
            System.out.println("5. Excluir Ponto de Coleta (Admin)");
            System.out.println("6. Mudar Perfil (Atual: " + userRole + ")");
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Por favor, digite um número.");
                choice = -1; // Opção inválida para continuar o loop
            }

            switch (choice) {
                case 1:
                    adicionarPonto(scanner);
                    break;
                case 2:
                    listarPontos();
                    break;
                case 3:
                    buscarPontosPorResiduo(scanner);
                    break;
                case 4:
                    atualizarPonto(scanner);
                    break;
                case 5:
                    excluirPonto(scanner);
                    break;
                case 6:
                    mudarPerfil(scanner);
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        } while (choice != 0);

        scanner.close();
    }

    private static void enviarComando(String command, Object data) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(command);
            out.writeObject(userRole); // RNF3: Envia o papel do usuário
            if (data != null) {
                out.writeObject(data);
            }

            Object response = in.readObject();
            if (response instanceof String) {
                System.out.println("Resposta do servidor: " + response);
            } else if (response instanceof List) {
                List<PontoColeta> pontos = (List<PontoColeta>) response;
                if (pontos.isEmpty()) {
                    System.out.println("Nenhum ponto de coleta encontrado.");
                } else {
                    System.out.println("--- Pontos de Coleta ---");
                    pontos.forEach(System.out::println);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro na comunicação com o servidor: " + e.getMessage());
        }
    }

    private static void adicionarPonto(Scanner scanner) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            System.out.println("Acesso negado. Apenas administradores podem adicionar pontos.");
            return;
        }
        System.out.println("\n--- Adicionar Novo Ponto de Coleta ---");
        System.out.print("Nome do Ponto: ");
        String nome = scanner.nextLine();
        System.out.print("Endereço: ");
        String endereco = scanner.nextLine();
        System.out.print("Tipos de Resíduo (separados por vírgula, ex: Plástico, Papel): ");
        List<String> tiposResiduos = Arrays.asList(scanner.nextLine().split(",\s*"));

        PontoColeta novoPonto = new PontoColeta(nome, endereco, tiposResiduos);
        enviarComando("ADICIONAR", novoPonto);
    }

    private static void listarPontos() {
        enviarComando("LISTAR", null);
    }

    private static void buscarPontosPorResiduo(Scanner scanner) {
        System.out.println("\n--- Buscar Pontos por Tipo de Resíduo ---");
        System.out.print("Digite o tipo de resíduo para buscar: ");
        String tipoResiduo = scanner.nextLine();
        enviarComando("BUSCAR_RESIDUO", tipoResiduo);
    }

    private static void atualizarPonto(Scanner scanner) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            System.out.println("Acesso negado. Apenas administradores podem atualizar pontos.");
            return;
        }
        System.out.println("\n--- Atualizar Ponto de Coleta ---");
        System.out.print("ID do ponto a ser atualizado: ");
        long id = Long.parseLong(scanner.nextLine());
        System.out.print("Novo Nome do Ponto: ");
        String nome = scanner.nextLine();
        System.out.print("Novo Endereço: ");
        String endereco = scanner.nextLine();
        System.out.print("Novos Tipos de Resíduo (separados por vírgula, ex: Plástico, Papel): ");
        List<String> tiposResiduos = Arrays.asList(scanner.nextLine().split(",\s*"));

        // Usando o novo construtor que aceita o ID
        PontoColeta pontoAtualizado = new PontoColeta(id, nome, endereco, tiposResiduos);
        enviarComando("ATUALIZAR", pontoAtualizado);
    }

    private static void excluirPonto(Scanner scanner) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            System.out.println("Acesso negado. Apenas administradores podem excluir pontos.");
            return;
        }
        System.out.println("\n--- Excluir Ponto de Coleta ---");
        System.out.print("ID do ponto a ser excluído: ");
        long id = Long.parseLong(scanner.nextLine());
        enviarComando("EXCLUIR", id);
    }

    private static void mudarPerfil(Scanner scanner) {
        System.out.println("\n--- Mudar Perfil de Usuário ---");
        System.out.print("Digite o novo perfil (ADMIN ou CIDADAO): ");
        String novoPerfil = scanner.nextLine().toUpperCase();
        if (novoPerfil.equals("ADMIN") || novoPerfil.equals("CIDADAO")) {
            userRole = novoPerfil;
            System.out.println("Perfil alterado para: " + userRole);
        } else {
            System.out.println("Perfil inválido. Mantendo perfil atual: " + userRole);
        }
    }
}

