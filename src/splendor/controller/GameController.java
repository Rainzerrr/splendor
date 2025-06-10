    package splendor.controller;

    import splendor.model.*;
    import splendor.util.ConsoleInput;
    import splendor.view.GraphicView;
    import splendor.view.PlayerView;
    import splendor.view.SplendorView;
    import splendor.view.TerminalView;

    import java.util.*;

    public class GameController {
        private final Game game;
        private final SplendorView view;
        private final PlayerController playerController;
        private final PlayerView playerView;
        public static final int MAX_GEMS = 10;

        public GameController(Game game, SplendorView view) {
            this.game = game;
            this.view = view;
            this.playerController = new PlayerController(new PlayerView());
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
         * Handles a player's turn in the game loop.
         * This method:
         * - Displays a message indicating the start of the player's turn
         * - Displays the current state of the game
         * - Displays the player's wallet
         * - Repeats the following until the player completes an action:
         *   - Displays the menu of possible actions
         *   - Asks the player to choose an action
         *   - Handles the chosen action
         *   - Displays the updated state of the game if the action was completed
         * - Checks if the player is eligible for a noble card and claims it if so
         * - Enforces the gem limit for the player
         * - Displays the updated state of the game
         */
        private void handlePlayerTurn(Player player) {
            Objects.requireNonNull(player, "player cannot be null");
            view.showPlayerTurn(player, game);
            view.showBoard(game);

            var playerView = new PlayerView();
            playerView.showWallet(player);

            boolean actionCompleted;
            do {
                view.showMenu(game);
                var action = view.getMenuChoice(game);
                actionCompleted = handleAction(action, player, game);

                if (actionCompleted) {
                    playerView.showWallet(player);
                    view.showBank(game.getBank());
                }
                else{
                    switch(view){
                        case TerminalView t -> {}
                        case GraphicView g -> view.displayMessage("Action incorrect. " + player.getName() + ", sélectionnez une action dans le menu");
                    }
                }
            } while (!actionCompleted);

            player.claimNobleIfEligible(game.getNobles());
            enforceGemLimit(player);
            playerView.showWallet(player);
            view.showBoard(game);
        }

        private void enforceGemLimit(Player player) {
            Objects.requireNonNull(player, "player cannot be null");
            int total = player.getTotalAmount();
            if (total <= MAX_GEMS) return;

            int toDiscard = total - MAX_GEMS;
            view.displayMessage("Vous avez " + total +
                    " gemmes, vous devez en défausser " + toDiscard + ".");

            while (player.getTotalAmount() > MAX_GEMS) {
                var choice = view.askGemToDiscard(player);
                player.discardGem(choice);
                game.getBank().add(choice, 1);
                playerView.showWallet(player);
            }
        }

        /**
         * Handles the action chosen by the player during their turn.
         * Depending on whether the game is in complete mode or simplified mode,
         * it delegates the action handling to the appropriate method.
         *
         * @param action the action chosen by the player
         * @param player the current player
         * @param game a flag indicating if the game is in complete mode
         * @return true if the action was successfully completed, false otherwise
         */
        private boolean handleAction(int action, Player player, Game game) {
            Objects.requireNonNull(player, "player cannot be null");
            Objects.requireNonNull(game, "game cannot be null");
            if (action < 0) {
                throw new IllegalArgumentException("Invalid action code: " + action);
            }
            return switch (game) {
                case SimplifiedGame s -> handleSimplifiedGameAction(action, player);
                case CompleteGame c -> handleCompleteGameAction(action, player);
            };
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
            Objects.requireNonNull(player, "player cannot be null");
            Objects.requireNonNull(game, "game cannot be null");

            if (action < 0) {
                throw new IllegalArgumentException("action must not be negative");
            }
            return switch (action) {
                case 1 -> handleBuyCard(player);
                case 2 -> handlePickTwoSameGems(player);
                case 3 -> handlePickThreeDifferentGems(player);
                case 4 -> handleReserveCard(player);
                case 5-> handleBuyReservedCard(player);
                case 6 -> { view.showNobles(game.getNobles()); yield false; }
                case 7 -> { view.showCards(game); yield false; }
                case 8 -> { view.showBank(game.getBank()); yield false; }
                case 9 -> { playerView.showPurchasedCards(player); yield false; }
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
            Objects.requireNonNull(player, "player cannot be null");

            if (action < 0) {
                throw new IllegalArgumentException("action must not be negative");
            }
            return switch (action) {
                case 1 -> handleBuyCard(player);
                case 2 -> handlePickTwoSameGems(player);
                case 3 -> handlePickThreeDifferentGems(player);
                case 4 -> { view.showCards(game); yield false; }
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
            Objects.requireNonNull(player, "player cannot be null");
            List<DevelopmentCard> cards = game.getDisplayedCards();
            view.showCards(game);
            switch(view){
                case TerminalView t -> {}
                case GraphicView g -> view.displayMessage("Sélectionner la carte de développement à acheter");
            }

            int choice = view.selectCard(cards.size(), false);
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
            Objects.requireNonNull(player, "player cannot be null");
            if (player.getReservedCards().size() >= 3) {
                view.displayMessage("Vous avez déjà 3 cartes réservées, vous ne pouvez pas en réserver d'autres.");
                return false;
            }

            List<DevelopmentCard> cards = game.getDisplayedCards();
            view.showCards(game);

            switch(view){
                case TerminalView t -> {}
                case GraphicView g -> view.displayMessage("Sélectionner la carte de développement à réserver");
            }

            int choice = view.selectCard(cards.size(), false);
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

        public GemToken handleSelectGemToken() {
            switch (view){
                case TerminalView t -> {
                    t.displayMessage("Choisissez une gemme :");
                    t.displayMessage("1. DIAMOND, 2. SAPPHIRE, 3. EMERALD, 4. RUBY, 5. ONYX");
                }
                case GraphicView g -> {}
            }

            var choice = view.selectToken(5);
            return switch (choice) {
                case 1 -> GemToken.DIAMOND;
                case 2 -> GemToken.SAPPHIRE;
                case 3 -> GemToken.EMERALD;
                case 4 -> GemToken.RUBY;
                case 5 -> GemToken.ONYX;
                default -> null;
            };
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
            Objects.requireNonNull(player, "player cannot be null");
            switch(view){
                case TerminalView t -> {}
                case GraphicView g -> view.displayMessage(player.getName() + ", sélectionnez le token souhaité");
            }

            GemToken token = handleSelectGemToken();
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
            Objects.requireNonNull(player, "player cannot be null");
            switch(view){
                case TerminalView t -> {}
                case GraphicView g -> view.displayMessage(player.getName() + ", sélectionnez trois tokens différents");
            }

            List<GemToken> pickedTokens = new ArrayList<>();

            for (int i = 0; i < 3; i++) {
                view.showRemainingChoices(3 - i);
                GemToken token = handleSelectGemToken();

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
            Objects.requireNonNull(player, "player cannot be null");
            List<DevelopmentCard> reserved = player.getReservedCards();
            if (reserved.isEmpty()) {
                view.displayMessage("Vous n'avez aucune carte réservée à acheter.");
                return false;
            }

            switch(view){
                case TerminalView t -> playerView.showReservedCards(player);
                case GraphicView g -> view.displayMessage(player.getName() + ", sélectionnez la carte réservée à acheter");
            }

            int choice = view.selectCard(reserved.size(), true);
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

        public boolean processAction(int action, Player player) {
            Objects.requireNonNull(player, "player cannot be null");

            if (action < 0) {
                throw new IllegalArgumentException("action must not be negative");
            }
            return handleAction(action, player, game);
        }

        /**
         * Si besoin d'afficher le classement final en console
         */
        public SplendorView getView() {
            return view;
        }

        /**
         * Initializes the game.
         * This method delegates the initialization to the underlying game instance.
         */
        public void initializeGame() {
            game.initializeGame();
        }

        /**
         * Returns the underlying game instance.
         *
         * @return the game instance
         */
        public Game getGame() {
            return game;
        }

    }