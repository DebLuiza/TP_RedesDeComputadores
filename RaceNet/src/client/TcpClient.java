package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class TcpClient {
    private Socket socket;
    private PrintWriter out;

    public void connect(String host, int port, Consumer<String> onMessage) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

        Thread readerThread = new Thread(() -> readLoop(onMessage), "TCP-Client-Reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    public void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    private void readLoop(Consumer<String> onMessage) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            String message;
            while ((message = in.readLine()) != null) {
                onMessage.accept(message);
            }
        } catch (IOException exception) {
            onMessage.accept("STATUS;Conexão TCP encerrada: " + exception.getMessage());
        }
    }
}
