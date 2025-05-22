package splendor.cards;

import splendor.tokens.GemStack;
import splendor.tokens.GemToken;

import java.util.*;
import java.util.stream.Stream;

public class Player {
    private final String name;
    private final GemStack wallet;
    private final List<DevelopmentCard> reservedCards;
    private final List<DevelopmentCard> purchasedCards;
    private final List<Noble> acquiredNobles;

    public Player(String name) {
        this.wallet = new GemStack(2);
        this.purchasedCards = new ArrayList<>();
        this.reservedCards = new ArrayList<>();
        this.name = name;
        this.acquiredNobles = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public GemStack getWallet() {
        return wallet;
    }

    public List<Noble> getAcquiredNobles() {
        return acquiredNobles;
    }

    public List<DevelopmentCard> getPurchasedCards() {
        return purchasedCards;
    }

    public void addPurchasedCard(DevelopmentCard card) {
        Objects.requireNonNull(card);
        purchasedCards.add(card);
    }

    public List<DevelopmentCard> getReservedCards() {
        return reservedCards;
    }

    public void reserveCard(DevelopmentCard card) {
        reservedCards.add(card);
    }

    public void removeCard(DevelopmentCard card) {
        Objects.requireNonNull(card);
        purchasedCards.remove(card);
    }

    public boolean hasCard(DevelopmentCard card) {
        Objects.requireNonNull(card);
        return purchasedCards.contains(card);
    }

    public void resetGame() {
        wallet.resetGame();
        purchasedCards.clear();
    }

    public int getPrestigeScore() {
        return Stream.concat(
                        purchasedCards.stream().map(DevelopmentCard::prestigeScore),
                        acquiredNobles.stream().map(Noble::prestigeScore)
                )
                .mapToInt(Integer::intValue)
                .sum();
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[").append("JOUEUR: ")
                .append(name)
                .append("] ");

        sb.append("Prestige: ")
                .append(getPrestigeScore())
                .append(" | ");

        sb.append("Bonus: ");
        if (purchasedCards.isEmpty()) {
            sb.append("Aucun");
        } else {
            Map<GemToken, Integer> bonusCounts = new HashMap<>();
            for (DevelopmentCard card : purchasedCards) {
                GemToken bonus = card.bonus();
                bonusCounts.put(bonus, bonusCounts.getOrDefault(bonus, 0) + 1);
            }

            boolean first = true;
            for (Map.Entry<GemToken, Integer> entry : bonusCounts.entrySet()) {
                if (!first) {
                    sb.append(" ");
                }
                sb.append(entry.getKey().name())
                        .append("(")
                        .append(entry.getValue())
                        .append(")");
                first = false;
            }
        }

        sb.append("\nJetons: ")
                .append(wallet.toString());

        return sb.toString();
    }

    public boolean canBuy(DevelopmentCard c) {
        Objects.requireNonNull(c);
        return wallet.canAfford(c.price());
    }

    public void buy(DevelopmentCard c) {
        Objects.requireNonNull(c);
        wallet.pay(c.price());
        purchasedCards.remove(c);
    }
}
