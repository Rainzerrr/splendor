import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("[MENU PRINCIPAL]");
        System.out.println("1. Mode Simplifié (2 joueurs exactement)");
        System.out.println("2. Mode Complet (2 à 4 joueurs)");

        int choice = getValidChoice(scanner);

        switch (choice) {
            case 1 -> launchSimplifiedGame(scanner);
            case 2 -> launchCompleteGame(scanner);
        }

        scanner.close();
    }

    private static int getValidChoice(Scanner scanner) {
        while (true) {
            try {
                System.out.print("Votre choix (1-2) : ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Nettoie le buffer

                if (choice == 1 || choice == 2) {
                    return choice;
                } else {
                    System.out.println("Erreur : vous devez saisir 1 ou 2");
                }
            } catch (InputMismatchException e) {
                System.out.println("Erreur : vous devez saisir un nombre");
                scanner.nextLine(); // Vide le buffer des mauvaises entrées
            }
        }
    }

    private static void launchSimplifiedGame(Scanner scanner) {
        System.out.println("\n[MODE SIMPLIFIÉ]");
        SimplifiedGame game = new SimplifiedGame(2);

        // Ajout des 2 joueurs obligatoires
        for (int i = 1; i <= 2; i++) {
            System.out.print("Nom du Joueur " + i + " : ");
            String name = scanner.nextLine();
            while (name.isBlank()) {
                System.out.println("Erreur : le nom ne peut pas être vide");
                System.out.print("Nom du Joueur " + i + " : ");
                name = scanner.nextLine();
            }
            game.addPlayer(new Player(name));
        }

        game.launch();
    }

    private static void launchCompleteGame(Scanner scanner) {
        System.out.println("\n[MODE COMPLET]");

        int playerCount = getValidPlayerCount(scanner);
        CompleteGame game = new CompleteGame(playerCount);

        // Ajout des joueurs
        for (int i = 1; i <= playerCount; i++) {
            System.out.print("Nom du Joueur " + i + " : ");
            String name = scanner.nextLine();
            while (name.isBlank()) {
                System.out.println("Erreur : le nom ne peut pas être vide");
                System.out.print("Nom du Joueur " + i + " : ");
                name = scanner.nextLine();
            }
            game.addPlayer(new Player(name));
        }

        game.launch();
    }

    private static int getValidPlayerCount(Scanner scanner) {
        while (true) {
            try {
                System.out.print("Nombre de joueurs (2-4) : ");
                int count = scanner.nextInt();
                scanner.nextLine(); // Nettoie le buffer

                if (count >= 2 && count <= 4) {
                    return count;
                } else {
                    System.out.println("Erreur : vous devez saisir un nombre entre 2 et 4");
                }
            } catch (InputMismatchException e) {
                System.out.println("Erreur : vous devez saisir un nombre valide");
                scanner.nextLine(); // Vide le buffer des mauvaises entrées
            }
        }
    }
}