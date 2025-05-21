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
        try (InputStream is = Main.class.getResourceAsStream("nobles.csv")) {
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
        try (InputStream is = Main.class.getResourceAsStream("cards.csv")) {

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

            for (DevelopmentCard carte : entry.getValue()) {
                System.out.printf("%2d - %s\n", compteur++, carte); // Incrémente le compteur
            }
        }
        System.out.println();
    }

    private void showNobles() {
        showHeader("Nobles de la partie");
        nobles.stream().map(Object::toString).forEach(System.out::println);
        System.out.println();
    }

    private void updateNoblesForPlayer(Player player) {
        // Calculer les bonus totaux du joueur
        EnumMap<GemToken, Integer> playerBonuses = player.getPurchasedCards().stream()
                .map(DevelopmentCard::bonus)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        () -> new EnumMap<>(GemToken.class),
                        Collectors.summingInt(b -> 1)
                ));

        Optional<Noble> acquiredNoble = nobles.stream()
                .filter(noble -> noble.price().entrySet().stream()
                        .allMatch(entry -> playerBonuses.getOrDefault(entry.getKey(), 0) >= entry.getValue()))
                .findFirst(); // ← On prend le premier au lieu de toList()

        acquiredNoble.ifPresent(noble -> {nobles.remove(noble);
            System.out.println("Vous venez d'acquérir le noble " + noble.name() + "\n");
        });
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
                updateNoblesForPlayer(current);
                if (current.getPrestigeScore() >= 15) {
                    gameOver = true;
                }
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

        showWallet(p);
        showHeader("CARTES DISPONIBLES À L'ACHAT");
        showCards(); // Devrait afficher les cartes numérotées de 1 à 12

        while (true) {
            try {
                int idx = askInt("Indice de la carte (1-12, 0 pour annuler) : ");

                // Annulation
                if (idx == 0) {
                    System.out.println("Achat annulé.\n");
                    return false;
                }

                // Validation de l'indice
                if (idx < 1 || idx > 12) {
                    System.out.println("Indice invalide, réessayez.");
                    continue;
                }
                int level = (idx - 1) / 4 + 1;
                int position = (idx - 1) % 4;

                List<DevelopmentCard> levelCards = displayedCards.get(level);
                if (position >= levelCards.size()) {
                    System.out.println("Cette carte n'est plus disponible, choisissez-en une autre.");
                    continue;
                }

                DevelopmentCard chosen = levelCards.get(position);

                if (!p.getWallet().canAfford(chosen.price())) {
                    System.out.println("Pas assez de gemmes pour cette carte, choisissez-en une autre.");
                    continue;
                }

                p.getWallet().pay(chosen.price());
                p.addPurchasedCard(chosen);
                levelCards.remove(position);
                System.out.println("Carte achetée : " + chosen + "\n");

                if (!cardDecks.get(level).isEmpty()) {
                    DevelopmentCard newCard = cardDecks.get(level).remove(0);
                    levelCards.add(newCard);
                    System.out.println("Nouvelle carte ajoutée : " + newCard);
                }

                return true;

            } catch (InputMismatchException e) {
                System.out.println("Erreur : Veuillez entrer un chiffre valide.");
            }
        }
    }

    @Override
    public void showMenu(Player player) {
        showHeader("ACTIONS DISPONIBLES");
        System.out.println("1. Acheter une carte disponible");
        System.out.println("2. Réserver une carte");
        System.out.println("3. Prendre deux gemmes de la même couleur");
        System.out.println("4. Prendre trois gemmes de couleurs différentes");
        while (true) {
            try {
                var action = askInt("Votre choix : ");
                System.out.println();

                boolean actionSuccess = false;
                switch (action) {
                    case 1 -> actionSuccess = buyCard(player);
                    case 2 -> actionSuccess = buyCard(player);
                    case 3 -> actionSuccess = pickTwiceSameGem(player, bank);
                    case 4 -> actionSuccess = pickThreeDifferentGems(player, bank);
                    default -> System.out.println("Option invalide. Veuillez choisir un nombre entre 1 et 4.\n");
                }

                if (actionSuccess) {
                    return; // Action valide, on quitte le menu
                } else {
                    System.out.println("Retour au menu.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Erreur : Veuillez entrer un chiffre valide.\n");
            }
        }
    }

    @Override
    public List<Player> getPlayers(){
        return players;
    }

}
