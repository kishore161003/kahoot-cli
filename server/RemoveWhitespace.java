package server;
public class RemoveWhitespace {

    public static void main(String[] args) {
        // Example usage
        String input = "This is a  \"string with\" whitespaces \"inside and\" outside";
        String result = removeWhitespaceOutsideQuotes(input);
        System.out.println(result);
    }

    public static String removeWhitespaceOutsideQuotes(String input) {
        StringBuilder result = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

            // Check if we are entering or exiting double quotes
            if (currentChar == '"') {
                inQuotes = !inQuotes;
                result.append(currentChar);
            } else if (!inQuotes && Character.isWhitespace(currentChar)) {
                // Skip white spaces when not inside quotes
                continue;
            } else {
                // Add the character to the result
                result.append(currentChar);
            }
        }

        return result.toString();
    }
}
