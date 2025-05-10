import java.util.EnumMap;
import java.util.Objects;

public record DevelopmentCard(String name, EnumMap<GemToken, Integer> price, GemToken bonus, Integer prestigeScore) {
    public DevelopmentCard{
        if(prestigeScore < 0){
            throw new IllegalArgumentException("The prestige score must be positive");
        }
        Objects.requireNonNull(name);
        Objects.requireNonNull(price);
        Objects.requireNonNull(bonus);
    }
}
