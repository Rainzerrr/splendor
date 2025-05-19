import java.util.*;
import java.util.Scanner;
import java.util.stream.Collectors;

public class SimplifiedGame implements Game {
    private final List<Player> players;
    private final GemStack bank;
    private final List<DevelopmentCard> cards;
    private boolean gameOver;
    private ArrayList<DevelopmentCard> displayedCards;

    public SimplifiedGame(int playerNumber) {
        players = new ArrayList<>();
        bank = new GemStack(7);
        cards = new ArrayList<>();
        gameOver = false;
    }

    public List<Player> getPlayers(){
        return players;
    };

    @Override
    public void initializeCards() {
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
        showBank(bank);

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

    public void showMenu(Player player) {
        showHeader("ACTIONS DISPONIBLES");
        System.out.println("1. Acheter une carte disponible");
        System.out.println("2. Prendre deux gemmes de la même couleur");
        System.out.println("3. Prendre trois gemmes de couleurs différentes");
        while (true) {
            try {
                var action = askInt("Votre choix : ");
                System.out.println();

                boolean actionSuccess = false;
                switch (action) {
                    case 1 -> actionSuccess = buyCard(player, cards, displayedCards);
                    case 2 -> actionSuccess = pickTwiceSameGem(player, bank);
                    case 3 -> actionSuccess = pickThreeDifferentGems(player, bank);
                    default -> System.out.println("Option invalide. Veuillez choisir un nombre entre 1 et 3.\n");
                }

                if (actionSuccess) {
                    return; // Action valide, on quitte le menu
                } else {
                    System.out.println("Retour au menu.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Erreur : Veuillez entrer un chiffre valide.\n");
            }
        }
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
                if (current.getPrestigeScore() >= 15) {
                    gameOver = true;
                }
                System.out.println("----------------------------------------\n");
            }
        }

        displayRanking();
    }
}
