package splendor.game;

import splendor.cards.DevelopmentCard;
import splendor.tokens.GemStack;
import splendor.tokens.GemToken;
import splendor.player.Player;
import splendor.game.Game;

import java.util.*;

public class SimplifiedGame implements Game {
    private final List<Player> players;
    private final GemStack bank;
    private final List<DevelopmentCard> cards;
    private boolean gameOver;
    private ArrayList<DevelopmentCard> displayedCards;

    public SimplifiedGame(int playerNumber) {
        players = new ArrayList<>();
        bank = new GemStack(7) ;
        cards = new ArrayList<>();
        gameOver = false;
    }

    public List<Player> getPlayers(){
        return players;
    };

    @Override
    public void initializeCards() {
        for (GemToken color : GemToken.values()) {
            for (int i = 0; i < 8; i++) {
                EnumMap<GemToken, Integer> cost = new EnumMap<>(GemToken.class);
                cost.put(color, 3);

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

    private boolean buyCard(Player p) {
        if (displayedCards.isEmpty()) {
            System.out.println("Aucune carte n'est actuellement proposée.");
            showMenu(p);
            return false;
        }

        p.showWallet();
        showHeader("CARTES DISPONIBLES À L'ACHAT");
        showCards();

        while (true) {
            int indice = askInt("Indice de la carte (1-" + displayedCards.size() + ", 0 pour annuler) : ", 0, displayedCards.size());

            if (indice == 0) {
                System.out.println("Achat annulé.\n");
                return false;
            }

            DevelopmentCard chosen = displayedCards.get(indice - 1);

            // Appeler la méthode dans Player pour acheter la carte
            boolean success = p.buyCard(chosen, bank);

            if (success) {
                // Retirer la carte de l'affichage
                displayedCards.remove(indice - 1);
                System.out.println("Carte achetée : " + chosen + "\n");

                // Ajouter une nouvelle carte piochée si disponible
                if (!cards.isEmpty()) {
                    DevelopmentCard newCard = cards.remove(0);  // Retirer la première carte de la pioche
                    displayedCards.add(newCard);  // Ajouter au tableau affiché
                    System.out.println("Nouvelle carte ajoutée : " + newCard);
                }

                return true;
            } else {
                System.out.println("Achat échoué. Veuillez réessayer.\n");
            }
        }
    }


    public void showMenu(Player player) {
        showHeader("ACTIONS DISPONIBLES");
        System.out.println("Actions : 1. Acheter | 2. 2 gemmes identiques | 3. 3 gemmes différentes");
        System.out.println("Afficher : 4. Cartes sur le plateau | 5. Contenu de la banque");
        while (true) {
                var action = askInt("Votre choix : ", 1, 5);
                System.out.println();

                boolean actionSuccess = false;
                switch (action) {
                    case 1 -> actionSuccess = buyCard(player);
                    case 2 -> actionSuccess = pickTwiceSameGem(player, bank);
                    case 3 -> actionSuccess = pickThreeDifferentGems(player, bank);
                    case 4 -> showCards();
                    case 5 -> showBank(bank);
                    default -> System.out.println("Option invalide. Veuillez choisir un nombre entre 1 et 5.\n");
                }

                if (actionSuccess) {
                    return;
                } else {
                    System.out.println("Retour au menu.");
                    showMenu(player);
                    return;
            }
        }
    }

    public void launch() {
        initializeCards();
        displayedCards = new ArrayList<>(cards.subList(0, 12));
        System.out.println("Let the game begin !\n");

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

        showFinalRanking(players);
    }
}
