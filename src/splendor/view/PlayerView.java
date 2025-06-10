package splendor.view;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import splendor.model.DevelopmentCard;
import splendor.model.GemToken;
import splendor.model.Player;
import splendor.util.ConsoleInput;

public class PlayerView {
    private final Scanner scanner = new Scanner(System.in);
    private final ConsoleInput input = new ConsoleInput();

    public void showWallet(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        System.out.println(player);
    }

    public void showMissingGems(Map<GemToken, Integer> missingGems, int totalGoldNeeded) {
        Objects.requireNonNull(missingGems, "Missing gems map cannot be null");

        if (totalGoldNeeded < 0) {
            throw new IllegalArgumentException("Total gold needed must not be negative");
        }

        System.out.println("Détail des jetons manquants:");
        missingGems.forEach((gem, missing) ->
                System.out.printf("  - %d %s%n", missing, gem));
        System.out.printf("Total manquant : %d jeton(s)%n", totalGoldNeeded);
        System.out.println("Vous pouvez utiliser vos jetons or comme joker.");
    }

    public Map<GemToken, Integer> askGoldReplacements(Map<GemToken, Integer> missingGems, int goldAvailable) {
        Objects.requireNonNull(missingGems, "Missing gems map cannot be null");

        if (goldAvailable < 0) {
            throw new IllegalArgumentException("Gold available must not be negative");
        }

        Map<GemToken, Integer> replacements = new EnumMap<>(GemToken.class);
        var remainingGold = goldAvailable;

        for (Map.Entry<GemToken, Integer> entry : missingGems.entrySet()) {
            var gem = entry.getKey();
            var missing = entry.getValue();

            System.out.printf("Remplacer %d %s manquant(s) par de l'or? (1=oui/0=non) ", missing, gem);
            int choice = input.askInt("", 0, 1);

            if (choice == 1) {
                int toReplace = Math.min(missing, remainingGold);
                replacements.put(gem, toReplace);
                remainingGold -= toReplace;
            }
        }

        int totalReplaced = replacements.values().stream().mapToInt(Integer::intValue).sum();
        int totalMissing = missingGems.values().stream().mapToInt(Integer::intValue).sum();

        if (totalReplaced < totalMissing && remainingGold > 0) {
            int additional = Math.min(totalMissing - totalReplaced, remainingGold);
            System.out.printf("Voulez-vous utiliser %d jeton(s) or supplémentaire(s) ? (1=oui/0=non) ", additional);
            if (input.askInt("", 0, 1) == 1) {
                replacements.merge(GemToken.GOLD, additional, Integer::sum);
            }
        }

        return replacements;
    }

    /* ---- Display messages ---- */
    public void showPlayerInfo(Player p) {
        Objects.requireNonNull(p, "Player cannot be null");
        System.out.println(p);
    }

    public void showPurchaseSuccess(DevelopmentCard card) {
        Objects.requireNonNull(card, "Development card cannot be null");
        System.out.println("Carte achetée : " + card + "\n");
    }

    public void showReservationSuccess(DevelopmentCard card) {
        Objects.requireNonNull(card, "Development card cannot be null");
        System.out.printf("Réservation réussie : %s\n", card);
    }

    public void showNotEnoughGold() {
        System.out.println("Pas assez de jetons or disponibles !");
    }

    public void showPurchaseCancelled() {
        System.out.println("Achat annulé.");
    }

    public void showReservationFailed() {
        System.out.println("Réservation échouée. Vous avez déjà 3 cartes réservées.");
    }

    public void showGoldUsed(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount of gold used must not be negative");
        }
        System.out.printf("%d jeton(s) or utilisé(s).%n", amount);
    }

    public void showPurchasedCards(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        System.out.println("Cartes achetées :");
        player.getPurchasedCards().forEach(System.out::println);
    }

    public void showReservedCards(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        System.out.println("Cartes réservées :");
        player.getReservedCards().forEach(System.out::println);
    }
}