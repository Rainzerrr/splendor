import java.util.ArrayList;
import java.util.List;

public class CompleteGame implements Game {
    private final List<Player> players;
    private final GemStack bank;
    private final List<DevelopmentCard> cards;
    private boolean gameOver;
    private ArrayList<DevelopmentCard> displayedCards;

    public CompleteGame(int playerNumber) {
        players = new ArrayList<>();
        bank = new GemStack(7);
        cards = new ArrayList<>();
        gameOver = false;
    }

    @Override
    public void initializeCards() {

    }

    @Override
    public void launch() {

    }

    @Override
    public void showMenu(Player p) {

    }

    @Override
    public List<Player> getPlayers(){
        return players;
    }



}
