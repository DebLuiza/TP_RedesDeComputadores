package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class UdpClient {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;

    public void connect(String host, int port, Consumer<String> onMessage) throws IOException {
        serverAddress = InetAddress.getByName(host);
        serverPort = port;
        socket = new DatagramSocket();

        Thread readerThread = new Thread(() -> readLoop(onMessage), "UDP-Client-Reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    public void send(String message) throws IOException {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, serverPort);
        socket.send(packet);
    }

    private void readLoop(Consumer<String> onMessage) {
        byte[] buffer = new byte[1024];
        while (socket != null && !socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                onMessage.accept(message);
            } catch (IOException exception) {
                onMessage.accept("STATUS;Conexão UDP encerrada: " + exception.getMessage());
                return;
            }
        }
    }
}
