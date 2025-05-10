import java.util.ArrayList;
import java.util.List;

public class Player {
    private GemStack wallet;
    private List<DevelopmentCard> cards;
    public Player(){
        this.wallet = new GemStack(0);
        this.cards = new ArrayList<>();
    }

    public GemStack getWallet() {
        return wallet;
    }

    public List<DevelopmentCard> getCards() {
        return cards;
    }

    public void addCard(DevelopmentCard card){
        cards.add(card);
    }

    public void removeCard(DevelopmentCard card){
        cards.remove(card);
    }

    public void resetGame(){
        wallet.resetGame();
        cards.clear();
    }

    @Override
    public String toString() {
        return "Player{" +
                "wallet=" + wallet +
                ", cards=" + cards +
                '}';
    }

    public int getNbCards(){
        return cards.size();
    }

    public boolean hasCard(DevelopmentCard card){
        return cards.contains(card);
    }
}
