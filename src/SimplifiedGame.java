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
                cards.add(new DevelopmentCard(cost, color, 1, 1));
            }
        }
        Collections.shuffle(cards);
    }


    public void showCards() {
        for (int i = 0; i < displayedCards.size(); i++) {
            System.out.println((i + 1) + " - " + displayedCards.get(i));
        }
        System.out.println();
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

    private boolean buyCard(Player p) {
        if (displayedCards.isEmpty()) {
            System.out.println("Aucune carte n'est actuellement proposée.");
            showMenu(p);
            return false;
        }

        showWallet(p);
        showHeader("CARTES DISPONIBLES À L'ACHAT");
        showCards();

        while (true) {
            try{
                var idx = askInt("Indice de la carte (1-%d, 0 pour annuler) : "
                        .formatted(displayedCards.size())) - 1;

                if (idx < 0) {
                    System.out.println("Achat annulé.\n");
                    return false;
                }
                if (idx >= displayedCards.size()) {
                    System.out.println("Indice invalide, réessayez.");
                    continue;
                }

                var chosen = displayedCards.get(idx);
                if (!p.getWallet().canAfford(chosen.price())) {
                    System.out.println("Pas assez de gemmes pour cette carte, choisissez-en une autre.");
                    continue;
                }

                p.getWallet().pay(chosen.price());
                displayedCards.remove(idx);
                p.addPurchasedCard(chosen);
                System.out.println("Carte achetée : " + chosen + "\n");

                if (!cards.isEmpty()) {
                    displayedCards.add(cards.removeFirst());
                }
                return true;
            }catch (InputMismatchException e){
                System.out.println("Erreur : Veuillez entrer un chiffre valide.");
            }

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
                    case 1 -> actionSuccess = buyCard(player);
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
        displayedCards = new ArrayList<>(cards.subList(0, 12));
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
