# Configuracao dos roteadores no Packet Tracer

Este roteiro usa a topologia:

```text
PC1 -- R1 -- R2 -- R3 -- PC2
```

## Enderecos sugeridos

| Dispositivo | Interface | IP | Mascara |
| --- | --- | --- | --- |
| PC1 | WiFi | `192.168.0.10` | `255.255.0.0` |
| R1 | LAN | `192.168.0.1` | `255.255.0.0` |
| R2 | WAN | `192.168.0.2` | `255.255.0.0` |
| R2 | LAN | `172.16.0.1` | `255.240.0.0` |
| R3 | WAN | `172.16.0.2` | `255.240.0.0` |
| R3 | LAN | `10.0.0.1` | `255.0.0.0` |
| PC2 | WiFi | `10.0.0.10` | `255.0.0.0` |

Gateways:

- PC1: `192.168.0.1`
- R2 WAN: `192.168.0.1`
- R3 WAN: `172.16.0.1`
- PC2: `10.0.0.1`

## Port forwarding

O servidor RaceNet fica no PC2.

Portas:

- TCP `5000`
- UDP `5001`

### R1

Encaminhar:

- TCP `5000` para `192.168.0.2:5000`
- UDP `5001` para `192.168.0.2:5001`

### R2

Encaminhar:

- TCP `5000` para `172.16.0.2:5000`
- UDP `5001` para `172.16.0.2:5001`

### R3

Encaminhar:

- TCP `5000` para `10.0.0.10:5000`
- UDP `5001` para `10.0.0.10:5001`

## Testes de conectividade

1. De PC1, testar ping para `192.168.0.1`.
2. De PC1, testar ping para `192.168.0.2`.
3. De R2, testar ping para `172.16.0.2`.
4. De R3, testar ping para `10.0.0.10`.
5. Executar o servidor no PC2.
6. Executar o cliente no PC1 usando:

```text
Servidor: 192.168.0.1
TCP: 5000
UDP: 5001
```

## Prints para o relatorio

Capturar imagens de:

- Tela de IP do PC1.
- Tela de IP do PC2.
- Configuracao LAN/WAN de R1, R2 e R3.
- Regras de port forwarding de R1, R2 e R3.
- Cliente RaceNet conectado.
- Servidor RaceNet exibindo logs.
