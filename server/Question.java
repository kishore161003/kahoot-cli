package server;

import java.util.Arrays;

public class Question {
  int id;
  String type;
  String question;
  String[] options;
  String answer;

  // Constructor
  public Question(int id, String type, String question, String[] options, String answer) {
    this.id = id;
    this.type = type;
    this.question = question;
    this.options = options;
    this.answer = answer;
  }

  public String toString() {
    return "id: " + id + ", type: " + type + ", question: " + question + ", options: " + Arrays.toString(options)
        + ", answer: " + answer;
  }

  // Add getters and setters for the fields as needed
}
