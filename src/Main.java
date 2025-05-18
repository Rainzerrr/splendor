public class Main {
    public static void main(String[] args) {
        Game game = new Game();
        // Ajout des joueurs de la partie
        var player1 = new Player("Rayan", 23);
        var player2 = new Player("Mounir", 21);
        var player3 = new Player("Mounisr", 29);

        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(player3);

        game.launch();
    }
}