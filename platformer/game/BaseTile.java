package platformer.game;

import java.awt.Graphics;
import java.awt.Image;
import java.util.HashMap;
import javax.swing.ImageIcon;


//Базовый элемент отображения плитки карты
class BaseTile implements IRenderToConvas {
    final public static int SIZE = 40;              //Размер плитки - ширина, высота
    private static HashMap images = new HashMap();  //Буфер загруженных изображений
    public String image;    //Имя изображения
    public int posX;        //положение плитки по оси X
    public int posY;        //положение плитки по оси Y
    public boolean isWalkable;

    public BaseTile(String image, int posX, int posY, boolean isWalkable) {
        this.image = image;
        this.posX = posX;
        this.posY = posY;
        this.isWalkable = isWalkable;
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(getImage(image), posX, posY, SIZE, SIZE, null);
    }

    //Вернет изображение по ссылке.
    public static Image getImage(String name) {
        if(images.get(name) == null) {
            images.put(name, new ImageIcon(BaseTile.class.getResource(name)).getImage());
        }
        return (Image)images.get(name);
    }

    /**
     *
     * @param tileId
     * @return BaseTile
     */
    public static  BaseTile getTileById(int tileId) {
        if(tileId == 1) {
            return new BaseTile( "/data/white.png", 0, 0,false);
        }
        if(tileId == 2) {
            return new BaseTile( "/data/black.png", 0, 0,true);
        }
        return null;

    }
}