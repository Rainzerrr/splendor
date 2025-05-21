import java.util.*;
import java.util.stream.Collectors;

public interface Game {
    List<Player> getPlayers();
    void initializeCards();
    void launch();
    void showMenu(Player p);
    void showCards();

    default void addPlayer(Player p){
        getPlayers().add(p);
    }

    default int askInt(String prompt) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(prompt);
        return scanner.nextInt();
    }

    default boolean pickTwiceSameGem(Player player, GemStack bank) {
        Objects.requireNonNull(player);
        showBank(bank);
        System.out.println("Vous pouvez récupérer deux gemmes de la même couleur, si > 4 : ");
        System.out.println("1. Ruby, 2. Emerald, 3. Diamond, 4. Sapphire, 5. Onyx");
        System.out.println("0. Annuler et retourner au menu");
        while (true) {
            try {
                var action = askInt("Votre choix : ");

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
                int action = askInt("Votre choix (" + (pickedGems.size() + 1) + "/3) : ");

                if (action == 0) {
                    // Remboursement des gemmes déjà prises
                    for (GemToken token : pickedGems) {
                        player.getWallet().remove(token, 1);
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

    // Méthodes utilitaires

    private boolean updateUserWalletForSameGem(Player player, GemToken token, GemStack bank) {
        if (bank.getAmount(token) < 4) {
            System.out.println("Pas assez de gemmes " + token + " dans la banque (il en faut au moins 4).\n");
            return false;
        }
        player.getWallet().add(token, 2);
        bank.remove(token, 2);
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
        player.getWallet().add(token, 1);
        bank.remove(token, 1);
        return true;
    }

    default void showFinalRanking(List<Player> players) {
        System.out.println("Nous avons un vainqueur !");
        showHeader("Classement final");

        // Tri par score puis nombre de cartes
        List<Player> sortedPlayers = players.stream()
                .sorted(Comparator
                        .comparingInt(Player::getPrestigeScore).reversed()
                        .thenComparingInt(p -> p.getPurchasedCards().size())
                )
                .toList();

        // Affichage avec #
        for (int i = 0; i < sortedPlayers.size(); i++) {
            Player p = sortedPlayers.get(i);
            System.out.println("#" + (i + 1) + " " + p.getName() + " - " + p.getPrestigeScore() + " pts");
        }
    }

    default void showHeader(String title) {
        Objects.requireNonNull(title);
        System.out.println("[" + title.toUpperCase() + "]");
    }

    default void showWallet(Player p) {
        Objects.requireNonNull(p);
        System.out.println("VOS JETONS :\n" + p.getWallet() + "\n");
    }

    default void showBank(GemStack bank) {
        System.out.println("JETONS DISPONIBLES :\n" + bank + "\n");
    }


}
