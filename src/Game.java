import java.io.ObjectStreamException;
import java.util.*;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Game {
    // Variables de classe
    private final List<Player> players;
    private final GemStack bank;
    private final List<DevelopmentCard> cards;
    private boolean gameOver;
    private ArrayList<DevelopmentCard> displayedCards;

    // Constructeur
    public Game(int playerNumber) {
        if (playerNumber < 1 || playerNumber > 9) {
            throw new IllegalArgumentException("Il n'y a plus de 9");
        }
        players = new ArrayList<>();
        bank = new GemStack(7);
        cards = new ArrayList<>();
        gameOver = false;
    }

    // Méthodes publiques
    public void launch() {
        initializeCards();
        shuffleCards();
        displayedCards = new ArrayList<>(cards.subList(0, 4));
        System.out.println("Let the game begin !\n");

        try (var scanner = new Scanner(System.in)) {
            while (!gameOver) {
                for (var current : players) {
                    displayBoard();
                    System.out.println(current.toString() + "\n");
                    showHeader("ACTIONS");
                    System.out.println("1. Piocher une carte");
                    System.out.println("2. Jouer une carte");
                    System.out.println("3. Prendre deux gemmes de la même couleur");
                    System.out.println("4. Prendre trois gemmes de couleurs différentes");

                    var action = askInt("Votre choix : ");
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

    public void addPlayer(Player player) {
        Objects.requireNonNull(player, "Le joueur ne peut pas être null");
        if (players.contains(player)) {
            throw new IllegalStateException("Joueur déjà existant");
        }
        players.add(player);
    }

    public void displayRanking() {
        players.sort(Comparator.comparingInt(Player::getPrestigeScore).reversed());

        IntStream.range(0, players.size())
                .forEach(i -> System.out.println((i + 1) + " - " + players.get(i)));
    }

    // Méthodes d'initialisation
    private void initializeCards() {
        Stream.<GemToken>of(GemToken.values())
                .flatMap(color -> IntStream.range(0, 8)
                        .mapToObj(i -> {
                            EnumMap<GemToken, Integer> cost = new EnumMap<>(GemToken.class);
                            cost.put(color, 3);
                            return new DevelopmentCard(cost, color, 1);
                        }))
                .forEach(cards::add);
    }

    private void shuffleCards() {
        Collections.shuffle(cards);
    }

    // Affichage du plateau
    private void displayBoard() {
        showBank();
        showHeader("CARTES DISPONIBLES");
        IntStream.range(0, displayedCards.size())
                .forEach(i -> System.out.println((i + 1) + " - " + displayedCards.get(i)));
        System.out.println();
    }

    // Actions joueur
    private void drawCard(Player p) {
        Objects.requireNonNull(p);
        if (displayedCards.isEmpty()) { System.out.println("Aucune carte n'est actuellement proposée."); return; }

        while (true) {
            showWallet(p);
            showCards(displayedCards);

            var idx = askInt("Indice de la carte (1-%d, 0 pour annuler) : ".formatted(displayedCards.size())) - 1;

            if (idx < 0) {
                System.out.println("Achat annulé.\n");
                return;
            } else if (idx >= displayedCards.size()) {
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
                displayedCards.add(cards.remove(0));
            }
            return;
        }
    }

    private void playCard(Player p) {
        if (p.getCards().isEmpty()) {
            System.out.println("Vous n'avez pas de carte à jouer");
            return;
        }

        var hand = p.getCards();
        showCards(hand);
        var cardIndex = askInt("Entrez un chiffre : ");

        if (cardIndex >= 0 && cardIndex < hand.size()) {
            p.removeCard(hand.get(cardIndex));
        } else {
            System.out.println("La carte choisie n'existe pas");
        }
    }

    private void pickTwiceSameGem(Player p) {
        showBank();
        var action = askInt("Votre choix : ");
        switch (action) {
            case 1 -> updateUserWalletForSameGem(p, GemToken.RUBY);
            case 2 -> updateUserWalletForSameGem(p, GemToken.EMERALD);
            case 3 -> updateUserWalletForSameGem(p, GemToken.DIAMOND);
            case 4 -> updateUserWalletForSameGem(p, GemToken.SAPPHIRE);
            case 5 -> updateUserWalletForSameGem(p, GemToken.ONYX);
            default -> System.out.println("Action inconnue.");
        }
    }

    private void pickThreeDifferentGems(Player p) {
        List<GemToken> pickedGems = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            int action = askInt("Votre choix (" + (i+1) + "/3) : ");
            switch (action) {
                case 1 -> updateUserWalletForDifferentGems(p, GemToken.RUBY, pickedGems);
                case 2 -> updateUserWalletForDifferentGems(p, GemToken.EMERALD, pickedGems);
                case 3 -> updateUserWalletForDifferentGems(p, GemToken.DIAMOND, pickedGems);
                case 4 -> updateUserWalletForDifferentGems(p, GemToken.SAPPHIRE, pickedGems);
                case 5 -> updateUserWalletForDifferentGems(p, GemToken.ONYX, pickedGems);
                default -> showText("Action inconnue");
            }
        }
    }

    // Helpers de transactions
    private void updateUserWalletForSameGem(Player p, GemToken token) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(token);

        if (bank.getAmount(token) >= 4) {
            p.getWallet().add(token, 2);
            bank.remove(token, 2);
            showWallet(p);
        } else {
            System.out.println("Coup invalide pour " + token);
        }
    }

    private void updateUserWalletForDifferentGems(Player p, GemToken token, List<GemToken> pickedGems) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(token);
        Objects.requireNonNull(pickedGems);

        var alreadyPicked = pickedGems.stream().anyMatch(t -> t == token);
        if (bank.getAmount(token) > 0) {
            pickedGems.add(token);
            p.getWallet().add(token, 1);
            bank.remove(token, 1);
        } else {
            System.out.println("Plus de gemmes: " + token);
        }
    }

    // Helpers d'affichage
    private void showHeader(String title) {
        System.out.println("[" + title + "]");
    }

    private void showText(String text) {
        System.out.println(text);
    }

    private void showCards(List<DevelopmentCard> cards) {
        Objects.requireNonNull(cards);
        IntStream.range(0, cards.size())
                .forEach(i -> System.out.println((i + 1) + " - " + cards.get(i)));
        System.out.println();
    }

    private void showWallet(Player p) {
        showHeader("VOS JETONS");
        System.out.println(p.getWallet() + "\n");
    }

    private void showBank() {
        showHeader("JETONS DISPONIBLES");
        System.out.println(bank + "\n");
    }

    // Helper d'entrée utilisateur
    private int askInt(String prompt) {
        Scanner scanner = new Scanner(System.in);
        showText(prompt);
        return scanner.nextInt();
    }
}