package splendor.game;

import splendor.app.Main;
import splendor.cards.*;
import splendor.player.Player;
import splendor.tokens.GemStack;
import splendor.tokens.GemToken;
import splendor.util.DevelopmentCardLoader;
import splendor.util.NobleLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CompleteGame implements Game {
    private final List<Player> players;
    private final GemStack bank;
    private final Map<Integer, List<DevelopmentCard>> cardDecks;
    private final Map<Integer, List<DevelopmentCard>> displayedCards;
    private List<Noble> nobles;
    private boolean gameOver;

    public CompleteGame(int playerNumber) {
        players = new ArrayList<>();
        bank = new GemStack(7);
        cardDecks = new HashMap<>();
        displayedCards = new HashMap<>();
        gameOver = false;
    }

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

    @Override
    public void initializeCards() {
        try (InputStream is = Main.class.getResourceAsStream("/splendor/resources/cards.csv")) {

            if (is == null) {
                throw new RuntimeException("Fichier CSV introuvable ! Vérifiez qu'il est bien dans le même dossier que vos classes.");
            }

            List<DevelopmentCard> cards = DevelopmentCardLoader.loadCardsFromInputStream(is);

            // 2. Séparez par niveau et méllevel
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

    private void showNobles() {
        showHeader("Nobles de la partie");
        nobles.stream().map(Object::toString).forEach(System.out::println);
        System.out.println();
    }

    @Override
    public void launch() {
        initializeCards();
        initializeNobles();
        System.out.println("Let the game begin mode complet !\n");

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

    private boolean buyCard(Player p) {
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
                    System.out.println("Nouvelle carte ajoutée : " + newCard);
                }

                return true;
            } else {
                System.out.println("Achat échoué. Veuillez réessayer.\n");
            }
        }
    }

    private boolean reserveCard(Player p) {
        Objects.requireNonNull(p, "Le joueur ne peut pas être null");

        // Vérifie si au moins une carte est disponible
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

    private void updateDisplayedCards() {
        for (int level : Arrays.asList(1, 2, 3)) {
            List<DevelopmentCard> currentDisplay = displayedCards.get(level);
            List<DevelopmentCard> deck = cardDecks.get(level);

            while (currentDisplay.size() < 4 && !deck.isEmpty()) {
                currentDisplay.add(deck.removeFirst());
            }
        }
    }

    @Override
    public void showMenu(Player player) {
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

    @Override
    public List<Player> getPlayers(){
        return List.copyOf(players);
    }
}