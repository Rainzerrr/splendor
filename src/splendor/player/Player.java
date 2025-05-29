package splendor.player;

import splendor.cards.DevelopmentCard;
import splendor.cards.Noble;
import splendor.tokens.GemStack;
import splendor.tokens.GemToken;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Player {
    private final String name;
    private final GemStack wallet;
    private final List<DevelopmentCard> reservedCards;
    private final List<DevelopmentCard> purchasedCards;
    private final List<Noble> acquiredNobles;

    public Player(String name) {
        Objects.requireNonNull(name);
        this.wallet = new GemStack(2);
        this.purchasedCards = new ArrayList<>();
        this.reservedCards = new ArrayList<>();
        this.name = name;
        this.acquiredNobles = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void showWallet() {
        System.out.println(wallet);
    }

    public int getPurchasedCardsCount() {
        return purchasedCards.size();  // retourne directement la taille de la liste des cartes achetées
    }

    // Added method
    /**
     * Tente de payer le prix demandé à partir du portefeuille du joueur.
     * @param price carte des gemmes à payer (GemToken -> quantité)
     * @return true si le paiement a réussi, false sinon.
     */
    public void pay(Map<GemToken, Integer> price) {
        wallet.pay(price);
    }

    public void addPurchasedCards(DevelopmentCard card) {
        Objects.requireNonNull(card);
        purchasedCards.add(card);
    }

    public int getPrestigeScore() {
        return Stream.concat(
                        purchasedCards.stream().map(DevelopmentCard::prestigeScore),
                        acquiredNobles.stream().map(Noble::prestigeScore)
                )
                .mapToInt(Integer::intValue)
                .sum();
    }

    public boolean reserveCard(DevelopmentCard card, GemStack bank) {
        Objects.requireNonNull(card);
        Objects.requireNonNull(bank);

        // Vérifie limite de réservation
        if (reservedCards.size() >= 3) {
            System.out.println("Limite de réservation atteinte (3 cartes).");
            return false;
        }

        reservedCards.add(card);

        // Donne un jeton or si possible
        if (bank.remove(GemToken.GOLD, 1)) {
            wallet.add(GemToken.GOLD, 1);
            System.out.println("Jeton or attribué !");
        }

        return true;
    }


    private int askInt(String prompt, int min, int max) {
        Objects.requireNonNull(prompt);
        var scanner = new Scanner(System.in);
        while (true) {
            System.out.print(prompt);
            try {
                int input = scanner.nextInt();
                if (input < min || input > max) {
                    System.out.println("Veuillez entrer un nombre entre " + min + " et " + max + ".");
                } else {
                    return input;
                }
            } catch (InputMismatchException e) {
                System.out.println("Erreur : veuillez entrer un nombre entier.");
                scanner.nextLine();
            }
        }
    }

    public boolean buyCard(DevelopmentCard card, GemStack bank) {
        Objects.requireNonNull(card);
        Objects.requireNonNull(bank);

        Map<GemToken, Integer> price = new EnumMap<>(card.price());

        int totalGoldNeeded = 0;
        Map<GemToken, Integer> missingGems = new EnumMap<>(GemToken.class);

        for (Map.Entry<GemToken, Integer> entry : price.entrySet()) {
            GemToken gem = entry.getKey();
            if (gem == GemToken.GOLD) continue;

            int required = entry.getValue();
            int owned = wallet.getAmount(gem);

            if (owned < required) {
                int missing = required - owned;
                missingGems.put(gem, missing);
                totalGoldNeeded += missing;
            }
        }

        int goldAvailable = wallet.getAmount(GemToken.GOLD);

        if (totalGoldNeeded == 0) {
            wallet.pay(price);
        } else if (goldAvailable >= totalGoldNeeded) {
            System.out.printf("Il vous manque %d jeton(s) pour payer cette carte.\n", totalGoldNeeded);
            System.out.println("Voulez-vous utiliser vos jetons or comme joker pour compenser ? (1=oui / 0=non)");

            int choice = askInt("Votre choix (1=oui / 0=non) : ", 0, 1);

            if (choice == 1) {
                for (Map.Entry<GemToken, Integer> missEntry : missingGems.entrySet()) {
                    GemToken gem = missEntry.getKey();
                    price.put(gem, price.get(gem) - missEntry.getValue());
                }

                wallet.pay(price);
                wallet.remove(GemToken.GOLD, totalGoldNeeded);
                bank.add(GemToken.GOLD, totalGoldNeeded);

                System.out.printf("%d jeton(s) or utilisés.\n", totalGoldNeeded);
            } else {
                System.out.println("Achat annulé, jetons or non utilisés.");
                return false;
            }
        } else {
            System.out.println("Pas assez de jetons, même avec jetons or.");
            return false;
        }

        purchasedCards.add(card);
        return true;
    }

    /**
     * Retire un nombre donné de jetons d'un type donné dans le portefeuille.
     *
     * @param gem    type de gemme (ex: GOLD)
     * @param amount nombre de jetons à retirer
     */
    public void remove(GemToken gem, int amount) {
        wallet.remove(gem, amount);
    }

    public void add(GemToken token, int amount) {
        wallet.add(token, amount);
        System.out.println(amount + " jeton(s) " + token + " ajouté(s) au portefeuille de " + getName());
    }

    public Optional<Noble> claimNobleIfEligible(List<Noble> nobles) {
        Objects.requireNonNull(nobles);

        EnumMap<GemToken, Integer> bonusMap = purchasedCards.stream()
                .map(DevelopmentCard::bonus)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        () -> new EnumMap<>(GemToken.class),
                        Collectors.summingInt(b -> 1)
                ));

        return nobles.stream()
                .filter(noble -> noble.price().entrySet().stream()
                        .allMatch(entry -> bonusMap.getOrDefault(entry.getKey(), 0) >= entry.getValue()))
                .findFirst();
    }

    public void receiveGold() {
        wallet.add(GemToken.GOLD, 1);
    }

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

    public void resetGame() {
        wallet.resetGame();
        purchasedCards.clear();
        reservedCards.clear();
        acquiredNobles.clear();
    }
}