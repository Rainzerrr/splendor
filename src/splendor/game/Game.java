package splendor.game;

import splendor.tokens.GemStack;
import splendor.tokens.GemToken;
import splendor.player.Player;

import java.util.*;

public interface Game {
    List<Player> getPlayers();
    void initializeCards();
    void launch();
    void showMenu(Player p);
    void showCards();

    default void addPlayer(Player p) {
        Objects.requireNonNull(p);
        getPlayers().add(p);
    }

    default int askInt(String prompt, int min, int max) {
        Scanner scanner = new Scanner(System.in);

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

    default boolean pickTwiceSameGem(Player player, GemStack bank) {
        Objects.requireNonNull(player);
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

    default boolean pickThreeDifferentGems(Player player, GemStack bank) {
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

    private boolean updateUserWalletForSameGem(Player player, GemToken token, GemStack bank) {
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

    private boolean updateUserWalletForDifferentGems(Player player, GemToken token, List<GemToken> pickedGems, GemStack bank) {
        if (pickedGems.contains(token)) {
            System.out.println("Vous avez déjà pris cette gemme !");
            return false;
        }
        if (bank.getAmount(token) <= 0) {
            System.out.println("Plus de gemmes disponibles pour " + token);
            return false;
        }
        pickedGems.add(token);
        // Utiliser la méthode add du joueur pour ajouter la gemme
        player.add(token, 1);
        bank.remove(token, 1);  // Retirer la gemme de la banque
        return true;
    }

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

    default void showHeader(String title) {
        Objects.requireNonNull(title);
        System.out.println("[" + title.toUpperCase() + "]");
    }

    default void showBank(GemStack bank) {
        System.out.println("JETONS DISPONIBLES :\n" + bank + "\n");
    }


}
