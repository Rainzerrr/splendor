

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        Game game = new Game(2);
        // Ajout des joueurs de la partie
        var player1 = new Player("Rayan");
        var player2 = new Player("Mounir");
        game.addPlayer(player1);
        game.addPlayer(player2);

        game.launch();
    }
}
