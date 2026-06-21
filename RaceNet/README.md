# RaceNet — Corrida Multiplayer em Rede

RaceNet é uma aplicação Java de corrida multiplayer simples para demonstrar o uso combinado de TCP e UDP em uma rede.

## Ideia do jogo

- Cada jogador começa em `0%`.
- Cada clique em `ACELERAR` (ou tecla `ESPAÇO`) aumenta a posição entre `1` e `6`.
- A corrida termina quando algum jogador chega a `100%`.
- Se apenas um jogador conectar, o servidor cria um `Bot`.
- **Regra de Início**: A corrida só começa quando **TODOS** os jogadores conectados estiverem no estado "PRONTO".

## Uso de TCP e UDP

### TCP — eventos confiáveis

Porta padrão: `5000`

O TCP é usado para mensagens importantes que não podem se perder:

- `ENTRAR;Nome`: Registro do jogador.
- `PRONTO;Nome`: Sinaliza que o jogador aguarda o início.
- `INICIAR_CORRIDA`: Comando para disparar a largada.
- `FINALIZAR;Nome;tempo=X`: Notifica a chegada com o tempo decorrido.
- `VENCEDOR;Nome`: Confirmação oficial do pódio.
- `RESET`: Comando para limpar o estado e permitir uma nova partida.

Justificativa: o TCP garante a entrega confiável de eventos críticos do jogo, como entrada de jogadores, início da corrida e confirmação oficial do vencedor.

### UDP — atualizações em tempo real

Porta padrão: `5001`

O UDP é usado para mensagens rápidas e frequentes:

- `POSICAO;Nome;47`: Atualização de progresso individual.
- `RANKING;Nome1=47;Nome2=42`: Broadcast do estado global da pista.

Justificativa: o UDP atualiza as posições durante a corrida. Como essas mensagens são frequentes e temporais, se uma atualização for perdida, outra posição será enviada logo depois, mantendo a fluidez visual sem sobrecarga.

## Guia de Demonstração

### Passo a passo para teste completo

1. **Inicie o Servidor**: Execute `RaceServer`. Observe os logs iniciais de porta.
2. **Conecte Jogadores**: Abra dois ou mais clientes. Informe os nomes (ex: `Alice` e `Bob`).
3. **Sinalize Prontidão**: Clique em `Estou Pronto` em todos os clientes.
4. **Largada**: Clique em `Iniciar Corrida`. Note que se alguém não estiver pronto, o servidor enviará um aviso de status e a corrida não iniciará.
5. **Aceleração**: Use o botão ou a tecla `ESPAÇO`. O cronômetro em tempo real será ativado.
6. **Finalização**: O primeiro a chegar a 100% envia o tempo final. O servidor anuncia o vencedor para todos.
7. **Nova Corrida**: O botão `Nova Corrida` aparecerá nos clientes. Ao clicar, o servidor reseta o estado e todos podem jogar novamente.

## Entrega do Trabalho Prático

Os arquivos de apoio para atender ao enunciado estão em `docs/`:

- `docs/relatorio.md`: relatório base com objetivo, requisitos atendidos, topologia, execução e evidências.
- `docs/configuracao-roteadores.md`: roteiro de endereçamento e port forwarding para R1, R2 e R3.
- `docs/checklist-wireshark.md`: filtros e prints recomendados para comprovar TCP e UDP.

Para a apresentação presencial, execute o servidor no PC2 e o cliente no PC1. No cliente, informe o IP externo acessível a partir do PC1 e as portas TCP/UDP exibidas no terminal do servidor.

## Protocolo de Comunicação

| Comando | Origem | Protocolo | Descrição |
| :--- | :--- | :--- | :--- |
| `ENTRAR;Nome` | Cliente | TCP | Solicita entrada no jogo. |
| `PRONTO;Nome` | Cliente | TCP | Informa que o jogador está pronto para começar. |
| `INICIAR_CORRIDA` | Cliente | TCP | Solicita o início da partida. |
| `FINALIZAR;Nome;tempo=X` | Cliente | TCP | Notifica que o jogador completou 100% com o tempo X. |
| `RESET` | Cliente | TCP | Solicita o reset global da corrida. |
| `POSICAO;Nome;X` | Cliente | UDP | Envia a posição atual (0-100). |
| `VENCEDOR;Nome` | Servidor | TCP | Anuncia o vencedor oficial. |
| `RANKING;N1=X;N2=Y` | Servidor | UDP | Broadcast das posições de todos os jogadores. |
| `STATUS;Mensagem` | Servidor | TCP | Envia logs informativos ou erros para a GUI. |

## Logs Esperados do Servidor

O servidor utiliza prefixos para facilitar a monitoria:

```text
[EVENTO] RaceNet Server iniciado.
[LOG] TCP: 5000
[LOG] UDP: 5001
[EVENTO] Novo cliente conectado: /127.0.0.1:56789
[LOG] TCP recebido de Alice: ENTRAR;Alice
[EVENTO] Jogador Alice entrou.
[LOG] Broadcast TCP: STATUS;Alice entrou no jogo.
[LOG] TCP recebido de Alice: PRONTO;Alice
[EVENTO] Jogador Alice está pronto.
[LOG] Tentativa de início falhou: nem todos estão prontos.
[EVENTO] Iniciando corrida com 2 jogadores.
[LOG] Broadcast TCP: INICIAR_CORRIDA
[EVENTO] Vencedor confirmado: Alice
[EVENTO] Resetando corrida...
```

## Screenshots (Placeholders)

### Interface do Cliente (GUI)
*(Insira aqui o print do RaceClientGUI em funcionamento com o cronômetro e a pista)*

### Terminal do Servidor
*(Insira aqui o print do terminal com os logs padronizados [LOG] e [EVENTO])*

---

## Estrutura do Projeto

```text
RaceNet/
├── src/
│   ├── client/
│   │   ├── RaceClientGUI.java
│   │   ├── TcpClient.java
│   │   └── UdpClient.java
│   ├── server/
│   │   ├── RaceServer.java
│   │   ├── TcpServer.java
│   │   ├── ClientHandler.java
│   │   ├── UdpServer.java
│   │   ├── RaceState.java
│   │   └── BotRunner.java
│   └── shared/
│       └── Protocol.java
└── README.md
```

## Como compilar

Abra o terminal dentro da pasta `RaceNet` e execute:

```bash
javac -d out src/shared/Protocol.java src/server/*.java src/client/*.java
```

## Como rodar

### 1. Iniciar o servidor
```bash
java -cp out server.RaceServer
```

### 2. Abrir o cliente
```bash
java -cp out client.RaceClientGUI
```
