package splendor.model;

import java.util.*;

public sealed interface Game permits SimplifiedGame, CompleteGame {
    List<Player> getPlayers();

    void initializeGame();

    List<DevelopmentCard> getDisplayedCards();

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
        Objects.requireNonNull(p);
        getPlayers().add(p);
    }

    default boolean isGameOver() {
        return getPlayers().stream().anyMatch(p -> p.getPrestigeScore() >= 15);
    }

}