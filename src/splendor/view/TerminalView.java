package splendor.view;

import splendor.model.DevelopmentCard;
import splendor.model.GemStock;
import splendor.model.Noble;
import splendor.model.Player;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class TerminalView {
    public void displayMessage(String message){
        Objects.requireNonNull(message);
        System.out.println(message);
    }

    public void displayActions(int min, int max){

    }

    public void showBank(GemStock bank) {
        Objects.requireNonNull(bank);
        System.out.println("JETONS DISPONIBLES :\n" + bank + "\n");
    }

    public void showCards(List<DevelopmentCard> cards) {
        Objects.requireNonNull(cards);
        System.out.println("CARTES DISPONIBLES :\n");
        for (var i = 0; i < cards.size(); i++) {
            System.out.println((i + 1) + " - " + cards.get(i).toString());
        }
        System.out.println();
    }

    public void showNobles(List<Noble> nobles) {
        Objects.requireNonNull(nobles);
        System.out.println("NOBLES DISPONIBLES :\n");
        for (var i = 0; i < nobles.size(); i++) {
            System.out.println((i + 1) + " - " + nobles.get(i).toString());
        }
    }

    public void showPlayerTurn(Player player) {
        Objects.requireNonNull(player);
        System.out.println(player.getName() + "\n");
    }

    public void showMenu() {
        displayMessage("[ACTIONS DISPONIBLES]");
        displayMessage("Actions : 1. Acheter | 2. Réserver | 3. 2 gemmes identiques | 4. 3 gemmes différentes");
        displayMessage("Afficher : 5. Nobles | 6. Cartes sur le plateau | 7. Contenu de la banque");
    }

    public int askInt(String prompt, int min, int max) {
        Objects.requireNonNull(prompt);
        if (min > max) {
            throw new IllegalArgumentException("Le minimum doit être plus petit que le maximum.");
        } else if (min < 0 || max < 0) {
            throw new IllegalArgumentException("Le minimum et le maximum doivent avoir une valeur positive.");
        }

        var scanner = new Scanner(System.in);

        while (true) {
            displayMessage(prompt);
            try {
                int input = scanner.nextInt();
                if (input < min || input > max) {
                    displayMessage("Veuillez entrer un nombre entre " + min + " et " + max + ".");
                } else {
                    return input;
                }
            } catch (InputMismatchException e) {
                displayMessage("Erreur : veuillez entrer un nombre entier.");
                scanner.nextLine();
            }
        }
    }
}
