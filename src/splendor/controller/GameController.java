package splendor.controller;

import splendor.model.*;
import splendor.view.ConsoleInput;
import splendor.view.PlayerView;
import splendor.view.TerminalView;

import java.util.*;

public class GameController {
    private final Game game;
    private final TerminalView view;
    private final PlayerController playerController;
    private final ConsoleInput consoleInput;
    private final PlayerView playerView;

    public GameController(Game game) {
        this.game = game;
        this.view = new TerminalView();
        this.playerController = new PlayerController(new PlayerView());
        this.consoleInput = new ConsoleInput();
        this.playerView = new PlayerView();
    }

    /**
     * Starts the game loop.
     * This method:
     * - Initializes the game
     * - Repeats the following until the game is over:
     *   - Makes each player take a turn
     * - Displays the final ranking of the players
     */
    public void launchGame() {
        game.initializeGame();

        while (!game.isGameOver()) {
            game.getPlayers().forEach(this::handlePlayerTurn);
        }

        view.showFinalRanking(game.getPlayers());
    }


    /**
     * Handle a player's turn in the game. This involves:
     * - Displaying the current player
     * - Displaying the current state of the board
     * - Displaying the player's wallet
     * - Repeating the following until the player has completed an action:
     *   - Asking the user for a choice of action
     *   - Handling the chosen action
     *   - If the action is completed, display the player's wallet and the current state of the bank
     * - Claiming a noble if the player is eligible
     * - Displaying the player's information
     * @param player the current player
     */
    private void handlePlayerTurn(Player player) {
        view.showPlayerTurn(player);
        view.showBoard(game.getBank(), game.getDisplayedCards(), game.getNobles());

        var playerView = new PlayerView();
        playerView.showWallet(player);

        var isCompleteGame = game.isCompleteGame();
        boolean actionCompleted;

        do {
            view.showMenu(isCompleteGame);
            var action = view.getMenuChoice(isCompleteGame);
            actionCompleted = handleAction(action, player, isCompleteGame);

            if (actionCompleted) {
                playerView.showWallet(player);
                view.showBank(game.getBank());
            }

        } while (!actionCompleted);

        player.claimNobleIfEligible(game.getNobles());
    }


    /**
     * Handles the action chosen by the player during their turn.
     * Depending on whether the game is in complete mode or simplified mode,
     * it delegates the action handling to the appropriate method.
     *
     * @param action the action chosen by the player
     * @param player the current player
     * @param isCompleteGame a flag indicating if the game is in complete mode
     * @return true if the action was successfully completed, false otherwise
     */
    private boolean handleAction(int action, Player player, boolean isCompleteGame) {
        Objects.requireNonNull(player);
        if (action < 0) {
            throw new IllegalArgumentException("Invalid action code: " + action);
        }
        if (isCompleteGame) {
            return handleCompleteGameAction(action, player);
        } else {
            return handleSimplifiedGameAction(action, player);
        }
    }


    /**
     * Handles the action chosen by the player during their turn in complete mode.
     * This method uses a switch statement to delegate the action to the appropriate
     * handler based on the action code. It covers actions such as buying a card,
     * reserving a card, picking gems, and displaying nobles, cards, or the bank.
     *
     * @param action the action code chosen by the player
     * @param player the current player executing the action
     * @return true if the action was successfully completed, false if the action
     *         was to display information or if the action code is unrecognized
     */
    private boolean handleCompleteGameAction(int action, Player player) {
        return switch (action) {
            case 1 -> handleBuyCard(player);
            case 2 -> handlePickTwoSameGems(player);
            case 3 -> handlePickThreeDifferentGems(player);
            case 4 -> handleReserveCard(player);
            case 5 -> { view.showBank(game.getBank()); yield false; }
            case 6 -> { playerView.showPurchasedCards(player); yield false; }
            case 7 -> { view.showNobles(game.getNobles()); yield false; }
            case 8 -> { view.showCards(game.getDisplayedCards()); yield false; }
            case 9 -> handleBuyReservedCard(player);
            default -> false;
        };
    }

    /**
     * Handles the action chosen by the player during their turn in simplified mode.
     * This method uses a switch statement to delegate the action to the appropriate
     * handler based on the action code. It covers actions such as buying a card,
     * picking gems, and displaying cards or the bank.
     *
     * @param action the action code chosen by the player
     * @param player the current player executing the action
     * @return true if the action was successfully completed, false if the action
     *         was to display information or if the action code is unrecognized
     */
    private boolean handleSimplifiedGameAction(int action, Player player) {
        return switch (action) {
            case 1 -> handleBuyCard(player);
            case 2 -> handlePickTwoSameGems(player);
            case 3 -> handlePickThreeDifferentGems(player);
            case 4 -> { view.showCards(game.getDisplayedCards()); yield false; }
            case 5 -> { view.showBank(game.getBank()); yield false; }
            default -> false;
        };
    }

