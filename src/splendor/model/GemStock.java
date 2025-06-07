package splendor.model;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Objects;

/*
 * GemStack class
 */
public class GemStock {
    private final EnumMap<GemToken, Integer> gems;

    public GemStock(int initialAmount, int goldAmount) {
        if (initialAmount < 0 || goldAmount < 0) {
            throw new IllegalArgumentException("Amounts cannot be negative");
        }

        this.gems = new EnumMap<>(GemToken.class);

        Arrays.stream(GemToken.values())
                .filter(token -> token != GemToken.GOLD)
                .forEach(token -> gems.put(token, initialAmount));

        gems.put(GemToken.GOLD, goldAmount);
    }

    /**
     * Returns the current amount of gems for the specified token.
     *
     * @param token the type of gem token to query
     * @return the number of gems of the specified type, or 0 if none exist
     * @throws NullPointerException if the token is null
     */
    public int getAmount(GemToken token) {
        Objects.requireNonNull(token);
        return gems.getOrDefault(token, 0);
    }

    /**
     * Adds the specified amount of gems to the current amount of the specified token.
     *
     * @param token the type of gem token to add to
     * @param amount the number of gems of the specified type to add
     * @throws NullPointerException if the token is null
     * @throws IllegalArgumentException if the amount is negative
     */
    public void add(GemToken token, int amount) {
        Objects.requireNonNull(token);
        if (amount < 0) {
            throw new IllegalArgumentException("Le montant des gemmes à ajouter doit être positif");
        }
        gems.put(token, gems.getOrDefault(token, 0) + amount);
    }

    /**
     * Removes the specified amount of gems from the current amount of the specified token.
     *
     * @param token  the type of gem token to remove from
     * @param amount the number of gems of the specified type to remove
     * @throws NullPointerException     if the token is null
     * @throws IllegalArgumentException if the amount is negative
     */
    public boolean remove(GemToken token, int amount){
        Objects.requireNonNull(token);
        if (amount > getAmount(token)) {
            return false;
        }
        gems.put(token, gems.getOrDefault(token, 0) - amount);
        return true;
    }

    /**
     * Returns a string representation of the object. In the format of
     * "RED(1), GREEN(2), BLUE(3), WHITE(4), GOLD(5)".
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        var builder = new StringBuilder();
        var i = 0;
        for (var entry : gems.entrySet()) {
            if (i++ > 0) builder.append(", ");
            builder.append(entry.getKey().name())
                    .append("(").append(entry.getValue()).append(")");
        }
        return builder.toString();
    }
}
