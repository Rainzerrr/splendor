package splendor.model;

import java.util.*;
import java.util.stream.Collectors;

public sealed interface Game permits SimplifiedGame, CompleteGame {
    List<Player> getPlayers();

    void initializeGame();

    List<DevelopmentCard> getDisplayedCards();

    List<Integer> getAmountsOfCardByLevel();

    GemStock getBank();

    List<Noble> getNobles();

    void replaceCard(DevelopmentCard card);

    void removeTokenFromBank(GemToken gem, int amount);

    /**
     * Adds a player to the game.
     *
     * @param p the player to add; must not be null
     * @throws NullPointerException if p is null
     */
    default void addPlayer(Player p) {
        Objects.requireNonNull(p, "Player cannot be null");
        getPlayers().add(p);
    }

    default boolean isGameOver() {
        Objects.requireNonNull(getPlayers(), "Player list cannot be null");
        return getPlayers().stream().anyMatch(p -> p.getPrestigeScore() >= 15);
    }

    default Map<Integer, List<DevelopmentCard>> groupCardsByLevel(List<DevelopmentCard> cards) {
        Objects.requireNonNull(cards, "Card list cannot be null");
        return cards.stream()
                .collect(Collectors.groupingBy(DevelopmentCard::level));
    }
}