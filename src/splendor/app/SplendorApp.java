package splendor.app;

import com.github.forax.zen.Application;
import com.github.forax.zen.Event;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;
import splendor.controller.GameController;
import splendor.model.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SplendorApp {
    // record pour mémoriser, à chaque frame, les zones de cartes et de jetons
    private record CardRect(int x, int y, int w, int h, int index) {}
    private record CoinRect(int x, int y, int size, GemToken token) {}

    public static void main(String[] args) {
        // 1) Instanciation du jeu et des joueurs
        Game game = new CompleteGame(2);
        game.addPlayer(new Player("Mounir"));
        game.addPlayer(new Player("Bob"));
        GameController controller = new GameController(game, null);

        // 2) Lancement de la boucle Zen
        Application.run(Color.DARK_GRAY, context -> {
            game.initializeGame();
            List<Player> players = game.getPlayers();
            int[] current = {0};

            // Ordre pour l'affichage de la banque
            List<GemToken> tokenOrder = List.of(
                    GemToken.RUBY, GemToken.EMERALD, GemToken.DIAMOND,
                    GemToken.SAPPHIRE, GemToken.ONYX, GemToken.GOLD
            );

            // Conteneurs pour les zones cliquables
            List<CardRect> cardRects = new ArrayList<>();
            List<CoinRect> coinRects = new ArrayList<>();

            // Boucle de jeu
            while (!game.isGameOver()) {
                Player player = players.get(current[0]);
                String header = "Au tour de " + player.getName();

                // --- 2a) Dessin du plateau et mémorisation des zones --------------
                context.renderFrame(g -> {
                    var screen = context.getScreenInfo();
                    int sw = screen.width(), sh = screen.height();

                    // En-tête
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("SansSerif", Font.BOLD, 28));
                    int tw = g.getFontMetrics().stringWidth(header);
                    g.drawString(header, (sw - tw) / 2, 40);

                    // Cartes
                    cardRects.clear();
                    List<DevelopmentCard> cards = game.getDisplayedCards();
                    int cols = 4, w = 100, h = 150, p = 20;
                    int startX = (sw - (cols*w + (cols-1)*p)) / 2;
                    int startY = 80;
                    g.setFont(new Font("SansSerif", Font.PLAIN, 16));
                    for (int i = 0; i < cards.size(); i++) {
                        int row = i/cols, col = i%cols;
                        int x = startX + col*(w+p), y = startY + row*(h+p);
                        // dessin
                        g.setColor(Color.WHITE);
                        g.fillRoundRect(x, y, w, h, 12, 12);
                        g.setColor(Color.BLACK);
                        g.drawRoundRect(x, y, w, h, 12, 12);
                        g.drawString("Niv " + cards.get(i).level(), x+8, y+24);
                        // mémorisation
                        cardRects.add(new CardRect(x, y, w, h, i));
                    }

                    // Banque
                    coinRects.clear();
                    int bx0 = 60, by0 = sh - 180, dx = 80, size = 40;
                    for (int i = 0; i < tokenOrder.size(); i++) {
                        GemToken token = tokenOrder.get(i);
                        int cnt = game.getBank().getAmount(token);
                        Color col = mapTokenToColor(token);
                        for (int j = 0; j < cnt; j++) {
                            int x = bx0 + i*dx, y = by0 - j*22;
                            g.setColor(col);
                            g.fillOval(x, y, size, size);
                            g.setColor(Color.BLACK);
                            g.drawOval(x, y, size, size);
                        }
                        if (cnt > 0) {
                            int y = by0 - (cnt-1)*22;
                            coinRects.add(new CoinRect(bx0 + i*dx, y, size, token));
                        }
                    }

                    // Panneaux joueurs
                    drawPlayerPanel(g, player, 30, sh - 220, tokenOrder);
                    drawPlayerPanel(g,
                            players.get((current[0]+1)%players.size()),
                            sw - 200, sh - 220, tokenOrder
                    );
                });

                // --- 2b) Gestion du clavier -------------------------------------
                Event evt = context.pollOrWaitEvent(200);
                if (evt instanceof KeyboardEvent ke
                        && ke.action() == KeyboardEvent.Action.KEY_PRESSED) {
                    int action = mapKeyToAction(ke.key());
                    if (action > 0
                            && controller.processAction(action, players.get(current[0]))) {
                        // action réussie → noble + joueur suivant
                        players.get(current[0])
                                .claimNobleIfEligible(game.getNobles());
                        current[0] = (current[0] + 1) % players.size();
                    }
                }

                // --- 2c) Gestion du clic souris --------------------------------
                else if (evt instanceof PointerEvent pe
                        && pe.action() == PointerEvent.Action.POINTER_DOWN) {
                    int mx = pe.location().x(), my = pe.location().y();

                    // Clic sur une carte → action “1: acheter”
                    boolean done = false;
                    for (var r : cardRects) {
                        if (mx >= r.x && mx <= r.x+r.w
                                && my >= r.y && my <= r.y+r.h) {
                            if (controller.processAction(1, players.get(current[0]))) {
                                players.get(current[0])
                                        .claimNobleIfEligible(game.getNobles());
                                current[0] = (current[0] + 1) % players.size();
                            }
                            done = true;
                            break;
                        }
                    }
                    if (done) continue;

                    // Clic sur un tas de jetons → action “2: deux identiques” ou “3: trois différents”
                    for (var c : coinRects) {
                        if (mx >= c.x && mx <= c.x+c.size
                                && my >= c.y && my <= c.y+c.size) {
                            int action = game.getBank().getAmount(c.token) >= 4 ? 2 : 3;
                            if (controller.processAction(action, players.get(current[0]))) {
                                players.get(current[0])
                                        .claimNobleIfEligible(game.getNobles());
                                current[0] = (current[0] + 1) % players.size();
                            }
                            break;
                        }
                    }
                }
            }

            // 3) Fin de partie
            controller.getView().showFinalRanking(game.getPlayers());
        });
    }

    /** Dessine le panneau d’un joueur */
    private static void drawPlayerPanel(Graphics2D g, Player p,
                                        int x0, int y0,
                                        List<GemToken> order) {
        g.setColor(new Color(255,255,255,60));
        g.fillRoundRect(x0-10, y0-30, 180, 260, 18, 18);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString(p.getName(), x0, y0);

        int y = y0 + 30;
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        for (var token : order) {
            int cnt = p.getTokenCount(token);
            Color col = mapTokenToColor(token);
            g.setColor(col);
            g.fillOval(x0, y, 30, 30);
            g.setColor(Color.BLACK);
            g.drawOval(x0, y, 30, 30);
            g.drawString(String.valueOf(cnt), x0+10, y+20);
            y += 36;
        }

        g.setColor(Color.YELLOW);
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("★ " + p.getPrestigeScore(), x0, y + 28);
    }

    /** Mapping des touches AZERTY → code action 1…9 */
    private static int mapKeyToAction(KeyboardEvent.Key key) {
        return switch (key) {
            case A -> 1; case Z -> 2; case E -> 3; case R -> 4;
            case T -> 5; case Y -> 6; case U -> 7; case I -> 8; case O -> 9;
            default  -> -1;
        };
    }

    /** Couleurs associées aux GemToken */
    private static Color mapTokenToColor(GemToken t) {
        return switch (t) {
            case RUBY     -> Color.RED;
            case EMERALD  -> Color.GREEN;
            case DIAMOND  -> Color.BLUE;
            case SAPPHIRE -> Color.YELLOW;
            case ONYX     -> Color.BLACK;
            case GOLD     -> Color.LIGHT_GRAY;
        };
    }
}