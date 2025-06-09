package splendor.app;

import com.github.forax.zen.Application;
import splendor.model.GemToken;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Demo {
  record Card(int x, int y, int width, int height, GemToken bonus, int level, int prestige, String imageUrl, List<CoinStack> cost) {}
  record CoinStack(int x, int y, Color color, int count) {}
  record Player(String name, int prestige, List<CoinStack> coins) {}

  static int playersStateWidth = 300;
  static int playersStateHeight = 250;

  public static void main(String[] args) {
    Application.run(Color.BLUE.darker(), context -> {
      var screen = context.getScreenInfo();
      int width = screen.width();
      int height = screen.height();

      System.out.println(width + " "  + height);
      String message = "Au tour de Alice";

      int cardWidth = 130;
      int cardHeight = 170;
      int padding = 20;
      int gridCols = 4;
      int gridRows = 3;

      Color[] gemColors = {
              new Color(210, 210, 210), // Blanc
              new Color(0, 102, 204),   // Bleu
              new Color(0, 128, 0),     // Vert
              new Color(204, 0, 0),     // Rouge
              new Color(30, 30, 30),    // Noir
      };

      int startX = 500;
      int startY = 100;

      var cards = new java.util.ArrayList<Card>();
      Random rand = new Random();

      GemToken[] bonusTypes = {
              GemToken.RUBY, GemToken.EMERALD, GemToken.DIAMOND, GemToken.SAPPHIRE, GemToken.ONYX
      };

      for (int row = 0; row < gridRows; row++) {
        int level = 3 - row;
        for (int col = 0; col < gridCols; col++) {
          int x = startX + col * (cardWidth + padding);
          int y = startY + row * (cardHeight + padding);
          int prestige = row + col;

          // 1. Bonus gemme aléatoire
          GemToken bonus = bonusTypes[rand.nextInt(bonusTypes.length)];

          // 2. Image name en fonction du bonus et du level
          String bonusStr = bonus.toString().toLowerCase();

          String typeStr;
          if (level == 1) typeStr = "mine";
          else if (level == 2) typeStr = "workshop";
          else {
            // level == 3 → soit "city" soit "monument"
            typeStr = rand.nextBoolean() ? "city" : "monument";
          }

          String imageUrl = "../resources/images/development_cards/" + bonusStr + "_" + typeStr + ".png";

          // 3. Coût aléatoire
          int tokenTypes = 1 + rand.nextInt(4);
          List<Color> shuffledColors = new ArrayList<>(List.of(gemColors));
          Collections.shuffle(shuffledColors);

          List<CoinStack> cost = new ArrayList<>();
          for (int i = 0; i < tokenTypes; i++) {
            int count = 1 + rand.nextInt(3);
            cost.add(new CoinStack(0, 0, shuffledColors.get(i), count));
          }

          // 4. Création de la carte avec image
          cards.add(new Card(x, y, cardWidth, cardHeight, bonus, level, prestige, imageUrl, cost));
        }
      }


      List<CoinStack> coins = List.of(
              new CoinStack(0, 0, new Color(210, 210, 210), 1),
              new CoinStack(0, 0, new Color(0, 102, 204), 3),
              new CoinStack(0, 0, new Color(0, 128, 0), 1),
              new CoinStack(0, 0, new Color(204, 0, 0), 2),
              new CoinStack(0, 0, new Color(30, 30, 30), 0),
              new CoinStack(0, 0, new Color(212, 175, 55), 1)
      );

      Player player1 = new Player("Alice", 7, List.of(
              new CoinStack(0, 0, new Color(210, 210, 210), 1),
              new CoinStack(0, 0, new Color(0, 102, 204), 3),
              new CoinStack(0, 0, new Color(0, 128, 0), 1),
              new CoinStack(0, 0, new Color(204, 0, 0), 2),
              new CoinStack(0, 0, new Color(30, 30, 30), 0),
              new CoinStack(0, 0, new Color(212, 175, 55), 1)
              ));

      Player player2 = new Player("Bob", 4, List.of(
              new CoinStack(0, 0, new Color(210, 210, 210), 1),
              new CoinStack(0, 0, new Color(0, 102, 204), 2),
              new CoinStack(0, 0, new Color(0, 128, 0), 0),
              new CoinStack(0, 0, new Color(204, 0, 0), 2),
              new CoinStack(0, 0, new Color(30, 30, 30), 1),
              new CoinStack(0, 0, new Color(212, 175, 55), 2)
      ));
      try {
        context.renderFrame(g -> {
            try {
              drawBackground(g);
              drawTokenImage(g, width - playersStateWidth - 400, 100, "../resources/images/tokens/diamond_token.png", 2);
              drawTokenImage(g, width - playersStateWidth - 400, 100, "../resources/images/tokens/diamond_token.png", 2);
              drawTokenImage(g, width - playersStateWidth - 400, 200, "../resources/images/tokens/sapphire_token.png", 2);
              drawTokenImage(g, width - playersStateWidth - 400, 300, "../resources/images/tokens/emerald_token.png", 2);
              drawTokenImage(g, width - playersStateWidth - 400, 400, "../resources/images/tokens/ruby_token.png", 2);
              drawTokenImage(g, width - playersStateWidth - 400, 500, "../resources/images/tokens/onyx_token.png", 2);
              drawTokenImage(g, width - playersStateWidth - 400, 600, "../resources/images/tokens/gold_token.png", 2);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
          drawHeader(g, message, width);
            int[] sizes = {16, 22, 18};
          drawCardStacks(g, 350, 100, cardWidth, cardHeight, padding, sizes);
          drawCards(g, cards);
          drawReservedCards(g, cards.subList(0,2), (width - playersStateWidth )/ 2);
          drawPlayerNobles(g, cards.subList(0,2), (width - playersStateWidth )/ 2);
          drawPlayerInfo(g, player1, width - playersStateWidth, 0);
          drawPlayerInfo(g, player2, width - playersStateWidth, 1);
          drawGemStones(g, coins, (width - playersStateWidth) / 2 - 100);
        });
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      while (true) {

      }
    });
  }

  private static void drawHeader(Graphics2D g, String message, int screenWidth) {
    // Rectangle
    g.setColor(Color.WHITE);
    g.fillRect(0, 10, screenWidth - playersStateWidth, 50);

    // Texte (centrage précis)
    g.setFont(new Font("SansSerif", Font.BOLD, 20));
    FontMetrics fm = g.getFontMetrics();
    int textX = ((screenWidth - playersStateWidth) - fm.stringWidth(message)) / 2;
    int textY = (50 - fm.getHeight()) / 2 + fm.getAscent() + 10; // Centrage vertical mathématique
    g.setColor(Color.BLACK);
    g.drawString(message, textX, textY);
  }

  private static void drawCardStacks(Graphics2D g, int x, int yStart, int cardWidth, int cardHeight, int padding, int[] stackSizes) {
    for (int level = 3; level >= 1; level--) {
      String imagePath = "../resources/images/development_cards/level" + level + "_cards.png";
      try (InputStream in = Main.class.getResourceAsStream(imagePath)) {
        if (in != null) {
          BufferedImage backImage = ImageIO.read(in);
          int y = yStart + (3 - level) * (cardHeight + padding);
          g.drawImage(backImage, x, y, cardWidth, cardHeight, null);

          // Cercle blanc en haut à gauche avec le nombre de cartes
          int count = stackSizes[3 - level]; // index 0 = level 3
          int circleSize = 32;
          int circleX = x + 8;               // petit padding à gauche
          int circleY = y + 8;               // petit padding en haut

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

  private static void drawCards(Graphics2D g, List<Card> cards) {
    g.setFont(new Font("SansSerif", Font.PLAIN, 24));
    for (Card card : cards) {
      try {
        InputStream inputStream = Main.class.getResourceAsStream(card.imageUrl);
        if (inputStream != null) {
          BufferedImage originalImage = ImageIO.read(inputStream);

          // Crée une image arrondie temporaire
          BufferedImage rounded = new BufferedImage(card.width, card.height, BufferedImage.TYPE_INT_ARGB);
          Graphics2D g2 = rounded.createGraphics();
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

          // Applique un clip en forme de rectangle arrondi
          g2.drawImage(originalImage, 0, 0, card.width, card.height, null);
          g2.dispose();

          // Dessine l'image arrondie finale sur le canvas
          g.drawImage(rounded, card.x, card.y, null);

        } else {
          throw new IOException("Image not found: " + card.imageUrl);
        }
      } catch (IOException e) {
        // Fallback rectangle blanc
        g.setColor(Color.WHITE);
        g.fillRoundRect(card.x, card.y, card.width, card.height, 15, 15);
        g.setColor(Color.BLACK);
        g.drawRoundRect(card.x, card.y, card.width, card.height, 15, 15);
      }

      drawCardInfos(g, card.x, card.y, card);
    }
  }


  private static void drawToken(Graphics2D g, CoinStack coin, int cx, int cy, int size) {
    g.setColor(coin.color());
    g.fillOval(cx, cy, size, size);

    g.setFont(new Font("SansSerif", Font.BOLD, 16));
    g.setColor(Color.WHITE);
    FontMetrics fm = g.getFontMetrics();
    String text = String.valueOf(coin.count());
    int tx = cx + (size - fm.stringWidth(text)) / 2;
    int ty = cy + ((size - fm.getHeight()) / 2) + fm.getAscent();
    g.drawString(text, tx, ty);
  }

  private static void drawCardInfos(Graphics2D g, int x, int y, Card card) {
    // Bandeau semi-transparent en haut
    g.setColor(new Color(240, 240, 240, 140));
    g.fillRoundRect(x, y, card.width, card.height / 4, 15, 15);

    // Prestige en haut à gauche avec contour noir
    int prestigeX = x + 15;
    int prestigeY = y + 30;
    String prestigeText = String.valueOf(card.prestige);

    g.setFont(new Font("SansSerif", Font.BOLD, 28));

    // Dessiner le contour noir (légers offsets)
    g.setColor(Color.BLACK);
    for (int dx = -1; dx <= 1; dx++) {
      for (int dy = -1; dy <= 1; dy++) {
        if (dx != 0 || dy != 0) {
          g.drawString(prestigeText, prestigeX + dx, prestigeY + dy);
        }
      }
    }

    // Dessiner le texte blanc par-dessus
    g.setColor(Color.WHITE);
    g.drawString(prestigeText, prestigeX, prestigeY);

    // Bonus token en haut à droite
    try {
      String bonusName = card.bonus.toString().toLowerCase();
      InputStream in = Main.class.getResourceAsStream("../resources/images/gems/" + bonusName + ".png");
      if (in != null) {
        BufferedImage bonusImage = ImageIO.read(in);
        int bonusSize = 30;
        int bonusX = x + card.width - bonusSize - 10;
        int bonusY = y + 4;
        g.drawImage(bonusImage, bonusX, bonusY, bonusSize, bonusSize, null);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Coût en bas de la carte
    List<CoinStack> costs = card.cost;
    int tokenSize = 36;
    int padding = 4;

    switch (costs.size()) {
      case 1 -> drawToken(g, costs.get(0), x + padding, y + card.height - tokenSize - padding, tokenSize);
      case 2 -> {
        for (int i = 0; i < 2; i++) {
          int cx = x + padding;
          int cy = y + card.height - ((2 - i) * (tokenSize + padding));
          drawToken(g, costs.get(i), cx, cy, tokenSize);
        }
      }
      case 3 -> {
        drawToken(g, costs.get(0), x + padding, y + card.height - tokenSize - padding, tokenSize);
        drawToken(g, costs.get(1), x + padding, y + card.height - 2 * (tokenSize + padding), tokenSize);
        drawToken(g, costs.get(2), x + tokenSize + 2 * padding, y + card.height - tokenSize - padding, tokenSize);
      }
      case 4 -> {
        for (int i = 0; i < 4; i++) {
          int row = i / 2;
          int col = i % 2;
          int cx = x + col * (tokenSize + padding) + padding;
          int cy = y + card.height - (2 - row) * (tokenSize + padding);
          drawToken(g, costs.get(i), cx, cy, tokenSize);
        }
      }
    }
  }




  private static void drawReservedCards(Graphics2D g, List<Card> cards, int width) {
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

  private static void drawPlayerNobles(Graphics2D g, List<Card> cards, int width) {
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

  private static void drawBackground(Graphics2D g) throws IOException {
    InputStream inputStream = Main.class.getResourceAsStream("../resources/images/background.png");
      if (inputStream == null) {
        throw new IOException("Image not found in resources!");
      }
      BufferedImage image = ImageIO.read(inputStream);
      System.out.println("Image loaded: " + image.getWidth() + "x" + image.getHeight());
      g.drawImage(image, 0, 0, null);
  }

  private static void drawPlayerInfo(Graphics2D g, Player player, int x, int yStart) {
    yStart = yStart * playersStateHeight + 20 * yStart;

    g.setColor(new Color(120, 120, 120, 200));
    g.fillRoundRect(x + 5, yStart + 10, playersStateWidth - 10, playersStateHeight, 50, 50);

    // Nom du joueur
    g.setColor(Color.WHITE);
    g.setFont(new Font("SansSerif", Font.BOLD, 24));
    g.drawString(player.name, x + 20, yStart + 40);


    // Étoile et score
    g.setColor(Color.YELLOW);
    g.setFont(new Font("SansSerif", Font.BOLD, 24));
    g.drawString("★ " + player.prestige, x + 240, yStart + 40);

    x += 20;

    // === TITRE : Jetons ===
    g.setColor(Color.WHITE);
    g.setFont(new Font("SansSerif", Font.PLAIN, 18));
    g.drawString("Jetons", x, yStart + 80);

    // Affiche les pièces du joueur
    int tokenY = yStart + 100;
    int tokenX = x;
    for (CoinStack stack : player.coins) {
      g.setColor(stack.color);
      g.fillRoundRect(tokenX, tokenY, 30, 40, 10, 10);
      g.setColor(Color.WHITE);
      g.drawString(String.valueOf(stack.count), tokenX + 10, tokenY + 28);
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
    for (CoinStack stack : player.coins) {
      if (stack.count > 0) {
        g.setColor(stack.color);
        g.fillRoundRect(bonusX, bonusY + 10, 30, 40, 10, 10);
        g.setColor(Color.WHITE);
        g.drawString(String.valueOf(stack.count), bonusX + 10, bonusY + 10 + 28);
        bonusX += 40;
      }
    }
  }

  private static void drawGemStones(Graphics2D g, List<CoinStack> gemStones, int width) {
    // Dimensions
    int rectHeight = 90;
    int x = 500;
    int y = 720;
    int cornerRadius = 20;
    int padding = 15;

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
    int totalGems = gemStones.stream().mapToInt(CoinStack::count).sum();
    String counterText = totalGems + "/10";
    int counterBubbleWidth = 50;
    int counterBubbleX = x + width - counterBubbleWidth - padding;
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
    int startX = x + padding;
    int startY = y + padding + 10;

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
      int count = i < gemStones.size() ? gemStones.get(i).count() : 0;
      String countText = String.valueOf(count);

      // Texte nombre de tokens
      g.setFont(new Font("SansSerif", Font.BOLD, 16));
      int countTextWidth = g.getFontMetrics().stringWidth(countText);
      g.drawString(countText,
              tokenX + bonusWidth + (tokensWidth - countTextWidth)/2,
              startY + tokenHeight/2 + 5);
    }
  }


  private static void drawTokenImage(Graphics2D g, int x, int y, String imagePath, int count) {
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
      int padding = 4; // marge intérieure
      int circleX = x - padding;
      int circleY = y + padding - 8; // ↰ on le remonte de 4 pixels

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
}
