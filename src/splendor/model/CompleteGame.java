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
                cardDecks.get(card.level()).add(card);
            }

            cardDecks.values().forEach(Collections::shuffle);

            displayedCards.put(1, new ArrayList<>(cardDecks.get(1).subList(0, 4)));
            displayedCards.put(2, new ArrayList<>(cardDecks.get(2).subList(0, 4)));
            displayedCards.put(3, new ArrayList<>(cardDecks.get(3).subList(0, 4)));

            // Remove displayed cards from decks
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
        return cardDecks.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // pour garantir l'ordre des niveaux
                .map(entry -> entry.getValue().size()) // on prend la taille de chaque deck
                .toList().reversed(); // Java 16+ ; sinon utilise collect(Collectors.toList())
    }

    @Override
    public List<DevelopmentCard> getDisplayedCards() {
        List<DevelopmentCard> allCards = new ArrayList<>();
        displayedCards.values().forEach(allCards::addAll);
        return Collections.unmodifiableList(allCards);
    }

    public List<Noble> getNobles() {
        return Collections.unmodifiableList(nobles);
    }

    public void addPlayer(String name) {
        players.add(new Player(name));
    }

    @Override
    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public void replaceCard(DevelopmentCard card) {
        for (List<DevelopmentCard> cards : displayedCards.values()) {
            if (cards.remove(card)) {
                int level = card.level();
                if (!cardDecks.get(level).isEmpty()) {
                    DevelopmentCard newCard = cardDecks.get(level).removeFirst();
                    cards.add(newCard);
                }
                break;
            }
        }
    }

    @Override
    public void removeTokenFromBank(GemToken gem, int amount) {
        bank.remove(gem, amount);
    }
}
