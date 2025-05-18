import java.util.*;
import java.util.Scanner;
import java.util.stream.Collectors;

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

                switch (action) {
                    case 1 -> { buyCard(player); return; }
                    case 2 -> { pickTwiceSameGem(player); return; }
                    case 3 -> { pickThreeDifferentGems(player); return; }
                    default -> System.out.println("Option invalide. Veuillez choisir un nombre entre 1 et 3.\n");
                }
            } catch (InputMismatchException e) {
                System.out.println();
                System.out.println("Veuillez entrer un chiffre.\n");
                showMenu(player);
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
        System.out.print(prompt);
        return scanner.nextInt();
    }

    private void buyCard(Player p) {
        Objects.requireNonNull(p);

        /* --------- plus de cartes sur l’étalage ? --------- */
        if (displayedCards.isEmpty()) {
            System.out.println("Aucune carte n'est actuellement proposée.");
            return;
        }

        /* --------- boucle jusqu’à un achat valide --------- */
        while (true) {
            showWallet(p);

            showHeader("CARTES DISPONIBLES À L'ACHAT");
            showCards(displayedCards);

            var idx = askInt("Indice de la carte (1-%d, 0 pour annuler) : "
                    .formatted(displayedCards.size())) - 1;

            /* --- 0 → on quitte la méthode sans rien changer --- */
            if (idx < 0) {
                System.out.println("Achat annulé.\n");
                return;
            }
            /* --- indice hors bornes : on redemande --- */
            if (idx >= displayedCards.size()) {
                System.out.println("Indice invalide, réessayez.\n");
                continue;
            }

            /* --- récupération de la carte choisie --- */
            var chosen = displayedCards.get(idx);
            var cost = chosen.price();

            /* --- vérification du porte-monnaie --- */
            if (!p.getWallet().canAfford(cost)) {
                System.out.println("Pas assez de gemmes pour cette carte, "
                        + "choisissez-en une autre.\n");
                continue;                       // on repart au début du while
            }

            /* --------- transaction validée --------- */
            p.getWallet().pay(cost);            // débite les gemmes
            displayedCards.remove(idx);         // enlève de l’étalage
            p.addCard(chosen);                  // ajoute à la main
            System.out.println("Carte achetée : " + chosen + "\n");

            /* --------- remplacement dans l’étalage --------- */
            if (!cards.isEmpty()) {
                displayedCards.add(cards.remove(0));
            }
            return;
        }
    }

    private void pickTwiceSameGem(Player player) {
        Objects.requireNonNull(player);
        showBank();
        var success = false;
        while (!success) {
            System.out.println("Vous pouvez récupérer deux gemmes de la même couleur, si > 4 : ");
            System.out.println("1. Ruby, 2. Emerald, 3. Diamond, 4. Sapphire, 5. Onyx");
            var action = askInt("Votre choix : ");

             success = switch (action) {
                case 1 -> updateUserWalletForSameGem(player, GemToken.RUBY);
                case 2 -> updateUserWalletForSameGem(player, GemToken.EMERALD);
                case 3 -> updateUserWalletForSameGem(player, GemToken.DIAMOND);
                case 4 -> updateUserWalletForSameGem(player, GemToken.SAPPHIRE);
                case 5 -> updateUserWalletForSameGem(player, GemToken.ONYX);
                default -> {
                    System.out.println();
                    System.out.println("Action inconnue. Veuillez réessayer.");
                    System.out.println();
                    yield false;
                }
            };
        }
    }

    private boolean updateUserWalletForSameGem(Player player, GemToken token) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(token);

        int amount = bank.getAmount(token);
        if (amount < 4) {
            System.out.println("Pas assez de gemmes " + token + " dans la banque (il en faut au moins 4).");
            return false;
        }

        System.out.println("\nDeux jetons " + token + " ont été ajoutés à vos jetons !\n");
        showWallet(player);

        player.getWallet().add(token, 2);
        bank.remove(token, 2);
        return true;
    }

    private boolean updateUserWalletForDifferentGems(Player player, GemToken token, List<GemToken> pickedGems) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(token);
        Objects.requireNonNull(pickedGems);

        if (pickedGems.contains(token)) {
            System.out.println("Coup invalide ! Vous avez déjà récupéré une gemme " + token);
            return false;
        }

        if (bank.getAmount(token) <= 0) {
            System.out.println("Plus de gemmes: " + token);
            return false;
        }

        pickedGems.add(token);
        player.getWallet().add(token, 1);
        bank.remove(token, 1);
        return true;
    }

    private void pickThreeDifferentGems(Player player) {
        List<GemToken> pickedGems = new ArrayList<>();
        var i = 0;
        System.out.println("Vous pouvez récupérer trois gemmes différentes dans la banque :");
        System.out.println("1. Ruby, 2. Emerald, 3. Diamond, 4. Sapphire, 5. Onyx");
        while (i < 3) {
            int action = askInt("Votre choix (" + (i + 1) + "/3) : ");

            var valid = switch (action) {
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

            if (valid) {
                i++;
            }
        }

        System.out.println("\nLes jetons " + pickedGems.stream()
                .map(Enum::name)
                .collect(Collectors.joining(", ")) + " ont bien été ajoutés à vos jetons !\n");
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

        try (var scanner = new Scanner(System.in)) {          // auto-close
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
        }

        displayRanking();
    }

    public void launchTest() {
        System.out.println("Let the game begin !!!!!");
        shuffleCards();
        displayedCards = new ArrayList<>(cards.subList(0, 4));
        displayBoard();
    }

    // Ajout d'un joueur dans la partie
    public void addPlayer(Player player) {
        Objects.requireNonNull(player);
        players.add(player);
    }

}
