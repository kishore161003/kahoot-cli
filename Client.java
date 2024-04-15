
// Client.java
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        final String SERVER_ADDRESS = "localhost";
        final int PORT = 12345;

        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Start a separate thread to continuously receive messages from the server
            Thread messageReceiverThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("Server message: " + message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            messageReceiverThread.start();

            // Read user input and send it to the server
            Scanner scanner = new Scanner(System.in);
            String userInput;
            while ((userInput = scanner.nextLine()) != null) {
                out.println(userInput);
            }
            scanner.close();
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + SERVER_ADDRESS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
