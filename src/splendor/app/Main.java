package splendor.app;

import splendor.controller.GameController;
import splendor.model.CompleteGame;
import splendor.model.SimplifiedGame;
import splendor.model.CompleteGame;
import splendor.model.Game;
import splendor.model.Player;
import splendor.view.ConsoleInput;
import splendor.view.TerminalView;

import java.util.InputMismatchException;
import java.util.Objects;
import java.util.Scanner;
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
        } else {
            controller = createCompleteGame(input);
        }

        controller.launchGame();
    }


    /**
     * Creates a simplified game setup with exactly 2 players.
     *
     * @param input An instance of ConsoleInput to gather player names.
     * @return A GameController initialized with a SimplifiedGame containing 2 players.
     */
    private static GameController createSimplifiedGame(ConsoleInput input) {
        SimplifiedGame game = new SimplifiedGame();
        IntStream.rangeClosed(1, 2)
                .forEach(i -> {
                    var name = input.askString("Nom du joueur n°" + i + " : ");
                    game.addPlayer(name);
                });
        return new GameController(game);
    }

    /**
     * Creates a complete game setup with 2 to 4 players.
     *
     * @param input An instance of ConsoleInput to gather player names.
     * @return A GameController initialized with a CompleteGame containing 2 to 4 players.
     */
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