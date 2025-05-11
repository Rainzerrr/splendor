import java.util.*;
import java.util.Scanner;

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
        System.out.println("---------------------------------------------------------");
        System.out.println("Board");
        System.out.println("---------------------------------------------------------");
        System.out.println(bank);
        System.out.println("---------------------------------------------------------");

        for (var i = 0; i < displayedCards.size(); i++) {
            System.out.println((i + 1) + " - " + displayedCards.get(i).toString());
//            if(i%4==3 && i<displayedCards.size()-1){
//                System.out.println();
//            }
        }
        System.out.println("---------------------------------------------------------");
    }

    public void displayRanking() {
        players.sort(Comparator.comparingInt(Player::getPrestigeScore).reversed());
        for (int i = 0; i < players.size(); i++) {
            System.out.println((i + 1) + " - " + players.get(i).toString());
        }
    }

    private void showHeader(String title) {
        System.out.println("------------------");
        System.out.println(title);
        System.out.println("------------------");
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

    private void showWallet(Player p) {
        showHeader("Votre porte-monnaie");
        for (var t : GemToken.values()) {
            System.out.printf("  %-8s : %d%n", t, p.getWallet().getGems().get(t));
        }
        System.out.println();
    }

    public void launch() {
        initializeCards();
        shuffleCards();
        displayedCards = new ArrayList<>(cards.subList(0, 4));
        displayBoard();

        try (var scanner = new Scanner(System.in)) {          // auto-close
            while (!gameOver) {
                for (var current : players) {
                    System.out.println("\nTour de " + current);
                    showHeader("Cartes visibles");
                    showCards(displayedCards);

                    showHeader("Actions");
                    System.out.println("1. Piocher une carte");
                    System.out.println("2. Jouer une carte");
                    System.out.println("3. Prendre des gemmes");
                    int action = askInt("Votre choix : ");

                    switch (action) {
                        case 1 -> drawCard(current);
                        case 2 -> playCard(current);
                        // 3ème action à implémenter
                        // case 3 -> pickGems(current);
                        default -> System.out.println("Action inconnue.");
                    }

                    if (current.getNbCards() >= 15) {
                        gameOver = true;
                    }
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
