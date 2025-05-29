package splendor.tokens;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/*
 * GemStack class
 */
public class GemStack {
    private final EnumMap<GemToken, Integer> gems;

    public GemStack(int initialAmount) {
        this.gems = new EnumMap<GemToken, Integer>(GemToken.class);
        for (GemToken token : GemToken.values()) {
            gems.put(token, initialAmount);
        }
    }

    public int getAmount(GemToken token) {
        Objects.requireNonNull(token);
        return gems.getOrDefault(token, 0);
    }

    public void add(GemToken token, int amount) {
        Objects.requireNonNull(token);
        if (amount < 0) {
            throw new IllegalArgumentException("Le montant des gemmes à ajouter doit être positif");
        }
        gems.put(token, gems.getOrDefault(token, 0) + amount);
    }

    public boolean remove(GemToken token, int amount){
        Objects.requireNonNull(token);
        if (amount > getAmount(token)) {
            throw new IllegalArgumentException("The amount of gems to remove is greater than the amount in the GemStack");
        }
        gems.put(token, gems.getOrDefault(token, 0) - amount);
        return true;
    }

    public void resetGame() {
        gems.replaceAll((token, amount) -> 0);
    }

    public Map<GemToken, Integer> getGems() {
        return Map.copyOf(gems);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Map.Entry<GemToken, Integer> entry : gems.entrySet()) {
            if (i++ > 0) {
                builder.append(", ");
            }
            builder.append(entry.getKey().name())
                    .append("(")
                    .append(entry.getValue()).append(")");
        }

        return builder.toString();
    }


    /** Retourne {@code true} si la pile contient au moins les quantités
     *  demandées pour chaque couleur du coût ; {@code false} sinon. */
    public boolean canAfford(Map<GemToken,Integer> cost) {
        for (var e : cost.entrySet()) {
            if (getAmount(e.getKey()) < e.getValue())
                return false;
        }
        return true;
    }

    /** Décrémente la pile selon le coût.  Lève une
     *  {@link IllegalStateException} si la pile est insuffisante. */
    public boolean pay(Map<GemToken,Integer> cost) {
        if (!canAfford(cost)) {
            throw new IllegalStateException("Pas assez de gemmes pour payer");
        }
        cost.forEach(this::remove);
        return true;
    }
}
