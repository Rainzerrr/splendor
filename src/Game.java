import java.util.*;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Game {
    // Variables de classe
    private final List<Player> players;
    private final GemStack bank;
    private final List<DevelopmentCard> cards;
    private boolean gameOver;
    private ArrayList<DevelopmentCard> displayedCards;

    // Constructeur
    public Game() {
        players = new ArrayList<>();
        cards = new ArrayList<>();
        bank = new GemStack(0);
        gameOver = false;
    }

    // Méthodes publiques
    public void launch() {
        initializeGame();
        playGame();
        displayRanking();
    }

    public void initGemStack() {
        var playerCount = switch (players.size()) {
            case 2 -> 4;
            case 3 -> 5;
            default -> 7;
        };

        EnumSet.allOf(GemToken.class)
                .forEach(token ->
                        bank.add(token, token == GemToken.GOLD ? 5 : playerCount)
                );
    }

    public void addPlayer(Player player) {
        Objects.requireNonNull(player, "Le joueur ne peut pas être null");
        if (players.contains(player)) {
            throw new IllegalStateException("Joueur déjà existant");
        }
        players.add(player);
    }

    public void displayRanking() {
        players.sort(Comparator.comparingInt(Player::getPrestigeScore).reversed());

        IntStream.range(0, players.size())
                .forEach(i -> System.out.println((i + 1) + " - " + players.get(i)));
    }

    // Méthodes d'initialisation
    private void initializeGame() {
        initGemStack();
        players.sort(Comparator.comparingInt(Player::getAge));
        initializeCards();
        shuffleCards();
        displayedCards = new ArrayList<>(cards.subList(0, 4));
        System.out.println("Let the game begin !\n");
    }

    private void initializeCards() {
        Stream.<GemToken>of(GemToken.values())
                .flatMap(color -> IntStream.range(0, 8)
                        .mapToObj(i -> {
                            EnumMap<GemToken, Integer> cost = new EnumMap<>(GemToken.class);
                            cost.put(color, 3);
                            return new DevelopmentCard(cost, color, 1);
                        }))
                .forEach(cards::add);
    }

    private void shuffleCards() {
        Collections.shuffle(cards);
    }

    // Méthodes de jeu principales
    private void playGame() {
        try (var scanner = new Scanner(System.in)) {
            while (!gameOver) {
                for (var current : players) {
                    playerTurn(current);
                    if (current.getNbCards() >= 15) {
                        gameOver = true;
                        break;
                    }
                    System.out.println("----------------------------------------\n");
                }
            }
        }
    }

    private void playerTurn(Player current) {
        Objects.requireNonNull(current);
        displayBoard();
        System.out.println(current.toString() + "\n");
        showPlayerMenu(current);
        checkTokenLimit(current); // Ajout de la vérification des jetons
    }

    private void checkTokenLimit(Player player) {
        Objects.requireNonNull(player);
        if (player.getWallet().getGems().size() <= 10) {
            return;
        }

        System.out.println("\nVous avez plus de 10 jetons (" + player.getWallet().getGems().size() + "). Vous devez en défausser.");

        while (player.getWallet().getGems().size() > 10) {
            showWallet(player);
            System.out.println("Vous devez défausser " + (player.getWallet().getGems().size() - 10) + " jetons.");

            System.out.println("Choisissez un type de jeton à défausser :");
            System.out.println("1. RUBY - 2. EMERALD - 3. DIAMOND - 4. SAPPHIRE - 5. ONYX - 6. GOLD");

            int choice = askInt("Votre choix : ");
            GemToken tokenToDiscard = switch (choice) {
                case 1 -> GemToken.RUBY;
                case 2 -> GemToken.EMERALD;
                case 3 -> GemToken.DIAMOND;
                case 4 -> GemToken.SAPPHIRE;
                case 5 -> GemToken.ONYX;
                case 6 -> GemToken.GOLD;
                default -> {
                    System.out.println("Choix invalide, veuillez réessayer.");
                    yield null;
                }
            };

            if (tokenToDiscard != null && player.getWallet().getAmount(tokenToDiscard) > 0) {
                player.getWallet().remove(tokenToDiscard, 1);
                bank.add(tokenToDiscard, 1);
                System.out.println("1 jeton " + tokenToDiscard + " a été défaussé.");
            } else if (tokenToDiscard != null) {
                System.out.println("Vous ne possédez pas de jeton " + tokenToDiscard);
            }
        }

        System.out.println("Vous avez maintenant " + player.getWallet().getGems().size() + " jetons.\n");
    }

    private void showPlayerMenu(Player player) {
        while (true) {
            showHeader("MON JOUEUR");
            System.out.println(player.toString());
            showWallet(player);

            showHeader("MES CARTES");
            if (!player.getCards().isEmpty()) {
                System.out.println("Cartes acquises:");
                showCards(player.getCards());
            }
            if (!player.getReservedCards().isEmpty()) {
                System.out.println("Cartes réservées:");
                showCards(player.getReservedCards());
            }
            if (player.getCards().isEmpty() && player.getReservedCards().isEmpty()) {
                System.out.println("Aucune carte");
            }
            System.out.println();

            showHeader("ACTIONS DISPONIBLES");
            System.out.println("1. Acheter une carte disponible");
            System.out.println("2. Réserver une carte disponible");
            System.out.println("3. Acheter une carte réservée");
            System.out.println("4. Prendre deux gemmes de la même couleur");
            System.out.println("5. Prendre trois gemmes de couleurs différentes");

            try {
                var action = askInt("Votre choix : ");
                System.out.println();

                switch (action) {
                    case 1 -> { drawCard(player); return; }
                    case 2 -> { reserveCard(player); return; }
                    case 3 -> { buyReservedCard(player); return; }
                    case 4 -> { pickTwiceSameGem(player); return; }
                    case 5 -> { pickThreeDifferentGems(player); return; }
                    default -> System.out.println("Option invalide. Veuillez choisir un nombre entre 1 et 6.\n");
                }
            } catch (InputMismatchException e) {
                System.out.println("Veuillez entrer un nombre valide.\n");
                new Scanner(System.in).nextLine(); // Vider le buffer
            }
        }
    }
    // Actions joueur
    private void drawCard(Player player) {
        Objects.requireNonNull(player);
        if (displayedCards.isEmpty()) {
            System.out.println("Aucune carte n'est actuellement proposée.");
            showPlayerMenu(player);
            return;
        }

        showHeader("CARTES DISPONIBLES À L'ACHAT");
        showCards(displayedCards);

        var idx = askInt("Indice de la carte (1-%d, 0 pour annuler) : ".formatted(displayedCards.size())) - 1;

        if (idx < 0) {
            System.out.println("Achat annulé.\n");
            return;
        }

        if (idx >= displayedCards.size()) {
            System.out.println("Indice invalide, réessayez.\n");
            drawCard(player); // Rappel récursif
            return;
        }

        var chosen = displayedCards.get(idx);
        if (!player.getWallet().canAfford(chosen.price())) {
            System.out.println("Pas assez de gemmes pour cette carte. Souhaitez-vous :");
            System.out.println("1. La réserver à la place");
            System.out.println("2. Choisir une autre carte");
            System.out.println("3. Annuler");

            var choice = askInt("Votre choix : ");
            switch (choice) {
                case 1 -> reserveSpecificCard(player, chosen);
                case 2 -> drawCard(player); // Rappel récursif
                default -> System.out.println("Achat annulé.\n");
            }
            return;
        }

        // Achat de la carte
        player.getWallet().pay(chosen.price());
        displayedCards.remove(idx);
        player.addCard(chosen);
        System.out.println("Carte achetée : " + chosen + "\n");

        // Remplacement de la carte
        if (!cards.isEmpty()) {
            displayedCards.add(cards.remove(0));
        }
    }

    private void reserveSpecificCard(Player player, DevelopmentCard card) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(card);
        if (player.getReservedCards().size() >= 3) {
            System.out.println("Vous avez déjà 3 cartes réservées (maximum).\n");
            showPlayerMenu(player);
            return;
        }

        displayedCards.remove(card);
        player.reserveCard(card);
        System.out.println("Carte réservée : " + card + "\n");

        // Donner un jeton or si disponible
        if (bank.getAmount(GemToken.GOLD) > 0) {
            player.getWallet().add(GemToken.GOLD, 1);
            bank.remove(GemToken.GOLD, 1);
            System.out.println("Vous avez reçu un jeton or pour avoir réservé une carte.\n");
        }

        // Remplacement de la carte
        if (!cards.isEmpty()) {
            displayedCards.add(cards.remove(0));
        }
    }

    private void buyReservedCard(Player player) {
        Objects.requireNonNull(player);
        if (player.getReservedCards().isEmpty()) {
            System.out.println("Vous n'avez aucune carte réservée.\n");
            showPlayerMenu(player);
            return;
        }

        showHeader("MES CARTES RÉSERVÉES");
        showCards(player.getReservedCards());

        var idx = askInt("Indice de la carte à acheter (1-%d, 0 pour annuler) : ".formatted(player.getReservedCards().size())) - 1;

        if (idx < 0) {
            System.out.println("Achat annulé.\n");
            return;
        }

        if (idx >= player.getReservedCards().size()) {
            System.out.println("Indice invalide.\n");
            buyReservedCard(player);
            return;
        }

        var chosen = player.getReservedCards().get(idx);
        if (!player.getWallet().canAfford(chosen.price())) {
            System.out.println("Vous n'avez pas assez de gemmes pour acheter cette carte.\n");
            return;
        }

        // Achat de la carte réservée
        player.getWallet().pay(chosen.price());
        player.removeReservedCard(chosen);
        player.addCard(chosen);
        System.out.println("Carte achetée : " + chosen + "\n");
    }

    private void reserveCard(Player player) {
        Objects.requireNonNull(player);
        if (displayedCards.isEmpty()) {
            System.out.println("Aucune carte n'est actuellement proposée.");
            return;
        }

        showWallet(player);
        showCards(displayedCards);

        var idx = askInt("Indice de la carte à réserver (1-%d, 0 pour annuler) : ".formatted(displayedCards.size())) - 1;

        if (idx < 0) {
            System.out.println("Réservation annulée.\n");
            return;
        } else if (idx >= displayedCards.size()) {
            System.out.println("Indice invalide.");
            reserveCard(player);
            return;
        }

        var chosen = displayedCards.remove(idx);
        player.reserveCard(chosen);
        System.out.println("Carte réservée : " + chosen + "\n");

        // Le joueur reçoit un jeton or (si disponible)
        if (bank.getAmount(GemToken.GOLD) > 0) {
            player.getWallet().add(GemToken.GOLD, 1);
            bank.remove(GemToken.GOLD, 1);
            System.out.println("Vous avez reçu un jeton or pour avoir réservé une carte.\n");
        }

        if (!cards.isEmpty()) {
            displayedCards.add(cards.removeFirst());
        }
    }

    private void playCard(Player player) {
        Objects.requireNonNull(player);
        List<DevelopmentCard> playableCards = new ArrayList<>();
        playableCards.addAll(player.getCards());
        playableCards.addAll(player.getReservedCards());

        if (playableCards.isEmpty()) {
            System.out.println("Vous n'avez pas de carte à jouer");
            return;
        }

        showCards(playableCards);
        var cardIndex = askInt("Entrez l'indice de la carte à jouer : ") - 1;

        if (cardIndex >= 0 && cardIndex < playableCards.size()) {
            DevelopmentCard cardToPlay = playableCards.get(cardIndex);
            if (player.getReservedCards().contains(cardToPlay)) {
                player.removeReservedCard(cardToPlay);
            } else {
                player.removeCard(cardToPlay);
            }
            player.addCard(cardToPlay); // Ajoute la carte aux cartes jouées
            System.out.println("Carte jouée : " + cardToPlay);
        } else {
            System.out.println("La carte choisie n'existe pas");
        }
    }

    private void pickTwiceSameGem(Player player) {
        Objects.requireNonNull(player);
        showBank();

        while (true) {
            showText("--- 1. RUBY - 2. EMERALD - 3. DIAMOND - 4. SAPPHIRE - 5. ONYX ---");
            var action = askInt("Votre choix : ");

            var success = switch (action) {
                case 1 -> updateUserWalletForSameGem(player, GemToken.RUBY);
                case 2 -> updateUserWalletForSameGem(player, GemToken.EMERALD);
                case 3 -> updateUserWalletForSameGem(player, GemToken.DIAMOND);
                case 4 -> updateUserWalletForSameGem(player, GemToken.SAPPHIRE);
                case 5 -> updateUserWalletForSameGem(player, GemToken.ONYX);
                default -> {
                    System.out.println("Action inconnue. Veuillez réessayer.");
                    yield false;
                }
            };

            if (success) {
                return;
            }
        }
    }

    private void pickThreeDifferentGems(Player player) {
        List<GemToken> pickedGems = new ArrayList<>();
        var i = 0;
        showText("--- 1. RUBY - 2. EMERALD - 3. DIAMOND - 4. SAPPHIRE - 5. ONYX ---");
        while (i < 3) {
            int action = askInt("Votre choix (" + (i + 1) + "/3) : ");

            var valid = switch (action) {
                case 1 -> updateUserWalletForDifferentGems(player, GemToken.RUBY, pickedGems);
                case 2 -> updateUserWalletForDifferentGems(player, GemToken.EMERALD, pickedGems);
                case 3 -> updateUserWalletForDifferentGems(player, GemToken.DIAMOND, pickedGems);
                case 4 -> updateUserWalletForDifferentGems(player, GemToken.SAPPHIRE, pickedGems);
                case 5 -> updateUserWalletForDifferentGems(player, GemToken.ONYX, pickedGems);
                default -> {
                    showText("Veuillez saisir le bon numéro d'action.");
                    yield false;
                }
            };

            if (valid) {
                i++;
            }
        }

        System.out.println("\nLes jetons " + pickedGems.stream()
                .map(Enum::name)
                .collect(Collectors.joining(", ")) + " ont bien été ajoutés à vos jetons !\n");
    }

    // Helpers de transactions
    private boolean updateUserWalletForSameGem(Player player, GemToken token) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(token);

        int amount = bank.getAmount(token);
        if (amount < 4) {
            System.out.println("Pas assez de gemmes " + token + " dans la banque (il en faut au moins 4).");
            return false;
        }

        player.getWallet().add(token, 2);
        bank.remove(token, 2);
        return true;
    }

    private boolean updateUserWalletForDifferentGems(Player player, GemToken token, List<GemToken> pickedGems) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(token);
        Objects.requireNonNull(pickedGems);

        if (pickedGems.contains(token)) {
            System.out.println("Gemme déjà récupérée: " + token);
            return false;
        }

        if (bank.getAmount(token) <= 0) {
            System.out.println("Plus de gemmes: " + token);
            return false;
        }

        pickedGems.add(token);
        player.getWallet().add(token, 1);
        bank.remove(token, 1);
        return true;
    }

    // Affichage du plateau
    private void displayBoard() {
        showBank();
        showHeader("CARTES DISPONIBLES");
        IntStream.range(0, displayedCards.size())
                .forEach(i -> System.out.println((i + 1) + " - " + displayedCards.get(i)));
        System.out.println();
    }

    // Helpers d'affichage
    private void showHeader(String title) {
        System.out.println("[" + title + "]");
    }

    private void showText(String text) {
        System.out.println(text);
    }

    private void showCards(List<DevelopmentCard> cards) {
        Objects.requireNonNull(cards);
        IntStream.range(0, cards.size())
                .forEach(i -> System.out.println((i + 1) + " - " + cards.get(i)));
        System.out.println();
    }

    private void showWallet(Player player) {
        Objects.requireNonNull(player);
        showHeader("VOS JETONS");
        System.out.println(player.getWallet() + "\n");

        if (!player.getReservedCards().isEmpty()) {
            showHeader("CARTES RÉSERVÉES");
            showCards(player.getReservedCards());
        }
    }

    private void showBank() {
        showHeader("JETONS DISPONIBLES");
        System.out.println(bank + "\n");
    }

    // Helper d'entrée utilisateur
    private int askInt(String prompt) {
        Scanner scanner = new Scanner(System.in);
        showText(prompt);
        return scanner.nextInt();
    }
}