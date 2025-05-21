package splendor.util;

import splendor.tokens.GemToken;
import splendor.cards.Noble;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class NobleLoader {

    public static List<Noble> loadNoblesFromInputStream(InputStream is) throws IOException {
        List<Noble> nobles = new ArrayList<>();
        StringBuilder fileContent = new StringBuilder();

        // Lecture du fichier via InputStream
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            fileContent.append(new String(buffer, 0, bytesRead));
        }

        // Découpage des lignes
        String[] lines = fileContent.toString().split("\n");

        // Traitement (en sautant l'en-tête)
        for (int i = 1; i < lines.length; i++) {
            Noble noble = parseNobleLine(lines[i]);
            if (noble != null) {
                nobles.add(noble);
            }
        }

        return nobles;
    }

    private static Noble parseNobleLine(String line) {
        String[] tokens = line.split(",");
        if (tokens.length != 7) return null;

        try {
            String name = tokens[0].trim();
            int prestige = Integer.parseInt(tokens[1].trim());

            EnumMap<GemToken, Integer> price = new EnumMap<>(GemToken.class);
            parseTokenCost(price, GemToken.DIAMOND, tokens[2]);
            parseTokenCost(price, GemToken.SAPPHIRE, tokens[3]);
            parseTokenCost(price, GemToken.EMERALD, tokens[4]);
            parseTokenCost(price, GemToken.RUBY, tokens[5]);
            parseTokenCost(price, GemToken.ONYX, tokens[6]);

            return new Noble(name, price, prestige);
        } catch (Exception e) {
            System.err.println("Erreur parsing ligne noble: " + line);
            return null;
        }
    }

    private static void parseTokenCost(EnumMap<GemToken, Integer> price,
                                       GemToken token, String value) {
        int cost = Integer.parseInt(value.trim());
        if (cost > 0) {
            price.put(token, cost);
        }
    }
}