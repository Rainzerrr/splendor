package splendor.app;

import splendor.controller.GameController;
import splendor.model.CompleteGame;
import splendor.model.SimplifiedGame;
import splendor.util.ConsoleInput;
import splendor.view.GraphicView;
import splendor.view.SplendorView;
import splendor.view.TerminalView;
import java.util.Objects;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
        var input = new ConsoleInput();
        var view = new TerminalView();
        view.displayMessage("[MENU PRINCIPAL]");

        view.displayMessage("1. Affichage dans la console");
        view.displayMessage("2. Affichage graphique");

        var choiceDisplay = input.askInt("Votre choix (1-2) : ", 1, 2);

        view.displayMessage("1. Mode Simplifié (2 joueurs exactement)");
        view.displayMessage("2. Mode Complet (2 à 4 joueurs)");

        var choiceGameMode = input.askInt("Votre choix (1-2) : ", 1, 2);
        GameController controller;

        SplendorView gameView = choiceDisplay == 1 ? view :  new GraphicView();

        if (choiceGameMode == 1) {
            controller = createSimplifiedGame(input, gameView);
            switch(gameView){
                case TerminalView t -> {}
                case GraphicView g -> g.run();

            }
            controller.launchGame();
        } else {
            controller = createCompleteGame(input, gameView);
            switch(gameView){
                case TerminalView t -> {}
                case GraphicView g -> g.run();
            }
            controller.launchGame();
        }
    }

    private static GameController createSimplifiedGame(ConsoleInput input, SplendorView view) {
        SimplifiedGame game = new SimplifiedGame();
        IntStream.rangeClosed(1, 2)
                .forEach(i -> {
                    var name = input.askString("Nom du joueur n°" + i + " : ");
                    game.addPlayer(name);
                });
        return new GameController(game, view);
    }

    private static GameController createCompleteGame(ConsoleInput input, SplendorView view) {
        Objects.requireNonNull(input);
        var playerCount = input.askInt("Nombre de joueurs (2-4) : ", 2, 4);
        var game = new CompleteGame(playerCount);
        IntStream.rangeClosed(1, playerCount)
                .forEach(i -> {
                    var name = input.askString("Nom du Joueur " + i + " : ");
                    game.addPlayer(name);
                });
        return new GameController(game, view);
    }
}