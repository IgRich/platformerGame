package platformer.game;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;


public class Player implements IRenderToConvas {
    protected Image imageSrc;   //Изображение персонажа
    public int posX;    //Положение персонажа в пространстве по X
    public int posY;    //Положение персонажа в пространстве по Y
    public int posRenderX;  //Указывает статическое положение при визуализации по оси X
    public int posRenderY;  //Указывает статическое положение при визуализации по оси Y


    public int speed = 5;   //скорость персонажа
    //Указывает на передвижение персонажа по оси Y и Z
    //(1)вправо/вниз, (-1)влево/вверх, (0)стоп
    public int directionX = 0;
    public int directionY = 0;  //(1)-вниз,(-1)-вверх, (0)стоп
    public int width = 30;  //Размер по ширине
    public int height = 30; //Размер персонажа по высоте
    public Player() {
        String name = "/data/player.png";
        imageSrc = new ImageIcon(getClass().getResource(name)).getImage();
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(imageSrc, posRenderX, posRenderY, width, height, null);
    }
}