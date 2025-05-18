import java.util.*;
import java.util.Scanner;

public class Game {
    private final List<Player> players;
    private final GemStack bank;
    private final List<DevelopmentCard> cards;
    private boolean gameOver;
    private ArrayList<DevelopmentCard> displayedCards;

    public Game(int playerNumber) {
        if (playerNumber < 2) {
            throw new IllegalArgumentException("Must have at least 2 players");
        }
        players = new ArrayList<>();
        bank = new GemStack(7);
        cards = new ArrayList<>();
        gameOver = false;
    }

    private void initializeCards() {
        // Pour chaque couleur dans GemToken
        for (GemToken color : GemToken.values()) {
            // Crée 8 cartes pour cette couleur
            for (int i = 0; i < 8; i++) {
                // Configure le coût
                EnumMap<GemToken, Integer> cost = new EnumMap<>(GemToken.class);
                cost.put(color, 3);

                // Ajoute la nouvelle carte
                cards.add(new DevelopmentCard(cost, color, 1));
            }
        }
    }


    private void shuffleCards() {
        Collections.shuffle(cards);
    }

    private void displayBoard() {
        showBank();

        showHeader("CARTES DISPONIBLES");
        for (var i = 0; i < displayedCards.size(); i++) {
            System.out.println((i + 1) + " - " + displayedCards.get(i).toString());
        }
        System.out.println();
    }

    public void displayRanking() {
        players.sort(Comparator.comparingInt(Player::getPrestigeScore).reversed());
        for (int i = 0; i < players.size(); i++) {
            System.out.println((i + 1) + " - " + players.get(i).toString());
        }
    }

