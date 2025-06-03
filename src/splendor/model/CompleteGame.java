package splendor.model;

import splendor.util.DevelopmentCardLoader;
import splendor.util.NobleLoader;
import splendor.view.ConsoleInput;
import splendor.view.TerminalView;

import java.io.InputStream;
import java.util.*;

public class CompleteGame implements Game {
    private final List<Player> players;
    private final GemStock bank;
    private final Map<Integer, LinkedList<DevelopmentCard>> cardDecks;
    private final Map<Integer, List<DevelopmentCard>> displayedCards;
    private List<Noble> nobles;
    private boolean gameOver;
    private final int playerCount;
    private final TerminalView view;
    private final ConsoleInput consoleInput;

    public CompleteGame(int playerCount) {
        this.players = new ArrayList<>();
        this.cardDecks = new HashMap<>();
        this.displayedCards = new HashMap<>();
        this.gameOver = false;
        this.playerCount = playerCount;
        this.bank = createBank(playerCount);
        this.view = new TerminalView();
        this.consoleInput = new ConsoleInput();
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

    public boolean buyCard(Player p) {
        Objects.requireNonNull(p);
        if (displayedCards.isEmpty()) {
            System.out.println("Aucune carte n'est actuellement proposée.");
            return false;
        }

        p.showWallet();
        System.out.println("CARTES DISPONIBLES À L'ACHAT");
        displayedCards.values().forEach(list -> list.forEach(System.out::println));

        while (true) {
            int indice = consoleInput.askInt("Indice de la carte (1-12, 0 pour annuler) : ", 0, 12);

            if (indice == 0) {
                System.out.println("Achat annulé.\n");
                return false;
            }

            int level = (indice - 1) / 4 + 1;
            int position = (indice - 1) % 4;

            List<DevelopmentCard> levelCards = displayedCards.get(level);
            if (position >= levelCards.size()) {
                System.out.println("Cette carte n'est plus disponible, choisissez-en une autre.");
                continue;
            }

            DevelopmentCard chosen = levelCards.get(position);

            boolean success = p.buyCard(chosen, bank);

            if (success) {
                levelCards.remove(position);
                System.out.println("Carte achetée : " + chosen + "\n");

                if (!cardDecks.get(level).isEmpty()) {
                    DevelopmentCard newCard = cardDecks.get(level).removeFirst();
                    levelCards.add(newCard);
                    System.out.println("Nouvelle carte ajoutée dans la pioche : " + newCard);
                }

                return true;
            } else {
                System.out.println("Achat échoué. Veuillez réessayer.\n");
            }
        }
    }

    public boolean reserveCard(Player p) {
        Objects.requireNonNull(p, "Le joueur ne peut pas être null");

        boolean noCardsAvailable = displayedCards.values().stream()
                .allMatch(List::isEmpty);

        if (noCardsAvailable) {
            System.out.println("Aucune carte disponible à la réservation.");
            return false;
        }

        showCards();

        int totalCards = displayedCards.values().stream()
                .mapToInt(List::size)
                .sum();

        int choice = consoleInput.askInt("Indice (1-%d, 0 pour annuler) : ".formatted(totalCards), 0, totalCards);

        if (choice == 0) {
            System.out.println("Réservation annulée.\n");
            return false;
        }

        int currentIndex = 1;
        for (Map.Entry<Integer, List<DevelopmentCard>> entry : displayedCards.entrySet()) {
            List<DevelopmentCard> cards = entry.getValue();
            for (int i = 0; i < cards.size(); i++) {
                if (currentIndex == choice) {
                    DevelopmentCard selectedCard = cards.remove(i);

                    boolean success = p.reserveCard(selectedCard, bank);

                    if (!success) {
                        System.out.println("Réservation échouée.");
                        cards.add(i, selectedCard); // Remet la carte si échec
                    } else {
                        updateDisplayedCards();
                        System.out.printf("Réservation réussie : %s\n", selectedCard);
                    }
                    return success;
                }
                currentIndex++;
            }
        }

        System.out.println("Carte non trouvée.");
        return false;
    }

    private void updateDisplayedCards() {
        for (int level : Arrays.asList(1, 2, 3)) {
            List<DevelopmentCard> currentDisplay = displayedCards.get(level);
            LinkedList<DevelopmentCard> deck = cardDecks.get(level);

            while (currentDisplay.size() < 4 && !deck.isEmpty()) {
                currentDisplay.add(deck.removeFirst());
            }
        }
    }

    public GemStock getBank() {
        return bank;
    }

    @Override
    public List<DevelopmentCard> getDisplayedCards() {
        List<DevelopmentCard> allCards = new ArrayList<>();
        displayedCards.values().forEach(allCards::addAll);
        return Collections.unmodifiableList(allCards);
    }

    @Override
    public List<DevelopmentCard> getReservedCards() {
        // À implémenter selon la logique de stockage des réservations
        return List.of();
    }

    public List<Noble> getNobles() {
        return Collections.unmodifiableList(nobles);
    }

    @Override
    public boolean isGameOver() {
        return players.stream().anyMatch(p -> p.getPrestigeScore() >= 15);
    }

    public void addPlayer(String name) {
        players.add(new Player(name));
    }

    @Override
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
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

    // Affiche toutes les cartes affichées
    private void showCards() {
        System.out.println("Cartes affichées :");
        int index = 1;
        for (int level : Arrays.asList(1, 2, 3)) {
            List<DevelopmentCard> cards = displayedCards.get(level);
            for (DevelopmentCard card : cards) {
                System.out.printf("%2d: %s\n", index++, card);
            }
        }
    }

    @Override
    public boolean isCompleteGame() {
        return true;
    }
}
