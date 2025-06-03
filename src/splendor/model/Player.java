package splendor.model;

import splendor.view.ConsoleInput;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Player {
    private final String name;
    private final GemStock wallet;
    private final List<DevelopmentCard> reservedCards;
    private final List<DevelopmentCard> purchasedCards;
    private final List<Noble> acquiredNobles;
    private final ConsoleInput consoleInput;

    public Player(String name) {
        Objects.requireNonNull(name);
        this.wallet = new GemStock(2, 0);
        this.purchasedCards = new ArrayList<>();
        this.reservedCards = new ArrayList<>();
        this.name = name;
        this.acquiredNobles = new ArrayList<>();
        this.consoleInput = new ConsoleInput();
    }

    /**
     * @return the name of this player
     */
    public String getName() {
        return name;
    }

    /**
     * Shows the content of the player's wallet.
     */
    public void showWallet() {
        System.out.println(wallet);
    }

    /**
     * Returns the number of purchased cards.
     *
     * @return the number of purchased cards, as an int.
     */
    public int getPurchasedCardsCount() {
        return purchasedCards.size();
    }

    /**
     * Returns the total prestige score of the player, based on the prestige scores
     * of the purchased cards and acquired nobles.
     *
     * @return the total prestige score, as an int
     */
    public int getPrestigeScore() {
        return Stream.concat(
                        purchasedCards.stream().map(DevelopmentCard::prestigeScore),
                        acquiredNobles.stream().map(Noble::prestigeScore)
                )
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * Reserve a development card from the given bank.
     *
     * @param card the card to be reserved
     * @param bank the bank from which to reserve the card
     * @return true if the card was successfully reserved, false if the player
     * has already reached the maximum number of reserved cards (3)
     * @throws NullPointerException if either card or bank is null
     */
    public boolean reserveCard(DevelopmentCard card, GemStock bank) {
        Objects.requireNonNull(card);
        Objects.requireNonNull(bank);
        if (reservedCards.size() >= 3) {
            return false;
        }

        reservedCards.add(card);

        if (bank.getAmount(GemToken.GOLD) > 0) {
            bank.remove(GemToken.GOLD, 1);
            wallet.add(GemToken.GOLD, 1);
        }

        return true;
    }

    /**
     * Returns the number of gold tokens in the player's wallet.
     *
     * @return the amount of gold tokens, as an int
     */
    public int getGold() {
        return wallet.getAmount(GemToken.GOLD);
    }

    /**
     * Calculates the number of each gem token the player lacks to purchase a
     * given development card.
     *
     * @param card the development card to calculate the missing gems for
     * @return a map where the keys are the gem tokens needed and the values are
     * the number of each gem token that the player lacks.
     */
    public Map<GemToken, Integer> calculateMissingGems(DevelopmentCard card) {
        var price = card.price();
        Map<GemToken, Integer> missingGems = new EnumMap<>(GemToken.class);

        for (var entry : price.entrySet()) {
            int missing = entry.getValue() - wallet.getAmount(entry.getKey());
            if (missing > 0) {
                missingGems.put(entry.getKey(), missing);
            }
        }
        return missingGems;
    }


    /**
     * Buys a development card by transferring the corresponding gem tokens from the player's wallet to the given bank.
     * If the player lacks some gem tokens, the method will ask if the player wants to use gold tokens as wildcards to cover
     * the deficit. If the player agrees, the method will transfer the required number of gold tokens from the player's
     * wallet to the bank and add the card to the player's purchased cards.
     *
     * @param card the development card to be purchased
     * @param bank the bank from which to transfer tokens
     * @return true if the card was successfully purchased, false if the player cancelled the purchase
     * @throws NullPointerException if either card or bank is null
     */
    public boolean buyCard(DevelopmentCard card, GemStock bank) {
        Objects.requireNonNull(card);
        Objects.requireNonNull(bank);

        Map<GemToken, Integer> price = new EnumMap<>(card.price());

        Map<GemToken, Integer> missingGems = price.entrySet().stream()
                .filter(e -> wallet.getAmount(e.getKey()) < e.getValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue() - wallet.getAmount(e.getKey()),
                        (a, b) -> b,
                        () -> new EnumMap<>(GemToken.class)
                ));

        int totalGoldNeeded = missingGems.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        int goldAvailable = wallet.getAmount(GemToken.GOLD);

        if (totalGoldNeeded == 0) {
            price.forEach((gem, amount) -> {
                wallet.remove(gem, amount);
                bank.add(gem, amount);
            });
            purchasedCards.add(card);
            return true;
        }

        if (goldAvailable < totalGoldNeeded) {
            System.out.println("Pas assez de jetons, même avec les jetons or.");
            return false;
        }

        System.out.printf("Il vous manque %d jeton(s) pour payer cette carte.%n", totalGoldNeeded);
        int choice = consoleInput.askInt("Voulez-vous utiliser vos jetons or comme joker ? (1=oui / 0=non) : ", 0, 1);

        if (choice != 1) {
            System.out.println("Achat annulé.");
            return false;
        }

        price.forEach((gem, required) -> {
            int owned = wallet.getAmount(gem);
            int amountToPay = Math.min(required, owned);
            if (amountToPay > 0) {
                wallet.remove(gem, amountToPay);
                bank.add(gem, amountToPay);
            }
        });

        wallet.remove(GemToken.GOLD, totalGoldNeeded);
        bank.add(GemToken.GOLD, totalGoldNeeded);

        System.out.printf("%d jeton(s) or utilisé(s).%n", totalGoldNeeded);
        purchasedCards.add(card);
        return true;
    }

    /**
     * Pays for a development card by transferring the corresponding gem tokens
     * from the player's wallet to the bank and adding the card to the player's
     * purchased cards.
     *
     * @param card the development card to be purchased
     * @param bank the bank from which to transfer tokens
     * @throws NullPointerException if either {@code card} or {@code bank} is null
     */
    public void payForCard(DevelopmentCard card, GemStock bank) {
        for (Map.Entry<GemToken, Integer> entry : card.price().entrySet()) {
            GemToken gem = entry.getKey();
            int amount = entry.getValue();
            wallet.remove(gem, amount);
            bank.add(gem, amount);
        }
        purchasedCards.add(card);
    }

    /**
     * Purchases a development card using a combination of gem tokens and gold tokens.
     * The method deducts the required gem tokens from the player's wallet and uses
     * gold tokens as wildcards to cover any deficit. The corresponding tokens are
     * transferred from the player's wallet to the bank, and the card is added to the
     * player's purchased cards.
     *
     * @param card             the development card to be purchased
     * @param bank             the bank from which to transfer tokens
     * @param goldReplacements a map specifying how many gold tokens are used to replace each gem type
     * @param totalGoldToUse   the total number of gold tokens to use for the purchase
     * @throws NullPointerException if any of the arguments are null
     */
    public void buyCardWithGold(DevelopmentCard card, GemStock bank,
                                Map<GemToken, Integer> goldReplacements,
                                int totalGoldToUse) {
        for (Map.Entry<GemToken, Integer> entry : card.price().entrySet()) {
            GemToken gem = entry.getKey();
            int required = entry.getValue();
            int owned = wallet.getAmount(gem);
            int normalPayment = Math.min(required, owned);

            if (normalPayment > 0) {
                wallet.remove(gem, normalPayment);
                bank.add(gem, normalPayment);
            }
        }

        wallet.remove(GemToken.GOLD, totalGoldToUse);
        bank.add(GemToken.GOLD, totalGoldToUse);
        purchasedCards.add(card);
    }


    /**
     * Removes a given number of gems of a given type from the player's wallet.
     *
     * @param gem   the type of gem to remove
     * @param amount the number of gems to remove
     */
    public void remove(GemToken gem, int amount) {
        wallet.remove(gem, amount);
    }


    /**
     * Adds a given number of gems of a given type to the player's wallet.
     *
     * @param token  the type of gem to add
     * @param amount the number of gems to add
     */
    public void add(GemToken token, int amount) {
        Objects.requireNonNull(token);
        if (amount <= 0) {
            return;
        }
        wallet.add(token, amount);
        // System.out.println(amount + " jeton(s) " + token + " ajouté(s) au portefeuille de " + getName());
    }

    /**
     * Tries to claim a noble from the given list if the player is eligible to
     * claim it. A player is eligible to claim a noble if the player has at
     * least as many bonus tokens of a given type as the noble's price
     * specifies.
     *
     * @param nobles the list of nobles to try to claim from
     */
    public void claimNobleIfEligible(List<Noble> nobles) {
        Objects.requireNonNull(nobles);

        EnumMap<GemToken, Integer> bonusMap = purchasedCards.stream()
                .map(DevelopmentCard::bonus)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        () -> new EnumMap<>(GemToken.class),
                        Collectors.summingInt(b -> 1)
                ));

        Optional<Noble> eligibleNoble = nobles.stream()
                .filter(noble -> noble.price().entrySet().stream()
                        .allMatch(entry ->
                                bonusMap.getOrDefault(entry.getKey(), 0) >= entry.getValue())
                )
                .findFirst();

        if (eligibleNoble.isPresent()) {
            Noble noble = eligibleNoble.get();
            acquiredNobles.add(noble);
            nobles.remove(noble);
        }

    }

    /**
     * Returns a string representation of the player
     * The string representation shows the player's name, prestige score,
     * bonus tokens (if any), and the player's current gems.
     *
     * @return a string representation of the player
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[JOUEUR: ").append(name).append("] ");
        sb.append("Prestige: ").append(getPrestigeScore()).append(" | Bonus: ");

        if (purchasedCards.isEmpty()) {
            sb.append("Aucun");
        } else {
            Map<GemToken, Integer> bonusCounts = new HashMap<>();
            for (DevelopmentCard card : purchasedCards) {
                GemToken bonus = card.bonus();
                bonusCounts.put(bonus, bonusCounts.getOrDefault(bonus, 0) + 1);
            }
            boolean first = true;
            for (Map.Entry<GemToken, Integer> entry : bonusCounts.entrySet()) {
                if (!first) sb.append(" ");
                sb.append(entry.getKey().name()).append("(").append(entry.getValue()).append(")");
                first = false;
            }
        }

        sb.append("\nJetons: ").append(wallet);
        return sb.toString();
    }

    public List<DevelopmentCard> getPurchasedCards() {
        return Collections.unmodifiableList(purchasedCards);
    }

    public List<DevelopmentCard> getReservedCards() {
        return Collections.unmodifiableList(reservedCards);
    }

    public void removeReservedCard(DevelopmentCard card) {
        reservedCards.remove(card);
    }
}