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

    private void playCard(Player p) {
        Objects.requireNonNull(p);
        if (p.getCards().isEmpty()) {
            System.out.println("Vous n'avez pas de carte à jouer");
            return;
        }

        var hand = p.getCards();
        showHeader("Cartes que vous disposez : ");
        showCards(hand);

        System.out.println("Choisissez une carte à jouer : (0-" + (hand.size() - 1) + ")");
        var cardIndex = askInt("Entrez un chiffre : ");

        if (cardIndex >= 0 && cardIndex < hand.size()) {
            var playedCard = hand.get(cardIndex);
            p.removeCard(playedCard);
        } else {
            System.out.println("La carte choisie n'existe pas");
        }
    }

    private void drawCard(Player p) {
        Objects.requireNonNull(p);

        /* --------- plus de cartes sur l’étalage ? --------- */
        if (displayedCards.isEmpty()) {
            System.out.println("Aucune carte n'est actuellement proposée.");
            return;
        }

        /* --------- boucle jusqu’à un achat valide --------- */
        while (true) {

            showHeader("Gemmes en votre possession");
            showWallet(p);

            showHeader("Cartes disponibles à l'achat");
            showCards(displayedCards);

            int idx = askInt("Indice de la carte (1-%d, 0 pour annuler) : "
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

    private void pickTwiceSameGem(Player p) {
        Objects.requireNonNull(p);
        showWallet(p);
        showBank();
        System.out.println("Vous pouvez récupérer deux gemmes de la même couleur, si > 4 : ");
        System.out.println("1. Ruby, 2. Emerald, 3. Diamond, 4. Sapphire, 5. Onyx");

        int action = askInt("Votre choix : ");

        switch (action) {
            case 1 -> updateUserWalletForSameGem(p, GemToken.RUBY);
            case 2 -> updateUserWalletForSameGem(p, GemToken.EMERALD);
            case 3 -> updateUserWalletForSameGem(p, GemToken.DIAMOND);
            case 4 -> updateUserWalletForSameGem(p, GemToken.SAPPHIRE);
            case 5 -> updateUserWalletForSameGem(p, GemToken.ONYX);
            default -> System.out.println("Action inconnue.");
        }
    }

    private void updateUserWalletForSameGem(Player p, GemToken token) {
        if(bank.getAmount(token) >= 4) {
            p.getWallet().add(token, 2);
            bank.remove(token, 2);

            System.out.println("\nDeux jetons " + token + " ont été ajoutés à vos jetons !\n");
            showWallet(p);
        }
        else{
            System.out.println("Coup invalide, car le nombre de gemmes disponible est inférieur à 4 pour les " + token);
        }
    }

    private void updateUserWalletForDifferentGems(Player p, GemToken token, List<GemToken> pickedGems) {
        if(pickedGems.contains(token)) {
            System.out.println("Vous avez déjà récupéré une gemme " + token);
            return;
        }
        if(bank.getAmount(token) > 0) {
            pickedGems.add(token);
            p.getWallet().add(token, 1);
            bank.remove(token, 1);
        }
        else{
            System.out.println("La banque ne contient plus de gemme " + token);
        }
    }


    private void pickThreeDifferentGems(Player p) {
        Objects.requireNonNull(p);
        List<GemToken> pickedGems = new ArrayList<>();
        showBank();
        showWallet(p);
        System.out.println("Vous pouvez récupérer trois gemmes différentes dans la banque :");
        System.out.println("1. Ruby, 2. Emerald, 3. Diamond, 4. Sapphire, 5. Onyx");
        for(int i = 0; i < 3; i++) {
            int action = askInt("Votre choix (" + (i+1) + "/3) : ");
            switch (action) {
                case 1 -> updateUserWalletForDifferentGems(p, GemToken.RUBY, pickedGems);
                case 2 -> updateUserWalletForDifferentGems(p, GemToken.EMERALD, pickedGems);
                case 3 -> updateUserWalletForDifferentGems(p, GemToken.DIAMOND, pickedGems);
                case 4 -> updateUserWalletForDifferentGems(p, GemToken.SAPPHIRE, pickedGems);
                case 5 -> updateUserWalletForDifferentGems(p, GemToken.ONYX, pickedGems);
                default -> System.out.println("Action inconnue.");
            }
        }
        System.out.println("\nLes jetons " + pickedGems.stream().map(Enum::name).collect(Collectors.joining(", ")) + " ont bien été ajoutés à vos jetons !\n");
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
                    showHeader("ACTIONS");
                    System.out.println("1. Piocher une carte");
                    System.out.println("2. Jouer une carte");
                    System.out.println("3. Prendre deux gemmes de la même couleur");
                    System.out.println("4. Prendre trois gemmes de couleurs différentes");

                    int action = askInt("Votre choix : ");
                    System.out.println();
                    switch (action) {
                        case 1 -> drawCard(current);
                        case 2 -> playCard(current);
                        case 3 -> pickTwiceSameGem(current);
                        case 4 -> pickThreeDifferentGems(current);
                        default -> System.out.println("Action inconnue.");
                    }

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
