import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public record DevelopmentCard(EnumMap<GemToken, Integer> price, GemToken bonus, Integer prestigeScore) {
    public DevelopmentCard{
        if(prestigeScore < 0){
            throw new IllegalArgumentException("The prestige score must be positive");
        }
        Objects.requireNonNull(price);
        Objects.requireNonNull(bonus);
    }

    @Override
    public String toString() {
        String priceStr = price.entrySet().stream()
                .map(entry -> entry.getValue() + " " + entry.getKey())
                .collect(Collectors.joining(", ", "{", "}"));

        return "[Price: " + priceStr +
                ", Bonus: " + bonus +
                ", Prestige: " + prestigeScore + "]";
    }
}