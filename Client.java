import java.io.*;
import java.net.*;
import java.util.Scanner;

class SharedData {
    private String value;
    private boolean isSet = false;

    public synchronized void setValue(String newValue) {
        isSet = true;
        value = newValue;
    }

    public synchronized void clearValue() {
        isSet = false;
        value = null;
    }

    public synchronized boolean isSet() {
        return isSet;
    }

    public synchronized String getValue() {
        return value;
    }
}

public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Prompt user to enter the server's IP address
        System.out.print("Enter the server's IP address: ");
        String SERVER_ADDRESS = scanner.nextLine();
        final int PORT = 12345;

        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            System.out.print("Enter your team name: ");
            String teamName = scanner.nextLine();
            out.println(teamName);
            SharedData sharedData = new SharedData();
            Thread messageReceiverThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("Server message: " + message);
                        try{
                            Thread receiveSenderThread = new Thread(() -> {
                                try {
                                    String response = scanner.nextLine();
                                    sharedData.setValue(response);
                                    out.println(response);
                                } catch (Exception e) {
                                    sharedData.clearValue();
                                }
                            });
                            Thread messageSenderThread = new Thread(() -> {
                                try {
                                    Thread.sleep(5000);
                                    if (!sharedData.isSet()) {
                                        System.out.println("Time out");
                                        out.println("Un answered");
                                        receiveSenderThread.interrupt();
                                    }
                                    sharedData.clearValue();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            });
                            receiveSenderThread.start();
                            messageSenderThread.start();
                        }catch (Exception e){
                            System.out.println("Error in sending message");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            messageReceiverThread.start();
            messageReceiverThread.join();
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + SERVER_ADDRESS);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
