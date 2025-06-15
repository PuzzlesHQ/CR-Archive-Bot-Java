package dev.puzzleshq.utils;

public class StringUtils {
    public static String title(String input, boolean format) {
        if (input == null || input.isEmpty()) return input;
        input = (format ? input.replaceAll("[-_]", " ") : input);
        String[] words = input.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.isEmpty()) continue;

            String lowerWord = word.toLowerCase();

            // Capitalize first char always
            StringBuilder w = new StringBuilder();
            w.append(Character.toUpperCase(lowerWord.charAt(0)));

            if (lowerWord.length() > 1) {
                // Check if the last char is a letter and preceded by digits (basic heuristic)
                char lastChar = lowerWord.charAt(lowerWord.length() - 1);
                if (Character.isLetter(lastChar) && Character.isDigit(lowerWord.charAt(lowerWord.length() - 2))) {
                    // uppercase last char, lowercase middle part
                    w.append(lowerWord, 1, lowerWord.length() - 1);
                    w.append(Character.toUpperCase(lastChar));
                } else {
                    // normal lowercase for the remainder
                    w.append(lowerWord, 1, lowerWord.length());
                }
            }

            result.append(w).append(" ");
        }

        return result.toString().trim();
    }

    public static String title(String input) {
        return title(input, true);
    }
}