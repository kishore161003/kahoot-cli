package server;

import java.util.Arrays;
import java.util.Scanner;

interface QuestionTypes {
  String[] questionTypes = new String[] { "mcq", "true/false" };
}

public class Question implements QuestionTypes {
  private int id;
  private String type;
  private String question;
  private String[] options;
  private String answer;
  private int duration;

  // Getters
  public int getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public String getQuestion() {
    return question;
  }

  public String[] getOptions() {
    return options;
  }

  public String getAnswer() {
    return answer;
  }

  public int getDuration() {
    return duration;
  }

  // Setters
  public void setType(String type) {
    this.type = type;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public void setOptions(String[] options) {
    this.options = options;
  }

  public void setAnswer(String answer) {
    this.answer = answer;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  // Constructor
  public Question(int id, String type, String question, String[] options, String answer, int duration) {
    this.id = id;
    this.type = type;
    this.question = question;
    this.options = options;
    if (this.type.equals("true/false")) {
      options = new String[] { "true", "false" };
    }
    this.answer = answer;
    this.duration = duration;
  }

  public Question() {
  }

  public boolean verifyAnswer(String answer) {
    return this.answer.equals(answer);
  }

  public void setId(int id) {
    this.id = id;
  }

  public String toString() {
    return "id: " + id + ", type: " + type + ", question: " + question + ", options: " + Arrays.toString(options)
        + ", answer: " + answer + ", duration: ";
  }

  public String toJson() {
    return "{\"id\":" + id + ",\"type\":\"" + type + "\",\"question\":\"" + question + "\",\"options\":"
        + Arrays.toString(Arrays.asList(options).stream().map(option -> "\"" + option + "\"").toArray())
        + ",\"answer\":\"" + answer + "\",\"duration\":" + duration + "}";
  }
  // Add getters and setters for the fields as needed

  public void readQuestionName() {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Enter the question: ");
    question = scanner.nextLine();
  }

  public void readQuestionType() {
    Scanner scanner = new Scanner(System.in);
    for (int i = 0; i < questionTypes.length; i++) {
      System.out.println((i + 1) + ". " + questionTypes[i]);
    }
    while (true) {
      System.out.print("Choose the question type: ");
      int questionType = scanner.nextInt();
      if (questionType <= 0 || questionType > questionTypes.length) {
        System.out.println("Invalid question type. Please try again.");
        continue;
      }
      type = questionTypes[questionType - 1];
      break;
    }
    scanner.nextLine();
  }

  public void readOptions() {
    Scanner scanner = new Scanner(System.in);
    if (this.type.equals("true/false")) {
      options = new String[] { "true", "false" };
      return;
    }
    System.out.println("Enter number of options: ");
    int n = scanner.nextInt();
    scanner.nextLine();
    options = new String[n];
    System.out.print("Enter the options: ");
    for (int i = 0; i < n; i++) {
      System.out.print((i + 1) + ". ");
      options[i] = scanner.nextLine().strip();
    }
  }

  public void readAnswerName() {
    Scanner scanner = new Scanner(System.in);
    if (this.type.equals("true/false")) {
      while (true) {
        System.out.println("Enter the correct answer: ");
        System.out.println("1. True");
        System.out.println("2. False");
        System.out.print("Choose the answer: ");
        int answerNumber = scanner.nextInt();
        if (answerNumber == 1) {
          answer = "true";
          break;
        } else if (answerNumber == 2) {
          answer = "false";
          break;
        } else {
          System.out.println("Invalid choice. Please enter again.");
          continue;
        }
      }
      return;
    }
    System.out.print("Enter the answer from options(" + 1 + " to " + options.length+"):");
    answer = options[scanner.nextInt()-1];
    System.out.println("Answer: " + answer);
    scanner.nextLine();
  }

  public void readDuration() {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Enter the duration(seconds): ");
    duration = scanner.nextInt();
  }

  public void readQuestion(String questionJson) {
    // Parse the JSON content and set the fields
    // "id":1,"type":"mcq","question":"What is the capital of
    // France?","options":["Berlin","Madrid","Paris","Rome"],"answer":"Paris"
    // I don't want the options to be split by comma
    String[] fields = questionJson.split(",");

    for (String field : fields) {
      // Split field into key and value
      field = field.replace("[", "").replace("]", "").trim();
      String[] keyValue = field.split(":");
      if (keyValue.length < 2) {
        String value = keyValue[0].replace("\"", "").trim();
        options = value.split(",");
        continue;
      }

      // Remove quotes and trim key and value
      String key = keyValue[0].replace("\"", "").trim();
      String value = keyValue[1].replace("\"", "").trim();

      // Parse the fields
      switch (key) {
        case "id":
          id = Integer.parseInt(value);
          break;
        case "type":
          type = value;
          break;
        case "question":
          question = value;
          break;
        case "options":
          options = value.split(",");
          break;
        case "answer":
          answer = value;
          break;
      }
    }
  }
}
