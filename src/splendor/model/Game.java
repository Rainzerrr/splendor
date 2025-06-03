package splendor.model;

import splendor.view.ConsoleInput;
import java.util.*;

public interface Game {
    List<Player> getPlayers();
    void initializeGame();

    List<DevelopmentCard> getDisplayedCards();
    List<DevelopmentCard> getReservedCards();
    GemStock getBank();
    boolean buyCard(Player p);
    abstract List<Noble> getNobles();
    abstract boolean reserveCard(Player p);
    void replaceCard(DevelopmentCard card);
    void removeTokenFromBank(GemToken gem, int amount);

    /**
     * Adds a player to the game.
     *
     * @param p the player to add; must not be null
     * @throws NullPointerException if p is null
     */
    default void addPlayer(Player p) {
        Objects.requireNonNull(p);
        getPlayers().add(p);
    }

    /**
     * Determines if the game is over.
     *
     * The game is over if at least one player has a prestige score of 15 or more.
     *
     * @return true if the game is over, false if the game is not over yet
     */
    default boolean isGameOver() {
        return getPlayers().stream().anyMatch(p -> p.getPrestigeScore() >= 15);
    }

    /**
     * Allows a player to pick two gem tokens of the same color from the bank,
     * provided there are at least four of that gem in the bank.
     *
     * The player is prompted to choose a gem type or cancel the action.
     * If the player cancels, the action is aborted, and the method returns false.
     *
     * If a valid gem type is selected and there are sufficient gems,
     * the player's wallet is updated, and the method returns true.
     *
     * @param player the player attempting to take gem tokens
     * @param bank the bank from which gem tokens are taken
     * @param consoleInput the input interface for player actions
     * @return true if the player successfully picks two gems, false if the action is canceled
     * @throws NullPointerException if any of the parameters are null
     */
    default boolean pickTwiceSameGem(Player player, GemStock bank, ConsoleInput consoleInput) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(bank);
        Objects.requireNonNull(consoleInput);

        showBank(bank);
        System.out.println("Vous pouvez récupérer deux gemmes de la même couleur, si > 4 : ");
        System.out.println("1. Ruby, 2. Emerald, 3. Diamond, 4. Sapphire, 5. Onyx");
        System.out.println("0. Annuler et retourner au menu");

        while (true) {
            try {
                int action = consoleInput.askInt("Votre choix : ", 0, 5);

                if (action == 0) {
                    System.out.println("Action annulée.\n");
                    return false;
                }

                boolean success = switch (action) {
                    case 1 -> updateUserWalletForSameGem(player, GemToken.RUBY, bank);
                    case 2 -> updateUserWalletForSameGem(player, GemToken.EMERALD, bank);
                    case 3 -> updateUserWalletForSameGem(player, GemToken.DIAMOND, bank);
                    case 4 -> updateUserWalletForSameGem(player, GemToken.SAPPHIRE, bank);
                    case 5 -> updateUserWalletForSameGem(player, GemToken.ONYX, bank);
                    default -> {
                        System.out.println("\nAction inconnue. Veuillez réessayer.\n");
                        yield false;
                    }
                };

                if (success) return true;

            } catch (InputMismatchException e) {
                System.out.println("Erreur : Veuillez entrer un chiffre valide.");
            }
        }
    }

    default boolean pickThreeDifferentGems(Player player, GemStock bank, ConsoleInput consoleInput) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(bank);
        Objects.requireNonNull(consoleInput);

        List<GemToken> pickedGems = new ArrayList<>();
        System.out.println("Vous pouvez récupérer trois gemmes différentes dans la banque :");
        System.out.println("1. Ruby, 2. Emerald, 3. Diamond, 4. Sapphire, 5. Onyx");
        System.out.println("0. Annuler et retourner au menu");

        while (pickedGems.size() < 3) {
            try {
                int action = consoleInput.askInt("Votre choix (" + (pickedGems.size() + 1) + "/3) : ", 0, 5);

                if (action == 0) {
                    for (GemToken token : pickedGems) {
                        player.remove(token, 1);
                        bank.add(token, 1);
                    }
                    System.out.println("Action annulée. Les jetons ont été restitués.\n");
                    return false;
                }

                var valid = switch (action) {
                    case 1 -> updateUserWalletForDifferentGems(player, GemToken.RUBY, pickedGems, bank);
                    case 2 -> updateUserWalletForDifferentGems(player, GemToken.EMERALD, pickedGems, bank);
                    case 3 -> updateUserWalletForDifferentGems(player, GemToken.DIAMOND, pickedGems, bank);
                    case 4 -> updateUserWalletForDifferentGems(player, GemToken.SAPPHIRE, pickedGems, bank);
                    case 5 -> updateUserWalletForDifferentGems(player, GemToken.ONYX, pickedGems, bank);
                    default -> {
                        System.out.println("Choix invalide ! Veuillez saisir un numéro d'action valide (1-5).");
                        yield false;
                    }
                };

                if (!valid) {
                    System.out.println("Ressaisissez un numéro d'action valide (1-5) ou 0 pour quitter");
                }
            } catch (InputMismatchException e) {
                System.out.println("Erreur : Veuillez entrer un chiffre valide.");
            }
        }
        System.out.println("\nJetons ajoutés avec succès !\n");
        return true;
    }

    private boolean updateUserWalletForSameGem(Player player, GemToken token, GemStock bank) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(token);
        Objects.requireNonNull(bank);
        if (bank.getAmount(token) < 4) {
            System.out.println("Pas assez de gemmes " + token + " dans la banque (il en faut au moins 4).\n");
            return false;
        }
        player.add(token, 2);
        bank.remove(token, 2);
        System.out.println("\nDeux jetons " + token + " ajoutés !\n");
        return true;
    }

    private boolean updateUserWalletForDifferentGems(Player player, GemToken token, List<GemToken> pickedGems, GemStock bank) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(token);
        Objects.requireNonNull(pickedGems);
        Objects.requireNonNull(bank);
        if (pickedGems.contains(token)) {
            System.out.println("Vous avez déjà pris cette gemme !");
            return false;
        }
        if (bank.getAmount(token) <= 0) {
            System.out.println("Plus de gemmes disponibles pour " + token);
            return false;
        }
        pickedGems.add(token);
        player.add(token, 1);
        bank.remove(token, 1);
        return true;
    }

    default void showFinalRanking(List<Player> players) {
        System.out.println("Nous avons un vainqueur !");
        System.out.println("Classement final");

        List<Player> sortedPlayers = players.stream()
                .sorted(Comparator
                        .comparingInt(Player::getPrestigeScore).reversed()
                        .thenComparingInt(Player::getPurchasedCardsCount).reversed()
                )
                .toList();

        for (int i = 0; i < sortedPlayers.size(); i++) {
            Player p = sortedPlayers.get(i);
            System.out.println("#" + (i + 1) + " " + p.getName() + " - " + p.getPrestigeScore() + " pts");
        }
    }

    default void showBank(GemStock bank) {
        System.out.println("JETONS DISPONIBLES :\n" + bank + "\n");
    }

    default boolean isCompleteGame() {
        return false;
    }
}