package splendor.controller;

import splendor.model.Game;
import splendor.model.GemStock;
import splendor.model.GemToken;
import splendor.model.Player;
import splendor.view.TerminalView;

import java.util.*;

public class GameController {
    private final Game game;
    private final TerminalView view;

    public GameController(Game game, TerminalView view) {
        this.game = game;
        this.view = view;
    }

    private void addPlayer(Player p) {
        Objects.requireNonNull(p);
        game.getPlayers().add(p);
    }

    private void handlePlayerTurn(Player player) {
        view.showPlayerTurn(player);
        view.showMenu();

        int action = view.askInt("Votre choix : ", 1, 7);
        switch (action) {
            case 1 -> game.buyCard(player);
            case 2 -> game.reserveCard(player);
            case 3 -> game.pickTwiceSameGem(player);
            case 4 -> game.pickThreeDifferentGems(player);
            case 5 -> view.showNobles(nobles);
            case 6 -> view.showCards(displayedCards);
            case 7 -> view.showBank(bank);
        }
    }

    private boolean updateUserWalletForSameGem(Player player, GemToken token) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(token);
        if (game.getTokenAmountInBank(token) < 4) {
            view.displayMessage("Pas assez de gemmes " + token + " dans la banque (il en faut au moins 4).\n");
            return false;
        }
        // Utiliser la méthode add du joueur pour ajouter les gemmes
        player.add(token, 2);
        game.removeTokensInBank(token, 2);  // Retirer les gemmes de la banque
        view.displayMessage("\nDeux jetons " + token + " ajoutés !\n");
        return true;
    }

    private boolean pickTwiceSameGem(Player player, GemStock bank) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(bank);
        showBank(bank);
        view.displayMessage("Vous pouvez récupérer deux gemmes de la même couleur, si > 4 : ");
        view.displayMessage("1. Ruby, 2. Emerald, 3. Diamond, 4. Sapphire, 5. Onyx");
        view.displayMessage("0. Annuler et retourner au menu");
        while (true) {
            try {
                var action = game.askInt("Votre choix : ", 0, 5);

                if (action == 0) {
                    view.displayMessage("Action annulée.\n");
                    return false;
                }

                var success = switch (action) {
                    case 1 -> updateUserWalletForSameGem(player, GemToken.RUBY);
                    case 2 -> updateUserWalletForSameGem(player, GemToken.EMERALD);
                    case 3 -> updateUserWalletForSameGem(player, GemToken.DIAMOND);
                    case 4 -> updateUserWalletForSameGem(player, GemToken.SAPPHIRE);
                    case 5 -> updateUserWalletForSameGem(player, GemToken.ONYX);
                    default -> {
                        view.displayMessage("\nAction inconnue. Veuillez réessayer.\n");
                        yield false;
                    }
                };

                if (success) return true;
            } catch (InputMismatchException e) {
                view.displayMessage("Erreur : Veuillez entrer un chiffre valide.");
            }
        }
    }

    private boolean updateUserWalletForDifferentGems(Player player, GemToken token, List<GemToken> pickedGems) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(token);
        Objects.requireNonNull(pickedGems);
        if (pickedGems.contains(token)) {
            view.displayMessage("Vous avez déjà pris cette gemme !");
            return false;
        }
        if (game.getTokenAmountInBank(token) <= 0) {
            view.displayMessage("Plus de gemmes disponibles pour " + token);
            return false;
        }
        pickedGems.add(token);
        player.add(token, 1);
        game.removeTokensInBank(token, 1);
        return true;
    }

    private boolean pickThreeDifferentGems(Player player, GemStock bank) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(bank);
        List<GemToken> pickedGems = new ArrayList<>();
        view.displayMessage("Vous pouvez récupérer trois gemmes différentes dans la banque :");
        view.displayMessage("1. Ruby, 2. Emerald, 3. Diamond, 4. Sapphire, 5. Onyx");
        view.displayMessage("0. Annuler et retourner au menu");

        while (pickedGems.size() < 3) {
            try {
                int action = game.askInt("Votre choix (" + (pickedGems.size() + 1) + "/3) : ", 0, 5);

                if (action == 0) {
                    for (GemToken token : pickedGems) {
                        player.remove(token, 1);
                        bank.add(token, 1);
                    }
                    view.displayMessage("Action annulée. Les jetons ont été restitués.\n");
                    return false;
                }

                boolean valid = switch (action) {
                    case 1 -> updateUserWalletForDifferentGems(player, GemToken.RUBY, pickedGems);
                    case 2 -> updateUserWalletForDifferentGems(player, GemToken.EMERALD, pickedGems);
                    case 3 -> updateUserWalletForDifferentGems(player, GemToken.DIAMOND, pickedGems);
                    case 4 -> updateUserWalletForDifferentGems(player, GemToken.SAPPHIRE, pickedGems);
                    case 5 -> updateUserWalletForDifferentGems(player, GemToken.ONYX, pickedGems);
                    default -> {
                        view.displayMessage("Choix invalide ! Veuillez saisir un numéro d'action valide (1-5).");
                        yield false;
                    }
                };

                if (!valid) {
                    view.displayMessage("Ressaisissez un numéro d'action valide (1-5) ou 0 pour quitter");
                }
            } catch (InputMismatchException e) {
                view.displayMessage("Erreur : Veuillez entrer un chiffre valide.");
            }
        }
        view.displayMessage("\nJetons ajoutés avec succès !\n");
        return true;
    }

    private void showFinalRanking(List<Player> players) {
        view.displayMessage("Nous avons un vainqueur !");
        showHeader("Classement final");

        List<Player> sortedPlayers = players.stream()
                .sorted(Comparator
                        .comparingInt(Player::getPrestigeScore).reversed()
                        .thenComparingInt(Player::getPurchasedCardsCount) // Utilisation de la méthode getPurchasedCardsCount
                )
                .toList();

        for (int i = 0; i < sortedPlayers.size(); i++) {
            Player p = sortedPlayers.get(i);
            view.displayMessage("#" + (i + 1) + " " + p.getName() + " - " + p.getPrestigeScore() + " pts");
        }
    }

    /**
     * Displays a header with the given title in uppercase, enclosed in square brackets.
     *
     * @param title the header title to display; must not be null
     */
    private void showHeader(String title) {
        Objects.requireNonNull(title);
        view.displayMessage("[" + title.toUpperCase() + "]");
    }

    /**
     * Displays the available tokens in the bank.
     *
     * @param bank the gem stock to display; must not be null
     */
    private void showBank(GemStock bank) {
        Objects.requireNonNull(bank);
        view.displayMessage("JETONS DISPONIBLES :\n" + bank + "\n");
    }


    public void launch() {
        game.initializeCards();
        game.initializeNobles();

        view.showCards(game.getDisplayedCards());
        view.showCards(game.getReservedCards());

        boolean isGameOver = false;

        while (!isGameOver) {
            for (var current : game.getPlayers()) {
                System.out.println(current.toString() + "\n");
                game.showMenu(current);
                current.claimNobleIfEligible(game.getNobles());
                if (current.getPrestigeScore() >= 15) {
                    isGameOver = true;
                }
                System.out.println();
                System.out.println("----------------------------------------\n");
            }
        }
        showFinalRanking(game.getPlayers());
    }
}
