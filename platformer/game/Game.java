package platformer.game;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
В центре всего находится класс Game выполняющий роль осинового контроллера.
Он оснащен двумя методами, которые могут запускать игровой процесс (start) и останавливать(stop).
При запуске игры запускается таймер, который раз в определенный промежуток времени вызывает метод update,
 */
public class Game implements Runnable{
    /////******!!!!
    PrintWriter out;

    private boolean running;        //Отвечает за запуск или остановку игры
    protected int timeDelay = 100;  //время шага таймера в игре
    protected Canvas canvas;        //холст
    protected GameMap map;          //карта
    protected Player player;        //игрок
    protected KeyAdapter keyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == 37) {
                player.directionX = -1;
            }
            if(e.getKeyCode() == 39) {
                player.directionX = 1;
            }
            if(e.getKeyCode() == 38) {
                player.directionY = -1;
            }
            if(e.getKeyCode() == 40) {
                player.directionY = 1;
            }

        }
        @Override
        public void keyReleased(KeyEvent e) {
            if(e.getKeyCode() == 37 || e.getKeyCode() == 39) {
                player.directionX = 0;
            }
            if(e.getKeyCode() == 38 || e.getKeyCode() == 40) {
                player.directionY = 0;
            }
        }

    };

    public void start() throws FileNotFoundException {   //запуск игры
        out=new PrintWriter("log.txt");
        //
        if(running)
            return;
        running = true;
        canvas = new Canvas();
        map = new GameMap();
        player = new Player();
        player.posX = BaseTile.SIZE;
        player.posY = BaseTile.SIZE;
        player.posRenderX =BaseTile.SIZE;
        player.posRenderY =BaseTile.SIZE;
        setListener();

        new Thread(this).start();
    }

    public void stop() {        //остановка игры
        running = false;
    }

    public Canvas getCanvas() { //Получить ссылку на холст игры
        return canvas;
    }
    @Override
    public void run() {
        while(running) {
            try {
                TimeUnit.MILLISECONDS.sleep(timeDelay);
            } catch (InterruptedException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                update();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void update() throws IOException {   //Вызывается по таймеру игры
        canvas.removeRenders();
        //добавляем на холст элементы карты
        int mapW=map.getWidth(),
            mapH=map.getHeight(),
            x,y, tileId,
                //ширина и высота квадрата - рамки в тайлах, зависит от экрана игры
            widthTile = 18,  heightTile = 12,
                //центральная точка отображаемого квадрата карты
            cWidthTile=widthTile/2-1, cHeightTile=heightTile/2-1,
                //здвиг карты
            offsetX = (player.posX - player.posRenderX)%BaseTile.SIZE,
            offsetY = (player.posY - player.posRenderY)%BaseTile.SIZE,
                //ограничиваем выводимые плитки
            startTileX = (int)Math.floor( Math.abs(player.posX - player.posRenderX) / BaseTile.SIZE),
            startTileY = (int)Math.floor( Math.abs(player.posY - player.posRenderY) / BaseTile.SIZE),
            endTileX = widthTile + startTileX > mapW ? mapW : widthTile + startTileX,
            endTileY = heightTile + startTileY > mapH ? mapH : heightTile + startTileY;
        //определяем когда двигать карту - а когда должен двигаться персонаж
        boolean movePlayerX = (player.posX / BaseTile.SIZE < cWidthTile || mapW - player.posX / BaseTile.SIZE < cWidthTile+1),
                movePlayerY = (player.posY / BaseTile.SIZE < cHeightTile || mapH - player.posY / BaseTile.SIZE < cHeightTile+2);

        BaseTile tile;
        for(y = startTileY; y < endTileY; y ++) {
            for(x = startTileX; x < endTileX; x ++) {
                tileId = map.getTileId(x, y);
                tile = BaseTile.getTileById(tileId);

                tile.posX= (x-startTileX)*BaseTile.SIZE;
                tile.posY= (y-startTileY)*BaseTile.SIZE;
                tile.posX-=offsetX;
                tile.posY-=offsetY;

                canvas.addRender( tile );
            }
        }

        canvas.addRender(player);   //добавляем персонажа
                //изменяем положение персонажа
        if((player.directionX != 0 || player.directionY  != 0) && accessMove()) {
            player.posX+=player.directionX*player.speed;
            player.posY+=player.directionY*player.speed;
            if(movePlayerX)
                player.posRenderX+=player.directionX*player.speed;
            if(movePlayerY)
                player.posRenderY+=player.directionY*player.speed;
        }
        canvas.repaint();   //вызываем перерисовку холста

    }

        //Проверяем, является ли плитка пригодной для прохождения персонажем.
    protected boolean tileIsWalkable(int x, int y) {
        BaseTile tile =  BaseTile.getTileById( map.getTileId(x, y) );
        return (tile != null && tile.isWalkable);
    }
    /**
     * Проверяем возможность персонажа переместится.
     *
     * В данной реализации, может случится, что объект не достигает границы плитки, а идти уже не может.
     * Причина вероятно в значении скорости игрока (player.speed), она должна делить без остатка размер плитки.
     * В противном случаи остаток нужно прибавить к максимальному значению right и down.
     */
    protected boolean accessMove() {
        int left ,right, top,down;
        boolean isWalkable = true;

                //верх и низ
        //Находим вероятные точки плиток с учетом направления directionY
        left =  (int)Math.ceil( (player.posX)/BaseTile.SIZE);
        right=  (int)Math.floor((player.posX+player.width-1)/BaseTile.SIZE);
        top  =  (int)Math.ceil( (player.posY+player.speed*player.directionY)/BaseTile.SIZE);
        down =  (int)Math.floor((player.posY+player.height+player.speed*player.directionY-1)/BaseTile.SIZE);

        //проверяем доступность направления по вершине правой и левой сверх (низу) - на тот случай,
        //если игрок находится вне начала плитки по оси Х
        if(player.directionY == -1 && !(tileIsWalkable(left,top) && tileIsWalkable(right,top))) {
            isWalkable = false;
        } else  {
            if(player.directionY == 1 && !(tileIsWalkable(left,down) && tileIsWalkable(right,down)))
                isWalkable = false;
        }
                //право и лево
        //Находим вероятные точки плиток с учетом направления directionX
        left = (int)Math.ceil ((player.posX + player.speed * player.directionX) / BaseTile.SIZE);
        right= (int)Math.floor((player.posX + player.width + player.speed * player.directionX - 1) / BaseTile.SIZE);
        //top  = (int)Math.ceil ((player.posY) / BaseTile.SIZE);
        //down = (int)Math.floor((player.posY + player.height -1) / BaseTile.SIZE);
        //проверяем доступность направления по вершине верха и низа лева (права) - на тот случай если игрок находится вне начала плитки по оси Y
        if(player.directionX == -1 && !(tileIsWalkable(left,top) && tileIsWalkable(left,down))) {
            isWalkable = false;
        } else{
            if(player.directionX == 1 && !(tileIsWalkable(right,top) && tileIsWalkable(right,down)))
                isWalkable = false;
        }
        //out.println("player.posX: " + player.posX +" player.posY: "+player.posY);
        //out.println("player.posRenderX: "+player.posRenderX+" player.posRenderX: "+player.posRenderX);
        //out.println("player.directionX: "+player.directionX+" player.directionY: "+player.directionY);

        return isWalkable;
    }


    //Установить обработчики событий
    public void setListener() {
        canvas.setFocusable(true);//нужно указать для получения событий с клавиатуры
        canvas.addKeyListener(keyListener);
    }
    //Удалить обработчики событий.
    public void unSetListener() {
        canvas.setFocusable(false);//убираем  фокус
        canvas.removeKeyListener(keyListener);
    }
}