    private void showMenu(Player player) {
        while (true) {
            showHeader("ACTIONS DISPONIBLES");
            System.out.println("1. Acheter une carte disponible");
            System.out.println("2. Prendre deux gemmes de la même couleur");
            System.out.println("3. Prendre trois gemmes de couleurs différentes");

            try {
                var action = askInt("Votre choix : ");
                System.out.println();

                boolean actionSuccess = false;
                switch (action) {
                    case 1 -> actionSuccess = buyCard(player);
                    case 2 -> actionSuccess = pickTwiceSameGem(player);
                    case 3 -> actionSuccess = pickThreeDifferentGems(player);
                    default -> System.out.println("Option invalide. Veuillez choisir un nombre entre 1 et 3.\n");
                }

                if (actionSuccess) {
                    return; // Action valide, on quitte le menu
                } else {
                    System.out.println("Retour au menu.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Veuillez entrer un chiffre.");
                new Scanner(System.in).nextLine(); // Vide le buffer
            }
        }
    }

    private void showHeader(String title) {
        System.out.println("[" + title + "]");
    }

    private void showCards(List<DevelopmentCard> cards) {
        Objects.requireNonNull(cards);
        for (var i = 0; i < cards.size(); i++) {
            System.out.println((i + 1) + " - " + cards.get(i).toString());
        }
        System.out.println();
    }

    private int askInt(String prompt) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.print(prompt);
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Coup invalide : veuillez uniquement un chiffre");
                scanner.nextLine(); // Vide le buffer
            }
        }
    }

    private boolean buyCard(Player p) {
        if (displayedCards.isEmpty()) {
            System.out.println("Aucune carte n'est actuellement proposée.");
            showMenu(p);
            return false;
        }

        while (true) {
            showWallet(p);
            showHeader("CARTES DISPONIBLES À L'ACHAT");
            showCards(displayedCards);

            var idx = askInt("Indice de la carte (1-%d, 0 pour annuler) : "
                    .formatted(displayedCards.size())) - 1;

            if (idx < 0) {
                System.out.println("Achat annulé.\n");
                return false;
            }
            if (idx >= displayedCards.size()) {
                System.out.println("Indice invalide, réessayez.\n");
                continue;
            }

            var chosen = displayedCards.get(idx);
            if (!p.getWallet().canAfford(chosen.price())) {
                System.out.println("Pas assez de gemmes pour cette carte, choisissez-en une autre.\n");
                continue;
            }

            p.getWallet().pay(chosen.price());
            displayedCards.remove(idx);
            p.addCard(chosen);
            System.out.println("Carte achetée : " + chosen + "\n");

            if (!cards.isEmpty()) {
                displayedCards.add(cards.removeFirst());
            }
            return true;
        }
    }

    private boolean pickTwiceSameGem(Player player) {
        Objects.requireNonNull(player);
        showBank();
        while (true) {
            System.out.println("Vous pouvez récupérer deux gemmes de la même couleur, si > 4 : ");
            System.out.println("1. Ruby, 2. Emerald, 3. Diamond, 4. Sapphire, 5. Onyx");
            System.out.println("0. Annuler et retourner au menu");
            var action = askInt("Votre choix : ");

            if (action == 0) {
                System.out.println("Action annulée.\n");
                return false;
            }

            var success = switch (action) {
                case 1 -> updateUserWalletForSameGem(player, GemToken.RUBY);
                case 2 -> updateUserWalletForSameGem(player, GemToken.EMERALD);
                case 3 -> updateUserWalletForSameGem(player, GemToken.DIAMOND);
                case 4 -> updateUserWalletForSameGem(player, GemToken.SAPPHIRE);
                case 5 -> updateUserWalletForSameGem(player, GemToken.ONYX);
                default -> {
                    System.out.println("\nAction inconnue. Veuillez réessayer.\n");
                    yield false;
                }
            };

            if (success) return true;
        }
    }

    private boolean updateUserWalletForSameGem(Player player, GemToken token) {
        if (bank.getAmount(token) < 4) {
            System.out.println("Pas assez de gemmes " + token + " dans la banque (il en faut au moins 4).\n");
            return false;
        }
        player.getWallet().add(token, 2);
        bank.remove(token, 2);
        System.out.println("\nDeux jetons " + token + " ajoutés !\n");
        return true;
    }

    private boolean updateUserWalletForDifferentGems(Player player, GemToken token, List<GemToken> pickedGems) {
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

    private boolean pickThreeDifferentGems(Player player) {
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
                    case 1 -> updateUserWalletForDifferentGems(player, GemToken.RUBY, pickedGems);
                    case 2 -> updateUserWalletForDifferentGems(player, GemToken.EMERALD, pickedGems);
                    case 3 -> updateUserWalletForDifferentGems(player, GemToken.DIAMOND, pickedGems);
                    case 4 -> updateUserWalletForDifferentGems(player, GemToken.SAPPHIRE, pickedGems);
                    case 5 -> updateUserWalletForDifferentGems(player, GemToken.ONYX, pickedGems);
                    default -> {
                        System.out.println("Choix invalide ! Veuillez saisir un numéro d'action valide (1-5).");
                        yield false;
                    }
                };

                if (!valid) {
                    System.out.println("Ressaisissez un numéro d'action valide (1-5) ou 0 pour quitter");
                }
            } catch (InputMismatchException e) {
                System.out.println("\nErreur : Veuillez entrer un chiffre valide.\n");
                new Scanner(System.in).nextLine();
            }
        }
        System.out.println("\nJetons ajoutés avec succès !\n");
        return true;
    }

    private void showWallet(Player p) {
        showHeader("VOS JETONS");
        System.out.println(p.getWallet() + "\n");
    }

    private void showBank() {
        showHeader("JETONS DISPONIBLES");
        System.out.println(bank + "\n");
    }

    public void launch() {
        initializeCards();
        shuffleCards();
        displayedCards = new ArrayList<>(cards.subList(0, 4));
        System.out.println("Let the game begin !\n");

        // auto-close
        while (!gameOver) {
            for (var current : players) {
                displayBoard();
                System.out.println(current.toString() + "\n");
                showMenu(current);
                if (current.getNbCards() >= 15) {
                    gameOver = true;
                }
                System.out.println("----------------------------------------\n");
            }
        }

        displayRanking();
    }

    // Ajout d'un joueur dans la partie
    public void addPlayer(Player player) {
        Objects.requireNonNull(player);
        players.add(player);
    }

}
