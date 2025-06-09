package splendor.view;

import com.github.forax.zen.Application;
import com.github.forax.zen.ApplicationContext;
import splendor.app.Demo;
import splendor.app.Main;
import splendor.model.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class GraphicView implements SplendorsView{
    private ApplicationContext context;
    List<Rectangle> cardBounds = new ArrayList<>();
    List<DevelopmentCard> displayedCards = new ArrayList<>();

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


    private void drawCardStacks(Graphics2D g, int x, int yStart, int[] stackSizes) {
        for (int level = 3; level >= 1; level--) {
            String imagePath = "../resources/images/development_cards/level" + level + "_cards.png";
            try (InputStream in = Main.class.getResourceAsStream(imagePath)) {
                if (in != null) {
                    BufferedImage backImage = ImageIO.read(in);
                    int y = yStart + (3 - level) * (CARD_HEIGHT + PADDING);
                    g.drawImage(backImage, x, y, CARD_WIDTH, CARD_HEIGHT, null);

                    // Cercle blanc en haut à gauche avec le nombre de cartes
                    int count = stackSizes[3 - level];
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

            // Configurer la grille
            int cardsPerRow = 4;
            int startX = 500;
            int startY = 100;

            for (int i = cards.size() -1; i >= 0; i--) {
                DevelopmentCard card = cards.get(i);

                // Calcul dynamique des coordonnées
                int row = i / cardsPerRow;
                int col = i % cardsPerRow;
                int x = startX + col * (CARD_WIDTH + PADDING);
                int y = startY + row * (CARD_HEIGHT + PADDING);

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

    private void drawToken(Graphics2D g, GemToken bonus, int amount, int cx, int cy, int size) {
        g.setColor(TOKEN_COLORS.get(bonus));
        g.fillOval(cx, cy, size, size);

        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        String text = String.valueOf(amount);
        int tx = cx + (size - fm.stringWidth(text)) / 2;
        int ty = cy + ((size - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(text, tx, ty);
    }

    private void drawCardInfos(Graphics2D g, int x, int y, DevelopmentCard card) {
        // Bandeau semi-transparent en haut
        g.setColor(new Color(240, 240, 240, 140));
        g.fillRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT / 4, 15, 15);

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

        // Dessiner les tokens en bas
        int tokenSize = 36;
        int PADDING = 4;

        // Filtrer les coûts non nuls
        Map<GemToken, Integer> price = card.price();
        List<Map.Entry<GemToken, Integer>> cost = price.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 0)
                .toList();

        for (int i = 0; i < cost.size(); i++) {
            int row = i / 2;
            int col = i % 2;
            int cx = x + col * (tokenSize + PADDING) + PADDING;
            int cy = y + CARD_HEIGHT - (2 - row) * (tokenSize + PADDING);
            drawToken(g, cost.get(i).getKey(), cost.get(i).getValue(), cx, cy, tokenSize);
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

    private static void drawReservedCards(Graphics2D g, List<DevelopmentCard> cards, int width) {
        // Position et dimensions de base
        int x = 370;
        int y = 850;
        int rectWidth = width/2 + 100;
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

        // Texte "Cartes réservées"
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        int textWidth = g.getFontMetrics().stringWidth("Cartes réservées");
        g.drawString("Cartes réservées", bubbleX + (bubbleWidth - textWidth)/2, bubbleY + 20);

        // Bulle du compteur (à droite)
        String counterText = cards.size() + "/3";
        int counterBubbleWidth = 50;
        int counterBubbleX = x + rectWidth - counterBubbleWidth - 10;

        // Fond bulle compteur
        g.setColor(new Color(90, 90, 90));
        g.fillRoundRect(counterBubbleX, bubbleY, counterBubbleWidth, bubbleHeight, 15, 15);

        // Bordure bulle compteur
        g.setColor(new Color(212, 175, 55));
        g.drawRoundRect(counterBubbleX, bubbleY, counterBubbleWidth, bubbleHeight, 15, 15);

        // Texte compteur
        g.setColor(Color.WHITE);
        textWidth = g.getFontMetrics().stringWidth(counterText);
        g.drawString(counterText, counterBubbleX + (counterBubbleWidth - textWidth)/2, bubbleY + 20);
    }

    private static void drawPlayerNobles(Graphics2D g, List<DevelopmentCard> cards, int width) {
        // Position et dimensions de base
        int x = 370 + width/2 + 120;
        int y = 850;
        int rectWidth = width/2 + 100;
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
        g.drawString("Nobles acquis", bubbleX + (bubbleWidth - textWidth)/2, bubbleY + 20);

        // Bulle du compteur (à droite)
        String counterText = cards.size() + "/3";
        int counterBubbleWidth = 50;
        int counterBubbleX = x + rectWidth - counterBubbleWidth - 10;
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
            drawCardStacks(g, 350, 100, new int[]{10, 12, 30});
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
            drawPlayerNobles(g, player.getPurchasedCards(), (width - PLAYERS_STATE_WIDTH )/ 2);
        });
    }

    @Override
    public void showMenu(Game game) {

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
        });
    }

    @Override
    public int getMenuChoice(Game game) {
        return 0;
    }

    @Override
    public int selectCard(int maxIndex) {
        return 0;
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
