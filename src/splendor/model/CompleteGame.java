package splendor.model;

import splendor.util.DevelopmentCardLoader;
import splendor.util.NobleLoader;
import java.io.InputStream;
import java.util.*;

public final class CompleteGame implements Game {
    private final List<Player> players;
    private final GemStock bank;
    private final Map<Integer, LinkedList<DevelopmentCard>> cardDecks;
    private final Map<Integer, List<DevelopmentCard>> displayedCards;
    private List<Noble> nobles;
    private final int playerCount;

    public CompleteGame(int playerCount) {
        if (playerCount < 2 || playerCount > 4) {
            throw new IllegalArgumentException("Player count must be between 2 and 4");
        }
        this.players = new ArrayList<>();
        this.cardDecks = new HashMap<>();
        this.displayedCards = new HashMap<>();
        this.playerCount = playerCount;
        this.bank = createBank(playerCount);
    }

    private GemStock createBank(int playerCount) {
        var regularTokens = switch (playerCount) {
            case 2 -> 4;
            case 3 -> 5;
            case 4 -> 7;
            default -> 0;
        };
        return new GemStock(regularTokens, 5);
    }

    public void initializeGame() {
        initializeCards();
        initializeNobles();
    }

    public void initializeNobles() {
        try (InputStream is = getClass().getResourceAsStream("/splendor/resources/nobles.csv")) {
            nobles = NobleLoader.loadNoblesFromInputStream(is);
            Collections.shuffle(nobles);
            nobles = new ArrayList<>(nobles.subList(0, playerCount + 1));
        } catch (Exception e) {
            throw new RuntimeException("Error loading nobles", e);
        }
    }

    public void initializeCards() {
        try (InputStream is = getClass().getResourceAsStream("/splendor/resources/cards.csv")) {

            List<DevelopmentCard> cards = DevelopmentCardLoader.loadCardsFromInputStream(is);

            cardDecks.put(1, new LinkedList<>());
            cardDecks.put(2, new LinkedList<>());
            cardDecks.put(3, new LinkedList<>());

            for (DevelopmentCard card : cards) {
                Objects.requireNonNull(card, "Card cannot be null");
                cardDecks.get(card.level()).add(card);
            }

            cardDecks.values().forEach(Collections::shuffle);

            displayedCards.put(1, new ArrayList<>(cardDecks.get(1).subList(0, 4)));
            displayedCards.put(2, new ArrayList<>(cardDecks.get(2).subList(0, 4)));
            displayedCards.put(3, new ArrayList<>(cardDecks.get(3).subList(0, 4)));

            cardDecks.get(1).subList(0, 4).clear();
            cardDecks.get(2).subList(0, 4).clear();
            cardDecks.get(3).subList(0, 4).clear();

        } catch (Exception e) {
            throw new RuntimeException("Error loading cards", e);
        }
    }

    public GemStock getBank() {
        return bank;
    }

    @Override
    public List<Integer> getAmountsOfCardByLevel() {
        Objects.requireNonNull(displayedCards, "displayedCards cannot be null");

        return cardDecks.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getValue().size())
                .toList().reversed();
    }

    @Override
    public List<DevelopmentCard> getDisplayedCards() {
        List<DevelopmentCard> allCards = new ArrayList<>();
        for (List<DevelopmentCard> cards : displayedCards.values()) {
            Objects.requireNonNull(cards, "Displayed cards list cannot be null");
            allCards.addAll(cards);
        }
        return Collections.unmodifiableList(allCards);
    }

    public List<Noble> getNobles() {
        Objects.requireNonNull(nobles, "nobles list cannot be null");
        return Collections.unmodifiableList(nobles);
    }

    public void addPlayer(String name) {
        Objects.requireNonNull(name, "Player name cannot be null");

        players.add(new Player(name));
    }

    @Override
    public List<Player> getPlayers() {
        Objects.requireNonNull(players, "players list cannot be null");
        return players;
    }

    @Override
    public void replaceCard(DevelopmentCard card) {
        Objects.requireNonNull(card, "Card to replace cannot be null");

        for (List<DevelopmentCard> cards : displayedCards.values()) {
            Objects.requireNonNull(cards, "Displayed cards list cannot be null");

            if (cards.remove(card)) {
                int level = card.level();
                if (!cardDecks.get(level).isEmpty()) {
                    DevelopmentCard newCard = cardDecks.get(level).removeFirst();
                    Objects.requireNonNull(newCard, "New card from deck cannot be null");
                    cards.add(newCard);
                }
                break;
            }
        }
    }

    @Override
    public void removeTokenFromBank(GemToken gem, int amount) {
        Objects.requireNonNull(gem, "Gem token cannot be null");

        if (amount < 0) {
            throw new IllegalArgumentException("Amount of tokens to remove must not be negative");
        }

        bank.remove(gem, amount);
    }
}