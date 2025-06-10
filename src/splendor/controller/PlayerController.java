package splendor.controller;

import splendor.model.*;
import splendor.view.PlayerView;

import java.util.Map;
import java.util.Objects;

public class PlayerController {
    private final PlayerView view;

    public PlayerController(PlayerView view) {
        Objects.requireNonNull(view, "view cannot be null");
        this.view = view;
    }

    public boolean buyCard(Player player, DevelopmentCard card, GemStock bank) {
        Objects.requireNonNull(player, "player cannot be null");
        Objects.requireNonNull(card, "card cannot be null");
        Objects.requireNonNull(bank, "bank cannot be null");

        Map<GemToken, Integer> missing = player.calculateMissingGems(card);
        var totalGoldNeeded = missing.values().stream().mapToInt(Integer::intValue).sum();

        if (totalGoldNeeded == 0) {
            player.payForCard(card, bank);
            view.showPurchaseSuccess(card);
            return true;
        }

        int goldAvailable = player.getGold();
        if (goldAvailable < totalGoldNeeded) {
            view.showNotEnoughGold();
            return false;
        }

        view.showMissingGems(missing, totalGoldNeeded);
        Map<GemToken, Integer> goldReplacements = view.askGoldReplacements(missing, goldAvailable);
        int totalGoldToUse = goldReplacements.values().stream().mapToInt(Integer::intValue).sum();

        if (totalGoldToUse == 0) {
            view.showPurchaseCancelled();
            return false;
        }

        player.buyCardWithGold(card, bank, goldReplacements, totalGoldToUse);
        view.showGoldUsed(totalGoldToUse);
        view.showPurchaseSuccess(card);
        return true;
    }

    public boolean reserveCard(Player player, DevelopmentCard card, GemStock bank) {
        Objects.requireNonNull(player, "player cannot be null");
        Objects.requireNonNull(card, "card cannot be null");
        Objects.requireNonNull(bank, "bank cannot be null");

        boolean success = player.reserveCard(card, bank);
        if (success) {
            view.showReservationSuccess(card);
        } else {
            view.showReservationFailed();
        }
        return success;
    }

    public void takeTokens(Player player, Map<GemToken, Integer> tokens, GemStock bank) {
        Objects.requireNonNull(player, "player cannot be null");
        Objects.requireNonNull(tokens, "tokens cannot be null");
        Objects.requireNonNull(bank, "bank cannot be null");

        for (Map.Entry<GemToken, Integer> entry : tokens.entrySet()) {
            player.add(entry.getKey(), entry.getValue());
            bank.remove(entry.getKey(), entry.getValue());
        }
    }
}