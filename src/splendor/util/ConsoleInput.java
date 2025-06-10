package splendor.util;

import splendor.model.GemToken;

import java.util.Objects;
import java.util.Scanner;

public class ConsoleInput {
    private final Scanner scanner = new Scanner(System.in);

    public int askInt(String prompt, int min, int max) {
        Objects.requireNonNull(prompt);
        while (true) {
            System.out.print(prompt);
            try {
                int input = scanner.nextInt();
                scanner.nextLine();
                if (input < min || input > max) {
                    System.out.println("Veuillez entrer un nombre entre " + min + " et " + max + ".");
                } else {
                    return input;
                }
            } catch (Exception e) {
                System.out.println("Erreur : veuillez entrer un nombre valide.");
                scanner.nextLine();
            }
        }
    }

    public String askString(String prompt) {
        Objects.requireNonNull(prompt);
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}