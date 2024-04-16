package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static final int MIN_PORT = 1025;
    private static final int MAX_PORT = 65535;
    private static final Random random = new Random();
    private List<ClientHandler> clients = new ArrayList<>();

    private static final Quiz quiz = new Quiz();
    private static Scanner sc = new Scanner(System.in);

    private void askQuestion() {
        System.out.println("Asking questions");
        Question[] questions = quiz.getQuestions();
        for (Question question : questions) {
            String questionString = question.getQuestion() + " ::";
            String[] options = question.getOptions();
            for (int i = 0; i < options.length; i++) {
                questionString += (i + 1) + ". " + options[i] + ',';
            }
            questionString += "::" + question.getDuration();
            broadcastMessage(questionString);
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            checkAnswer(question);
        }
        displayScores();
    }

    public static void main(String[] args) {
        quiz.displayMenu();
        displayDashBoard();
    }

    private static void displayDashBoard() {
        displayServerInfo();
        System.out.println("KAHOOT SERVER");
        System.out.print("Host the quiz(Y/n):");
        String start = sc.nextLine();
        if (start.equalsIgnoreCase("n")) {
            System.exit(0);
        } else {
            Server server = new Server();
            server.start();
        }
    }

    public static void displayServerInfo() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            System.out.println("Server running on: " + localhost.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static int getRandomPort() {
        return random.nextInt(MAX_PORT - MIN_PORT + 1) + MIN_PORT;
    }

    public void start() {
        int port = getRandomPort();
        try (ServerSocket serverSocket = new ServerSocket(port, 0, InetAddress.getByName("0.0.0.0"))) {
            System.out.println("Game Id (port number) " + port);
            System.out.println("1. to start the quizz");
            System.out.println("2. to exit the server");

            Thread messageSenderThread = new Thread(() -> {
                while (true) {
                    System.out.print("Enter your choice(1/2): ");
                    int message = sc.nextInt();
                    if (message <= 0 || message >= 3) {
                        System.out.println("Invalid choice. Please try again.");
                        continue;
                    }
                    if (message == 1) {
                        askQuestion();
                        System.out.println("start the quiz again");
                    } else {
                        System.exit(0);
                    }
                }
            });
            messageSenderThread.start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Create a new client handler for the connected client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    private void checkAnswer(Question q) {
        for (ClientHandler client : clients) {
            try {
                System.out.println(client.getAnswer());
                if (q.verifyAnswer(Integer.parseInt(client.getAnswer().split(":")[0]))) {
                    client.addScore(10);
                }
            } catch (Exception e) {
                System.out.println("Error in checking answer");
            }
        }
    }

    private void displayScores() {
        for (ClientHandler client : clients) {
            System.out.println(client.getTeamName() + ": " + client.getScore());
        }
    }
}

class ClientHandler extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private String teamName;
    private int score;

    private String answer;

    public String getTeamName() {
        return teamName;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int score) {
        this.score += score;
    }

    public String getAnswer() {
        return answer;
    }

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
            while (teamName == null || teamName.isEmpty()) {
                teamName = in.readLine();
            }
            System.out.println("Team name: " + teamName);
            this.teamName = teamName;
            while ((inputLine = in.readLine()) != null) {
                this.answer = inputLine;
                System.out.println("Received from " + teamName + " : " + inputLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}