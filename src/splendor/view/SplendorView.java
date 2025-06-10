package splendor.view;

import splendor.model.*;

import java.util.List;

public sealed interface SplendorView permits TerminalView, GraphicView {
    void displayMessage(String message);

    void showBank(GemStock bank);
    void showCards(Game game);
    void showNobles(List<Noble> nobles);
    void showPlayerTurn(Player player, Game game);
    void showMenu(Game game);
    void showBoard(Game game);

    int getMenuChoice(Game game);
    int selectCard(int maxIndex, boolean isReserved);
    int selectToken(int maxIndex);
    GemToken askGemToDiscard(Player player);

    void showNotEnoughTokens();
    void showTokensTaken(GemToken token, int amount);
    void showFinalRanking(List<Player> players);
    void showTokenAlreadyTaken();
    void showNoMoreTokens(GemToken token);
    void showRemainingChoices(int remaining);
}