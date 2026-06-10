package client;

import shared.Protocol;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class RaceClientGUI extends JFrame {
    private final JTextField nameField = new JTextField("Ana");
    private final JTextField serverField = new JTextField("127.0.0.1");
    private final JTextField tcpPortField = new JTextField(String.valueOf(Protocol.DEFAULT_TCP_PORT));
    private final JTextField udpPortField = new JTextField(String.valueOf(Protocol.DEFAULT_UDP_PORT));
    private final JButton connectButton = new JButton("Conectar");
    private final JButton readyButton = new JButton("Estou Pronto");
    private final JButton startButton = new JButton("Iniciar Corrida");
    private final JButton accelerateButton = new JButton("ACELERAR");
    private final JProgressBar myProgress = new JProgressBar(0, 100);
    private final JPanel opponentsPanel = new JPanel(new GridLayout(0, 1, 4, 4));
    private final JTextArea statusArea = new JTextArea(8, 40);
    private final JLabel connectionStatusLabel = new JLabel("Desconectado");
    private final TcpClient tcpClient = new TcpClient();
    private final UdpClient udpClient = new UdpClient();
    private final Map<String, JProgressBar> opponentBars = new LinkedHashMap<>();
    private final Random random = new Random();
    private boolean connected;
    private boolean raceStarted;
    private int myPosition;

    public RaceClientGUI() {
        super("RaceNet - Corrida Multiplayer");
        buildLayout();
        bindActions();
        setButtons(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void buildLayout() {
        Color background = new Color(245, 247, 251);
        Color card = Color.WHITE;
        Color primary = new Color(41, 98, 255);
        Color success = new Color(0, 150, 95);

        getContentPane().setBackground(background);

        JLabel titleLabel = new JLabel("RaceNet");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(primary);

        JLabel subtitleLabel = new JLabel("Corrida multiplayer com TCP + UDP");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(90, 96, 110));

        JPanel titlePanel = new JPanel(new BorderLayout(4, 4));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        connectionStatusLabel.setOpaque(true);
        connectionStatusLabel.setForeground(Color.WHITE);
        connectionStatusLabel.setBackground(new Color(130, 136, 148));
        connectionStatusLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        JPanel headerPanel = new JPanel(new BorderLayout(12, 12));
        headerPanel.setOpaque(false);
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        headerPanel.add(connectionStatusLabel, BorderLayout.EAST);

        JPanel fieldsPanel = new JPanel(new GridLayout(4, 2, 6, 6));
        fieldsPanel.setOpaque(false);
        fieldsPanel.add(new JLabel("Nome:"));
        fieldsPanel.add(nameField);
        fieldsPanel.add(new JLabel("Servidor:"));
        fieldsPanel.add(serverField);
        fieldsPanel.add(new JLabel("TCP:"));
        fieldsPanel.add(tcpPortField);
        fieldsPanel.add(new JLabel("UDP:"));
        fieldsPanel.add(udpPortField);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(connectButton);
        buttonsPanel.add(readyButton);
        buttonsPanel.add(startButton);

        styleButton(connectButton, primary);
        styleButton(readyButton, new Color(91, 95, 110));
        styleButton(startButton, success);
        styleButton(accelerateButton, new Color(255, 111, 0));

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setOpaque(false);
        topPanel.add(fieldsPanel, BorderLayout.CENTER);
        topPanel.add(buttonsPanel, BorderLayout.SOUTH);

        JPanel configCard = cardPanel(new BorderLayout(8, 8), card);
        configCard.add(headerPanel, BorderLayout.NORTH);
        configCard.add(topPanel, BorderLayout.CENTER);

        myProgress.setStringPainted(true);
        myProgress.setPreferredSize(new Dimension(480, 34));
        myProgress.setForeground(success);
        myProgress.setBackground(new Color(226, 232, 240));
        myProgress.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel racePanel = new JPanel(new BorderLayout(6, 6));
        racePanel.setOpaque(false);
        racePanel.add(new JLabel("Minha corrida:"), BorderLayout.NORTH);
        racePanel.add(myProgress, BorderLayout.CENTER);

        opponentsPanel.setOpaque(false);

        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(racePanel, BorderLayout.NORTH);
        centerPanel.add(new JLabel("Adversários:"), BorderLayout.CENTER);
        centerPanel.add(opponentsPanel, BorderLayout.SOUTH);

        statusArea.setEditable(false);
        statusArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        statusArea.setBackground(new Color(248, 250, 252));

        accelerateButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        accelerateButton.setPreferredSize(new Dimension(180, 48));

        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        bottomPanel.setOpaque(false);
        bottomPanel.add(accelerateButton, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);

        JPanel contentPanel = new JPanel(new BorderLayout(12, 12));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        contentPanel.add(configCard, BorderLayout.NORTH);
        contentPanel.add(cardPanel(centerPanel, card), BorderLayout.CENTER);
        contentPanel.add(cardPanel(bottomPanel, card), BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
        setMinimumSize(new Dimension(620, 640));
    }

    private void bindActions() {
        connectButton.addActionListener(event -> connect());
        readyButton.addActionListener(event -> sendTcp(Protocol.READY + ";" + playerName()));
        startButton.addActionListener(event -> sendTcp(Protocol.START_RACE));
        accelerateButton.addActionListener(event -> accelerate());
    }

    private void connect() {
        try {
            String host = serverField.getText().trim();
            int tcpPort = Integer.parseInt(tcpPortField.getText().trim());
            int udpPort = Integer.parseInt(udpPortField.getText().trim());

            tcpClient.connect(host, tcpPort, this::handleTcpMessage);
            udpClient.connect(host, udpPort, this::handleUdpMessage);
            connected = true;
            setButtons(true);
            connectionStatusLabel.setText("Conectado");
            connectionStatusLabel.setBackground(new Color(0, 150, 95));
            sendTcp(Protocol.ENTER + ";" + playerName());
            log("Conectado ao servidor.");
        } catch (IOException | NumberFormatException exception) {
            JOptionPane.showMessageDialog(this, "Erro ao conectar: " + exception.getMessage());
        }
    }

    private void accelerate() {
        if (!connected || !raceStarted || myPosition >= 100) {
            return;
        }

        int advance = 1 + random.nextInt(6);
        myPosition = Math.min(100, myPosition + advance);
        updateMyProgress();
        sendUdp(Protocol.POSITION + ";" + playerName() + ";" + myPosition);
        log(playerName() + " enviou posição " + myPosition + " via UDP.");

        if (myPosition >= 100) {
            sendTcp(Protocol.FINISH + ";" + playerName() + ";tempo=0");
            accelerateButton.setEnabled(false);
        }
    }

    private void handleTcpMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String[] parts = Protocol.split(message);
            if (parts.length == 0) {
                return;
            }

            switch (parts[0]) {
                case Protocol.ENTER_OK -> log("Entrada confirmada por TCP: " + parts[1]);
                case Protocol.STATUS -> log(parts.length > 1 ? parts[1] : message);
                case Protocol.START_RACE -> {
                    raceStarted = true;
                    accelerateButton.setEnabled(true);
                    startButton.setEnabled(false);
                    log("Corrida iniciada.");
                }
                case Protocol.WINNER -> {
                    raceStarted = false;
                    accelerateButton.setEnabled(false);
                    String winner = parts.length > 1 ? parts[1] : "desconhecido";
                    log("Vencedor confirmado por TCP: " + winner);
                    JOptionPane.showMessageDialog(this, "Vencedor: " + winner);
                }
                default -> log("TCP recebido: " + message);
            }
        });
    }

    private void handleUdpMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String[] parts = Protocol.split(message);
            if (parts.length == 0 || !Protocol.RANKING.equals(parts[0])) {
                log("UDP recebido: " + message);
                return;
            }

            for (int index = 1; index < parts.length; index++) {
                String[] pair = parts[index].split("=");
                if (pair.length != 2) {
                    continue;
                }
                String name = pair[0];
                int position = Integer.parseInt(pair[1]);
                if (name.equals(playerName())) {
                    myPosition = Math.max(myPosition, position);
                    updateMyProgress();
                } else {
                    updateOpponent(name, position);
                }
            }
            log("Ranking recebido do servidor.");
        });
    }

    private void updateMyProgress() {
        myProgress.setValue(myPosition);
        myProgress.setString(playerName() + " - " + myPosition + "%");
    }

    private void updateOpponent(String name, int position) {
        JProgressBar bar = opponentBars.computeIfAbsent(name, ignored -> {
            JProgressBar newBar = new JProgressBar(0, 100);
            newBar.setStringPainted(true);
            newBar.setPreferredSize(new Dimension(480, 28));
            newBar.setForeground(new Color(41, 98, 255));
            newBar.setBackground(new Color(226, 232, 240));
            newBar.setFont(new Font("Segoe UI", Font.BOLD, 12));
            opponentsPanel.add(newBar);
            opponentsPanel.revalidate();
            return newBar;
        });
        bar.setValue(position);
        bar.setString(name + " - " + position + "%");
        opponentsPanel.repaint();
    }

    private void sendTcp(String message) {
        tcpClient.send(message);
        log("TCP enviado: " + message);
    }

    private void sendUdp(String message) {
        try {
            udpClient.send(message);
        } catch (IOException exception) {
            log("Erro UDP: " + exception.getMessage());
        }
    }

    private String playerName() {
        String name = nameField.getText().trim();
        return name.isEmpty() ? "Jogador" : name;
    }

    private void setButtons(boolean enabledAfterConnect) {
        connectButton.setEnabled(!enabledAfterConnect);
        readyButton.setEnabled(enabledAfterConnect);
        startButton.setEnabled(enabledAfterConnect);
        accelerateButton.setEnabled(enabledAfterConnect && raceStarted);
    }

    private void log(String message) {
        statusArea.append("- " + message + System.lineSeparator());
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }

    private JPanel cardPanel(LayoutManager layout, Color background) {
        JPanel panel = new JPanel(layout);
        return cardPanel(panel, background);
    }

    private JPanel cardPanel(JPanel panel, Color background) {
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        return panel;
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new RaceClientGUI().setVisible(true));
    }
}
