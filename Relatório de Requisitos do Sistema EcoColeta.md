# Relatório de Requisitos do Sistema EcoColeta

## 1. Introdução

O presente relatório detalha os requisitos funcionais e não funcionais para o desenvolvimento do sistema **EcoColeta**, uma aplicação cliente-servidor em Java destinada à gestão e consulta de pontos de coleta seletiva de resíduos recicláveis. A iniciativa visa mitigar o problema do descarte inadequado de resíduos sólidos, facilitando o acesso da população a informações sobre pontos de coleta e os tipos de materiais aceitos [1].

## 2. Requisitos Funcionais (RF)

Os requisitos funcionais descrevem as funcionalidades que o sistema deve oferecer aos seus usuários. Para o sistema EcoColeta, foram identificados os seguintes:

*   **RF1: Cadastro de Pontos de Coleta**: O sistema deve permitir que usuários com perfil de administrador cadastrem novos pontos de coleta, informando o nome do local, endereço completo e os tipos de resíduos recicláveis aceitos (e.g., plástico, papel, vidro, metal).
*   **RF2: Listagem de Pontos de Coleta**: O sistema deve permitir que usuários (cidadãos e administradores) visualizem uma lista de todos os pontos de coleta cadastrados, exibindo informações como nome, endereço e os tipos de resíduos aceitos em cada um.
*   **RF3: Busca de Pontos de Coleta por Tipo de Resíduo**: O sistema deve possibilitar que cidadãos busquem pontos de coleta específicos com base no tipo de resíduo que desejam descartar, retornando uma lista de locais que aceitam aquele material.
*   **RF4: Atualização de Pontos de Coleta**: O sistema deve permitir que usuários com perfil de administrador editem as informações de pontos de coleta existentes, como nome, endereço ou tipos de resíduos aceitos.
*   **RF5: Exclusão de Pontos de Coleta**: O sistema deve permitir que usuários com perfil de administrador removam pontos de coleta que não estão mais ativos ou que foram cadastrados incorretamente.

## 3. Requisitos Não Funcionais (RNF)

Os requisitos não funcionais especificam critérios que podem ser usados para julgar a operação de um sistema, em vez de comportamentos específicos. Para o sistema EcoColeta, foram definidos:

*   **RNF1: Desempenho**: O sistema deve responder a requisições de listagem e busca de pontos de coleta em no máximo 3 segundos, mesmo com um volume de até 1000 pontos de coleta cadastrados.
*   **RNF2: Usabilidade**: A interface do cliente deve ser intuitiva e de fácil compreensão, permitindo que um usuário leigo realize as operações básicas (listagem e busca) sem a necessidade de treinamento prévio.
*   **RNF3: Segurança**: O sistema deve garantir que apenas usuários autenticados com perfil de administrador possam realizar operações de cadastro, atualização e exclusão de pontos de coleta.
*   **RNF4: Escalabilidade**: A arquitetura cliente-servidor deve ser projetada para suportar um aumento de até 50% no número de usuários simultâneos e pontos de coleta sem degradação significativa do desempenho.
*   **RNF5: Manutenibilidade**: O código-fonte do sistema deve ser modular, bem documentado e seguir padrões de codificação Java, facilitando futuras modificações e a correção de eventuais erros.

## 4. Conclusão

Este relatório delineou os requisitos essenciais para o desenvolvimento do sistema EcoColeta, abrangendo tanto as funcionalidades esperadas quanto os atributos de qualidade necessários. A implementação desses requisitos garantirá um sistema robusto, útil e alinhado aos objetivos de promoção da coleta seletiva e sustentabilidade ambiental.

