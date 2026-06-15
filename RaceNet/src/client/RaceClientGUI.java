package client;

import shared.Protocol;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
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
    private final JButton resetButton = new JButton("Nova Corrida");
    private final JButton accelerateButton = new JButton("ACELERAR");
    private final JProgressBar myProgress = new JProgressBar(0, 100);
    private final RaceTrackPanel raceTrackPanel = new RaceTrackPanel();
    private final JPanel opponentsPanel = new JPanel(new GridLayout(0, 1, 4, 4));
    private final JTextArea statusArea = new JTextArea(8, 40);
    private final JLabel connectionStatusLabel = new JLabel("Desconectado");
    private final JLabel timerLabel = new JLabel("Tempo: 0.0s");
    private final javax.swing.Timer raceTimer;
    private final TcpClient tcpClient = new TcpClient();
    private final UdpClient udpClient = new UdpClient();
    private final Map<String, JProgressBar> opponentBars = new LinkedHashMap<>();
    private final Random random = new Random();
    private boolean connected;
    private boolean raceStarted;
    private int myPosition;
    private long startTime;

    public RaceClientGUI() {
        super("RaceNet - Corrida Multiplayer");
        raceTimer = new javax.swing.Timer(100, e -> updateTimerLabel());
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
        buttonsPanel.add(resetButton);

        styleButton(connectButton, primary);
        styleButton(readyButton, new Color(91, 95, 110));
        styleButton(startButton, success);
        styleButton(resetButton, new Color(130, 136, 148));
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

        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        timerLabel.setForeground(primary);

        JPanel raceHeaderPanel = new JPanel(new BorderLayout());
        raceHeaderPanel.setOpaque(false);
        raceHeaderPanel.add(new JLabel("Minha corrida:"), BorderLayout.WEST);
        raceHeaderPanel.add(timerLabel, BorderLayout.EAST);

        JPanel racePanel = new JPanel(new BorderLayout(6, 6));
        racePanel.setOpaque(false);
        racePanel.add(raceHeaderPanel, BorderLayout.NORTH);
        racePanel.add(myProgress, BorderLayout.CENTER);

        opponentsPanel.setOpaque(false);

        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(raceTrackPanel, BorderLayout.NORTH);
        centerPanel.add(racePanel, BorderLayout.CENTER);
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
        resetButton.addActionListener(event -> sendTcp(Protocol.RESET));
        accelerateButton.addActionListener(event -> accelerate());
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "accelerate");
        getRootPane().getActionMap().put("accelerate", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                accelerate();
            }
        });
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
            long duration = System.currentTimeMillis() - startTime;
            double seconds = duration / 1000.0;
            sendTcp(Protocol.FINISH + ";" + playerName() + ";tempo=" + String.format("%.1f", seconds));
            accelerateButton.setEnabled(false);
            raceTimer.stop();
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
                    startTime = System.currentTimeMillis();
                    raceTimer.start();
                    accelerateButton.setEnabled(true);
                    startButton.setEnabled(false);
                    resetButton.setVisible(false);
                    updateMyProgress();
                    log("Corrida iniciada.");
                }
                case Protocol.WINNER -> {
                    raceStarted = false;
                    raceTimer.stop();
                    accelerateButton.setEnabled(false);
                    resetButton.setVisible(true);
                    String winner = parts.length > 1 ? parts[1] : "desconhecido";
                    log("Vencedor confirmado por TCP: " + winner);
                    JOptionPane.showMessageDialog(this, "Vencedor: " + winner);
                }
                case Protocol.RESET -> {
                    raceStarted = false;
                    raceTimer.stop();
                    myPosition = 0;
                    startTime = 0;
                    timerLabel.setText("Tempo: 0.0s");
                    updateMyProgress();
                    opponentBars.clear();
                    opponentsPanel.removeAll();
                    opponentsPanel.revalidate();
                    opponentsPanel.repaint();
                    raceTrackPanel.clear();
                    setButtons(true);
                    resetButton.setVisible(false);
                    log("A corrida foi resetada pelo servidor.");
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
        raceTrackPanel.updatePosition(playerName(), myPosition, true);
    }

    private void updateTimerLabel() {
        if (raceStarted) {
            long duration = System.currentTimeMillis() - startTime;
            timerLabel.setText(String.format("Tempo: %.1fs", duration / 1000.0));
        }
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
        raceTrackPanel.updatePosition(name, position, false);
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

    private static class RaceTrackPanel extends JPanel {
        private final Map<String, Integer> positions = new LinkedHashMap<>();
        private String mainPlayerName = "";

        RaceTrackPanel() {
            setPreferredSize(new Dimension(560, 220));
            setMinimumSize(new Dimension(560, 180));
            setBackground(new Color(30, 41, 59));
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        }

        void updatePosition(String name, int position, boolean mainPlayer) {
            if (mainPlayer) {
                mainPlayerName = name;
            }
            positions.put(name, Math.max(0, Math.min(100, position)));
            repaint();
        }

        void clear() {
            positions.clear();
            mainPlayerName = "";
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int left = 90;
            int right = width - 45;
            int trackWidth = right - left;
            int laneHeight = 52;
            int startY = 44;

            drawTrackTitle(graphics2D, width);
            drawFinishLine(graphics2D, right, startY, laneHeight);

            if (positions.isEmpty()) {
                graphics2D.setColor(new Color(203, 213, 225));
                graphics2D.drawString("Conecte e inicie a corrida para ver os carrinhos.", 95, 120);
                graphics2D.dispose();
                return;
            }

            int laneIndex = 0;
            for (Map.Entry<String, Integer> entry : positions.entrySet()) {
                int y = startY + laneIndex * laneHeight;
                drawLane(graphics2D, left, right, y, laneHeight);
                drawPlayerName(graphics2D, entry.getKey(), y);
                drawCar(graphics2D, entry.getKey(), entry.getValue(), left, trackWidth, y);
                laneIndex++;
            }

            graphics2D.dispose();
        }

        private void drawTrackTitle(Graphics2D graphics2D, int width) {
            graphics2D.setColor(new Color(226, 232, 240));
            graphics2D.setFont(new Font("Segoe UI", Font.BOLD, 17));
            graphics2D.drawString("Pista RaceNet", 14, 24);

            graphics2D.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            graphics2D.setColor(new Color(148, 163, 184));
            graphics2D.drawString("Clique em ACELERAR para movimentar seu carrinho", width - 310, 24);
        }

        private void drawLane(Graphics2D graphics2D, int left, int right, int y, int laneHeight) {
            graphics2D.setColor(new Color(51, 65, 85));
            graphics2D.fillRoundRect(left, y, right - left, laneHeight - 10, 18, 18);

            graphics2D.setColor(new Color(148, 163, 184));
            graphics2D.setStroke(new BasicStroke(2));
            int middleY = y + (laneHeight - 10) / 2;
            for (int x = left + 12; x < right - 20; x += 34) {
                graphics2D.drawLine(x, middleY, x + 16, middleY);
            }
        }

        private void drawFinishLine(Graphics2D graphics2D, int right, int startY, int laneHeight) {
            int finishHeight = 3 * laneHeight;
            graphics2D.setColor(new Color(248, 250, 252));
            graphics2D.fillRect(right - 8, startY - 4, 8, finishHeight);

            graphics2D.setColor(new Color(15, 23, 42));
            for (int y = startY - 4; y < startY - 4 + finishHeight; y += 12) {
                graphics2D.fillRect(right - 8, y, 4, 6);
                graphics2D.fillRect(right - 4, y + 6, 4, 6);
            }
        }

        private void drawPlayerName(Graphics2D graphics2D, String name, int y) {
            graphics2D.setFont(new Font("Segoe UI", Font.BOLD, 12));
            graphics2D.setColor(name.equals(mainPlayerName) ? new Color(125, 211, 252) : new Color(226, 232, 240));
            graphics2D.drawString(name, 14, y + 25);
        }

        private void drawCar(Graphics2D graphics2D, String name, int position, int left, int trackWidth, int y) {
            int carWidth = 42;
            int carHeight = 22;
            int carX = left + Math.round((trackWidth - carWidth - 10) * (position / 100f));
            int carY = y + 9;
            Color carColor = name.equals(mainPlayerName) ? new Color(249, 115, 22) : new Color(59, 130, 246);

            graphics2D.setColor(new Color(15, 23, 42, 100));
            graphics2D.fillOval(carX + 3, carY + 17, carWidth - 6, 9);

            graphics2D.setColor(carColor);
            graphics2D.fillRoundRect(carX, carY + 6, carWidth, carHeight - 6, 12, 12);
            graphics2D.fillRoundRect(carX + 9, carY, 22, 14, 10, 10);

            graphics2D.setColor(new Color(224, 242, 254));
            graphics2D.fillRoundRect(carX + 16, carY + 3, 12, 8, 6, 6);

            graphics2D.setColor(new Color(15, 23, 42));
            graphics2D.fillOval(carX + 7, carY + 19, 9, 9);
            graphics2D.fillOval(carX + 27, carY + 19, 9, 9);

            graphics2D.setColor(new Color(226, 232, 240));
            graphics2D.setFont(new Font("Segoe UI", Font.BOLD, 11));
            String label = position + "%";
            FontMetrics metrics = graphics2D.getFontMetrics();
            graphics2D.drawString(label, carX + (carWidth - metrics.stringWidth(label)) / 2, carY - 4);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new RaceClientGUI().setVisible(true));
    }
}
