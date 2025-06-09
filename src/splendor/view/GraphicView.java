package splendor.view;

import com.github.forax.zen.Application;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.Event;
import com.github.forax.zen.PointerEvent;
import splendor.app.Demo;
import splendor.app.Main;
import splendor.model.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

public final class GraphicView implements SplendorsView{
    private ApplicationContext context;
    private final Map<Rectangle, Integer> menuButtons;

    public GraphicView() {
        menuButtons = new HashMap<>();
    }

    public static final Map<GemToken, Color> TOKEN_COLORS = new EnumMap<>(Map.of(
            GemToken.DIAMOND, new Color(210, 210, 210),
            GemToken.SAPPHIRE, new Color(0, 102, 204),
            GemToken.EMERALD, new Color(0, 128, 0),
            GemToken.RUBY, new Color(204, 0, 0),
            GemToken.ONYX, new Color(30, 30, 30),
            GemToken.GOLD, new Color(212, 175, 55)
    ));

    private static final int PLAYER_FRAME_WIDTH = 300;
    private static final int PLAYER_FRAME_HEIGHT = 250;
    
    private static final int CARD_WIDTH = 130;
    private static final int CARD_HEIGHT = 170;

    private static final int NOBLE_WIDTH = 130;
    private static final int NOBLE_HEIGHT = 130;

    private static final int PADDING = 20;
    private static final Color PANEL_BACKGROUND = new Color(120, 120, 120, 200);
    private static final Color PANEL_BORDER = new Color(212, 175, 55);

    private static final Color BUBBLE_BACKGROUND = new Color(90, 90, 90);
    private static final Color BUBBLE_BORDER = PANEL_BORDER;
    private static final Color BUBBLE_TEXT_COLOR = Color.WHITE;

    private static final int CORNER_RADIUS = 20;

    private static final int BUTTON_WIDTH = 290;
    private static final int BUTTON_HEIGHT = 50;
    private static final int BUTTON_SPACING = 20;
    private static final int BUTTON_RADIUS = 20;


