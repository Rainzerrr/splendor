package splendor.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public record Noble(String name, EnumMap<GemToken, Integer> price, int prestigeScore, String imageUrl) {
    public Noble {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(price, "Price cannot be null");
        Objects.requireNonNull(imageUrl, "Image URL cannot be null");

        if (prestigeScore < 0) {
            throw new IllegalArgumentException("Prestige score must not be negative");
        }
    }

    @Override
    public String toString() {
        StringBuilder priceBuilder = new StringBuilder("[");
        boolean first = true;
        for (Map.Entry<GemToken, Integer> entry : price.entrySet()) {
            if (!first) priceBuilder.append(", ");
            priceBuilder.append(entry.getKey()).append("(").append(entry.getValue()).append(")");
            first = false;
        }
        priceBuilder.append("]");

        return "[Name: " + name + ", Requirement: " + priceBuilder +
                ", Prestige: " + prestigeScore + "]";
    }
}