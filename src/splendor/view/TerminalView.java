package splendor.view;

import splendor.model.*;
import splendor.util.ConsoleInput;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class TerminalView implements SplendorsView {
    private final ConsoleInput input = new ConsoleInput();
// TODO add game as attribute to avoid function parameters
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
     * Displays the development cards of the game to the user.
     *
     * For a complete game, the cards are grouped by level and displayed in ascending order.
     * For a simplified game, the cards are displayed in a single list.
     *
     * @param game the type of game
     */
    public void showCards(Game game) {
        Objects.requireNonNull(game);
        var cards = game.getDisplayedCards();

        switch (game) {
            case CompleteGame c -> {
                System.out.println("CARTES DISPONIBLES :\n");

                cards.stream()
                        .collect(Collectors.groupingBy(DevelopmentCard::level))
                        .entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(entry -> {
                            System.out.println("----- NIVEAU " + entry.getKey() + " -----");
                            entry.getValue().forEach(card ->
                                    System.out.println("• " + card.toString())
                            );
                            System.out.println();
                        });
            }
            case SimplifiedGame s -> {
                System.out.println("CARTES DISPONIBLES :\n");
                IntStream.range(0, cards.size())
                        .forEach(i -> System.out.println((i + 1) + " - " + cards.get(i)));
                System.out.println();
            }
            default -> throw new IllegalStateException("Type de jeu inconnu");
        }
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
     * @param game The type of game.
     */
    public void showMenu(Game game) {
        displayMessage("[ACTIONS DISPONIBLES]");
        switch(game) {
            case CompleteGame c -> displayMessage(
                    "Actions : 1. Acheter | 2. 2 gemmes identiques | 3. 3 gemmes différentes | 4. Réserver | 5. Acheter carte réservée\n" +
                            "Affichage : 6. Nobles | 7. Cartes sur le plateau | 8. Contenu de la banque | 9. Cartes achetées "
            );
            case SimplifiedGame s -> displayMessage(
                    "Actions : 1. Acheter | 2. 2 gemmes identiques | 3. 3 gemmes différentes\n" +
                            "Affichage : 4. Cartes sur le plateau | 5. Contenu de la banque"
            );
        }
    }



    /**
     * Displays the state of the board to the user. This includes the gemstones
     * available in the bank, the development cards on the board, and the
     * nobles (if any) that are present in the game.
     *
     * @param game the game to display the board for
     */
    public void showBoard(Game game) {
        var nobles = game.getNobles();
        showBank(game.getBank());
        showCards(game);
        if (!nobles.isEmpty()) showNobles(nobles);
    }

    /**
     * Displays the main menu of the game to the user, and asks for input to
     * select an action to take.
     *
     * @param game Whether the game is complete or not. If true, the
     *        menu will include options to display the state of the board.
     * @return the choice of the user, as an integer. The value will be between
     */
    public int getMenuChoice(Game game) {
        return switch (game){
            case SimplifiedGame s -> input.askInt("Votre choix : ", 1, 5);
            case CompleteGame s -> input.askInt("Votre choix : ", 1, 9);
        };
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

    public GemToken askGemToDiscard(Player player) {
        List<GemToken> types = Arrays.stream(GemToken.values())
                .filter(t -> player.getTokenCount(t) > 0)
                .toList();

        System.out.println("Choisissez une gemme à défausser :");
        for (int i = 0; i < types.size(); i++) {
            GemToken t = types.get(i);
            System.out.printf("%d) %s (x%d)%n",
                    i + 1,
                    t,
                    player.getTokenCount(t));
        }
        var choix = input.askInt("Votre choix (1–" + types.size() + ") : ",
                1,
                types.size());
        return types.get(choix - 1);
    }
}
