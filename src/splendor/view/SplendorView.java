package splendor.view;

import com.github.forax.zen.Application;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.PointerEvent;
import splendor.controller.GameController;
import splendor.model.*;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class SplendorView {
    private final GameController controller;
    private final Game game;
    private String message = "Bienvenue dans Splendor!";
    private int width = 1200, height = 800;
    private Font uiFont;

    private static final Color PRIMARY    = new Color(255, 215, 0);
    private static final Color SECONDARY  = new Color(70, 130, 180);
    private static final Color BACKGROUND = new Color(16, 24, 32);
    private static final Color TEXT       = Color.WHITE;

    private final Map<Rectangle, Runnable> zones = new LinkedHashMap<>();

    private static final int PADDING = 20;
    private static final int HEADER_H = 100;
    private static final int NOBLES_H = 200;

    public SplendorView(GameController controller) {
        this.controller = controller;
        this.game = controller.getGame();
        loadFont();
    }

    private void loadFont() {
        try {
            uiFont = Font.createFont(Font.TRUETYPE_FONT,
                            new File("splendor/app/ttf/EduQLDHand-Regular.ttf"))
                    .deriveFont(Font.PLAIN, 16);
            GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .registerFont(uiFont);
        } catch (FontFormatException | IOException e) {
            uiFont = new Font("Arial", Font.PLAIN, 16);
        }
    }

    public void run() {
        controller.initializeGame();
        Application.run(BACKGROUND, ctx -> {
            while (true) {
                width  = ctx.getScreenInfo().width();
                height = ctx.getScreenInfo().height();
                zones.clear();
                ctx.renderFrame(this::renderFrame);
                handleClicks(ctx);
            }
        });
    }

    private void handleClicks(ApplicationContext ctx) {
        var e = ctx.pollOrWaitEvent(100);
        if (e instanceof PointerEvent p && p.action() == PointerEvent.Action.POINTER_DOWN) {
            int x = p.location().x(), y = p.location().y();
            zones.entrySet().stream()
                    .filter(en -> en.getKey().contains(x, y))
                    .findFirst()
                    .ifPresent(en -> en.getValue().run());
        }
    }

    private void renderFrame(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(uiFont);
        drawHeader(g);
        drawPlayersArea(g);
        drawBankAndCardsArea(g);
        drawNoblesArea(g);
    }

    private void drawHeader(Graphics2D g) {
        g.setColor(SECONDARY);
        g.fillRect(0, 0, width, HEADER_H);
        g.setColor(TEXT);
        g.setFont(uiFont.deriveFont(Font.BOLD, 48f));
        g.drawString("Splendor", width/2 - 150, 60);
        g.setFont(uiFont.deriveFont(18f));
        g.drawString(message, width/2 - 80, 85);
    }

    private void drawPlayersArea(Graphics2D g) {
        List<Player> players = game.getPlayers();
        int areaW = width / 5;                     // 20% de l’écran
        int y0    = HEADER_H + PADDING;
        int h0    = height - HEADER_H - NOBLES_H - 2 * PADDING;
        Font bold20 = uiFont.deriveFont(Font.BOLD, 20f);

        // Premier joueur à gauche
        Player p0 = players.get(0);
        int x0 = PADDING;
        paintPlayer(g, p0, x0, y0, areaW - 2*PADDING, h0);
        zones.put(new Rectangle(x0, y0, areaW - 2*PADDING, h0),
                () -> onPlayerClick(p0));

        // Deuxième joueur à droite
        Player p1 = players.get(1);
        int x1 = width - areaW + PADDING;
        paintPlayer(g, p1, x1, y0, areaW - 2*PADDING, h0);
        zones.put(new Rectangle(x1, y0, areaW - 2*PADDING, h0),
                () -> onPlayerClick(p1));
    }

    private void paintPlayer(Graphics2D g, Player p,
                             int x, int y, int w, int h) {
        RoundRectangle2D bg = new RoundRectangle2D.Float(x,y,w,h,10,10);
        g.setColor(new Color(30,40,50,220)); g.fill(bg);
        g.setColor(PRIMARY);                 g.draw(bg);
        g.setColor(TEXT);
        g.setFont(uiFont.deriveFont(Font.BOLD,20f));
        g.drawString(p.getName(), x+10, y+30);
        g.drawString("★"+p.getPrestigeScore(), x+w-60, y+30);
        int ty = y+60;
        for (GemToken t : GemToken.values()) {
            int cnt = p.getTokenCount(t);
            if (cnt>0) {
                drawToken(g, t, cnt, x+10, ty);
                ty += 30;
            }
        }
        g.setFont(uiFont);
        g.drawString("Cartes: "+p.getPurchasedCards().size(),
                x+10, y+h-20);
    }

    private void drawBankAndCardsArea(Graphics2D g) {
        int x0 = width/5 + PADDING;
        int areaW = width*3/5 - 2*PADDING;
        int y0 = HEADER_H+PADDING;
        int h0 = height - HEADER_H - NOBLES_H - 2*PADDING;
        int bankH = 180;
        drawBank(g, x0, y0, areaW, bankH);
        zones.put(new Rectangle(x0,y0,areaW,bankH), this::onBankClick);
        drawCardLevels(g, x0, y0+bankH+PADDING,
                areaW, h0-bankH-PADDING);
        zones.put(new Rectangle(x0,y0+bankH+PADDING,
                        areaW,h0-bankH-PADDING),
                this::onCardClick);
    }

    private void drawBank(Graphics2D g,
                          int x, int y, int w, int h) {
        RoundRectangle2D r = new RoundRectangle2D.Float(x,y,w,h,10,10);
        g.setColor(new Color(30,40,50,220)); g.fill(r);
        g.setColor(PRIMARY);                 g.draw(r);
        g.setColor(TEXT);
        g.setFont(uiFont.deriveFont(Font.BOLD,20f));
        g.drawString("Réserve", x+10, y+30);
        int ts=30, sp=20, perRow=2, cx=x+20, cy=y+50;
        for (GemToken t : GemToken.values()) {
            drawToken(g, t, game.getBank().getAmount(t), cx, cy);
            zones.put(new Rectangle(cx,cy,ts,ts),
                    () -> onTokenClick(t));
            if ((cy - y - 50)/ (ts+sp) % perRow == perRow-1) {
                cx = x+20; cy += ts+sp;
            } else {
                cx += ts+sp+20;
            }
        }
    }

    private void drawCardLevels(Graphics2D g,
                                int x, int y, int w, int h) {
        Map<Integer,List<DevelopmentCard>> byLvl =
                game.groupCardsByLevel(game.getDisplayedCards());
        int lvlH = h / byLvl.size(), ly = y;
        for (var e : byLvl.entrySet()) {
            if (e.getValue().isEmpty()) { ly += lvlH; continue; }
            g.setColor(PRIMARY);
            g.setFont(uiFont.deriveFont(Font.BOLD,18f));
            g.drawString("Niveau "+e.getKey(), x+10, ly+20);
            drawCards(g, x, ly+30, w, e.getValue());
            ly += lvlH;
        }
    }

    private void drawCards(Graphics2D g,
                           int x, int y, int w, List<DevelopmentCard> cards) {
        int cw=120, pad=20;
        int total = cards.size()*(cw+pad);
        int start = x + Math.max(0,(w-total)/2);
        for (int i=0; i<cards.size(); i++) {
            drawCard(g, cards.get(i),
                    start+i*(cw+pad), y, cw, 180);
        }
    }

    private void drawNoblesArea(Graphics2D g) {
        int y0 = height - NOBLES_H;
        RoundRectangle2D r = new RoundRectangle2D.Float(0,y0,
                width,NOBLES_H,10,10);
        g.setColor(new Color(30,40,50,220)); g.fill(r);
        g.setColor(PRIMARY);                 g.draw(r);
        g.setColor(TEXT);
        g.setFont(uiFont.deriveFont(Font.BOLD,20f));
        g.drawString("Nobles", PADDING, y0+30);
        int cx=PADDING, cw=100, sp=30;
        for (Noble n : game.getNobles()) {
            drawNobleCard(g, n, cx, y0+50, cw, 120);
            zones.put(new Rectangle(cx,y0+50,cw,120),
                    () -> onNobleClick(n));
            cx += cw + sp;
        }
    }

    private void drawCard(Graphics2D g, DevelopmentCard c,
                          int x, int y, int w, int h) {
        RoundRectangle2D r = new RoundRectangle2D.Float(x,y,w,h,10,10);
        g.setColor(Color.WHITE); g.fill(r);
        g.setColor(Color.BLACK); g.draw(r);
        g.setFont(uiFont.deriveFont(Font.BOLD,18f));
        g.drawString(""+c.prestigeScore(), x+10, y+30);
        g.setColor(getTokenColor(c.bonus()));
        g.fillOval(x+w-40, y+10, 20, 20);
        g.setFont(uiFont);
        int dy=60;
        for (var en : c.price().entrySet()) {
            g.drawString(en.getKey()+": "+en.getValue(),
                    x+10, y+dy);
            dy += 20;
        }
    }

    private void drawToken(Graphics2D g, GemToken t,
                           int cnt, int x, int y) {
        g.setColor(getTokenColor(t)); g.fillOval(x,y,30,30);
        g.setColor(Color.BLACK);        g.drawOval(x,y,30,30);
        g.setColor(TEXT);
        g.setFont(uiFont.deriveFont(Font.BOLD,14f));
        g.drawString(""+cnt, x+20, y+22);
    }

    private void drawNobleCard(Graphics2D g, Noble n,
                               int x, int y, int w, int h) {
        RoundRectangle2D r = new RoundRectangle2D.Float(x,y,w,h,10,10);
        g.setColor(new Color(230,230,250)); g.fill(r);
        g.setColor(new Color(139,69,19));    g.draw(r);
        g.setColor(Color.BLACK);
        g.setFont(uiFont.deriveFont(Font.BOLD,18f));
        g.drawString("★"+n.prestigeScore(), x+10, y+30);
    }

    private Color getTokenColor(GemToken t) {
        return switch (t) {
            case RUBY     -> new Color(255, 99, 71);
            case EMERALD  -> new Color(50, 205, 50);
            case DIAMOND  -> new Color(100, 149, 237);
            case SAPPHIRE -> new Color(0, 191, 255);
            case ONYX     -> new Color(30,  30,  30);
            case GOLD     -> PRIMARY;
        };
    }

    private void onPlayerClick(Player p) {
        message = "Joueur cliqué: " + p.getName();
    }
    private void onBankClick() {
        message = "Banque cliquée";
    }
    private void onTokenClick(GemToken t) {
        message = "Jeton cliqué: " + t;
    }
    private void onCardClick() {
        message = "Carte cliquée";
    }
    private void onNobleClick(Noble n) {
        message = "Noble cliqué: " + n.prestigeScore();
    }
}
