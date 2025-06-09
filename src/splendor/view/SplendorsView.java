package splendor.view;

import splendor.model.*;

import java.util.List;

public sealed interface SplendorsView permits TerminalView, GraphicView {
    void displayMessage(String message);

    // Affichage des éléments du jeu
    void showBank(GemStock bank);
    void showCards(Game game);
    void showNobles(List<Noble> nobles);
    void showPlayerTurn(Player player);
    void showMenu(Game game);
    void showBoard(Game game);

    // Interactions utilisateur
    int getMenuChoice(Game game);
    int selectCard(int maxIndex);
    GemToken askGemToDiscard(Player player);

    // Messages d'information
    void showNotEnoughTokens();
    void showTokensTaken(GemToken token, int amount);
    void showFinalRanking(List<Player> players);
    void showTokenAlreadyTaken();
    void showNoMoreTokens(GemToken token);
    void showRemainingChoices(int remaining);
}