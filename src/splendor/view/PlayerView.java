package splendor.view;

import java.util.Objects;
import splendor.model.Player;

public class PlayerView {
    public void showPlayerInfo(Player p) {
        Objects.requireNonNull(p);
        System.out.println(p);
    }

    public void showWallet(Player p) {
        Objects.requireNonNull(p);
        p.showWallet();
    }
}
