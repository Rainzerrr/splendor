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
            throw new IllegalArgumentException("The amount of gems to add must be positive");
        }
        gems.put(token, gems.getOrDefault(token, 0) + amount);
    }

    public void remove(GemToken token, int amount){
        Objects.requireNonNull(token);
        if (amount > getAmount(token)) {
            throw new IllegalArgumentException("The amount of gems to remove is greater than the amount in the GemStack");
        }
        gems.put(token, gems.getOrDefault(token, 0) - amount);
    }

    public void resetGame() {
        gems.replaceAll((token, amount) -> 0);
    }

    public Map<GemToken, Integer> getGems() {
        return Map.copyOf(gems);
    }
}
