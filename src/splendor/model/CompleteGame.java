package splendor.model;

import splendor.app.Main;
import splendor.util.DevelopmentCardLoader;
import splendor.util.NobleLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class CompleteGame implements Game {
    private final List<Player> players;
    private final GemStock bank;
    private final Map<Integer, List<DevelopmentCard>> cardDecks;
    private final Map<Integer, List<DevelopmentCard>> displayedCards;
    private List<Noble> nobles;
    private boolean gameOver;

    public CompleteGame(int numberOfPlayers) {
        if (numberOfPlayers < 2 || numberOfPlayers > 4) {
            throw new IllegalArgumentException("Le nombre de joueurs doit etre compris entre 2 et 4.");
        }
        players = new ArrayList<>();
        cardDecks = new HashMap<>();
        displayedCards = new HashMap<>();
        gameOver = false;

        int regularTokens;
        final int goldTokens = 5;

        switch (numberOfPlayers) {
            case 2 -> regularTokens = 4;
            case 3 -> regularTokens = 5;
            case 4 -> regularTokens = 7;
            default -> throw new IllegalArgumentException("Nombre de joueurs invalide : " + numberOfPlayers);
        }

        bank = new GemStock(regularTokens, goldTokens);
    }

    /**
     * Initializes the nobles for the game.
     *
     * This method loads nobles from a CSV file located at the specified path.
     * The nobles are shuffled and a subset is selected based on the number of players.
     * If the CSV file cannot be found, an exception is thrown.
     *
     * @throws RuntimeException if the CSV file is not found
     */
    public void initializeNobles() {
        try (InputStream is = Main.class.getResourceAsStream("/splendor/resources/nobles.csv")) {
            if (is == null) {
                throw new RuntimeException("Fichier CSV introuvable ! Vérifiez qu'il est bien dans le même dossier que vos classes.");
            }
            nobles = NobleLoader.loadNoblesFromInputStream(is);
            Collections.shuffle(nobles);
            nobles = nobles.subList(0, players.size()+1);
        } catch (IOException e) {
            System.err.println("Erreur de lecture : " + e.getMessage());
        }
    }

    /**
     * Initializes the development cards in the game.
     * This method loads the development cards from the CSV file "cards.csv"
     * and shuffles them by level. The first four cards of each level are
     * added to the game state as the initially displayed cards.
     */
    @Override
    public void initializeCards() {
        try (InputStream is = Main.class.getResourceAsStream("/splendor/resources/cards.csv")) {

            if (is == null) {
                throw new RuntimeException("Fichier CSV introuvable ! Vérifiez qu'il est bien dans le même dossier que vos classes.");
            }

            List<DevelopmentCard> cards = DevelopmentCardLoader.loadCardsFromInputStream(is);

            cardDecks.put(1, cards.stream().filter(c -> c.level() == 1).collect(Collectors.toList()));
            cardDecks.put(2, cards.stream().filter(c -> c.level() == 2).collect(Collectors.toList()));
            cardDecks.put(3, cards.stream().filter(c -> c.level() == 3).collect(Collectors.toList()));
            cardDecks.values().forEach(Collections::shuffle);
            displayedCards.put(1, new ArrayList<>(cardDecks.get(1).subList(0, 4)));
            displayedCards.put(2, new ArrayList<>(cardDecks.get(2).subList(0, 4)));
            displayedCards.put(3, new ArrayList<>(cardDecks.get(3).subList(0, 4)));

        } catch (IOException e) {
            System.err.println("Erreur de lecture : " + e.getMessage());
        }
    }


    /**
     * Shows the development cards that are currently available in the game.
     * The cards are grouped by level and displayed with their index.
     * The index is used to refer to the card when buying or reserving it.
     */
    @Override
    public void showCards() {
        showHeader("CARTES DISPONIBLES");
        int compteur = 1; // Initialisation du compteur

        for (Map.Entry<Integer, List<DevelopmentCard>> entry : displayedCards.entrySet()) {
            System.out.printf("--- Niveau %d ---\n", entry.getKey());

            for (DevelopmentCard card : entry.getValue()) {
                System.out.printf("%2d - %s\n", compteur++, card); // Incrémente le compteur
            }
        }
        System.out.println();
    }

    /**
     * Shows the nobles present in the game, one per line.
     */
    private void showNobles() {
        showHeader("Nobles de la partie");
        nobles.stream().map(Object::toString).forEach(System.out::println);
        System.out.println();
    }

    /**
     * Launches the complete game mode.
     * Initializes the cards and nobles, displays them, and handles the game loop.
     * The game continues until a player reaches a prestige score of 15.
     * Each player's turn consists of displaying their status, showing available actions,
     * and allowing them to claim a noble if eligible. The final ranking of players is displayed
     * once the game is over.
     */
    @Override
    public void launch() {
        initializeCards();
        initializeNobles();

        showCards();
        showNobles();

        while (!gameOver) {
            for (var current : players) {
                System.out.println(current.toString() + "\n");
                showMenu(current);
                current.claimNobleIfEligible(nobles);
                if (current.getPrestigeScore() >= 15) {
                    gameOver = true;
                }
                System.out.println();
                System.out.println("----------------------------------------\n");
            }
        }
        showFinalRanking(players);
    }

    /**
     * Allows a player to buy a development card from the displayed cards.
     * The player can choose a card to buy or cancel the purchase.
     * If a card is successfully bought, it is removed from the display,
     * and the displayed cards are updated with a new card from the deck if available.
     *
     * @param p the player who is buying the card, must not be null
     * @return true if the purchase is successful, false otherwise
     * @throws NullPointerException if the player is null
     */
    private boolean buyCard(Player p) {
        Objects.requireNonNull(p);
        if (displayedCards.isEmpty()) {
            System.out.println("Aucune carte n'est actuellement proposée.");
            showMenu(p);
            return false;
        }

        p.showWallet();
        showHeader("CARTES DISPONIBLES À L'ACHAT");
        showCards();

        while (true) {
            int indice = askInt("Indice de la carte (1-12, 0 pour annuler) : ", 0, 12);

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

    /**
     * Allows a player to reserve a development card from the displayed cards.
     * The player can choose a card to reserve or cancel the reservation.
     * If a card is successfully reserved, it is removed from the display,
     * and the displayed cards are updated with a new card from the deck if available.
     *
     * @param p the player who is reserving the card, must not be null
     * @return true if the reservation is successful, false otherwise
     * @throws NullPointerException if the player is null
     */
    private boolean reserveCard(Player p) {
        Objects.requireNonNull(p, "Le joueur ne peut pas être null");

        boolean noCardsAvailable = displayedCards.values().stream()
                .allMatch(List::isEmpty);

        if (noCardsAvailable) {
            System.out.println("Aucune carte disponible à la réservation.");
            showMenu(p);
            return false;
        }

        showCards();

        int totalCards = displayedCards.values().stream()
                .mapToInt(List::size)
                .sum();

        int choice = askInt("Indice (1-%d, 0 pour annuler) : ".formatted(totalCards), 0, totalCards);

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

                    // Laisse Player gérer la réservation + jetons or
                    boolean success = p.reserveCard(selectedCard, bank);

                    if (!success) {
                        System.out.println("Réservation échouée.");
                        cards.add(i, selectedCard); // remet la carte si échec
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

    /**
     * Updates the displayed cards for each level.
     * Ensures that each level has exactly four cards displayed.
     * If a level has fewer than four cards, it draws additional cards from the corresponding deck
     * until the display is full or the deck is empty.
     */
    private void updateDisplayedCards() {
        for (int level : Arrays.asList(1, 2, 3)) {
            List<DevelopmentCard> currentDisplay = displayedCards.get(level);
            List<DevelopmentCard> deck = cardDecks.get(level);

            while (currentDisplay.size() < 4 && !deck.isEmpty()) {
                currentDisplay.add(deck.removeFirst());
            }
        }
    }

    /**
     * Shows the menu of actions to the player.
     * The menu lists the available actions: buying a card, reserving a card, taking two identical gems,
     * taking three different gems, and displaying the nobles, cards on the board, and the bank's content.
     * The player is then prompted to choose an action.
     * If the chosen action is valid, the action is executed and the menu is exited.
     * If the chosen action is invalid, an error message is displayed and the menu is displayed again.
     * This method calls itself recursively until a valid action is chosen.
     *
     * @param player the player who is shown the menu, must not be null
     * @throws NullPointerException if the player is null
     */
    @Override
    public void showMenu(Player player) {
        Objects.requireNonNull(player);
        showHeader("ACTIONS DISPONIBLES");
        System.out.println("Actions : 1. Acheter | 2. Réserver | 3. 2 gemmes identiques | 4. 3 gemmes différentes");
        System.out.println("Afficher : 5. Nobles | 6. Cartes sur le plateau | 7. Contenu de la banque");
        while (true) {
            var action = askInt("Votre choix : ", 0, 7);
            System.out.println();

            boolean actionSuccess = false;
            switch (action) {
                case 1 -> actionSuccess = buyCard(player);
                case 2 -> actionSuccess = reserveCard(player); // reserveCard()
                case 3 -> actionSuccess = pickTwiceSameGem(player, bank);
                case 4 -> actionSuccess = pickThreeDifferentGems(player, bank);
                case 5 -> showNobles();
                case 6 -> showCards();
                case 7 -> showBank(bank);
                default -> System.out.println("Option invalide. Veuillez choisir un nombre entre 1 et 7.\n");
            }

            if (actionSuccess) {
                return; // Action valide, on quitte le menu
            } else {
                showMenu(player);
                return;
            }
        }
    }

    /**
     * Returns the list of players in the game.
     *
     * @return an unmodifiable list of players
     */
    @Override
    public List<Player> getPlayers(){
        return players;
    }
}