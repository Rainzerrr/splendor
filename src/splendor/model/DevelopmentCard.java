package splendor.model;

import java.util.EnumMap;
import java.util.Objects;

public record DevelopmentCard(EnumMap<GemToken, Integer> price, GemToken bonus, int prestigeScore, int level, String imageUrl) {
    public DevelopmentCard {
        Objects.requireNonNull(price, "price cannot be null");
        Objects.requireNonNull(bonus, "bonus cannot be null");
        Objects.requireNonNull(imageUrl, "imageUrl cannot be null");

        if (prestigeScore < 0) {
            throw new IllegalArgumentException("prestigeScore must not be negative");
        }
        if (level < 1 || level > 3) {
            throw new IllegalArgumentException("level must be between 1 and 3");
        }
    }

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