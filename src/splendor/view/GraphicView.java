package splendor.view;

import com.github.forax.zen.Application;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.Event;
import com.github.forax.zen.PointerEvent;
import splendor.app.Demo;
import splendor.app.Main;
import splendor.model.*;
import splendor.util.ResolutionManager;

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
    private ResolutionManager resolutionManager;

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
    private static final Color CARD_HEADER_COLOR = new Color(240, 240, 240, 140);

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
            setResolutionManager(context.getScreenInfo().width(), context.getScreenInfo().height());
            context.renderFrame(this::draw);
        });
    }

    public void setContext(ApplicationContext context) {
        this.context = context;
    }

    public void setResolutionManager(int width, int height) {
        this.resolutionManager = new ResolutionManager(width, height);
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

    private void drawHeader(Graphics2D g, String message) {
        // Utilisation du ResolutionManager pour toutes les dimensions
        g.setColor(Color.WHITE);
        int headerWidth = resolutionManager.scaleX(1920 - PLAYER_FRAME_WIDTH);
        int headerHeight = resolutionManager.scaleY(50);
        int topPadding = resolutionManager.scaleY(10);

        g.fillRect(0, topPadding, headerWidth, headerHeight);

        // Texte
        Font adjustedFont = resolutionManager.scaleFont(HEADER_FONT);
        g.setFont(adjustedFont);

        FontMetrics fm = g.getFontMetrics();
        int textX = (headerWidth - fm.stringWidth(message)) / 2;
        int textY = (headerHeight - fm.getHeight()) / 2 + fm.getAscent() + topPadding;

        g.setColor(Color.BLACK);
        g.drawString(message, textX, textY);
    }

    // Méthode appelée à chaque frame
    private void draw(Graphics2D g) {
        int width = context.getScreenInfo().width();

        try {
            drawBackground(g);
            drawHeader(g, "Au tour d'alice");
            // autres appels de rendering avec currentGame / currentPlayer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawTokenImageAmount(Graphics2D g, int x, int y, int count) {
        int circleRadius = resolutionManager.scaleSize(14);
        int PADDING = resolutionManager.scaleSize(4);
        int verticalOffset = resolutionManager.scaleSize(8);

        int circleX = x - PADDING;
        int circleY = y + PADDING - verticalOffset;

        g.setColor(Color.WHITE);
        g.fillOval(circleX, circleY, circleRadius * 2, circleRadius * 2);
        g.setColor(Color.BLACK);
        g.drawOval(circleX, circleY, circleRadius * 2, circleRadius * 2);

        Font adjustedFont = resolutionManager.scaleFont(BANK_TOKEN_COUNT_FONT);
        g.setFont(adjustedFont);

        String text = String.valueOf(count);
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        int textVerticalAdjustment = resolutionManager.scaleSize(2);

        int textX = circleX + (circleRadius * 2 - textWidth) / 2;
        int textY = circleY + (circleRadius * 2 + textHeight) / 2 - textVerticalAdjustment;

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
            int scaledSize = resolutionManager.scaleSize(70);

            int scaledX = resolutionManager.scaleX(x);
            int scaledY = resolutionManager.scaleY(y);

            g.drawImage(tokenImage, scaledX, scaledY, scaledSize, scaledSize, null);

            drawTokenImageAmount(g, scaledX, scaledY, count);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawFramedPanel(Graphics2D g, int x, int y, int width, int height) {
        int scaledX = resolutionManager.scaleX(x);
        int scaledY = resolutionManager.scaleY(y);
        int scaledWidth = resolutionManager.scaleX(width);
        int scaledHeight = resolutionManager.scaleY(height);
        int scaledCornerRadius = resolutionManager.scaleSize(CORNER_RADIUS);
        int scaledBorder = resolutionManager.scaleSize(2);

        g.setColor(PANEL_BACKGROUND);
        g.fillRoundRect(scaledX, scaledY, scaledWidth, scaledHeight, scaledCornerRadius, scaledCornerRadius);

        g.setColor(PANEL_BORDER);
        g.setStroke(new BasicStroke(scaledBorder));
        g.drawRoundRect(scaledX, scaledY, scaledWidth, scaledHeight, scaledCornerRadius, scaledCornerRadius);
    }

    private void drawBubble(Graphics2D g, String text, int x, int y, int width, int height) {
        int scaledX = resolutionManager.scaleX(x);
        int scaledY = resolutionManager.scaleY(y);
        int scaledWidth = resolutionManager.scaleX(width);
        int scaledHeight = resolutionManager.scaleY(height);
        int scaledBubbleRadius = resolutionManager.scaleSize(15);

        g.setColor(BUBBLE_BACKGROUND);
        g.fillRoundRect(scaledX, scaledY, scaledWidth, scaledHeight, scaledBubbleRadius, scaledBubbleRadius);

        g.setColor(BUBBLE_BORDER);
        g.drawRoundRect(scaledX, scaledY, scaledWidth, scaledHeight, scaledBubbleRadius, scaledBubbleRadius);

        Font scaledFont = resolutionManager.scaleFont(BUBBLE_FONT);
        g.setFont(scaledFont);

        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textVerticalAdjustment = resolutionManager.scaleSize(2);

        int textX = scaledX + (scaledWidth - textWidth) / 2;
        int textY = scaledY + (scaledHeight + fm.getAscent()) / 2 - textVerticalAdjustment;

        g.setColor(BUBBLE_TEXT_COLOR);
        g.drawString(text, textX, textY);
    }

    private void drawGemStones(Graphics2D g, Player p, int x, int y) {
        int tokenWidth = resolutionManager.scaleSize(100);
        int tokenHeight = resolutionManager.scaleSize(50);
        int tokenSpacing = resolutionManager.scaleSize(10);
        int startX = resolutionManager.scaleX(x + PADDING);
        int startY = resolutionManager.scaleY(y + PADDING + 10);

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
            g.setFont(resolutionManager.scaleFont(BUBBLE_FONT));
            String bonusText = "0";  // Peut être remplacé par une valeur dynamique
            int bonusTextWidth = g.getFontMetrics().stringWidth(bonusText);
            g.drawString(
                    bonusText,
                    tokenX + (bonusWidth - bonusTextWidth) / 2,
                    startY + tokenHeight / 2 + resolutionManager.scaleSize(5)
            );

            int tokensWidth = tokenWidth - bonusWidth;
            int count = p.getTokenCount(token);
            String countText = String.valueOf(count);
            g.setFont(resolutionManager.scaleFont(PLAYER_TOKEN_COUNT_FONT));
            int countTextWidth = g.getFontMetrics().stringWidth(countText);
            g.drawString(
                    countText,
                    tokenX + bonusWidth + (tokensWidth - countTextWidth) / 2,
                    startY + tokenHeight / 2 + resolutionManager.scaleSize(5)
            );
        }
    }

    private void drawPlayerGemStones(Graphics2D g, Player p, int width) {
        int rectHeight = resolutionManager.scaleSize(90);
        int x = resolutionManager.scaleX(500);
        int y = resolutionManager.scaleY(720);
        int scaledWidth = resolutionManager.scaleX(width);
        int PADDING = resolutionManager.scaleSize(15);

        drawFramedPanel(g, x, y, scaledWidth, rectHeight);

        int titleBubbleWidth = resolutionManager.scaleSize(180);
        int titleBubbleHeight = resolutionManager.scaleSize(30);
        int titleBubbleX = x + (scaledWidth - titleBubbleWidth) / 2;
        int titleBubbleY = y - resolutionManager.scaleSize(15);
        drawBubble(g, "Pierres précieuses", titleBubbleX, titleBubbleY, titleBubbleWidth, titleBubbleHeight);

        int totalGems = p.getTotalAmount();
        String counterText = totalGems + "/10";
        int counterBubbleWidth = resolutionManager.scaleSize(50);
        int counterBubbleX = x + scaledWidth - counterBubbleWidth - PADDING;
        int counterBubbleY = y - resolutionManager.scaleSize(15);
        drawBubble(g, counterText, counterBubbleX, counterBubbleY, counterBubbleWidth, titleBubbleHeight);

        drawGemStones(g, p, resolutionManager.scaleX(x), resolutionManager.scaleY(y));
    }



    private void drawCardStacks(Graphics2D g, int x, int yStart, List<Integer> stackSizes) {
        int scaledX = resolutionManager.scaleX(x);
        int scaledYStart = resolutionManager.scaleY(yStart);
        int scaledCardWidth = resolutionManager.scaleSize(CARD_WIDTH);
        int scaledCardHeight = resolutionManager.scaleSize(CARD_HEIGHT);
        int scaledPadding = resolutionManager.scaleSize(PADDING);
        int scaledCircleSize = resolutionManager.scaleSize(32);
        int circlePadding = resolutionManager.scaleSize(8);

        for (int level = 3; level >= 1; level--) {
            String imagePath = "../resources/images/development_cards/level" + level + "_cards.png";
            try (InputStream in = Main.class.getResourceAsStream(imagePath)) {
                if (in != null) {
                    BufferedImage backImage = ImageIO.read(in);
                    int y = scaledYStart + (3 - level) * (scaledCardHeight + scaledPadding);
                    g.drawImage(backImage, scaledX, y, scaledCardWidth, scaledCardHeight, null);

                    int count = stackSizes.get(3 - level);
                    int circleX = scaledX + circlePadding;
                    int circleY = y + circlePadding;

                    g.setColor(Color.WHITE);
                    g.fillOval(circleX, circleY, scaledCircleSize, scaledCircleSize);

                    g.setColor(Color.BLACK);
                    g.setFont(resolutionManager.scaleFont(CARD_STACK_AMOUNT_FONT));
                    FontMetrics fm = g.getFontMetrics();
                    String text = String.valueOf(count);
                    int textWidth = fm.stringWidth(text);
                    int textHeight = fm.getAscent();
                    g.drawString(
                            text,
                            circleX + (scaledCircleSize - textWidth) / 2,
                            circleY + (scaledCircleSize + textHeight) / 2 - resolutionManager.scaleSize(3)
                    );
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
        int startX = resolutionManager.scaleX(500);

        int scaledCardWidth = resolutionManager.scaleSize(CARD_WIDTH);
        int scaledCardHeight = resolutionManager.scaleSize(CARD_HEIGHT);
        int scaledPadding = resolutionManager.scaleSize(PADDING);

        int totalRows = (int) Math.ceil(cards.size() / (double) cardsPerRow);
        int startY = resolutionManager.scaleY(100) + (totalRows - 1) * (scaledCardHeight + scaledPadding);

        for (int i = 0; i < cards.size(); i++) {
            DevelopmentCard card = cards.get(i);

            int row = i / cardsPerRow;
            int col = i % cardsPerRow;

            int x = startX + col * (scaledCardWidth + scaledPadding);
            int y = startY - row * (scaledCardHeight + scaledPadding); // ligne 0 en bas

            try (InputStream inputStream = Main.class.getResourceAsStream(card.imageUrl())) {
                if (inputStream != null) {
                    BufferedImage image = ImageIO.read(inputStream);
                    g.drawImage(image, x, y, scaledCardWidth, scaledCardHeight, null);
                } else {
                    throw new IOException("Image not found: " + card.imageUrl());
                }
            } catch (IOException e) {
                g.setColor(Color.WHITE);
                g.fillRect(x, y, scaledCardWidth, scaledCardHeight);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, scaledCardWidth, scaledCardHeight);
            }

            drawCardInfos(g, x, y, card);
        }
    }


    private void drawNumber(Graphics2D g, Font font, int amount, int cx, int cy, int sizeX, int sizeY) {
        Font scaledFont = resolutionManager.scaleFont(font);
        g.setFont(scaledFont);
        String text = String.valueOf(amount);
        FontMetrics fm = g.getFontMetrics();
        int tx = cx + (sizeX - fm.stringWidth(text)) / 2;
        int ty = cy + ((sizeY - fm.getHeight()) / 2) + fm.getAscent();

        g.setColor(Color.BLACK);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    g.drawString(text, tx + dx, ty + dy);
                }
            }
        }

        g.setColor(Color.WHITE);
        g.drawString(text, tx, ty);
    }


    private void drawCardToken(Graphics2D g, GemToken bonus, int amount, int cx, int cy, int size) {
        int scaledX = resolutionManager.scaleX(cx);
        int scaledY = resolutionManager.scaleY(cy);
        int scaledSize = resolutionManager.scaleSize(size);

        g.setColor(TOKEN_COLORS.get(bonus));
        g.fillOval(scaledX, scaledY, scaledSize, scaledSize);

        drawNumber(g, CARD_TOKEN_AMOUNT_FONT, amount, scaledX, scaledY, scaledSize, scaledSize);
    }


    private void drawNobleToken(Graphics2D g, GemToken bonus, int amount, int cx, int cy) {
        int scaledX = resolutionManager.scaleX(cx);
        int scaledY = resolutionManager.scaleY(cy);
        int scaledWidth = resolutionManager.scaleSize(CARD_WIDTH / 7);
        int scaledHeight = resolutionManager.scaleSize(CARD_HEIGHT / 6);

        g.setColor(TOKEN_COLORS.get(bonus));
        g.fillRect(scaledX, scaledY, scaledWidth, scaledHeight);

        drawNumber(g, NOBLE_TOKEN_AMOUNT_FONT, amount, scaledX, scaledY, scaledWidth, scaledHeight);
    }


    private void drawCardCostTokens(Graphics2D g, int x, int y, DevelopmentCard card) {
        int tokenSize = resolutionManager.scaleSize(36);
        int padding = resolutionManager.scaleSize(4);
        int margin = resolutionManager.scaleSize(8);
        int cardHeight = resolutionManager.scaleSize(CARD_HEIGHT); // Assure toi que CARD_HEIGHT est adaptable

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
                    cy = y + cardHeight - tokenSize - margin;
                }
                case 2 -> {
                    cx = x + margin;
                    cy = y + cardHeight - (2 - i) * (tokenSize + padding) - margin;
                }
                case 3 -> {
                    if (i == 0) {
                        cx = x + margin;
                        cy = y + cardHeight - 2 * tokenSize - padding - margin;
                    } else if (i == 1) {
                        cx = x + margin;
                        cy = y + cardHeight - tokenSize - margin;
                    } else {
                        cx = x + tokenSize + padding + margin;
                        cy = y + cardHeight - tokenSize - margin;
                    }
                }
                case 4 -> {
                    int row = i / 2;
                    int col = i % 2;
                    cx = x + col * (tokenSize + padding) + margin;
                    cy = y + cardHeight - (2 - row) * (tokenSize + padding) + padding - margin;
                }
                default -> {
                    cx = x + margin + i * (tokenSize + padding);
                    cy = y + cardHeight - tokenSize - margin;
                }
            }

            drawCardToken(g, cost.get(i).getKey(), cost.get(i).getValue(), cx, cy, tokenSize);
        }
    }


    private void drawCardHeader(Graphics2D g, int x, int y, DevelopmentCard card) {
        int cardWidth = resolutionManager.scaleX(CARD_WIDTH);
        int cardHeight = resolutionManager.scaleY(CARD_HEIGHT);
        int headerHeight = cardHeight / 4;
        int prestigeX = x + resolutionManager.scaleX(15);
        int prestigeY = y + resolutionManager.scaleY(30);

        g.setColor(CARD_HEADER_COLOR);
        g.fillRect(x, y, cardWidth, headerHeight);

        String prestigeText = String.valueOf(card.prestigeScore());

        g.setFont(resolutionManager.scaleFont(CARD_PRESTIGE_FONT)); // Assure-toi que scaleFont existe
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
                int bonusSize = resolutionManager.scaleSize(30);
                int cardWidth = resolutionManager.scaleX(CARD_WIDTH);
                int bonusX = x + cardWidth - bonusSize - resolutionManager.scaleSize(10);
                int bonusY = y + resolutionManager.scaleY(4);
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
        int playerFrameWidth = resolutionManager.scaleX(PLAYER_FRAME_WIDTH);
        int nobleWidth = resolutionManager.scaleX(NOBLE_WIDTH);
        int nobleHeight = resolutionManager.scaleY(NOBLE_HEIGHT);
        int padding = resolutionManager.scaleSize(PADDING);

        int startX = width - playerFrameWidth - resolutionManager.scaleX(200);
        int startY = resolutionManager.scaleY(100);

        for (int i = 0; i < nobles.size(); i++) {
            Noble noble = nobles.get(i);

            int x = startX;
            int y = startY + i * (nobleHeight + padding);

            try {
                InputStream inputStream = Main.class.getResourceAsStream(noble.imageUrl());
                if (inputStream != null) {
                    BufferedImage image = ImageIO.read(inputStream);
                    g.drawImage(image, x, y, nobleWidth, nobleHeight, null);
                } else {
                    throw new IOException("Image not found: " + noble.imageUrl());
                }
            } catch (IOException e) {
                g.setColor(Color.WHITE);
                g.fillRect(x, y, nobleWidth, nobleHeight);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, nobleWidth, nobleHeight);
            }

            drawNobleInfos(g, x, y, noble); // À adapter aussi si nécessaire
        }
    }

    private void drawNobleCostTokens(Graphics2D g, int x, int y, Noble noble) {
        int nobleHeight = resolutionManager.scaleY(NOBLE_HEIGHT);
        int tokenSize = nobleHeight / 5;
        int padding = resolutionManager.scaleSize(6);

        List<Map.Entry<GemToken, Integer>> cost = noble.price().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .toList();

        int baseX = x + padding;
        int baseY = y + nobleHeight - tokenSize - padding;

        for (int i = 0; i < cost.size(); i++) {
            int cx = baseX;
            int cy = baseY - i * (tokenSize + padding);
            drawNobleToken(g, cost.get(i).getKey(), cost.get(i).getValue(), cx, cy);
        }
    }

    private void drawNoblePrestige(Graphics2D g, int x, int y, Noble noble) {
        int nobleWidth = resolutionManager.scaleX(NOBLE_WIDTH);
        int nobleHeight = resolutionManager.scaleY(NOBLE_HEIGHT);
        int panelWidth = nobleWidth / 4;

        g.setColor(CARD_HEADER_COLOR);
        g.fillRect(x, y, panelWidth, nobleHeight);

        String prestigeText = String.valueOf(noble.prestigeScore());
        int prestigeX = x + resolutionManager.scaleSize(8);
        int prestigeY = y + resolutionManager.scaleSize(28);

        g.setFont(resolutionManager.scaleFont(NOBLE_PRESTIGE_FONT));

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

    private void drawPlayerBox(Graphics2D g, int x, int yStart) {
        g.setColor(PANEL_BACKGROUND);
        g.fillRoundRect(
                resolutionManager.scaleX(x + 5),
                resolutionManager.scaleY(yStart + 10),
                resolutionManager.scaleX(PLAYER_FRAME_WIDTH - 10),
                resolutionManager.scaleY(PLAYER_FRAME_HEIGHT),
                resolutionManager.scaleSize(50),
                resolutionManager.scaleSize(50)
        );
    }

    private void drawPlayerHeader(Graphics2D g, Player player, int x, int yStart) {
        g.setColor(Color.WHITE);
        g.setFont(resolutionManager.scaleFont(PLAYER_NAME_FONT));
        g.drawString(player.getName(), resolutionManager.scaleX(x + 20), resolutionManager.scaleY(yStart + 40));

        g.setColor(Color.YELLOW);
        g.drawString("★ " + player.getPrestigeScore(), resolutionManager.scaleX(x + 240), resolutionManager.scaleY(yStart + 40));
    }

    private void drawPlayerTokens(Graphics2D g, Player player, int x, int y) {
        g.setColor(Color.WHITE);
        Font scaledTitleFont = resolutionManager.scaleFont(PLAYER_INFOS_TITLE_FONT);
        g.setFont(scaledTitleFont);
        g.drawString("Jetons", resolutionManager.scaleX(x), resolutionManager.scaleY(y));

        int tokenX = resolutionManager.scaleX(x);
        int tokenY = resolutionManager.scaleY(y + 20);
        int tokenWidth = resolutionManager.scaleX(30);
        int tokenHeight = resolutionManager.scaleY(40);
        int tokenSpacing = resolutionManager.scaleX(40);
        int cornerRadius = resolutionManager.scaleSize(10);

        for (Map.Entry<GemToken, Integer> entry : player.getWallet().entries()) {
            g.setColor(TOKEN_COLORS.get(entry.getKey()));
            g.fillRoundRect(tokenX, tokenY, tokenWidth, tokenHeight, cornerRadius, cornerRadius);

            g.setColor(Color.WHITE);
            Font scaledValueFont = resolutionManager.scaleFont(PLAYER_INFOS_TITLE_FONT);
            g.setFont(scaledValueFont);
            int textOffsetX = resolutionManager.scaleX(10);
            int textOffsetY = resolutionManager.scaleY(28);
            g.drawString(String.valueOf(entry.getValue()), tokenX + textOffsetX, tokenY + textOffsetY);

            tokenX += tokenSpacing;
        }
    }

    private void drawPlayerBonuses(Graphics2D g, Player player, int x, int y) {
        g.setColor(Color.WHITE);
        Font scaledTitleFont = resolutionManager.scaleFont(PLAYER_INFOS_TITLE_FONT);
        g.setFont(scaledTitleFont);
        g.drawString("Bonus", resolutionManager.scaleX(x), resolutionManager.scaleY(y));

        EnumMap<GemToken, Integer> bonusCounts = new EnumMap<>(GemToken.class);
        for (DevelopmentCard card : player.getPurchasedCards()) {
            GemToken bonus = card.bonus();
            bonusCounts.put(bonus, bonusCounts.getOrDefault(bonus, 0) + 1);
        }

        int bonusX = resolutionManager.scaleX(x);
        int bonusY = resolutionManager.scaleY(y + 10);
        int bonusWidth = resolutionManager.scaleX(30);
        int bonusHeight = resolutionManager.scaleY(40);
        int bonusSpacing = resolutionManager.scaleX(40);
        int cornerRadius = resolutionManager.scaleSize(10);

        for (Map.Entry<GemToken, Integer> entry : bonusCounts.entrySet()) {
            if (entry.getValue() > 0) {
                g.setColor(TOKEN_COLORS.get(entry.getKey()));
                g.fillRoundRect(bonusX, bonusY + resolutionManager.scaleY(10), bonusWidth, bonusHeight, cornerRadius, cornerRadius);

                g.setColor(Color.WHITE);
                Font scaledBonusFont = resolutionManager.scaleFont(PLAYER_INFOS_BONUS_FONT);
                g.setFont(scaledBonusFont);
                int textOffsetX = resolutionManager.scaleX(10);
                int textOffsetY = resolutionManager.scaleY(38);
                g.drawString(String.valueOf(entry.getValue()), bonusX + textOffsetX, bonusY + textOffsetY);

                bonusX += bonusSpacing;
            }
        }
    }

    private void drawPlayerInfo(Graphics2D g, Player player, int x, int yIndex) {
        int yStart = resolutionManager.scaleY(yIndex * PLAYER_FRAME_HEIGHT + 20 * yIndex);
        int scaledX = resolutionManager.scaleX(x);

        drawPlayerBox(g, scaledX, yStart);
        drawPlayerHeader(g, player, scaledX, yStart);
        drawPlayerTokens(g, player, scaledX + resolutionManager.scaleX(20), yStart + resolutionManager.scaleY(80));
        drawPlayerBonuses(g, player, scaledX + resolutionManager.scaleX(20), yStart + resolutionManager.scaleY(180));
    }

    private void drawReservedCards(Graphics2D g, List<DevelopmentCard> cards, int width) {
        int x = resolutionManager.scaleX(370);
        int y = resolutionManager.scaleY(850);
        int rectWidth = resolutionManager.scaleX(width / 2 + 100);
        int rectHeight = resolutionManager.scaleY(200);

        drawFramedPanel(g, x, y, rectWidth, rectHeight);

        Font scaledFont = resolutionManager.scaleFont(BUBBLE_FONT); // À adapter à ta constante de font
        g.setFont(scaledFont);

        drawBubble(g, "Cartes réservées", x + resolutionManager.scaleX(10), y - resolutionManager.scaleY(15), resolutionManager.scaleX(150), resolutionManager.scaleY(30));

        String counterText = cards.size() + "/3";
        drawBubble(g, counterText, x + rectWidth - resolutionManager.scaleX(60), y - resolutionManager.scaleY(15), resolutionManager.scaleX(50), resolutionManager.scaleY(30));

        int startX = x + resolutionManager.scaleX(20);
        int startY = y + resolutionManager.scaleY(20);
        int spacing = resolutionManager.scaleX(20);
        int scaledCardWidth = resolutionManager.scaleX(CARD_WIDTH);
        int scaledCardHeight = resolutionManager.scaleY(CARD_HEIGHT);

        for (int i = 0; i < cards.size(); i++) {
            DevelopmentCard card = cards.get(i);
            int cardX = startX + i * (scaledCardWidth + spacing);
            int cardY = startY;

            try {
                InputStream inputStream = Main.class.getResourceAsStream(card.imageUrl());
                if (inputStream != null) {
                    BufferedImage image = ImageIO.read(inputStream);
                    g.drawImage(image, cardX, cardY, scaledCardWidth, scaledCardHeight, null);
                } else {
                    throw new IOException("Image not found: " + card.imageUrl());
                }
            } catch (IOException e) {
                g.setColor(Color.WHITE);
                g.fillRect(cardX, cardY, scaledCardWidth, scaledCardHeight);
                g.setColor(Color.BLACK);
                g.drawRect(cardX, cardY, scaledCardWidth, scaledCardHeight);
            }

            drawCardInfos(g, cardX, cardY, card); // Assurez-vous que cette fonction supporte le scaling si nécessaire
        }
    }

    private void drawPlayerNobles(Graphics2D g, List<Noble> nobles, int width) {
        int x = resolutionManager.scaleX(370 + width / 2 + 120);
        int y = resolutionManager.scaleY(850);
        int rectWidth = resolutionManager.scaleX(width / 2 + 100);
        int rectHeight = resolutionManager.scaleY(200);

        drawFramedPanel(g, x, y, rectWidth, rectHeight);

        Font scaledFont = resolutionManager.scaleFont(BUBBLE_FONT); // Même remarque que plus haut
        g.setFont(scaledFont);

        drawBubble(g, "Nobles acquis", x + resolutionManager.scaleX(10), y - resolutionManager.scaleY(15), resolutionManager.scaleX(150), resolutionManager.scaleY(30));

        int nobleX = x + resolutionManager.scaleX(20);
        int nobleY = y + resolutionManager.scaleY(40);
        int scaledNobleWidth = resolutionManager.scaleX(NOBLE_WIDTH);
        int scaledNobleHeight = resolutionManager.scaleY(NOBLE_HEIGHT);
        int scaledPadding = resolutionManager.scaleX(PADDING);

        for (int i = 0; i < nobles.size(); i++) {
            Noble noble = nobles.get(i);
            int currentX = nobleX + i * (scaledNobleWidth + scaledPadding);

            try {
                InputStream inputStream = Main.class.getResourceAsStream(noble.imageUrl());
                if (inputStream != null) {
                    BufferedImage image = ImageIO.read(inputStream);
                    g.drawImage(image, currentX, nobleY, scaledNobleWidth, scaledNobleHeight, null);
                } else {
                    throw new IOException("Image not found: " + noble.imageUrl());
                }
            } catch (IOException e) {
                g.setColor(Color.WHITE);
                g.fillRect(currentX, nobleY, scaledNobleWidth, scaledNobleHeight);
                g.setColor(Color.BLACK);
                g.drawRect(currentX, nobleY, scaledNobleWidth, scaledNobleHeight);
            }

            drawNobleInfos(g, currentX, nobleY, noble);
        }
    }

    @Override
    public void displayMessage(String message) {
        context.renderFrame(g -> drawHeader(g, message));
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
