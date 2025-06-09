package splendor.model;

import java.util.*;
import java.util.stream.IntStream;

public final class SimplifiedGame implements Game {
    private final List<Player> players = new ArrayList<>();
    private final GemStock bank = new GemStock(7, 7);
    private final List<DevelopmentCard> cardDecks = new ArrayList<>();
    private final List<DevelopmentCard> displayedCards = new ArrayList<>();

    /**
     * Initializes the game by setting up the deck of cards.
     * This is only done once, at the start of the game.
     */
    @Override
    public void initializeGame() {
        initializeCards();
    }

    /**
     * Returns an unmodifiable list of all players in the game.
     *
     * @return The list of players.
     */
    @Override
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /**
     * Initializes the deck of cards by adding 8 cards of each color with cost 3 and victory points 1.
     * The cards are shuffled and the first 12 are displayed.
     */
    public void initializeCards() {
        Arrays.stream(GemToken.values())
                .filter(color -> color != GemToken.GOLD)
                .forEach(color -> {
                    IntStream.range(0, 8).forEach(i -> {
                        EnumMap<GemToken, Integer> cost = new EnumMap<>(GemToken.class);
                        cost.put(color, 3);
                        cardDecks.add(new DevelopmentCard(cost, color, 1, 1, "../resources/cards/" + color.toString().toLowerCase() + "_mine.png"));
                    });
                });

        Collections.shuffle(cardDecks);
        displayedCards.addAll(cardDecks.subList(0, 12));
    }

    /**
     * Adds a player to the game.
     * @param name The name of the player to be added.
     */
    public void addPlayer(String name) {
        players.add(new Player(name));
    }

    /**
     * Replaces a card in the displayed cards with a new one from the deck.
     * If the deck is empty, the card is simply removed from the displayed cards.
     * @param card The card to be replaced.
     */
    public void replaceCard(DevelopmentCard card) {
        displayedCards.remove(card);
        if (!cardDecks.isEmpty()) {
            displayedCards.add(cardDecks.removeFirst());
        }
    }
    @Override
    public List<Integer> getAmountsOfCardByLevel(){
        return List.of(cardDecks.size());
    };


    /**
     * Returns an unmodifiable list of currently displayed development cards.
     * These cards are visible to all players and available for purchase.
     *
     * @return An unmodifiable list of displayed development cards.
     */
    @Override
    public List<DevelopmentCard> getDisplayedCards() {
        return Collections.unmodifiableList(displayedCards);
    }

    /**
     * Returns the bank of gems available for purchase.
     *
     * @return The bank of gems.
     */
    @Override
    public GemStock getBank() {
        return bank;
    }

    /**
     * Attempts to purchase a card from the displayed cards.
     * In the simplified game, this is not possible, so this method always returns false.
     *
     * @param p The player who wishes to purchase the card.
     * @return Whether the card could be purchased.
     */

    public boolean buyCard(Player p) {
        return false;
    }

    /**
     * Returns an empty list because there are no nobles in the simplified game.
     * @return An empty list of nobles.
     */
    @Override
    public List<Noble> getNobles() {
        return Collections.emptyList();
    }

    /**
     * Removes a certain amount of a gem token from the bank.
     * @param gem The gem token to remove.
     * @param amount The amount of the gem token to remove.
     */
    public void removeTokenFromBank(GemToken gem, int amount) {
        Objects.requireNonNull(gem);
        bank.remove(gem, amount);
    }
}