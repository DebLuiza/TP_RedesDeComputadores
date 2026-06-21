# Checklist de evidencias no Wireshark

Use esta lista para coletar os prints pedidos no enunciado.

## Captura TCP

Filtro:

```text
tcp.port == 5000
```

O que deve aparecer:

- Conexao TCP entre cliente e servidor.
- Pacotes com mensagens de controle.
- Eventos como conectar, pronto, iniciar corrida, finalizar e vencedor.

Print sugerido:

- Lista de pacotes filtrada por `tcp.port == 5000`.
- Detalhe de um pacote TCP selecionado.

## Captura UDP

Filtro:

```text
udp.port == 5001
```

O que deve aparecer:

- Datagramas de posicao enviados pelo cliente.
- Datagramas de ranking enviados pelo servidor.
- Repeticao frequente durante a corrida.

Print sugerido:

- Lista de pacotes filtrada por `udp.port == 5001`.
- Detalhe de um pacote UDP selecionado.

## Captura por IP

Se a captura for feita no PC2:

```text
ip.addr == 10.0.0.10
```

Se a captura for feita no PC1:

```text
ip.addr == 192.168.0.10
```

## Texto curto para o relatorio

TCP foi usado para eventos confiaveis, como entrada do jogador, inicio da corrida, fim da corrida e confirmacao do vencedor. UDP foi usado para atualizacoes de posicao e ranking, pois essas mensagens sao frequentes e podem ser substituidas pela proxima atualizacao caso uma delas seja perdida.
