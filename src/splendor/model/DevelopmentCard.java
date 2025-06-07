package splendor.model;

import java.util.EnumMap;
import java.util.Objects;

public record DevelopmentCard(EnumMap<GemToken, Integer> price, GemToken bonus, int prestigeScore, int level) {
    public DevelopmentCard{
        if(prestigeScore < 0){
            throw new IllegalArgumentException("The prestige score must be positive");
        }
        Objects.requireNonNull(price);
        Objects.requireNonNull(bonus);
    }

    /**
     * Returns a string representation of the DevelopmentCard object.
     * The format includes the price as a list of GemToken types with their counts,
     * the bonus GemToken, and the prestige score.
     *
     * @return a string in the format "[Price: [GemToken(count), ...], Bonus: GemToken, Prestige: int]"
     */
    @Override
    public String toString() {
        var priceBuilder = new StringBuilder("[");
        var first = true;
        for (var entry : price.entrySet()) {
            if (!first) priceBuilder.append(", ");
            priceBuilder.append(entry.getKey()).append("(").append(entry.getValue()).append(")");
            first = false;
        }
        priceBuilder.append("]");

        return "[Price: " + priceBuilder +
                ", Bonus: " + bonus +
                ", Prestige: " + prestigeScore + "]";
    }
}
