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
                scanner.nextLine(); // Consume newline
                if (input < min || input > max) {
                    System.out.println("Veuillez entrer un nombre entre " + min + " et " + max + ".");
                } else {
                    return input;
                }
            } catch (Exception e) {
                System.out.println("Erreur : veuillez entrer un nombre valide.");
                scanner.nextLine(); // Clear invalid input
            }
        }
    }

    public String askString(String prompt) {
        Objects.requireNonNull(prompt);
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public GemToken selectGemToken() {
        System.out.println("Choisissez une gemme :");
        System.out.println("1. RUBY, 2. EMERALD, 3. DIAMOND, 4. SAPPHIRE, 5. ONYX");
        var choice = askInt("Votre choix (1-5, 0 pour revenir au menu) : ", 0, 5);
        return switch (choice) {
            case 1 -> GemToken.RUBY;
            case 2 -> GemToken.EMERALD;
            case 3 -> GemToken.DIAMOND;
            case 4 -> GemToken.SAPPHIRE;
            case 5 -> GemToken.ONYX;
            default -> null;
        };
    }
}