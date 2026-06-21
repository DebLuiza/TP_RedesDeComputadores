# Relatorio do Trabalho Pratico - Redes de Computadores I

## 1. Objetivo

Este trabalho implementa uma aplicacao em rede chamada **RaceNet**, desenvolvida em Java, com interface grafica em Swing. A aplicacao demonstra comunicacao cliente-servidor usando TCP e UDP, alem de multithreading para manter conexoes simultaneas e atualizacoes em tempo real.

## 2. Requisitos do enunciado

| Requisito | Atendimento no RaceNet |
| --- | --- |
| Linguagem Java | Codigo em `src/client`, `src/server` e `src/shared` |
| Interface Java Swing no cliente | `client.RaceClientGUI` |
| Funcionalidade de rede com TCP | Entrada, pronto, inicio, fim, vencedor e reset |
| Funcionalidade de rede com UDP | Atualizacao de posicoes e ranking em tempo real |
| Multithreading | Threads TCP/UDP do servidor, handlers por cliente e leitores TCP/UDP no cliente |
| Execucao em PCs diferentes | Cliente permite configurar IP do servidor, porta TCP e porta UDP |

## 3. Descricao da aplicacao

O RaceNet e um jogo simples de corrida multiplayer. Cada jogador se conecta ao servidor, informa que esta pronto e participa de uma corrida ate 100%. O bot e criado automaticamente quando existe apenas um jogador conectado.

O TCP e usado para mensagens criticas, pois exige entrega confiavel:

- `ENTRAR;Nome`
- `PRONTO;Nome`
- `INICIAR_CORRIDA`
- `FINALIZAR;Nome;tempo=X`
- `VENCEDOR;Nome`
- `RESET`

O UDP e usado para mensagens frequentes e temporais:

- `POSICAO;Nome;X`
- `RANKING;Nome1=X;Nome2=Y`

Essa divisao mostra na pratica a diferenca entre confiabilidade do TCP e baixa latencia do UDP.

## 4. Topologia de rede

Topologia solicitada:

```text
PC1 -- WiFi -- R1 -- Cabo 1 -- R2 -- Cabo 2 -- R3 -- WiFi -- PC2
```

O servidor deve ser executado no PC2. O cliente deve ser executado no PC1.

Endereco sugerido para o Packet Tracer:

| Equipamento | Interface | IP | Mascara/Gateway |
| --- | --- | --- | --- |
| PC1 | WiFi | `192.168.0.10` | `/16`, gateway `192.168.0.1` |
| R1 | LAN | `192.168.0.1` | `/16` |
| R2 | WAN | `192.168.0.2` | `/16`, gateway `192.168.0.1` |
| R2 | LAN | `172.16.0.1` | `/12` |
| R3 | WAN | `172.16.0.2` | `/12`, gateway `172.16.0.1` |
| R3 | LAN | `10.0.0.1` | `/8` |
| PC2 | WiFi | `10.0.0.10` | `/8`, gateway `10.0.0.1` |

Redes do enunciado:

- R1: `192.168.0.0/16`
- R2: `172.16.0.0/12`
- R3: `10.0.0.0/8`

## 5. Redirecionamento de portas

O RaceNet usa duas portas por padrao:

- TCP `5000`
- UDP `5001`

Como o servidor fica no PC2, os roteadores devem encaminhar essas portas ate `10.0.0.10`.

Fluxo:

```text
PC1 -> R1 -> R2 -> R3 -> PC2
```

Regras:

| Roteador | Porta externa | Encaminhar para |
| --- | --- | --- |
| R1 | TCP 5000 | `192.168.0.2:5000` |
| R1 | UDP 5001 | `192.168.0.2:5001` |
| R2 | TCP 5000 | `172.16.0.2:5000` |
| R2 | UDP 5001 | `172.16.0.2:5001` |
| R3 | TCP 5000 | `10.0.0.10:5000` |
| R3 | UDP 5001 | `10.0.0.10:5001` |

No cliente em PC1, preencher:

- Servidor: IP externo acessivel pelo PC1. Na topologia sugerida, usar `192.168.0.1`.
- TCP: `5000`
- UDP: `5001`

## 6. Execucao

Compilar:

```bash
javac -d out src/shared/Protocol.java src/server/*.java src/client/*.java
```

No PC2, executar o servidor:

```bash
java -cp out server.RaceServer
```

O servidor imprime as portas e os IPs locais disponiveis.

No PC1, executar o cliente:

```bash
java -cp out client.RaceClientGUI
```

Passos de uso:

1. Informar nome, IP do servidor, porta TCP e porta UDP.
2. Clicar em `Conectar`.
3. Clicar em `Estou Pronto`.
4. Clicar em `Iniciar Corrida`.
5. Usar `ACELERAR` ou a tecla `ESPACO`.
6. Aguardar a mensagem de vencedor.

## 7. Evidencias no Wireshark

Capturas recomendadas:

1. Filtro TCP:

```text
tcp.port == 5000
```

Evidencia esperada: mensagens de entrada, pronto, inicio e vencedor.

2. Filtro UDP:

```text
udp.port == 5001
```

Evidencia esperada: datagramas de posicao e ranking durante a corrida.

3. Filtro por IP do servidor:

```text
ip.addr == 10.0.0.10
```

Evidencia esperada: trafego chegando ao PC2.

Inserir no relatorio os prints das capturas TCP e UDP.

## 8. Conclusao

O RaceNet demonstra o uso combinado de TCP e UDP em uma aplicacao Java com interface Swing. O TCP foi usado para mensagens confiaveis do fluxo da corrida, enquanto o UDP foi usado para atualizacoes frequentes de posicao. O uso de threads permite que servidor e cliente tratem comunicacao de rede sem travar a interface grafica.
