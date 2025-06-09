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

    private static final int PLAYERS_STATE_WIDTH = 300;
    private static final int PLAYERS_STATE_HEIGHT = 250;


    private static final int CARD_WIDTH = 130;

    private static final int CARD_HEIGHT = 170;

    private static final int NOBLE_WIDTH = 130;

    private static final int NOBLE_HEIGHT = 130;

    private static final int PADDING = 20;

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
        g.fillRect(0, 10, screenWidth - PLAYERS_STATE_WIDTH, 50);

        // Texte (centrage précis)
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();
        int textX = ((screenWidth - PLAYERS_STATE_WIDTH) - fm.stringWidth(message)) / 2;
        int textY = (50 - fm.getHeight()) / 2 + fm.getAscent() + 10; // Centrage vertical mathématique
        g.setColor(Color.BLACK);
        g.drawString(message, textX, textY);
    }

    // Méthode appelée à chaque frame
    private void draw(Graphics2D g) {
        int width = context.getScreenInfo().width();
        int height = context.getScreenInfo().height();

        try {
            drawBackground(g);
            drawHeader(g, "Au tour d'alice", width);
            // autres appels de rendering avec currentGame / currentPlayer
        } catch (IOException e) {
            e.printStackTrace();
        }
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

            // Compteur (en haut à droite du token, légèrement plus haut)
            int circleRadius = 14;
            int PADDING = 4; // marge intérieure
            int circleX = x - PADDING;
            int circleY = y + PADDING - 8; // ↰ on le remonte de 4 pixels

            // Cercle blanc
            g.setColor(Color.WHITE);
            g.fillOval(circleX, circleY, circleRadius * 2, circleRadius * 2);

            // Contour noir
            g.setColor(Color.BLACK);
            g.drawOval(circleX, circleY, circleRadius * 2, circleRadius * 2);

            // Texte centré dans le petit cercle
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            String text = String.valueOf(count);
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent();
            int textX = circleX + (circleRadius * 2 - textWidth) / 2;
            int textY = circleY + (circleRadius * 2 + textHeight) / 2 - 2;

            g.setColor(Color.BLACK);
            g.drawString(text, textX, textY);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawGemStones(Graphics2D g, Player p, int width) {
        // Dimensions
        int rectHeight = 90;
        int x = 500;
        int y = 720;
        int cornerRadius = 20;
        int PADDING = 15;

        // Rectangle principal
        g.setColor(new Color(120, 120, 120, 200));
        g.fillRoundRect(x, y, width, rectHeight, cornerRadius, cornerRadius);

        // Bordure principale
        g.setColor(new Color(212, 175, 55));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, width, rectHeight, cornerRadius, cornerRadius);

        // Bulle du titre (centrée en haut)
        String title = "Pierres précieuses";
        int titleBubbleWidth = 180;
        int titleBubbleHeight = 30;
        int titleBubbleX = x + (width - titleBubbleWidth) / 2;
        int titleBubbleY = y - 15;

        // Fond bulle titre
        g.setColor(new Color(90, 90, 90));
        g.fillRoundRect(titleBubbleX, titleBubbleY, titleBubbleWidth, titleBubbleHeight, 15, 15);

        // Bordure bulle titre
        g.setColor(new Color(212, 175, 55));
        g.drawRoundRect(titleBubbleX, titleBubbleY, titleBubbleWidth, titleBubbleHeight, 15, 15);

        // Texte titre
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, titleBubbleX + (titleBubbleWidth - titleWidth)/2, titleBubbleY + 20);

        // Compteur total (à droite)
        int totalGems = p.getTotalAmount();
        String counterText = totalGems + "/10";
        int counterBubbleWidth = 50;
        int counterBubbleX = x + width - counterBubbleWidth - PADDING;
        int counterBubbleY = y - 15;

        // Fond bulle compteur
        g.setColor(new Color(90, 90, 90));
        g.fillRoundRect(counterBubbleX, counterBubbleY, counterBubbleWidth, titleBubbleHeight, 15, 15);

        // Bordure bulle compteur
        g.setColor(new Color(212, 175, 55));
        g.drawRoundRect(counterBubbleX, counterBubbleY, counterBubbleWidth, titleBubbleHeight, 15, 15);

        // Texte compteur
        g.setColor(Color.WHITE);
        int counterWidth = g.getFontMetrics().stringWidth(counterText);
        g.drawString(counterText, counterBubbleX + (counterBubbleWidth - counterWidth)/2, counterBubbleY + 20);

        // Configuration des jetons
        int tokenWidth = 100;
        int tokenHeight = 50;
        int tokenSpacing = 10;
        int startX = x + PADDING;
        int startY = y + PADDING + 10;

        // Couleurs des jetons
        Color[] tokenColors = {
                new Color(210, 210, 210),  // Blanc
                new Color(0, 102, 204),    // Bleu
                new Color(0, 128, 0),      // Vert
                new Color(204, 0, 0),      // Rouge
                new Color(30, 30, 30),     // Noir
                new Color(212, 175, 55)    // Or
        };

        // Dessin des 6 jetons
        for (int i = 0; i < 6; i++) {
            int tokenX = startX + i * (tokenWidth + tokenSpacing);

            // Fond du jeton (sans bordure)
            g.setColor(tokenColors[i]);
            g.fillRect(tokenX, startY, tokenWidth, tokenHeight);

            // Partie gauche (Bonus) - 30% de largeur
            int bonusWidth = (int)(tokenWidth * 0.3);
            g.setColor(new Color(255, 255, 255, 100));
            g.fillRect(tokenX, startY, bonusWidth, tokenHeight);

            // Texte bonus
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            String bonusText = "0"; // Valeur de bonus
            int bonusTextWidth = g.getFontMetrics().stringWidth(bonusText);
            g.drawString(bonusText,
                    tokenX + (bonusWidth - bonusTextWidth)/2,
                    startY + tokenHeight/2 + 5);

            // Partie droite (Tokens) - 70% de largeur
            int tokensWidth = tokenWidth - bonusWidth;
            GemToken[] tokens = GemToken.values();
            GemToken token = tokens[i];
            int count = p.getTokenCount(token);
            String countText = String.valueOf(count);

            // Texte nombre de tokens
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            int countTextWidth = g.getFontMetrics().stringWidth(countText);
            g.drawString(countText,
                    tokenX + bonusWidth + (tokensWidth - countTextWidth)/2,
                    startY + tokenHeight/2 + 5);
        }
    }


    private void drawCardStacks(Graphics2D g, int x, int yStart, List<Integer> stackSizes) {
        for (int level = 3; level >= 1; level--) {
            String imagePath = "../resources/images/development_cards/level" + level + "_cards.png";
            try (InputStream in = Main.class.getResourceAsStream(imagePath)) {
                if (in != null) {
                    BufferedImage backImage = ImageIO.read(in);
                    int y = yStart + (3 - level) * (CARD_HEIGHT + PADDING);
                    g.drawImage(backImage, x, y, CARD_WIDTH, CARD_HEIGHT, null);

                    // Cercle blanc en haut à gauche avec le nombre de cartes
                    int count = stackSizes.get(3 - level);
                    int circleSize = 32;
                    int circleX = x + 8;
                    int circleY = y + 8;

                    g.setColor(Color.WHITE);
                    g.fillOval(circleX, circleY, circleSize, circleSize);

                    g.setColor(Color.BLACK);
                    g.setFont(new Font("SansSerif", Font.BOLD, 16));
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
        g.setFont(new Font("SansSerif", Font.PLAIN, 24));
        int cardsPerRow = 4;
        int startX = 500;

        // Nombre total de lignes (arrondi vers le haut)
        int totalRows = (int) Math.ceil(cards.size() / (double) cardsPerRow);

        // Position de la première ligne (en bas)
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


    private void drawCardToken(Graphics2D g, GemToken bonus, int amount, int cx, int cy, int size) {
        g.setColor(TOKEN_COLORS.get(bonus));
        g.fillOval(cx, cy, size, size);

        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        String text = String.valueOf(amount);
        FontMetrics fm = g.getFontMetrics();
        int tx = cx + (size - fm.stringWidth(text)) / 2;
        int ty = cy + ((size - fm.getHeight()) / 2) + fm.getAscent();

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

    private void drawNobleToken(Graphics2D g, GemToken bonus, int amount, int cx, int cy) {
        int width = CARD_WIDTH / 7;
        int height = CARD_HEIGHT / 6;

        g.setColor(TOKEN_COLORS.get(bonus));
        g.fillRect(cx, cy, width, height);

        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        String text = String.valueOf(amount);
        FontMetrics fm = g.getFontMetrics();
        int tx = cx + (width - fm.stringWidth(text)) / 2;
        int ty = cy + ((height - fm.getHeight()) / 2) + fm.getAscent();

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


    private void drawCardInfos(Graphics2D g, int x, int y, DevelopmentCard card) {
        // Bandeau semi-transparent en haut
        g.setColor(new Color(240, 240, 240, 140));
        g.fillRect(x, y, CARD_WIDTH, CARD_HEIGHT / 4);

        // Prestige en haut à gauche avec contour noir
        int prestigeX = x + 15;
        int prestigeY = y + 30;
        String prestigeText = String.valueOf(card.prestigeScore());

        g.setFont(new Font("SansSerif", Font.BOLD, 28));

        // Contour noir
        g.setColor(Color.BLACK);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    g.drawString(prestigeText, prestigeX + dx, prestigeY + dy);
                }
            }
        }

        // Texte blanc
        g.setColor(Color.WHITE);
        g.drawString(prestigeText, prestigeX, prestigeY);

        // Bonus token en haut à droite
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

        // Affichage dynamique des tokens
        int tokenSize = 36;
        int padding = 4;
        int margin = 8;

        Map<GemToken, Integer> price = card.price();
        List<Map.Entry<GemToken, Integer>> cost = price.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 0)
                .toList();

        int n = cost.size();

        for (int i = 0; i < n; i++) {
            int cx = 0;
            int cy = 0;

            switch (n) {
                case 1:
                    // Bas gauche
                    cx = x + margin;
                    cy = y + CARD_HEIGHT - tokenSize - margin;
                    break;
                case 2:
                    // Colonne bas gauche
                    cx = x + margin;
                    cy = y + CARD_HEIGHT - (2 - i) * (tokenSize + padding) - margin;
                    break;
                case 3:
                    if (i == 0) {
                        // Haut gauche
                        cx = x + margin;
                        cy = y + CARD_HEIGHT - 2 * tokenSize - padding - margin;
                    } else if (i == 1) {
                        // Bas gauche
                        cx = x + margin;
                        cy = y + CARD_HEIGHT - tokenSize - margin;
                    } else {
                        // À droite du second
                        cx = x + tokenSize + padding + margin;
                        cy = y + CARD_HEIGHT - tokenSize - margin;
                    }
                    break;
                case 4:
                    // Carré 2x2 aligné bas gauche
                    int row = i / 2;
                    int col = i % 2;
                    cx = x + col * (tokenSize + padding) + margin;
                    cy = y + CARD_HEIGHT - (2 - row) * (tokenSize + padding) + padding - margin;
                    break;
                default:
                    // Fallback horizontal
                    cx = x + margin + i * (tokenSize + padding);
                    cy = y + CARD_HEIGHT - tokenSize - margin;
            }

            drawCardToken(g, cost.get(i).getKey(), cost.get(i).getValue(), cx, cy, tokenSize);
        }
    }

    private void drawGameNobles(Graphics2D g, List<Noble> nobles, int width) {
        g.setFont(new Font("SansSerif", Font.PLAIN, 24));

        int startX = width - PLAYERS_STATE_WIDTH - 200;
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

    private void drawNobleInfos(Graphics2D g, int x, int y, Noble noble) {
        // Bandeau semi-transparent à gauche
        g.setColor(new Color(240, 240, 240, 140));
        g.fillRect(x, y, NOBLE_WIDTH / 4, NOBLE_HEIGHT);

        // Prestige score affiché en haut du bandeau
        int prestigeX = x + 8;
        int prestigeY = y + 28;
        String prestigeText = String.valueOf(noble.prestigeScore());

        g.setFont(new Font("SansSerif", Font.BOLD, 28));

        // Contour noir du prestige
        g.setColor(Color.BLACK);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    g.drawString(prestigeText, prestigeX + dx, prestigeY + dy);
                }
            }
        }

        // Texte blanc du prestige
        g.setColor(Color.WHITE);
        g.drawString(prestigeText, prestigeX, prestigeY);

        // Dessiner les tokens de bas en haut à gauche
        int tokenSize = NOBLE_HEIGHT / 5;
        int padding = 6;

        Map<GemToken, Integer> price = noble.price();
        List<Map.Entry<GemToken, Integer>> cost = price.entrySet().stream()
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

    private static void drawPlayerInfo(Graphics2D g, Player player, int x, int yStart) {
        yStart = yStart * PLAYERS_STATE_HEIGHT + 20 * yStart;

        g.setColor(new Color(120, 120, 120, 200));
        g.fillRoundRect(x + 5, yStart + 10, PLAYERS_STATE_WIDTH - 10, PLAYERS_STATE_HEIGHT, 50, 50);

        // Nom du joueur
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.drawString(player.getName(), x + 20, yStart + 40);


        // Étoile et score
        g.setColor(Color.YELLOW);
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.drawString("★ " + player.getPrestigeScore(), x + 240, yStart + 40);

        x += 20;

        // === TITRE : Jetons ===
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g.drawString("Jetons", x, yStart + 80);

        // Affiche les pièces du joueur
        int tokenY = yStart + 100;
        int tokenX = x;
        for (Map.Entry<GemToken, Integer> entry : player.getWallet().entries()) {
            g.setColor(TOKEN_COLORS.get(entry.getKey()));
            g.fillRoundRect(tokenX, tokenY, 30, 40, 10, 10);
            g.setColor(Color.WHITE);
            g.drawString(String.valueOf(entry.getValue()), tokenX + 10, tokenY + 28);
            tokenX += 40;
        }

        // === TITRE : Vos bonus ===
        int bonusY = tokenY + 80;
        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        g.drawString("Bonus", x, bonusY);

        // Affiche uniquement les bonus > 0
        int bonusX = x;
        bonusY += 10;

        EnumMap<GemToken, Integer> bonusCounts = new EnumMap<>(GemToken.class);
        for (DevelopmentCard card : player.getPurchasedCards()) {
            GemToken bonus = card.bonus();
            bonusCounts.put(bonus, bonusCounts.getOrDefault(bonus, 0) + 1);
        }
        for (Map.Entry<GemToken, Integer> entry : bonusCounts.entrySet()) {
            GemToken token = entry.getKey();
            int count = entry.getValue();

            if (count > 0) {
                Color color = TOKEN_COLORS.get(token);  // Tu dois avoir une méthode ou une map pour ça
                g.setColor(color);
                g.fillRoundRect(bonusX, bonusY + 10, 30, 40, 10, 10);

                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 16));
                g.drawString(String.valueOf(count), bonusX + 10, bonusY + 10 + 28);

                bonusX += 40; // Décale pour afficher le suivant à droite
            }
        }
    }

    private void drawReservedCards(Graphics2D g, List<DevelopmentCard> cards, int width) {
        // Dimensions générales
        int x = 370;
        int y = 850;
        int rectWidth = width / 2 + 100;
        int rectHeight = 200;
        int cornerRadius = 20;

        // Fond principal
        g.setColor(new Color(120, 120, 120, 200));
        g.fillRoundRect(x, y, rectWidth, rectHeight, cornerRadius, cornerRadius);

        // Bordure
        g.setColor(new Color(212, 175, 55));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, rectWidth, rectHeight, cornerRadius, cornerRadius);

        // Titre "Cartes réservées"
        int bubbleWidth = 150;
        int bubbleHeight = 30;
        int bubbleX = x + 10;
        int bubbleY = y - 15;

        g.setColor(new Color(90, 90, 90));
        g.fillRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 15, 15);
        g.setColor(new Color(212, 175, 55));
        g.drawRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 15, 15);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        String title = "Cartes réservées";
        int textWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, bubbleX + (bubbleWidth - textWidth) / 2, bubbleY + 20);

        // Bulle compteur
        String counterText = cards.size() + "/3";
        int counterBubbleWidth = 50;
        int counterBubbleX = x + rectWidth - counterBubbleWidth - 10;

        g.setColor(new Color(90, 90, 90));
        g.fillRoundRect(counterBubbleX, bubbleY, counterBubbleWidth, bubbleHeight, 15, 15);
        g.setColor(new Color(212, 175, 55));
        g.drawRoundRect(counterBubbleX, bubbleY, counterBubbleWidth, bubbleHeight, 15, 15);

        g.setColor(Color.WHITE);
        textWidth = g.getFontMetrics().stringWidth(counterText);
        g.drawString(counterText, counterBubbleX + (counterBubbleWidth - textWidth) / 2, bubbleY + 20);

        // === Affichage des cartes réservées ===
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
        // Position et dimensions de base
        int x = 370 + width / 2 + 120;
        int y = 850;
        int rectWidth = width / 2 + 100;
        int rectHeight = 200;
        int cornerRadius = 20;

        // Fond principal
        g.setColor(new Color(120, 120, 120, 200));
        g.fillRoundRect(x, y, rectWidth, rectHeight, cornerRadius, cornerRadius);

        // Bordure principale
        g.setColor(new Color(212, 175, 55));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, rectWidth, rectHeight, cornerRadius, cornerRadius);

        // Bulle du titre (à gauche)
        int bubbleWidth = 150;
        int bubbleHeight = 30;
        int bubbleX = x + 10;
        int bubbleY = y - 15;

        // Fond bulle titre
        g.setColor(new Color(90, 90, 90));
        g.fillRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 15, 15);

        // Bordure bulle titre
        g.setColor(new Color(212, 175, 55));
        g.drawRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 15, 15);

        // Texte "Nobles acquis"
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        int textWidth = g.getFontMetrics().stringWidth("Nobles acquis");
        g.drawString("Nobles acquis", bubbleX + (bubbleWidth - textWidth) / 2, bubbleY + 20);

        // Bulle du compteur (à droite)
        String counterText = nobles.size() + "/3";
        int counterBubbleWidth = 50;
        int counterBubbleHeight = 30;
        int counterBubbleX = x + rectWidth - counterBubbleWidth - 10;
        int counterBubbleY = y - 15;

        g.setColor(new Color(90, 90, 90));
        g.fillRoundRect(counterBubbleX, counterBubbleY, counterBubbleWidth, counterBubbleHeight, 15, 15);

        g.setColor(new Color(212, 175, 55));
        g.drawRoundRect(counterBubbleX, counterBubbleY, counterBubbleWidth, counterBubbleHeight, 15, 15);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        int countTextWidth = g.getFontMetrics().stringWidth(counterText);
        g.drawString(counterText, counterBubbleX + (counterBubbleWidth - countTextWidth) / 2, counterBubbleY + 20);

        // Affichage des nobles du joueur (horizontale dans la zone)
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
           drawGemStones(g, player, (width - PLAYERS_STATE_WIDTH) / 2 - 100);
            drawReservedCards(g, player.getReservedCards(), (width - PLAYERS_STATE_WIDTH )/ 2);
            drawPlayerNobles(g, player.getAcquiredNobles(), (width - PLAYERS_STATE_WIDTH )/ 2);
        });
    }

    @Override
    public void showMenu(Game game) {
        context.renderFrame(g -> {
            menuButtons.clear();

            int buttonWidth = 290;
            int buttonHeight = 50;
            int spacing = 20;
            int startX = 20;
            int startY = 100;
            String[] options = {
                    "Acheter une carte",
                    "Récupérer 3 différentes",
                    "Récupérer 2 identiques",
                    "Réserver une carte",
                    "Acheter une carte réservée"
            };

            g.setFont(new Font("SansSerif", Font.BOLD, 18));

            for (int i = 0; i < options.length; i++) {
                int x = startX;
                int y = startY + i * (buttonHeight + spacing);

                Rectangle rect = new Rectangle(x, y, buttonWidth, buttonHeight);
                menuButtons.put(rect, i+1);
                // Fond
                g.setColor(new Color(80, 80, 80));
                g.fillRoundRect(x, y, buttonWidth, buttonHeight, 20, 20);

                // Bordure
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(2));
                g.drawRoundRect(x, y, buttonWidth, buttonHeight, 20, 20);

                // Texte centré
                String text = options[i];
                FontMetrics fm = g.getFontMetrics();
                int textX = x + (buttonWidth - fm.stringWidth(text)) / 2;
                int textY = y + (buttonHeight - fm.getHeight()) / 2 + fm.getAscent();

                g.setColor(Color.WHITE);
                g.drawString(text, textX, textY);
            }
        });
    }

    @Override
    public void showBoard(Game game) {
        int width = context.getScreenInfo().width();
        context.renderFrame(g -> {
            drawTokenImage(g, width - PLAYERS_STATE_WIDTH - 400, 100, "../resources/images/tokens/diamond_token.png", game.getBank().getAmount(GemToken.DIAMOND));
            drawTokenImage(g, width - PLAYERS_STATE_WIDTH - 400, 200, "../resources/images/tokens/sapphire_token.png", game.getBank().getAmount(GemToken.SAPPHIRE));
            drawTokenImage(g, width - PLAYERS_STATE_WIDTH - 400, 300, "../resources/images/tokens/emerald_token.png", game.getBank().getAmount(GemToken.EMERALD));
            drawTokenImage(g, width - PLAYERS_STATE_WIDTH - 400, 400, "../resources/images/tokens/ruby_token.png", game.getBank().getAmount(GemToken.RUBY));
            drawTokenImage(g, width - PLAYERS_STATE_WIDTH - 400, 500, "../resources/images/tokens/onyx_token.png", game.getBank().getAmount(GemToken.ONYX));
            drawTokenImage(g, width - PLAYERS_STATE_WIDTH - 400, 600, "../resources/images/tokens/gold_token.png", game.getBank().getAmount(GemToken.GOLD));
            showCards(game);
            int[] index = {0};
            game.getPlayers().forEach(player -> {
                drawPlayerInfo(g, player, width - PLAYERS_STATE_WIDTH, index[0]);
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
