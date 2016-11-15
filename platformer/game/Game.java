package platformer.game;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
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
    private boolean running;        //Отвечает за запуск или остановку игры
    protected int timeDelay = 100;  //время шага таймера в игре
    protected Canvas canvas;        //холст
    protected GameMap map;          //карта
    protected Player player;        //игрок

    protected KeyAdapter keyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == 37)
                player.directionX =-1;
            if(e.getKeyCode() == 39)
                player.directionX = 1;
            if(e.getKeyCode() == 38)
                player.directionY = -1;
            if(e.getKeyCode() == 40)
                player.directionY = 1;
        }
        @Override
        public void keyReleased(KeyEvent e) {
            if(e.getKeyCode() == 37 || e.getKeyCode() == 39)
                player.directionX = 0;
            if(e.getKeyCode() == 38 || e.getKeyCode() == 40)
                player.directionY = 0;
        }

    };

    public void start() throws FileNotFoundException {   //запуск игры
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
            //перемещение персонажа(если возможно)

       if((player.directionX!=0 || player.directionY!=0) && accessMove()) {
            player.posX+=player.directionX*player.speed;
            player.posY+=player.directionY*player.speed;
        }

        //все эти величины заданы в тайлах!
        int mapW=map.getWidth(),            //ширина карты
            mapH=map.getHeight();           //высота карты
        final int vScreenX=17,vScreenY=13;  //сколько влазит в экран
        int wScreenX=0,wScreenY=0;          //мировые координаты центра
            //по умолчанию в центре рисуется(если не край)
        //Это всегда левый-верхний угол блока, куда влезет наш перс.
        player.posRenderX=vScreenX/2*BaseTile.SIZE;
        player.posRenderY=vScreenY/2*BaseTile.SIZE;
        wScreenX=player.posX/BaseTile.SIZE;
        wScreenY=player.posY/BaseTile.SIZE;
        //левая сторона
        int offsetX=0,offsetY=0;
        boolean ofX=true,ofY=true;
        if(player.posX/BaseTile.SIZE<vScreenX/2){
            wScreenX=vScreenX/2;
            player.posRenderX=player.posX;
            offsetX=0;
            ofX=false;
        }
        //верхняя сторон
        if(player.posY/BaseTile.SIZE<vScreenY/2){
            wScreenY=vScreenY/2;
            player.posRenderY=player.posY;
            offsetY=0;
            ofY=false;
        }
        //правая сторона
        if(player.posX>(mapW-vScreenX/2)*BaseTile.SIZE){
            wScreenX=mapW-vScreenX/2;
            player.posRenderX=BaseTile.SIZE*(vScreenX-1)-(mapW*BaseTile.SIZE-player.posX);
            offsetX=0;
            ofX=false;
        }
        //нижняя сторона
        if(player.posY>(mapH-vScreenY/2)*BaseTile.SIZE){
            wScreenY=mapH-vScreenY/2;
            player.posRenderY=(vScreenY-mapH-1)*BaseTile.SIZE+player.posY;
            offsetY=0;
            ofY=false;
        }
        BaseTile tile;
        int startTileY=(wScreenY-vScreenY/2),
            endTileY=(wScreenY+vScreenY/2),
            startTileX=(wScreenX-vScreenX/2),
            endTileX=(wScreenX+vScreenX/2);
        if(ofX==true)
            offsetX=player.posX-(wScreenX*BaseTile.SIZE);
        if(ofY==true)
            offsetY=player.posY-(wScreenY*BaseTile.SIZE);

        for(int y=0; y<mapW; y++){
            for(int x=0; x<mapH; x++){
                tile = BaseTile.getTileById(map.getTileId(x, y));
                tile.posX=(x-startTileX)*BaseTile.SIZE-offsetX;
                tile.posY=(y-startTileY)*BaseTile.SIZE-offsetY;
                canvas.addRender( tile );
            }
        }
        canvas.addRender(player);
        canvas.repaint();
    }

        //Проверяем, является ли плитка пригодной для прохождения персонажем.
    protected boolean tileIsWalkable(int x, int y) {
        BaseTile tile=BaseTile.getTileById(map.getTileId(x,y));
        return (tile!=null && tile.isWalkable);
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
        left =  (int)Math.ceil ((player.posX+player.speed*player.directionX)/BaseTile.SIZE);
        right=  (int)Math.floor((player.posX+player.width+player.speed*player.directionX-1)/BaseTile.SIZE);
        top  =  (int)Math.ceil ((player.posY+player.speed*player.directionY)/BaseTile.SIZE);
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
        //left = (int)Math.ceil ((player.posX + player.speed * player.directionX) / BaseTile.SIZE);
        //right= (int)Math.floor((player.posX + player.width + player.speed * player.directionX - 1) / BaseTile.SIZE);
        //top  = (int)Math.ceil ((player.posY) / BaseTile.SIZE);
        //down = (int)Math.floor((player.posY + player.height -1) / BaseTile.SIZE);
        //проверяем доступность направления по вершине верха и низа лева (права) - на тот случай если игрок находится вне начала плитки по оси Y
        if(player.directionX == -1 && !(tileIsWalkable(left,top) && tileIsWalkable(left,down))) {
            isWalkable = false;
        } else{
            if(player.directionX == 1 && !(tileIsWalkable(right,top) && tileIsWalkable(right,down)))
                isWalkable = false;
        }
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
