import java.util.*;

public class Player {
    private final String name;
    private final GemStack wallet;
    private final ArrayList<DevelopmentCard> reservedCards;
    private final ArrayList<DevelopmentCard> purchasedCards;

    public Player(String name) {
        this.wallet = new GemStack(4);
        this.purchasedCards = new ArrayList<>();
        this.reservedCards = new ArrayList<>();
        this.name = name;
    }

    public GemStack getWallet() {
        return wallet;
    }

    public List<DevelopmentCard> getPurchasedCards() {
        return purchasedCards;
    }

    public void addCard(DevelopmentCard card) {
        Objects.requireNonNull(card);
        purchasedCards.add(card);
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
        int score = 0;
        for (DevelopmentCard card : purchasedCards) {
            score += card.prestigeScore();
        }
        return score;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // [PLAYER X]
        sb.append("[").append("JOUEUR: ")
                .append(name)
                .append("] ");

        // Prestige: X
        sb.append("Prestige: ")
                .append(getPrestigeScore())
                .append(" | ");

        // Bonus: TYPE(count)...
        sb.append("Bonus: ");
        if (purchasedCards.isEmpty()) {
            sb.append("Aucun");
        } else {
            // Compter les bonus par type
            Map<GemToken, Integer> bonusCounts = new HashMap<>();
            for (DevelopmentCard card : purchasedCards) {
                GemToken bonus = card.bonus();
                bonusCounts.put(bonus, bonusCounts.getOrDefault(bonus, 0) + 1);
            }

            // Formater les bonus
            boolean first = true;
            for (Map.Entry<GemToken, Integer> entry : bonusCounts.entrySet()) {
                if (!first) {
                    sb.append(" ");
                }
                sb.append(entry.getKey().name()) // 3 premières lettres
                        .append("(")
                        .append(entry.getValue())
                        .append(")");
                first = false;
            }
        }

        // Jetons: TYPE(count)...
        sb.append("\nJetons: ")
                .append(wallet.toString()); // Supposons que GemStack a un toString() au format "TYPE(count)"

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