    /**
     * Handles the action of buying a card.
     * This method shows the player all the cards in the game, asks the player to choose one,
     * and then calls the player controller to attempt to buy the card. If the buying is successful,
     * the card is replaced in the game.
     *
     * @param player the current player executing the action
     * @return true if the card was successfully bought, false if the action was cancelled or if
     *         the card couldn't be bought
     */
    private boolean handleBuyCard(Player player) {
        List<DevelopmentCard> cards = game.getDisplayedCards();
        view.showCards(cards);

        int choice = view.selectCard(cards.size());
        if (choice < 0) return false;

        DevelopmentCard card = cards.get(choice);
        boolean success = playerController.buyCard(player, card, game.getBank());

        if (success) {
            game.replaceCard(card);
        }
        return success;
    }


    /**
     * Handles the action of reserving a development card for the player.
     * Displays available cards, allows the player to select one, and attempts
     * to reserve it through the player controller. If successful, the card is
     * replaced in the display, and a success message is shown. Otherwise, an
     * error message is displayed.
     *
     * @param player the current player attempting to reserve a card
     * @return true if the card was successfully reserved, false otherwise
     */
    private boolean handleReserveCard(Player player) {
        if (player.getReservedCards().size() >= 3) {
            view.displayMessage("Vous avez déjà 3 cartes réservées, vous ne pouvez pas en réserver d'autres.");
            return false;
        }

        List<DevelopmentCard> cards = game.getDisplayedCards();
        view.showCards(cards);

        int choice = view.selectCard(cards.size());
        if (choice < 0) {
            return false;
        }

        DevelopmentCard card = cards.get(choice);
        boolean success = playerController.reserveCard(player, card, game.getBank());

        if (success) {
            game.replaceCard(card);
            view.displayMessage("Réservation réussie !");
        } else {
            view.displayMessage("Impossible de réserver cette carte.");
        }

        return success;
    }

    /**
     * Handles the action of picking two same gem tokens for the player.
     * It displays a message, asks the player to select a gem token, and
     * attempts to pick two tokens of the same type. If successful, it adds
     * the tokens to the player's inventory and removes them from the bank,
     * and displays a success message. If there are not enough tokens in the
     * bank, it displays an error message and returns false.
     *
     * @param player the current player picking the tokens
     * @return true if the tokens were successfully picked, false otherwise
     */
    private boolean handlePickTwoSameGems(Player player) {
        view.displayMessage("Piochez deux même gemmes");
        GemToken token = consoleInput.selectGemToken();
        if (token == null) return false;

        if (game.getBank().getAmount(token) < 4) {
            view.showNotEnoughTokens();
            return false;
        }

        player.add(token, 2);
        game.removeTokenFromBank(token, 2);
        view.showTokensTaken(token, 2);
        return true;
    }

    private boolean handlePickThreeDifferentGems(Player player) {
        List<GemToken> pickedTokens = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            view.showRemainingChoices(3 - i);
            GemToken token = consoleInput.selectGemToken();

            if (token == null) {
                for (GemToken t : pickedTokens) {
                    player.remove(t, 1);
                    game.getBank().add(t, 1);
                }
                return false;
            }

            if (pickedTokens.contains(token)) {
                view.showTokenAlreadyTaken();
                i--;
                continue;
            }

            if (game.getBank().getAmount(token) <= 0) {
                view.showNoMoreTokens(token);
                i--;
                continue;
            }

            player.add(token, 1);
            game.removeTokenFromBank(token, 1);
            pickedTokens.add(token);
        }

        return true;
    }

    /**
     * Handles the action of buying a reserved card.
     * This method shows the player the cards they have reserved, asks the player to choose one,
     * and then calls the player controller to attempt to buy the card. If the buying is successful,
     * the card is removed from the player's reserved cards and a success message is shown. Otherwise,
     * an error message is displayed.
     *
     * @param player the current player executing the action
     * @return true if the card was successfully bought, false if the action was cancelled or if
     *         the card couldn't be bought
     */
    private boolean handleBuyReservedCard(Player player) {
        List<DevelopmentCard> reserved = player.getReservedCards();
        if (reserved.isEmpty()) {
            view.displayMessage("Vous n'avez aucune carte réservée à acheter.");
            return false;
        }

        playerView.showReservedCards(player);

        int choice = view.selectCard(reserved.size());
        if (choice < 0) {
            view.displayMessage("Achat annulé.");
            return false;
        }

        var card = reserved.get(choice);
        var success = playerController.buyCard(player, card, game.getBank());

        if (success) {
            player.removeReservedCard(card);
            view.displayMessage("Carte réservée achetée avec succès !");
        } else {
            view.displayMessage("Impossible d'acheter cette carte réservée.");
        }

        return success;
    }

}