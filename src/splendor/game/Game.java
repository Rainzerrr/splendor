package splendor.game;

import splendor.tokens.GemStock;
import splendor.tokens.GemToken;
import splendor.player.Player;

import java.util.*;

public interface Game {
    List<Player> getPlayers();
    void initializeCards();
    void launch();
    void showMenu(Player p);
    void showCards();

    /**
     * Adds a player to the game.
     * @param p the player to add. Must not be null.
     */
    default void addPlayer(Player p) {
        Objects.requireNonNull(p);
        getPlayers().add(p);
    }

    /**
     * Ask the user for an integer input within a range.
     *
     * @param prompt the text to be displayed to the user
     * @param min the minimum value of the range
     * @param max the maximum value of the range
     * @return the integer input by the user
     */
    default int askInt(String prompt, int min, int max) {
        Objects.requireNonNull(prompt);
        if (min > max) {
            throw new IllegalArgumentException("Le minimum doit être plus petit que le maximum.");
        } else if (min < 0 || max < 0) {
            throw new IllegalArgumentException("Le minimum et le maximum doivent avoir une valeur positive.");
        }

        var scanner = new Scanner(System.in);

        while (true) {
            System.out.print(prompt);

            try {
                int input = scanner.nextInt();
                if (input < min || input > max) {
                    System.out.println("Veuillez entrer un nombre entre " + min + " et " + max + ".");
                } else {
                    return input;
                }
            } catch (InputMismatchException e) {
                System.out.println("Erreur : veuillez entrer un nombre entier.");
                scanner.nextLine();
            }
        }
    }

    /**
     * Allows the player to pick two tokens of the same gem from the bank.
     *
     * The player can choose from available gem types: Ruby, Emerald, Diamond, Sapphire, and Onyx.
     * If the player enters 0, the action is canceled, and any selected gems are returned to the bank.
     * The action will continue until the player has successfully picked two tokens of the same gem or canceled.
     *
     * @param player the player who will pick the gems
     * @param bank the gem stock from which the player can pick gems
     * @return true if two tokens of the same gem were successfully added to the player's wallet, false if the action was canceled
     */
    default boolean pickTwiceSameGem(Player player, GemStock bank) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(bank);
        showBank(bank);
        System.out.println("Vous pouvez récupérer deux gemmes de la même couleur, si > 4 : ");
        System.out.println("1. Ruby, 2. Emerald, 3. Diamond, 4. Sapphire, 5. Onyx");
        System.out.println("0. Annuler et retourner au menu");
        while (true) {
            try {
                var action = askInt("Votre choix : ", 0, 5);

                if (action == 0) {
                    System.out.println("Action annulée.\n");
                    return false;
                }

                var success = switch (action) {
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

    /**
     * Allows the player to pick three different gems from the bank.
     *
     * The player can choose from available gem types: Ruby, Emerald, Diamond, Sapphire, and Onyx.
     * If the player enters 0, the action is canceled, and any selected gems are returned to the bank.
     * The action will continue until the player has successfully picked three different gems.
     *
     * @param player the player who will pick the gems
     * @param bank the gem stock from which the player can pick gems
     * @return true if three different gems were successfully added to the player's wallet, false if the action was canceled
     */
    default boolean pickThreeDifferentGems(Player player, GemStock bank) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(bank);
        List<GemToken> pickedGems = new ArrayList<>();
        System.out.println("Vous pouvez récupérer trois gemmes différentes dans la banque :");
        System.out.println("1. Ruby, 2. Emerald, 3. Diamond, 4. Sapphire, 5. Onyx");
        System.out.println("0. Annuler et retourner au menu");

        while (pickedGems.size() < 3) {
            try {
                int action = askInt("Votre choix (" + (pickedGems.size() + 1) + "/3) : ", 0, 5);

                if (action == 0) {
                    for (GemToken token : pickedGems) {
                        player.remove(token, 1);
                        bank.add(token, 1);
                    }
                    System.out.println("Action annulée. Les jetons ont été restitués.\n");
                    return false;
                }

                boolean valid = switch (action) {
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

    /**
     * Updates the player's wallet with two tokens of the same gem.
     * @param player the player to update
     * @param token the gem to add
     * @param bank the gem stock
     * @return true if the gem was added successfully, false if there are not enough gems in the bank
     */
    private boolean updateUserWalletForSameGem(Player player, GemToken token, GemStock bank) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(token);
        Objects.requireNonNull(bank);
        if (bank.getAmount(token) < 4) {
            System.out.println("Pas assez de gemmes " + token + " dans la banque (il en faut au moins 4).\n");
            return false;
        }
        // Utiliser la méthode add du joueur pour ajouter les gemmes
        player.add(token, 2);
        bank.remove(token, 2);  // Retirer les gemmes de la banque
        System.out.println("\nDeux jetons " + token + " ajoutés !\n");
        return true;
    }


    /**
     * Update the player's wallet with a different gem.
     *
     * @param player the player to update
     * @param token the gem to add
     * @param pickedGems the list of already picked gems
     * @param bank the gem stock
     * @return true if the gem was added successfully, false if it was already picked or not available in the bank
     */
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

    /**
     * Displays the final ranking of players based on their prestige scores.
     *
     * The players are sorted in descending order of their prestige scores. If two
     * players have the same prestige score, they are further sorted by the number
     * of purchased cards they have, in descending order.
     *
     * @param players the list of players to rank and display
     */
    default void showFinalRanking(List<Player> players) {
        System.out.println("Nous avons un vainqueur !");
        showHeader("Classement final");

        List<Player> sortedPlayers = players.stream()
                .sorted(Comparator
                        .comparingInt(Player::getPrestigeScore).reversed()
                        .thenComparingInt(Player::getPurchasedCardsCount) // Utilisation de la méthode getPurchasedCardsCount
                )
                .toList();

        for (int i = 0; i < sortedPlayers.size(); i++) {
            Player p = sortedPlayers.get(i);
            System.out.println("#" + (i + 1) + " " + p.getName() + " - " + p.getPrestigeScore() + " pts");
        }
    }

    /**
     * Displays a header with the given title in uppercase, enclosed in square brackets.
     *
     * @param title the header title to display; must not be null
     */
    default void showHeader(String title) {
        Objects.requireNonNull(title);
        System.out.println("[" + title.toUpperCase() + "]");
    }

    /**
     * Displays the available tokens in the bank.
     *
     * @param bank the gem stock to display; must not be null
     */
    default void showBank(GemStock bank) {
        System.out.println("JETONS DISPONIBLES :\n" + bank + "\n");
    }
}
