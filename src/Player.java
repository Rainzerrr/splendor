import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Player {
    private String name;
    private GemStack wallet;
    private ArrayList<DevelopmentCard> cards;

    public Player(String name) {
        this.wallet = new GemStack(0);
        this.cards = new ArrayList<>();
        this.name = name;
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

    @Override
    public String toString() {
        return this.name;
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
}
