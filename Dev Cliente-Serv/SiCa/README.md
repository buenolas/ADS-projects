# Sistema de Compartilhamento de Arquivos (SiCA) - Versão Java Simplificada

## Descrição Geral

Este projeto implementa um Sistema de Compartilhamento de Arquivos (SiCA) simples utilizando sockets TCP em **Java**. 
O sistema é composto por um servidor e um cliente, permitindo que o cliente realize as seguintes operações:

- **UPLOAD**: Enviar um arquivo local para o servidor.
- **LIST**: Listar todos os arquivos atualmente disponíveis no servidor.
- **DOWNLOAD**: Baixar um arquivo específico do servidor para o diretório local do cliente.

O objetivo é demonstrar a comunicação entre processos via rede, utilizando o protocolo TCP para garantir a entrega confiável dos dados.

## Arquitetura do Sistema

### Servidor (`Server.java`)

O servidor é responsável por:
- Escutar por novas conexões de clientes em um endereço IP e porta definidos.
- Lidar com múltiplos clientes simultaneamente, utilizando threads separadas para processar cada conexão.
- Receber e salvar arquivos enviados pelos clientes no diretório `server_files/`.
- Fornecer uma lista dos arquivos armazenados quando solicitado.
- Enviar arquivos para clientes que solicitam download.
- Gerenciar erros de conexão e operações de arquivo.

### Cliente (`Client.java`)

O cliente é responsável por:
- Conectar-se ao servidor em um endereço IP e porta específicos.
- Apresentar um menu interativo ao usuário para escolher as operações (UPLOAD, LIST, DOWNLOAD, Sair).
- Enviar arquivos para o servidor, lendo-os do diretório `client_files/`.
- Solicitar e exibir a lista de arquivos disponíveis no servidor.
- Solicitar e receber arquivos do servidor, salvando-os no diretório `client_files/`.
- Gerenciar erros de conexão e operações de arquivo.

## Protocolo de Comunicação Simplificado

A comunicação entre cliente e servidor é baseada em strings simples para comandos e metadados, seguidas por dados binários para os arquivos. As mensagens de comando e resposta são strings de uma linha, terminadas por um caractere de nova linha (`\n`), e são lidas/escritas usando `DataInputStream` e `DataOutputStream`.

### Formato da Mensagem

- **Comandos do Cliente**: `COMANDO [filename] [filesize]\n`
- **Respostas do Servidor**: `STATUS [data] [message]\n`

### Fluxo de Operações

#### UPLOAD
1. **Cliente**: Envia `UPLOAD <filename> <filesize>\n`.
2. **Servidor**: Responde com `OK\n` ou `ERROR <message>\n`.
3. **Cliente**: Se `OK`, envia os bytes brutos do arquivo.
4. **Servidor**: Recebe os bytes e salva o arquivo. Não há confirmação final explícita do servidor após o envio dos bytes do arquivo.

#### LIST
1. **Cliente**: Envia `LIST\n`.
2. **Servidor**: Responde com `OK <file1>;<file2>;...\n` (lista de arquivos separados por ponto e vírgula) ou `ERROR <message>\n`.

#### DOWNLOAD
1. **Cliente**: Envia `DOWNLOAD <filename>\n`.
2. **Servidor**: Verifica a existência do arquivo. Se encontrado, responde com `OK <filename> <filesize>\n` ou `ERROR <message>\n`.
3. **Servidor**: Se `OK`, envia os bytes brutos do arquivo.
4. **Cliente**: Recebe os bytes e salva o arquivo, sabendo quantos bytes esperar pelo `filesize` no cabeçalho.

## Como Executar

### Pré-requisitos
- Java Development Kit (JDK) 11 ou superior instalado.

### Passos

1.  **Crie os arquivos e diretórios:**
    Crie os arquivos `Server.java`, `ClientHandler.java` e `Client.java` no mesmo diretório. Crie também os diretórios `server_files/` e `client_files/` no mesmo nível dos arquivos `.java`.
    Para o cliente, crie um arquivo de teste para upload, por exemplo: `client_files/test_file_java_simple.txt`.

2.  **Compile os arquivos Java:**
    Abra um terminal no diretório onde os arquivos `.java` estão e execute:
    ```bash
    javac Server.java ClientHandler.java Client.java
    ```
    Isso criará os arquivos `.class` correspondentes.

3.  **Inicie o Servidor:**
    Em um terminal, no mesmo diretório, execute:
    ```bash
    java Server
    ```
    O servidor começará a escutar por conexões na porta `65432`.

4.  **Inicie o Cliente:**
    Abra outro terminal, no mesmo diretório, e execute:
    ```bash
    java Client
    ```
    O cliente se conectará ao servidor e apresentará um menu de opções.

5.  **Interaja com o Cliente:**
    -   **UPLOAD**: Digite `1` e forneça o caminho de um arquivo (ex: `client_files/test_file_java_simple.txt`).
    -   **LIST**: Digite `2` para ver os arquivos no servidor.
    -   **DOWNLOAD**: Digite `3` e forneça o nome de um arquivo listado (ex: `test_file_java_simple.txt`).
    -   **Sair**: Digite `4` para desconectar o cliente.

## Estrutura de Diretórios

```
.  
├── Server.java
├── ClientHandler.java
├── Client.java
├── Server.class # Gerado após compilação
├── ClientHandler.class # Gerado após compilação
├── Client.class # Gerado após compilação
├── server_files/  # Onde o servidor armazena os arquivos
└── client_files/  # Onde o cliente armazena os arquivos e de onde envia
    └── test_file_java_simple.txt # Exemplo de arquivo para upload
```

## Observações

-   O sistema utiliza `127.0.0.1` (localhost) como endereço IP padrão. Para testar em máquinas diferentes, o `HOST` em `Client.java` e a porta em `Server.java` precisariam ser ajustados para o endereço IP do servidor.
-   A porta `65432` é utilizada por padrão. Certifique-se de que não está em uso por outro serviço.
-   O tratamento de erros é básico, focado em falhas de conexão e arquivo. Para um sistema de produção, seria necessário um tratamento de erros mais robusto e mecanismos de segurança.
-   A comunicação de arquivos grandes é feita em chunks de 4KB para evitar o carregamento de todo o arquivo na memória de uma vez.


