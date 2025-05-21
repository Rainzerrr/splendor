import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public record DevelopmentCard(EnumMap<GemToken, Integer> price, GemToken bonus, int prestigeScore, int level) {
    public DevelopmentCard{
        if(prestigeScore < 0){
            throw new IllegalArgumentException("The prestige score must be positive");
        }
        Objects.requireNonNull(price);
        Objects.requireNonNull(bonus);
    }

    @Override
    public String toString() {
        StringBuilder priceBuilder = new StringBuilder("[");

        boolean first = true;
        for (Map.Entry<GemToken, Integer> entry : price.entrySet()) {
            if (!first) {
                priceBuilder.append(", ");
            }
            priceBuilder.append(entry.getKey()).append("(").append(entry.getValue()).append(")");
            first = false;
        }
        priceBuilder.append("]");

        return "[Price: " + priceBuilder +
                ", Bonus: " + bonus +
                ", Prestige: " + prestigeScore + "]";
    }
}