    private static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 20);
    private static final Font BANK_TOKEN_COUNT_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font BUBBLE_FONT = new Font("SansSerif", Font.BOLD, 14);

    private static final Font PLAYER_TOKEN_COUNT_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font CARD_STACK_AMOUNT_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font CARD_TOKEN_AMOUNT_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font NOBLE_TOKEN_AMOUNT_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font NOBLE_PRESTIGE_FONT = new Font("SansSerif", Font.BOLD, 28);
    private static final Font CARD_PRESTIGE_FONT = new Font("SansSerif", Font.BOLD, 28);
    private static final Font PLAYER_NAME_FONT = new Font("SansSerif", Font.BOLD, 24);
    private static final Font PLAYER_INFOS_TITLE_FONT = new Font("SansSerif", Font.PLAIN, 18);
    private static final Font PLAYER_INFOS_BONUS_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font MENU_BUTTON_FONT = new Font("SansSerif", Font.BOLD, 18);

    public void run(){
        Application.run(Color.BLUE.darker(), context -> {
            setContext(context);
            context.renderFrame(this::draw);
        });
    }

    public void setContext(ApplicationContext context) {
        this.context = context;
    }

    private void drawBackground(Graphics2D g) throws IOException {
        InputStream inputStream = Main.class.getResourceAsStream("../resources/images/background.png");
        if (inputStream == null) {
            throw new IOException("Image not found in resources!");
        }
        BufferedImage image = ImageIO.read(inputStream);
        System.out.println("Image loaded: " + image.getWidth() + "x" + image.getHeight());
        g.drawImage(image, 0, 0, null);
    }

    private void drawHeader(Graphics2D g, String message, int screenWidth) {
        // Rectangle
        g.setColor(Color.WHITE);
        g.fillRect(0, 10, screenWidth - PLAYER_FRAME_WIDTH, 50);

        // Texte (centrage précis)
        g.setFont(HEADER_FONT);
        FontMetrics fm = g.getFontMetrics();
        int textX = ((screenWidth - PLAYER_FRAME_WIDTH) - fm.stringWidth(message)) / 2;
        int textY = (50 - fm.getHeight()) / 2 + fm.getAscent() + 10;
        g.setColor(Color.BLACK);
        g.drawString(message, textX, textY);
    }

    // Méthode appelée à chaque frame
    private void draw(Graphics2D g) {
        int width = context.getScreenInfo().width();

        try {
            drawBackground(g);
            drawHeader(g, "Au tour d'alice", width);
            // autres appels de rendering avec currentGame / currentPlayer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawTokenImageAmount(Graphics2D g, int x, int y, String imagePath, int count) {
        int circleRadius = 14;
        int PADDING = 4;
        int circleX = x - PADDING;
        int circleY = y + PADDING - 8;

        // Cercle blanc + contour
        g.setColor(Color.WHITE);
        g.fillOval(circleX, circleY, circleRadius * 2, circleRadius * 2);
        g.setColor(Color.BLACK);
        g.drawOval(circleX, circleY, circleRadius * 2, circleRadius * 2);

        // Texte centré
        g.setFont(BANK_TOKEN_COUNT_FONT);
        String text = String.valueOf(count);
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        int textX = circleX + (circleRadius * 2 - textWidth) / 2;
        int textY = circleY + (circleRadius * 2 + textHeight) / 2 - 2;

        g.setColor(Color.BLACK);
        g.drawString(text, textX, textY);
    }

    private void drawTokenImage(Graphics2D g, int x, int y, String imagePath, int count) {
        try {
            InputStream inputStream = Main.class.getResourceAsStream(imagePath);
            if (inputStream == null) {
                throw new IOException("Image not found in resources!");
            }
            BufferedImage tokenImage = ImageIO.read(inputStream);

            int size = 70; // taille du token
            g.drawImage(tokenImage, x, y, size, size, null);

            drawTokenImageAmount(g, x, y, imagePath, count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawFramedPanel(Graphics2D g, int x, int y, int width, int height) {
        g.setColor(PANEL_BACKGROUND);
        g.fillRoundRect(x, y, width, height, CORNER_RADIUS, CORNER_RADIUS);

        g.setColor(PANEL_BORDER);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, width, height, CORNER_RADIUS, CORNER_RADIUS);
    }

    private void drawBubble(Graphics2D g, String text, int x, int y, int width, int height) {
        g.setColor(BUBBLE_BACKGROUND);
        g.fillRoundRect(x, y, width, height, 15, 15);

        g.setColor(BUBBLE_BORDER);
        g.drawRoundRect(x, y, width, height, 15, 15);

        g.setColor(BUBBLE_TEXT_COLOR);
        g.setFont(BUBBLE_FONT);
        int textWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, x + (width - textWidth) / 2, y + (height + BUBBLE_FONT.getSize()) / 2 - 2);
    }

    private void drawGemStones(Graphics2D g, Player p, int x, int y){
        int tokenWidth = 100;
        int tokenHeight = 50;
        int tokenSpacing = 10;
        int startX = x + PADDING;
        int startY = y + PADDING + 10;

        GemToken[] tokens = GemToken.values();

        for (int i = 0; i < tokens.length; i++) {
            GemToken token = tokens[i];
            int tokenX = startX + i * (tokenWidth + tokenSpacing);

            Color tokenColor = TOKEN_COLORS.getOrDefault(token, Color.GRAY);
            g.setColor(tokenColor);
            g.fillRect(tokenX, startY, tokenWidth, tokenHeight);

            int bonusWidth = (int)(tokenWidth * 0.3);
            g.setColor(new Color(255, 255, 255, 100));
            g.fillRect(tokenX, startY, bonusWidth, tokenHeight);

            g.setColor(Color.WHITE);
            g.setFont(BUBBLE_FONT);  // police définie ailleurs
            String bonusText = "0";  // valeur statique, peut être dynamique si besoin
            int bonusTextWidth = g.getFontMetrics().stringWidth(bonusText);
            g.drawString(bonusText,
                    tokenX + (bonusWidth - bonusTextWidth) / 2,
                    startY + tokenHeight / 2 + 5);

            int tokensWidth = tokenWidth - bonusWidth;
            int count = p.getTokenCount(token);
            String countText = String.valueOf(count);
            g.setFont(PLAYER_TOKEN_COUNT_FONT);
            int countTextWidth = g.getFontMetrics().stringWidth(countText);
            g.drawString(countText,
                    tokenX + bonusWidth + (tokensWidth - countTextWidth) / 2,
                    startY + tokenHeight / 2 + 5);
        }
    }

    private void drawPlayerGemStones(Graphics2D g, Player p, int width) {
        int rectHeight = 90;
        int x = 500;
        int y = 720;
        int PADDING = 15;

        drawFramedPanel(g, x, y, width, rectHeight);

        int titleBubbleWidth = 180;
        int titleBubbleHeight = 30;
        int titleBubbleX = x + (width - titleBubbleWidth) / 2;
        int titleBubbleY = y - 15;
        drawBubble(g, "Pierres précieuses", titleBubbleX, titleBubbleY, titleBubbleWidth, titleBubbleHeight);

        int totalGems = p.getTotalAmount();
        String counterText = totalGems + "/10";
        int counterBubbleWidth = 50;
        int counterBubbleX = x + width - counterBubbleWidth - PADDING;
        int counterBubbleY = y - 15;
        drawBubble(g, counterText, counterBubbleX, counterBubbleY, counterBubbleWidth, titleBubbleHeight);

        drawGemStones(g, p, x, y);
    }


    private void drawCardStacks(Graphics2D g, int x, int yStart, List<Integer> stackSizes) {
        for (int level = 3; level >= 1; level--) {
            String imagePath = "../resources/images/development_cards/level" + level + "_cards.png";
            try (InputStream in = Main.class.getResourceAsStream(imagePath)) {
                if (in != null) {
                    BufferedImage backImage = ImageIO.read(in);
                    int y = yStart + (3 - level) * (CARD_HEIGHT + PADDING);
                    g.drawImage(backImage, x, y, CARD_WIDTH, CARD_HEIGHT, null);

                    int count = stackSizes.get(3 - level);
                    int circleSize = 32;
                    int circleX = x + 8;
                    int circleY = y + 8;

                    g.setColor(Color.WHITE);
                    g.fillOval(circleX, circleY, circleSize, circleSize);

                    g.setColor(Color.BLACK);
                    g.setFont(CARD_STACK_AMOUNT_FONT);
                    FontMetrics fm = g.getFontMetrics();
                    String text = String.valueOf(count);
                    int textWidth = fm.stringWidth(text);
                    int textHeight = fm.getAscent();
                    g.drawString(text, circleX + (circleSize - textWidth) / 2, circleY + (circleSize + textHeight) / 2 - 3);
                } else {
                    System.err.println("Image non trouvée : " + imagePath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void drawCards(Graphics2D g, List<DevelopmentCard> cards) {
        int cardsPerRow = 4;
        int startX = 500;

        int totalRows = (int) Math.ceil(cards.size() / (double) cardsPerRow);

        int startY = 100 + (totalRows - 1) * (CARD_HEIGHT + PADDING);

        for (int i = 0; i < cards.size(); i++) {
            DevelopmentCard card = cards.get(i);

            int row = i / cardsPerRow;
            int col = i % cardsPerRow;

            int x = startX + col * (CARD_WIDTH + PADDING);
            int y = startY - row * (CARD_HEIGHT + PADDING); // Inversé pour que ligne 0 soit en bas

            try {
                InputStream inputStream = Main.class.getResourceAsStream(card.imageUrl());
                if (inputStream != null) {
                    BufferedImage image = ImageIO.read(inputStream);
                    g.drawImage(image, x, y, CARD_WIDTH, CARD_HEIGHT, null);
                } else {
                    throw new IOException("Image not found: " + card.imageUrl());
                }
            } catch (IOException e) {
                g.setColor(Color.WHITE);
                g.fillRect(x, y, CARD_WIDTH, CARD_HEIGHT);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, CARD_WIDTH, CARD_HEIGHT);
            }

            drawCardInfos(g, x, y, card);
        }
    }

    private void drawNumber(Graphics2D g, Font font, int amount, int cx, int cy, int sizeX, int sizeY){
        g.setFont(font);
        String text = String.valueOf(amount);
        FontMetrics fm = g.getFontMetrics();
        int tx = cx + (sizeX - fm.stringWidth(text)) / 2;
        int ty = cy + ((sizeY - fm.getHeight()) / 2) + fm.getAscent();

        // Contour noir
        g.setColor(Color.BLACK);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    g.drawString(text, tx + dx, ty + dy);
                }
            }
        }

        // Texte blanc
        g.setColor(Color.WHITE);
        g.drawString(text, tx, ty);
    }

    private void drawCardToken(Graphics2D g, GemToken bonus, int amount, int cx, int cy, int size) {
        g.setColor(TOKEN_COLORS.get(bonus));
        g.fillOval(cx, cy, size, size);

        drawNumber(g, CARD_TOKEN_AMOUNT_FONT, amount, cx, cy, size, size);
    }

    private void drawNobleToken(Graphics2D g, GemToken bonus, int amount, int cx, int cy) {
        int width = CARD_WIDTH / 7;
        int height = CARD_HEIGHT / 6;

        g.setColor(TOKEN_COLORS.get(bonus));
        g.fillRect(cx, cy, width, height);
        drawNumber(g, NOBLE_TOKEN_AMOUNT_FONT, amount, cx, cy, width, height);
    }

    private void drawCardCostTokens(Graphics2D g, int x, int y, DevelopmentCard card) {
        int tokenSize = 36;
        int padding = 4;
        int margin = 8;

        List<Map.Entry<GemToken, Integer>> cost = card.price().entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 0)
                .toList();

        int n = cost.size();

        for (int i = 0; i < n; i++) {
            int cx, cy;

            switch (n) {
                case 1 -> {
                    cx = x + margin;
                    cy = y + CARD_HEIGHT - tokenSize - margin;
                }
                case 2 -> {
                    cx = x + margin;
                    cy = y + CARD_HEIGHT - (2 - i) * (tokenSize + padding) - margin;
                }
                case 3 -> {
                    if (i == 0) {
                        cx = x + margin;
                        cy = y + CARD_HEIGHT - 2 * tokenSize - padding - margin;
                    } else if (i == 1) {
                        cx = x + margin;
                        cy = y + CARD_HEIGHT - tokenSize - margin;
                    } else {
                        cx = x + tokenSize + padding + margin;
                        cy = y + CARD_HEIGHT - tokenSize - margin;
                    }
                }
                case 4 -> {
                    int row = i / 2;
                    int col = i % 2;
                    cx = x + col * (tokenSize + padding) + margin;
                    cy = y + CARD_HEIGHT - (2 - row) * (tokenSize + padding) + padding - margin;
                }
                default -> {
                    cx = x + margin + i * (tokenSize + padding);
                    cy = y + CARD_HEIGHT - tokenSize - margin;
                }
            }

            drawCardToken(g, cost.get(i).getKey(), cost.get(i).getValue(), cx, cy, tokenSize);
        }
    }

    private void drawCardHeader(Graphics2D g, int x, int y, DevelopmentCard card) {
        g.setColor(new Color(240, 240, 240, 140));
        g.fillRect(x, y, CARD_WIDTH, CARD_HEIGHT / 4);

        String prestigeText = String.valueOf(card.prestigeScore());
        int prestigeX = x + 15;
        int prestigeY = y + 30;

        g.setFont(CARD_PRESTIGE_FONT);
        g.setColor(Color.BLACK);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    g.drawString(prestigeText, prestigeX + dx, prestigeY + dy);
                }
            }
        }

        g.setColor(Color.WHITE);
        g.drawString(prestigeText, prestigeX, prestigeY);
    }

    private void drawCardBonusIcon(Graphics2D g, int x, int y, DevelopmentCard card) {
        try {
            String bonusName = card.bonus().toString().toLowerCase();
            InputStream in = Main.class.getResourceAsStream("../resources/images/gems/" + bonusName + ".png");
            if (in != null) {
                BufferedImage bonusImage = ImageIO.read(in);
                int bonusSize = 30;
                int bonusX = x + CARD_WIDTH - bonusSize - 10;
                int bonusY = y + 4;
                g.drawImage(bonusImage, bonusX, bonusY, bonusSize, bonusSize, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawCardInfos(Graphics2D g, int x, int y, DevelopmentCard card) {
        drawCardHeader(g, x, y, card);
        drawCardBonusIcon(g, x, y, card);
        drawCardCostTokens(g, x, y, card);
    }

    private void drawGameNobles(Graphics2D g, List<Noble> nobles, int width) {
        int startX = width - PLAYER_FRAME_WIDTH - 200;
        int startY = 100;

        for (int i = 0; i < nobles.size(); i++) {
            Noble noble = nobles.get(i);

            int x = startX;
            int y = startY + i * (NOBLE_HEIGHT + PADDING);

            try {
                InputStream inputStream = Main.class.getResourceAsStream(noble.imageUrl());
                if (inputStream != null) {
                    BufferedImage image = ImageIO.read(inputStream);
                    g.drawImage(image, x, y, NOBLE_WIDTH, NOBLE_HEIGHT, null);
                } else {
                    throw new IOException("Image not found: " + noble.imageUrl());
                }
            } catch (IOException e) {
                g.setColor(Color.WHITE);
                g.fillRect(x, y, NOBLE_WIDTH, NOBLE_HEIGHT);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, NOBLE_WIDTH, NOBLE_HEIGHT);
            }

            drawNobleInfos(g, x, y, noble);
        }
    }

    private void drawNobleCostTokens(Graphics2D g, int x, int y, Noble noble) {
        int tokenSize = NOBLE_HEIGHT / 5;
        int padding = 6;

        List<Map.Entry<GemToken, Integer>> cost = noble.price().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .toList();

        int baseX = x + padding;
        int baseY = y + NOBLE_HEIGHT - tokenSize - padding;

        for (int i = 0; i < cost.size(); i++) {
            int cx = baseX;
            int cy = baseY - i * (tokenSize + padding);
            drawNobleToken(g, cost.get(i).getKey(), cost.get(i).getValue(), cx, cy);
        }
    }

    private void drawNoblePrestige(Graphics2D g, int x, int y, Noble noble) {
        g.setColor(new Color(240, 240, 240, 140));
        g.fillRect(x, y, NOBLE_WIDTH / 4, NOBLE_HEIGHT);

        String prestigeText = String.valueOf(noble.prestigeScore());
        int prestigeX = x + 8;
        int prestigeY = y + 28;

        g.setFont(NOBLE_PRESTIGE_FONT);

        g.setColor(Color.BLACK);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    g.drawString(prestigeText, prestigeX + dx, prestigeY + dy);
                }
            }
        }

        g.setColor(Color.WHITE);
        g.drawString(prestigeText, prestigeX, prestigeY);
    }

    private void drawNobleInfos(Graphics2D g, int x, int y, Noble noble) {
        drawNoblePrestige(g, x, y, noble);
        drawNobleCostTokens(g, x, y, noble);
    }

    private static void drawPlayerBox(Graphics2D g, int x, int yStart) {
        g.setColor(PANEL_BACKGROUND);
        g.fillRoundRect(x + 5, yStart + 10, PLAYER_FRAME_WIDTH - 10, PLAYER_FRAME_HEIGHT, 50, 50);
    }

    private static void drawPlayerHeader(Graphics2D g, Player player, int x, int yStart) {
        // Nom
        g.setColor(Color.WHITE);
        g.setFont(PLAYER_NAME_FONT);
        g.drawString(player.getName(), x + 20, yStart + 40);

        // Score avec étoile
        g.setColor(Color.YELLOW);
        g.drawString("★ " + player.getPrestigeScore(), x + 240, yStart + 40);
    }

    private static void drawPlayerTokens(Graphics2D g, Player player, int x, int y) {
        g.setColor(Color.WHITE);
        g.setFont(PLAYER_INFOS_TITLE_FONT);
        g.drawString("Jetons", x, y);

        int tokenX = x;
        int tokenY = y + 20;

        for (Map.Entry<GemToken, Integer> entry : player.getWallet().entries()) {
            g.setColor(TOKEN_COLORS.get(entry.getKey()));
            g.fillRoundRect(tokenX, tokenY, 30, 40, 10, 10);

            g.setColor(Color.WHITE);
            g.drawString(String.valueOf(entry.getValue()), tokenX + 10, tokenY + 28);

            tokenX += 40;
        }
    }

    private static void drawPlayerBonuses(Graphics2D g, Player player, int x, int y) {
        g.setColor(Color.WHITE);
        g.setFont(PLAYER_INFOS_TITLE_FONT);
        g.drawString("Bonus", x, y);

        EnumMap<GemToken, Integer> bonusCounts = new EnumMap<>(GemToken.class);
        for (DevelopmentCard card : player.getPurchasedCards()) {
            GemToken bonus = card.bonus();
            bonusCounts.put(bonus, bonusCounts.getOrDefault(bonus, 0) + 1);
        }

        int bonusX = x;
        int bonusY = y + 10;

        for (Map.Entry<GemToken, Integer> entry : bonusCounts.entrySet()) {
            if (entry.getValue() > 0) {
                g.setColor(TOKEN_COLORS.get(entry.getKey()));
                g.fillRoundRect(bonusX, bonusY + 10, 30, 40, 10, 10);

                g.setColor(Color.WHITE);
                g.setFont(PLAYER_INFOS_BONUS_FONT);
                g.drawString(String.valueOf(entry.getValue()), bonusX + 10, bonusY + 38);

                bonusX += 40;
            }
        }
    }

    private static void drawPlayerInfo(Graphics2D g, Player player, int x, int yIndex) {
        int yStart = yIndex * PLAYER_FRAME_HEIGHT + 20 * yIndex;

        drawPlayerBox(g, x, yStart);
        drawPlayerHeader(g, player, x, yStart);
        drawPlayerTokens(g, player, x + 20, yStart + 80);
        drawPlayerBonuses(g, player, x + 20, yStart + 180);
    }

    private void drawReservedCards(Graphics2D g, List<DevelopmentCard> cards, int width) {
        int x = 370;
        int y = 850;
        int rectWidth = width / 2 + 100;
        int rectHeight = 200;

        drawFramedPanel(g, x, y, rectWidth, rectHeight);

        drawBubble(g, "Cartes réservées", x + 10, y - 15, 150, 30);

        String counterText = cards.size() + "/3";
        drawBubble(g, counterText, x + rectWidth - 60, y - 15, 50, 30);

        int startX = x + 20;
        int startY = y + 20;
        int spacing = 20;

        for (int i = 0; i < cards.size(); i++) {
            DevelopmentCard card = cards.get(i);
            int cardX = startX + i * (CARD_WIDTH + spacing);
            int cardY = startY;

            try {
                InputStream inputStream = Main.class.getResourceAsStream(card.imageUrl());
                if (inputStream != null) {
                    BufferedImage image = ImageIO.read(inputStream);
                    g.drawImage(image, cardX, cardY, CARD_WIDTH, CARD_HEIGHT, null);
                } else {
                    throw new IOException("Image not found: " + card.imageUrl());
                }
            } catch (IOException e) {
                g.setColor(Color.WHITE);
                g.fillRect(cardX, cardY, CARD_WIDTH, CARD_HEIGHT);
                g.setColor(Color.BLACK);
                g.drawRect(cardX, cardY, CARD_WIDTH, CARD_HEIGHT);
            }

            drawCardInfos(g, cardX, cardY, card);
        }
    }

    private void drawPlayerNobles(Graphics2D g, List<Noble> nobles, int width) {
        int x = 370 + width / 2 + 120;
        int y = 850;
        int rectWidth = width / 2 + 100;
        int rectHeight = 200;

        drawFramedPanel(g, x, y, rectWidth, rectHeight);

        drawBubble(g, "Nobles acquis", x + 10, y - 15, 150, 30);

        int nobleX = x + 20;
        int nobleY = y + 40;

        for (int i = 0; i < nobles.size(); i++) {
            Noble noble = nobles.get(i);
            int currentX = nobleX + i * (NOBLE_WIDTH + PADDING);

            try {
                InputStream inputStream = Main.class.getResourceAsStream(noble.imageUrl());
                if (inputStream != null) {
                    BufferedImage image = ImageIO.read(inputStream);
                    g.drawImage(image, currentX, nobleY, NOBLE_WIDTH, NOBLE_HEIGHT, null);
                } else {
                    throw new IOException("Image not found: " + noble.imageUrl());
                }
            } catch (IOException e) {
                g.setColor(Color.WHITE);
                g.fillRect(currentX, nobleY, NOBLE_WIDTH, NOBLE_HEIGHT);
                g.setColor(Color.BLACK);
                g.drawRect(currentX, nobleY, NOBLE_WIDTH, NOBLE_HEIGHT);
            }

            drawNobleInfos(g, currentX, nobleY, noble);
        }
    }

    @Override
    public void displayMessage(String message) {
        context.renderFrame(g -> drawHeader(g, message, context.getScreenInfo().width()));
    }

    @Override
    public void showBank(GemStock bank) {
    }

    @Override
    public void showCards(Game game) {
        context.renderFrame(g ->{
            drawCardStacks(g, 350, 100, game.getAmountsOfCardByLevel());
            drawCards(g, game.getDisplayedCards());
        });
    }

    @Override
    public void showNobles(List<Noble> nobles) {

    }

    @Override
    public void showPlayerTurn(Player player) {
        int width = context.getScreenInfo().width();
        context.renderFrame(g -> {
           drawPlayerGemStones(g, player, (width - PLAYER_FRAME_WIDTH) / 2 - 100);
            drawReservedCards(g, player.getReservedCards(), (width - PLAYER_FRAME_WIDTH )/ 2);
            drawPlayerNobles(g, player.getAcquiredNobles(), (width - PLAYER_FRAME_WIDTH )/ 2);
        });
    }

    @Override
    public void showMenu(Game game) {
        context.renderFrame(g -> {
            menuButtons.clear();

            int startX = 20;
            int startY = 100;
            String[] options = {
                    "Acheter une carte",
                    "Récupérer 3 différentes",
                    "Récupérer 2 identiques",
                    "Réserver une carte",
                    "Acheter une carte réservée"
            };

            g.setFont(MENU_BUTTON_FONT);

            for (int i = 0; i < options.length; i++) {
                int x = startX;
                int y = startY + i * (BUTTON_HEIGHT + BUTTON_SPACING);

                Rectangle rect = new Rectangle(x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
                menuButtons.put(rect, i + 1);

                // Fond
                g.setColor(new Color(80, 80, 80));
                g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_RADIUS, BUTTON_RADIUS);

                // Bordure
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(2));
                g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_RADIUS, BUTTON_RADIUS);

                // Texte centré
                String text = options[i];
                FontMetrics fm = g.getFontMetrics();
                int textX = x + (BUTTON_WIDTH - fm.stringWidth(text)) / 2;
                int textY = y + (BUTTON_HEIGHT - fm.getHeight()) / 2 + fm.getAscent();

                g.setColor(Color.WHITE);
                g.drawString(text, textX, textY);
            }
        });
    }

    @Override
    public void showBoard(Game game) {
        int width = context.getScreenInfo().width();
        context.renderFrame(g -> {
            drawTokenImage(g, width - PLAYER_FRAME_WIDTH - 400, 100, "../resources/images/tokens/diamond_token.png", game.getBank().getAmount(GemToken.DIAMOND));
            drawTokenImage(g, width - PLAYER_FRAME_WIDTH - 400, 200, "../resources/images/tokens/sapphire_token.png", game.getBank().getAmount(GemToken.SAPPHIRE));
            drawTokenImage(g, width - PLAYER_FRAME_WIDTH - 400, 300, "../resources/images/tokens/emerald_token.png", game.getBank().getAmount(GemToken.EMERALD));
            drawTokenImage(g, width - PLAYER_FRAME_WIDTH - 400, 400, "../resources/images/tokens/ruby_token.png", game.getBank().getAmount(GemToken.RUBY));
            drawTokenImage(g, width - PLAYER_FRAME_WIDTH - 400, 500, "../resources/images/tokens/onyx_token.png", game.getBank().getAmount(GemToken.ONYX));
            drawTokenImage(g, width - PLAYER_FRAME_WIDTH - 400, 600, "../resources/images/tokens/gold_token.png", game.getBank().getAmount(GemToken.GOLD));
            showCards(game);
            int[] index = {0};
            game.getPlayers().forEach(player -> {
                drawPlayerInfo(g, player, width - PLAYER_FRAME_WIDTH, index[0]);
                index[0]++;
            });
            drawGameNobles(g,game.getNobles(), width);
        });
    }

    @Override
    public int getMenuChoice(Game game) {
        while (true) {
            Event event = context.pollOrWaitEvent(0); // Attend un événement

            if (event != null) {
                switch (event) {
                    case PointerEvent p when p.action() == PointerEvent.Action.POINTER_DOWN -> {
                        Point clickPoint = new Point(p.location().x(), p.location().y());

                        for (Map.Entry<Rectangle, Integer> entry : menuButtons.entrySet()) {
                            if (entry.getKey().contains(clickPoint)) {
                                return entry.getValue(); // Renvoie l'action liée au bouton cliqué
                            }
                        }
                    }
                    default -> {
                    }
                }
            }
        }
    }

    @Override
    public int selectCard(int maxIndex) {
        while (true) {
            Event event = context.pollOrWaitEvent(0); // Attend un événement
            if (event != null) {
                switch (event) {
                    case PointerEvent p when p.action() == PointerEvent.Action.POINTER_DOWN -> {
                        Point clickPoint = new Point(p.location().x(), p.location().y());

                        for (Map.Entry<Rectangle, Integer> entry : menuButtons.entrySet()) {
                            if (entry.getKey().contains(clickPoint)) {
                                return entry.getValue(); // Renvoie l'action liée au bouton cliqué
                            }
                        }
                    }
                    default -> {
                    }
                }
            }
        }
    }

    @Override
    public GemToken askGemToDiscard(Player player) {
        return null;
    }

    @Override
    public void showNotEnoughTokens() {

    }

    @Override
    public void showTokensTaken(GemToken token, int amount) {

    }

    @Override
    public void showFinalRanking(List<Player> players) {

    }

    @Override
    public void showTokenAlreadyTaken() {

    }

    @Override
    public void showNoMoreTokens(GemToken token) {

    }

    @Override
    public void showRemainingChoices(int remaining) {
    }
}
