package splendor.app;

import splendor.controller.GameController;
import splendor.model.CompleteGame;
import splendor.model.SimplifiedGame;
import splendor.util.ConsoleInput;
import splendor.view.SplendorView;
import splendor.view.TerminalView;
import java.util.Objects;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
        var input = new ConsoleInput();
        var view = new TerminalView();
        view.displayMessage("[MENU PRINCIPAL]");
        view.displayMessage("1. Mode Simplifié (2 joueurs exactement)");
        view.displayMessage("2. Mode Complet (2 à 4 joueurs)");

        var choice = input.askInt("Votre choix (1-2) : ", 1, 2);
        GameController controller;

        if (choice == 1) {
            controller = createSimplifiedGame(input);
            // Lancement de l'interface graphique pour le mode simplifié
            new SplendorView(controller).run();
        } else {
            controller = createCompleteGame(input);
            // Lancement de l'interface graphique pour le mode complet
            new SplendorView(controller).run();
        }
    }

    private static GameController createSimplifiedGame(ConsoleInput input) {
        SimplifiedGame game = new SimplifiedGame();
        IntStream.rangeClosed(1, 2)
                .forEach(i -> {
                    var name = input.askString("Nom du joueur n°" + i + " : ");
                    game.addPlayer(name);
                });
        return new GameController(game);
    }

    private static GameController createCompleteGame(ConsoleInput input) {
        Objects.requireNonNull(input);
        var playerCount = input.askInt("Nombre de joueurs (2-4) : ", 2, 4);
        var game = new CompleteGame(playerCount);
        IntStream.rangeClosed(1, playerCount)
                .forEach(i -> {
                    var name = input.askString("Nom du Joueur " + i + " : ");
                    game.addPlayer(name);
                });
        return new GameController(game);
    }
}