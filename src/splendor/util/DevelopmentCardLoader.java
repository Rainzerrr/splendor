package splendor.util;

import splendor.model.DevelopmentCard;
import splendor.model.GemToken;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

public class DevelopmentCardLoader {

    public static List<DevelopmentCard> loadCardsFromInputStream(InputStream is) throws IOException {
        List<DevelopmentCard> cards = new ArrayList<>();
        StringBuilder fileContent = new StringBuilder();

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            fileContent.append(new String(buffer, 0, bytesRead));
        }

        String[] lines = fileContent.toString().split("\n");

        for (int i = 1; i < lines.length; i++) {
            DevelopmentCard card = parseCardLine(lines[i]);
            if (card != null) {
                cards.add(card);
            }
        }

        return cards;
    }

    private static DevelopmentCard parseCardLine(String line) {
        Objects.requireNonNull(line, "Line cannot be null");
        String[] tokens = line.split(",");
        if (tokens.length != 9) return null;

        try {
            int level = Integer.parseInt(tokens[0].trim());
            GemToken bonus = GemToken.valueOf(tokens[1].trim());
            int prestige = Integer.parseInt(tokens[2].trim());

            EnumMap<GemToken, Integer> price = new EnumMap<>(GemToken.class);
            parseTokenCost(price, GemToken.DIAMOND, tokens[3]);
            parseTokenCost(price, GemToken.SAPPHIRE, tokens[4]);
            parseTokenCost(price, GemToken.EMERALD, tokens[5]);
            parseTokenCost(price, GemToken.RUBY, tokens[6]);
            parseTokenCost(price, GemToken.ONYX, tokens[7]);

            String imageUrl = tokens[8].trim();

            return new DevelopmentCard(price, bonus, prestige, level, imageUrl);
        } catch (Exception e) {
            System.err.println("Erreur parsing ligne: " + line);
            return null;
        }
    }

    private static void parseTokenCost(EnumMap<GemToken, Integer> price,
                                       GemToken token, String value) {
        Objects.requireNonNull(price, "Price map cannot be null");
        Objects.requireNonNull(token, "GemToken cannot be null");
        Objects.requireNonNull(value, "Token value string cannot be null");
        int cost = Integer.parseInt(value.trim());
        if (cost > 0) {
            price.put(token, cost);
        }
    }
}