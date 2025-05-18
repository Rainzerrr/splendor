import java.util.*;

public class Player {
    private final String name;
    private final int age;
    private final GemStack wallet;
    private final List<DevelopmentCard> cards;
    private final List<DevelopmentCard> reservedCards;

    public Player(String name, int age) {
        if (age < 0 || age > 100) {
            throw new IllegalArgumentException("Entrez un âge correct.");
        }
        this.wallet = new GemStack(4);
        this.cards = new ArrayList<>();
        this.reservedCards = new ArrayList<>();
        this.name = name;
        this.age = age;
    }

    public GemStack getWallet() {
        return wallet;
    }

    public List<DevelopmentCard> getCards() {
        return cards;
    }

    public void addCard(DevelopmentCard card) {
        Objects.requireNonNull(card);
        cards.add(card);
    }

    public void removeCard(DevelopmentCard card) {
        Objects.requireNonNull(card);
        cards.remove(card);
    }

    public boolean hasCard(DevelopmentCard card) {
        Objects.requireNonNull(card);
        return cards.contains(card);
    }

    public void resetGame() {
        wallet.resetGame();
        cards.clear();
    }

    public int getPrestigeScore() {
        int score = 0;
        for (DevelopmentCard card : cards) {
            score += card.prestigeScore();
        }
        return score;
    }

    public void reserveCard(DevelopmentCard card) {
        reservedCards.add(card);
    }

    public List<DevelopmentCard> getReservedCards() {
        return reservedCards;
    }

    public void removeReservedCard(DevelopmentCard card) {
        reservedCards.remove(card);
    }

    public int getNbReservedCards() {
        return reservedCards.size();
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
        if (cards.isEmpty()) {
            sb.append("Aucun");
        } else {
            // Compter les bonus par type
            Map<GemToken, Integer> bonusCounts = new HashMap<>();
            for (DevelopmentCard card : cards) {
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

    public int getNbCards() {
        return cards.size();
    }

    public boolean canBuy(DevelopmentCard c) {
        Objects.requireNonNull(c);
        return wallet.canAfford(c.price());
    }

    public void buy(DevelopmentCard c) {
        Objects.requireNonNull(c);
        wallet.pay(c.price());
        cards.remove(c);
    }

    public int getAge() {
        return age;
    }
}