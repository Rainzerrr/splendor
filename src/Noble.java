import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public record Noble(String name, EnumMap<GemToken, Integer> price, int prestigeScore) {
    public Noble{
        Objects.requireNonNull(name);
        Objects.requireNonNull(price);
        if(prestigeScore < 0){
            throw new IllegalArgumentException("The prestige score must be positive");
        }
    }

    @Override
    public String toString() {
        StringBuilder priceBuilder = new StringBuilder("{");

        boolean first = true;
        for (Map.Entry<GemToken, Integer> entry : price.entrySet()) {
            if (!first) {
                priceBuilder.append(", ");
            }
            priceBuilder.append(entry.getValue()).append(" ").append(entry.getKey());
            first = false;
        }
        priceBuilder.append("}");

        return name + "[Price: " + priceBuilder +
                ", Prestige: " + prestigeScore + "]";
    }
}
