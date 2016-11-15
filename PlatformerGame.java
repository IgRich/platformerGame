import platformer.game.*;

import java.io.FileNotFoundException;

public class PlatformerGame {
    public static void main(String[] args) throws FileNotFoundException {
        Game game = new Game();
        game.start();
        javax.swing.JFrame f =  new javax.swing.JFrame();
        f.setLayout(null);
        f.setDefaultCloseOperation( javax.swing.WindowConstants.EXIT_ON_CLOSE );
        f.setResizable(false);
        //устанавливаем размер холста
        game.getCanvas().setBounds(0, 0, 680, 520);
        //ставим холст для отображения
        f.add(game.getCanvas());
        f.setTitle("Platformer!");
        f.setBounds(110,110,640,480);
        f.setVisible(true);
    }
}
