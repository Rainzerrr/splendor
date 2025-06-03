package splendor.view;

import splendor.model.*;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class TerminalView {
    private final ConsoleInput input = new ConsoleInput();

    /**
     * Displays a message to the user.
     *
     * @param message the message to be displayed
     */
    public void displayMessage(String message){
        Objects.requireNonNull(message);
        System.out.println(message);
    }

    /**
     * Displays the current stock of gemstones available in the bank.
     *
     * @param bank the current stock of gemstones
     */
    public void showBank(GemStock bank) {
        Objects.requireNonNull(bank);
        System.out.println("JETONS DISPONIBLES :\n" + bank + "\n");
    }

    /**
     * Displays the list of development cards available to the user.
     * Each card is shown with an index number starting from 1.
     *
     * @param cards the list of development cards to display
     */
    public void showCards(List<DevelopmentCard> cards) {
        Objects.requireNonNull(cards);
        System.out.println("CARTES DISPONIBLES :\n");
        for (var i = 0; i < cards.size(); i++) {
            System.out.println((i + 1) + " - " + cards.get(i).toString());
        }
        System.out.println();
    }

    /**
     * Displays the nobles of the game to the user.
     *
     * @param nobles the nobles of the game.
     */
    public void showNobles(List<Noble> nobles) {
        System.out.println("[NOBLES DE LA PARTIE]");
        nobles.forEach(System.out::println);
        System.out.println();
    }

    /**
     * Displays the name of the player whose turn it is. The name is displayed
     * with a newline after it.
     *
     * @param player the player whose turn it is
     */
    public void showPlayerTurn(Player player) {
        Objects.requireNonNull(player);
        System.out.println(player.getName() + "\n");
    }

    /**
     * Displays the main menu of the game to the user. The menu will indicate which actions are available, and which actions can be used to display the state of the board.
     *
     * @param isCompleteGame Whether the game is complete or not. If true, the menu will include options to display the state of the board.
     */
    public void showMenu(boolean isCompleteGame) {
        displayMessage("[ACTIONS DISPONIBLES]");
        if (isCompleteGame) {
            displayMessage(
                    "Actions : 1. Acheter | 2. 2 gemmes identiques | 3. 3 gemmes différentes | " +
                            "4. Réserver | 5. Nobles | 6. Cartes sur le plateau | 7. Contenu de la banque | 8. Cartes achetées | 9. Acheter carte réservée"
            );
        } else {
            displayMessage(
                    "Actions : 1. Acheter | 2. 2 gemmes identiques | 3. 3 gemmes différentes | " +
                            "4. Cartes sur le plateau | 5. Contenu de la banque"
            );
        }
    }

    /**
     * Shows the current state of the board to the user, including the current stock of gemstones, the development cards available for purchase, and the nobles present in the game.
     *
     * @param bank the current stock of gemstones
     * @param cards the development cards available for purchase
     * @param nobles the nobles present in the game
     */
    public void showBoard(GemStock bank, List<DevelopmentCard> cards, List<Noble> nobles) {
        showBank(bank);
        showCards(cards);
        if (!nobles.isEmpty()) showNobles(nobles);
    }

    /**
     * Displays the main menu of the game to the user, and asks for input to
     * select an action to take.
     *
     * @param isCompleteGame Whether the game is complete or not. If true, the
     *        menu will include options to display the state of the board.
     * @return the choice of the user, as an integer. The value will be between
     */
    public int getMenuChoice(boolean isCompleteGame) {
        var max = isCompleteGame ? 9 : 5;
        return input.askInt("Votre choix : ", 1, max);
    }

    /**
     * Displays a prompt to the user to select a card from a list, and
     * returns the index of the selected card (or -1 if 0 is chosen).
     *
     * @param maxIndex the maximum valid index of the list (1-indexed)
     * @return the index of the card selected by the user, or -1 if 0 was
     *         chosen.
     */
    public int selectCard(int maxIndex) {
        return input.askInt("Sélectionnez une carte (1-" + maxIndex + ", 0 pour annuler) : ", 0, maxIndex) - 1;
    }

    /* ---- Display of messages ---- */
    /**
     * Informs the user that there are not enough tokens of a certain type available in the bank.
     */
    public void showNotEnoughTokens() {
        System.out.println("Pas assez de jetons de ce type dans la banque !");
    }

    /**
     * Displays a message indicating that a certain number of tokens of a specific type have been added.
     *
     * @param token the type of token that has been taken
     * @param amount the number of tokens that have been taken
     */
    public void showTokensTaken(GemToken token, int amount) {
        Objects.requireNonNull(token);
        System.out.println(amount + " jeton(s) " + token + " ajouté(s) !\n");
    }

    /**
     * Displays the final ranking of the players after the end of the game. The
     * ranking is sorted by prestige score, and then by the number of cards
     * purchased. The message is displayed to the user.
     *
     * @param players the list of players to rank
     */
    public void showFinalRanking(List<Player> players) {
        Objects.requireNonNull(players);
        displayMessage("Nous avons un vainqueur !");
        displayMessage("[CLASSEMENT FINAL]");

        players.sort((p1, p2) -> {
            int scoreCompare = Integer.compare(p2.getPrestigeScore(), p1.getPrestigeScore());
            if (scoreCompare != 0) return scoreCompare;
            return Integer.compare(p2.getPurchasedCardsCount(), p1.getPurchasedCardsCount());
        });

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            System.out.println("#" + (i + 1) + " " + p.getName() + " - " + p.getPrestigeScore() + " pts");
        }
    }

    /**
     * Displays a message indicating that the user has already taken a token of a certain type.
     */
    public void showTokenAlreadyTaken() {
        System.out.println("Vous avez déjà pris ce type de jeton.");
    }

    /**
     * Displays a message indicating that there are no more tokens available for a specific type.
     *
     * @param token the type of token that is no longer available
     */
    public void showNoMoreTokens(GemToken token) {
        System.out.println("Plus de jetons disponibles pour : " + token);
    }

    /**
     * Displays a message indicating how many tokens the user still has to choose.
     *
     * @param remaining the number of tokens the user still has to choose
     */
    public void showRemainingChoices(int remaining) {
        System.out.println("Nombre de gemmes restantes à choisir : " + remaining);
    }
}
