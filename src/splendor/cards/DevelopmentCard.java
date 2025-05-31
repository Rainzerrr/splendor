package splendor.cards;

import splendor.tokens.GemToken;

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

    /**
     * Converts the development card to a string, in the format "[Price: [X(x), Y(y), ...], Bonus: Z, Prestige: W]"
     * where X(x), Y(y), ... are the gem tokens and their respective costs, Z is the bonus gem token, and W is the prestige score.
     *
     * @return A string representation of the development card.
     */
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
