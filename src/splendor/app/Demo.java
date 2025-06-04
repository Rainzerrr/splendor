package splendor.app;

import com.github.forax.zen.Application;

import java.awt.*;
import java.util.List;

public class Demo {
  record Card(int x, int y, int width, int height, Color color, int level) {}
  record CoinStack(int x, int y, Color color, int count) {}
  record Player(String name, int prestige, List<CoinStack> coins) {}

  public static void main(String[] args) {
    Application.run(Color.BLUE.darker(), context -> {
      var screen = context.getScreenInfo();
      int width = screen.width();
      int height = screen.height();

      System.out.println(width + " "  + height);
      String message = "Au tour de Alice";

      int cardWidth = 100;
      int cardHeight = 150;
      int padding = 20;
      int gridCols = 4;
      int gridRows = 3;

      int startX = (width - (cardWidth * gridCols + padding * (gridCols - 1))) / 2;
      int startY = height / 4;

      var cards = new java.util.ArrayList<Card>();
      for (int row = 0; row < gridRows; row++) {
        int level = 3 - row;
        for (int col = 0; col < gridCols; col++) {
          int x = startX + col * (cardWidth + padding);
          int y = startY + row * (cardHeight + padding);
          cards.add(new Card(x, y, cardWidth, cardHeight, Color.WHITE, level));
        }
      }

      List<CoinStack> commonCoins = List.of(
              new CoinStack(100, height - 180, Color.RED, 5),
              new CoinStack(180, height - 180, Color.GREEN, 5),
              new CoinStack(260, height - 180, Color.BLUE, 5),
              new CoinStack(340, height - 180, Color.YELLOW, 5),
              new CoinStack(420, height - 180, Color.BLACK, 5),
              new CoinStack(500, height - 180, Color.LIGHT_GRAY, 5)
      );

      Player player1 = new Player("Alice", 7, List.of(
              new CoinStack(0, 0, Color.RED, 2),
              new CoinStack(0, 0, Color.GREEN, 1),
              new CoinStack(0, 0, Color.BLUE, 3),
              new CoinStack(0, 0, Color.YELLOW, 1),
              new CoinStack(0, 0, Color.BLACK, 0),
              new CoinStack(0, 0, Color.LIGHT_GRAY, 1)
      ));

      Player player2 = new Player("Bob", 4, List.of(
              new CoinStack(0, 0, Color.RED, 1),
              new CoinStack(0, 0, Color.GREEN, 2),
              new CoinStack(0, 0, Color.BLUE, 0),
              new CoinStack(0, 0, Color.YELLOW, 2),
              new CoinStack(0, 0, Color.BLACK, 1),
              new CoinStack(0, 0, Color.LIGHT_GRAY, 2)
      ));

      while (true) {
        try {
          context.renderFrame(g -> {
            drawHeader(g, message, width);
            drawCards(g, cards);
            drawCoins(g, commonCoins);
            drawPlayerInfo(g, player1, 30, 100);
            drawPlayerInfo(g, player2, width - 200, 100);
          });
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  private static void drawHeader(Graphics2D g, String message, int screenWidth) {
    g.setColor(Color.WHITE);
    g.setFont(new Font("SansSerif", Font.BOLD, 28));
    int textWidth = g.getFontMetrics().stringWidth(message);
    g.drawString(message, (screenWidth - textWidth) / 2, 50);
  }

  private static void drawCards(Graphics2D g, List<Card> cards) {
    g.setFont(new Font("SansSerif", Font.PLAIN, 20));
    for (Card card : cards) {
      g.setColor(card.color);
      g.fillRoundRect(card.x, card.y, card.width, card.height, 15, 15);
      g.setColor(Color.BLACK);
      g.drawRoundRect(card.x, card.y, card.width, card.height, 15, 15);
      String levelText = "Niv " + card.level;
      g.drawString(levelText, card.x + 10, card.y + 25);
    }
  }

  private static void drawCoins(Graphics2D g, List<CoinStack> coins) {
    for (CoinStack stack : coins) {
      for (int i = 0; i < stack.count; i++) {
        int y = stack.y - i * 22;
        g.setColor(stack.color);
        g.fillOval(stack.x, y, 40, 40);
        g.setColor(Color.BLACK);
        g.drawOval(stack.x, y, 40, 40);
      }
    }
  }

  private static void drawPlayerInfo(Graphics2D g, Player player, int x, int yStart) {
    g.setColor(new Color(255, 255, 255, 50));
    g.fillRoundRect(x - 20, yStart - 40, 170, 250, 20, 20);
    g.setColor(Color.WHITE);
    g.setFont(new Font("SansSerif", Font.BOLD, 18));
    g.drawString(player.name, x, yStart);

    // Affiche les pièces du joueur
    int y = yStart + 30;
    for (CoinStack stack : player.coins) {
      g.setColor(stack.color);
      g.fillOval(x, y, 30, 30);
      g.setColor(Color.BLACK);
      g.drawOval(x, y, 30, 30);
      g.drawString(String.valueOf(stack.count), x + 12, y + 20);
      y += 40;
    }

    // Étoile et score
    g.setColor(Color.YELLOW);
    g.setFont(new Font("SansSerif", Font.BOLD, 24));
    g.drawString("★ " + player.prestige, x, y + 30);
  }
}
