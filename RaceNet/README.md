# RaceNet — Corrida Multiplayer em Rede

RaceNet é uma aplicação Java de corrida multiplayer simples para demonstrar o uso combinado de TCP e UDP em uma rede.

## Ideia do jogo

- Dois jogadores participam de uma corrida.
- Cada jogador começa em `0%`.
- Cada clique em `ACELERAR` aumenta a posição entre `1` e `6`.
- A corrida termina quando algum jogador chega a `100%`.
- Se apenas um jogador conectar, o servidor cria um `Bot`.

## Uso de TCP e UDP

### TCP — eventos confiáveis

Porta padrão: `5000`

O TCP é usado para mensagens importantes que não podem se perder:

- `ENTRAR;Ana`
- `PRONTO;Ana`
- `INICIAR_CORRIDA`
- `FINALIZAR;Ana;tempo=0`
- `VENCEDOR;Ana`

Justificativa: o TCP garante a entrega confiável de eventos críticos do jogo, como entrada de jogadores, início da corrida e confirmação oficial do vencedor.

### UDP — atualizações em tempo real

Porta padrão: `5001`

O UDP é usado para mensagens rápidas e frequentes:

- `POSICAO;Ana;47`
- `RANKING;Ana=47;Bruno=42`

Justificativa: o UDP atualiza as posições durante a corrida. Como essas mensagens são frequentes e temporais, se uma atualização for perdida, outra posição será enviada logo depois.

## Estrutura

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

No PowerShell do Windows:

```powershell
javac -d out src\shared\Protocol.java src\server\*.java src\client\*.java
```

## Como rodar

### 1. Iniciar o servidor

No PC2, ou no mesmo computador para teste local:

```bash
java -cp out server.RaceServer
```

O servidor usa:

- TCP `5000`
- UDP `5001`

### 2. Abrir um cliente

Em outro terminal:

```bash
java -cp out client.RaceClientGUI
```

Na tela do cliente:

1. Informe o nome do jogador.
2. Informe o IP do servidor.
   - Para teste no mesmo PC: `127.0.0.1`
   - Para teste em outro PC: IP do PC2
3. Use TCP `5000`.
4. Use UDP `5001`.
5. Clique em `Conectar`.
6. Clique em `Estou Pronto`.
7. Clique em `Iniciar Corrida`.
8. Clique em `ACELERAR` até chegar a `100%`.

## Como testar multiplayer

### Teste local com dois clientes

1. Compile o projeto.
2. Rode o servidor.
3. Abra dois terminais e execute o cliente duas vezes:

```bash
java -cp out client.RaceClientGUI
java -cp out client.RaceClientGUI
```

4. Use nomes diferentes, por exemplo `Ana` e `Bruno`.
5. Nos dois clientes, conecte ao servidor `127.0.0.1`.
6. Inicie a corrida.

### Teste com bot

1. Rode o servidor.
2. Abra apenas um cliente.
3. Conecte, marque pronto e inicie a corrida.
4. O servidor criará automaticamente o jogador `Bot`.

## Configuração para roteadores

Para a parte de rede física do trabalho, documente o redirecionamento das duas portas para o PC2:

- Porta TCP `5000`: controle confiável da corrida.
- Porta UDP `5001`: atualização em tempo real das posições.
