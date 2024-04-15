import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        Server server = new Server();
        server.displayServerInfo();
        server.start();
    }

    public void displayServerInfo() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            System.out.println("Server running on: " + localhost.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT, 0, InetAddress.getByName("0.0.0.0"))) {
            System.out.println("Server started. Listening on port " + PORT);

            // Start a separate thread to send messages to clients
            Thread messageSenderThread = new Thread(() -> {
                while (true) {
                    String message = getMessageFromUser();
                    broadcastMessage(message);
                }
            });
            messageSenderThread.start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Create a new client handler for the connected client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getMessageFromUser() {
        System.out.print("Enter message to send to clients: ");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    public void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}

class ClientHandler extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private String teamName;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String inputLine;
            String teamName = in.readLine();
            System.out.println("Team name: " + teamName);
            this.teamName = teamName;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from "+teamName+" : " + inputLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}
