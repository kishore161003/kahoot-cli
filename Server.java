import java.io.*;
import java.net.*;
import java.util.*;

class Question {
    private String question;
    private ArrayList<String> options;
    private int correctOption;
    private int waitingTime;

    public Question(String question, ArrayList<String> options, int correctOption, int waitingTime) {
        this.question = question;
        this.options = options;
        this.correctOption = correctOption;
        this.waitingTime = waitingTime;
    }

    public int getWaitingTime() {
        return waitingTime;
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
    private static final int MIN_PORT = 1025;
    private static final int MAX_PORT = 65535;
    private static final Random random = new Random();
    private List<ClientHandler> clients = new ArrayList<>();

    private static final List<Question> questions = new ArrayList<>();
    private static Scanner sc = new Scanner(System.in);

    private static void generateQuestions() {
        questions.add(new Question("What is the capital of France?",
                new ArrayList<>(Arrays.asList("Paris", "London", "Berlin", "Madrid")), 0 , 10000));
        questions.add(new Question("What is the capital of Germany?",
                new ArrayList<>(Arrays.asList("Paris", "London", "Berlin", "Madrid")), 2,5000));
        questions.add(new Question("What is the capital of Spain?",
                new ArrayList<>(Arrays.asList("Paris", "London", "Berlin", "Madrid")), 3,3000));
        questions.add(new Question("What is the capital of England?",
                new ArrayList<>(Arrays.asList("Paris", "London", "Berlin", "Madrid")), 1,2000));
    }

    private void askQuestion() {
        System.out.println("Asking questions");
        for (Question question : questions) {
            String questionString = question.getQuestion() + " ::";
            ArrayList<String> options = question.getOptions();
            for (int i = 0; i < options.size(); i++) {
                questionString += (i + 1) + ". " + options.get(i) + ',';
            }
            questionString += "::" + question.getWaitingTime();
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
        generateQuestions();
        displayDashBoard();
    }

    private static void displayDashBoard() {
        displayServerInfo();
        System.out.println("KAHOOT SERVER");
        System.out.println("1. Start the quiz");
        System.out.println("2. Create new quiz");
        while (true) {
            try {
                int choice = sc.nextInt();
                if (choice == 1) {
                    Server server = new Server();
                    server.start();
                } else if (choice == 2) {
                    Server server = new Server();
                    server.start();
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter 1 or 2");
            }
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
            System.out.println("1. Enter start to start the quizz");
            System.out.println("2. Enter exit to exit the server");

            Thread messageSenderThread = new Thread(() -> {
                while (true) {
                    String message = sc.nextLine();
                    message = message.toLowerCase().trim();
                    if (message.equals("start")) {
                        askQuestion();
                        System.out.println("start the quiz again");
                    } else if (message.equals("exit")) {
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
    public int calculateScore(int sec) {
        final int MAX_SCORE = 1000;
        final int MIN_SCORE = 100;
        int score = MAX_SCORE - (sec * ((MAX_SCORE - MIN_SCORE) / 60));
        score = Math.max(MIN_SCORE, score);  
        return score;
    }

    private void checkAnswer(Question q) {
        for (ClientHandler client : clients) {
            try {
                System.out.println(client.getAnswer());
                if (q.isCorrect(Integer.parseInt(client.getAnswer().split("::")[0]))) {
                    int score = calculateScore(Integer.parseInt(client.getAnswer().split("::")[1]));
                    client.addScore(score);
                    client.addScoreArray(score);
                }
            } catch (Exception e) {
                System.out.println("Error in checking answer");
            }
        }
    }
    public void displayScores(int n) {
        System.out.println("       Leaderboard      ");
        System.out.println("+-------+-----------------+----------+");
        System.out.println("| S.No. |    Team Name    |  Points  |");
        System.out.println("+-------+-----------------+----------+");
        // clients.sort(Comparator.comparingInt(ClientHandler::getScore).reversed());
        Collections.sort(clients, Comparator.comparingInt(ClientHandler::getScore).reversed());

        int serialNumber = 1;
        int numberOfEntries = Math.min(clients.size(), n); 
        for (int i = 0; i < numberOfEntries; i++) {
            ClientHandler client = clients.get(i);
            System.out.printf("| %-5d | %-15s | %-8d |%n", serialNumber++, client.getTeamName(), client.getScore());
        }
        System.out.println("+-------+-----------------+----------+");
    }
    public void displayScores() {
        System.out.println("       Leaderboard      ");
        System.out.println("+-------+-----------------+----------+");
        System.out.println("| S.No. |    Team Name    |  Points  |");
        System.out.println("+-------+-----------------+----------+");
        int serialNumber = 1;
        Collections.sort(clients, Comparator.comparingInt(ClientHandler::getScore).reversed());

        for (ClientHandler client : clients) {
            System.out.printf("| %-5d | %-15s | %-8d |%n", serialNumber++, client.getTeamName(), client.getScore());
        }
        System.out.println("+-------+-----------------+----------+");
    }   
}
class ClientHandler extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private String teamName;
    private int score;
    private ArrayList<Integer> scores = new ArrayList<>();

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
    public void addScoreArray(int score) {
        scores.add(score);
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
            while(teamName == null || teamName.isEmpty()) {
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