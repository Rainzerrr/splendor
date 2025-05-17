public class Main {
    public static void main(String[] args) {
        var game = new Game(4);
        // Ajout des joueurs de la partie
        var player1 = new Player("Rayan");
        var player2 = new Player("Mounir");
        var player3 = new Player("Rayan");
        var player4 = new Player("Mounir");

        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(player3);
        game.addPlayer(player4);

        game.launch();
    }
}