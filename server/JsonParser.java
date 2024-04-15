package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import server.RemoveWhitespace;


public class JsonParser {

    public static void main(String[] args) {
        // Path to the JSON file
        String filePath = "server/question.json";

        // Read and parse the JSON file
        Question[] qrr = parseJsonFile(filePath);
        System.out.println(Arrays.toString(qrr));
    }

    public static Question[] parseJsonFile(String filePath) {
        StringBuilder jsonContent = new StringBuilder();

        // Read the file
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            throw new RuntimeException("Error reading file: " + e.getMessage());
        }

        // Convert the JSON content to a string
        String json = jsonContent.toString();
        // System.out.println(json);
        json = RemoveWhitespace.removeWhitespaceOutsideQuotes(json);
        // System.out.println(json);
        // Parse the JSON content
        return parseJson(json);
    }

    public static Question[] parseJson(String json) {
        List<Question> questionArr = new ArrayList<>();
        // Remove surrounding curly braces
        json = json.trim().substring(1, json.length() - 1);
        // System.out.println(json);
        // Split the JSON content by questions array
        String[] parts = json.split("\"questions\":\\[");
        if (parts.length < 2) {
            System.out.println("Invalid JSON format");
            throw new IllegalArgumentException("Invalid JSON format");
        }
        System.out.println(Arrays.toString(parts));

        // Get the questions array
        String questionsJson = parts[1].substring(1, parts[1].length() - 2).trim();
        System.out.println();
        System.out.println("Questions JSON:");
        System.out.println(questionsJson);
        System.out.println();

        // Split questions array into individual questions
        String[] questionJsons = questionsJson.split("\\},\\{");
        System.out.println();
        System.out.println("Question JSONs:");
        System.out.println(Arrays.toString(questionJsons));
        System.out.println(questionJsons.length);
        System.out.println(questionJsons[0]);
        System.out.println();

        // Loop through each question
        for (String questionJson : questionJsons) {
            // Clean up JSON format
            if (questionJson.startsWith("{")) {
                questionJson = questionJson.substring(1);
            }
            if (questionJson.endsWith("}")) {
                questionJson = questionJson.substring(0, questionJson.length() - 1);
            }

            // Parse each question
            Question question = parseQuestion(questionJson);
            if (question != null) {
                // Store or use the question object as needed
                System.out.println("Parsed question: " + question.question);
            }
            questionArr.add(question);
        }
        return questionArr.toArray(new Question[0]);
    }

    public static Question parseQuestion(String questionJson) {
        // Create a Question object and parse the JSON content
        int id = 0;
        String type = "";
        String question = "";
        String answer = "";

        // Split the question JSON into fields,
        // "id":1,"type":"mcq","question":"What is the capital of
        // France?","options":["Berlin","Madrid","Paris","Rome"],"answer":"Paris"
        // I don't want the options to be split by comma
        String[] fields = questionJson.split(",");

        List<String> options = new ArrayList<>();

        for (String field : fields) {
            // Split field into key and value
            if (field.contains("options")) {
                System.out.println("field: " + field);
            }
            field = field.replace("[", "").replace("]", "").trim();
            String[] keyValue = field.split(":");
            if (keyValue.length < 2) {
                String value = keyValue[0].replace("\"", "").trim();
                options.add(field);
                System.out.println("Invalid field: " + value);
                continue;
            }

            // Remove quotes and trim key and value
            String key = keyValue[0].replace("\"", "").trim();
            String value = keyValue[1].replace("\"", "").trim();
            if (key.equals("options")) {
                System.out.println("option: " + key + " value: " + Arrays.toString(keyValue));
            }
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
                    options.add(value);
                    break;
                case "answer":
                    answer = value;
                    break;
            }
        }
        Question q = new Question(id, type, question, options.toArray(new String[0]), answer);
        System.out.println();
        System.out.println(Arrays.toString(q.options));
        if (q.options.length > 0) {
            System.out.println(q.options[0]);
        }
        System.out.println();
        // Return a new Question object
        return q;
    }
}
