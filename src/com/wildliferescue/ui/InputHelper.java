package com.wildliferescue.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//reads and checks everything the user types
//until the input is valid, so the rest of the program always gets good data.
public class InputHelper {

    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    //text that must not be empty
    public String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("  This field is required.");
        }
    }

    //text that may be left empty
    public String readOptionalText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    //A whole number between min and max
    public int readInt(String prompt, int min, int max) {
        while (true) {
            String text = readText(prompt);
            try {
                int number = Integer.parseInt(text);
                if (number >= min && number <= max) {
                    return number;
                }
                System.out.println("  Please enter a number from " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  '" + text + "' is not a whole number.");
            }
        }
    }

    //yes - no
    public boolean readYesNo(String prompt) {
        while (true) {
            String text = readText(prompt).toLowerCase();
            if (text.equals("y") || text.equals("yes")) {
                return true;
            }
            if (text.equals("n") || text.equals("no")) {
                return false;
            }
            System.out.println("  Please answer y or n.");
        }
    }

    //one of a fixed list of words
    //the method accepts any number of options
    public String readChoice(String prompt, String... options) {
        while (true) {
            String text = readText(prompt);
            for (String option : options) {
                if (option.equalsIgnoreCase(text)) {
                    return option;
                }
            }
            System.out.println("  Please enter one of: " + String.join(", ", options) + ".");
        }
    }

    //phone num
    public String readPhone(String prompt) {
        while (true) {
            String text = readText(prompt);
            if (text.matches("0\\d{9}")) {
                return text;
            }
            System.out.println("  Use 10 digits starting with 0, e.g. 0771234567.");
        }
    }

    //empty input gives an empty list
    public List<String> readList(String prompt) {
        List<String> items = new ArrayList<>();
        for (String part : readOptionalText(prompt).split(",")) {
            if (!part.isBlank()) {
                items.add(part.trim());
            }
        }
        return items;
    }
}