package splendor.controller;
import splendor.model.Game;
import splendor.model.Player;
import splendor.view.TerminalView;

import java.util.Objects;

public class PlayerController {
    private final Player player;
    private final Game game;
    private final TerminalView view;

    public PlayerController(Player player, TerminalView view) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(view);
        this.player = player;
        this.view = view;
    }

    public void buyCard() {
        // var card = view.selectCard();
        // var success = player.buyCard(card, game)

    }
}
