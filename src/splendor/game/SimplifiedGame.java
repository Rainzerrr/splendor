package splendor.game;

import splendor.cards.DevelopmentCard;
import splendor.tokens.GemStock;
import splendor.tokens.GemToken;
import splendor.player.Player;

import java.util.*;
import java.util.stream.IntStream;

public class SimplifiedGame implements Game {
    private final List<Player> players;
    private final GemStock bank;
    private final List<DevelopmentCard> cardDecks;
    private boolean gameOver;
    private ArrayList<DevelopmentCard> displayedCards;

    public SimplifiedGame() {
        players = new ArrayList<>();
        bank = new GemStock(7, 7) ;
        cardDecks = new ArrayList<>();
        gameOver = false;
    }

    /**
     * Return the list of players in the game.
     *
     * @return an unmodifiable list of players
     */
    public List<Player> getPlayers(){
        return players;
    };

    /**
     * Initializes the deck of cards. The deck is composed of 28 cards :
     * 7 cards of each color (except gold) and a cost of 3 tokens of the same color.
     * The cards are also given a prestige value of 1.
     * The deck is then shuffled.
     */
    @Override
    public void initializeCards() {
        Arrays.stream(GemToken.values())
                .filter(color -> color != GemToken.GOLD)
                .forEach(color -> {
                    IntStream.range(0, 8).forEach(i -> {
                        EnumMap<GemToken, Integer> cost = new EnumMap<>(GemToken.class);
                        cost.put(color, 3);
                        cardDecks.add(new DevelopmentCard(cost, color, 1, 1));
                    });
                });

        Collections.shuffle(cardDecks);
    }

    /**
     * Display the cards that are currently available to buy.
     *
     * Each card is displayed with its index (starting from 1) and its string representation.
     * The cards are separated by a newline and an extra newline is added at the end.
     */
    public void showCards() {
        for (int i = 0; i < displayedCards.size(); i++) {
            System.out.println((i + 1) + " - " + displayedCards.get(i));
        }
        System.out.println();
    }

    /**
     * Display the current state of the board.
     *
     * The board's state consists of the bank's current gems and the cards that are currently available to buy.
     * The bank's gems are displayed first, followed by the cards available to buy, each with its index (starting from 1).
     * A newline is added at the end of the display.
     */
    private void displayBoard() {
        showBank(bank);

        showHeader("CARTES DISPONIBLES");
        for (var i = 0; i < displayedCards.size(); i++) {
            System.out.println((i + 1) + " - " + displayedCards.get(i).toString());
        }
        System.out.println();
    }

    /**
     * Attempts to buy a card for the specified player.
     *
     * This method displays the currently available cards and prompts the player
     * to select one for purchase. If the player successfully purchases a card,
     * it is removed from the list of displayed cards and a new card is drawn
     * from the deck if available. If no cards are available, the method returns
     * to the menu without making a purchase.
     *
     * @param p The player attempting to buy a card.
     * @return true if the card was successfully purchased, false otherwise.
     */
    private boolean buyCard(Player p) {
        Objects.requireNonNull(p);
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
                displayedCards.remove(indice - 1);
                System.out.println("Carte achetée : " + chosen + "\n");

                // Ajouter une nouvelle carte piochée si disponible
                if (!cardDecks.isEmpty()) {
                    DevelopmentCard newCard = cardDecks.removeFirst();  // Retirer la première carte de la pioche
                    displayedCards.add(newCard);  // Ajouter au tableau affiché
                    System.out.println("Nouvelle carte ajoutée dans la pioche : " + newCard);
                }

                return true;
            } else {
                System.out.println("Achat échoué. Veuillez réessayer.\n");
            }
        }
    }

    /**
     * Display the menu of actions available to the player, and handle the player's choice.
     * The player can choose from the following actions:
     * 1. Buy a card
     * 2. Pick two identical gems
     * 3. Pick three different gems
     * 4. Show cards available to buy
     * 5. Show bank
     * The method will continue to prompt the user until a valid choice is made.
     * If the chosen action is successful, the method will return. Otherwise, it will show the menu again.
     * @param player the player who is making the choice
     */
    public void showMenu(Player player) {
        Objects.requireNonNull(player);
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

    /**
     * Starts the game.
     *
     * This method initializes the deck of cards, the bank of gems, and the list of displayed cards.
     * It then enters a loop where it will repeatedly ask each player to choose an action until a player's prestige score reaches 15.
     * The game then ends and the final ranking is displayed.
     */
    public void launch() {
        initializeCards();
        displayedCards = new ArrayList<>(cardDecks.subList(0, 12));
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
