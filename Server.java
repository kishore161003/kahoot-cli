import java.io.*;
import java.net.*;
import java.util.*;

class Question {
    private String question;
    private ArrayList<String> options;
    private int correctOption;

    public Question(String question, ArrayList<String> options, int correctOption) {
        this.question = question;
        this.options = options;
        this.correctOption = correctOption;
    }

    public String getQuestion() {
        return question;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public boolean isCorrect(int option) {
        return option == correctOption;
    }
}

public class Server {
    private static final int PORT = 12345;
    private List<ClientHandler> clients = new ArrayList<>();

    private static final List<Question> questions = new ArrayList<>();
    private static Scanner sc = new Scanner(System.in);

    private static void generateQuestions() {
        questions.add(new Question("What is the capital of France?",
                new ArrayList<>(Arrays.asList("Paris", "London", "Berlin", "Madrid")), 0));
        questions.add(new Question("What is the capital of Germany?",
                new ArrayList<>(Arrays.asList("Paris", "London", "Berlin", "Madrid")), 2));
        questions.add(new Question("What is the capital of Spain?",
                new ArrayList<>(Arrays.asList("Paris", "London", "Berlin", "Madrid")), 3));
        questions.add(new Question("What is the capital of England?",
                new ArrayList<>(Arrays.asList("Paris", "London", "Berlin", "Madrid")), 1));
    }

    private void askQuestion() {
        for (Question question : questions) {
            String questionString = question.getQuestion() + " ";
            ArrayList<String> options = question.getOptions();
            for (int i = 0; i < options.size(); i++) {
                questionString += (i + 1) + ". " + options.get(i) + ' ';
            }
            broadcastMessage(questionString);
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            checkAnswer(question);
        }
        displayScores();
    }

    public static void main(String[] args) {
        generateQuestions();
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
                    System.out.println("type start to start the quiz");
                    String message = sc.nextLine();
                    message = message.toLowerCase().trim();
                    if (message.equals("start")) {
                        askQuestion();
                        System.out.println("start the quiz again");
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

                if (q.isCorrect(Integer.parseInt(client.getAnswer()))) {
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

    private String answer ;

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
            System.out.println("Team name: " + teamName);
            this.teamName = teamName;
            while ((inputLine = in.readLine()) != null) {
                this.answer = inputLine;
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
