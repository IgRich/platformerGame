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
    private int timeDelay = 100;  //время шага таймера в игре
    private Canvas canvas;        //холст
    private GameMap map;          //карта
    private Player player;        //игрок

    private KeyAdapter keyListener = new KeyAdapter() {
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

    private void update() throws IOException {   //Вызывается по таймеру игры
        canvas.removeRenders();
            ////перемещение персонажа(если возможно)
       if((player.directionX!=0 || player.directionY!=0) && accessMove()) {
            player.posX+=player.directionX*player.speed;
            player.posY+=player.directionY*player.speed;
        }
        int mapW=map.getWidth(),            //ширина карты
            mapH=map.getHeight(),           //высота карты
                //сколько влазит в экран в тайлах
            vScreenX=17,vScreenY=13,
                //мировые координаты центра
            wScreenX=player.posX/BaseTile.SIZE,
            wScreenY=player.posY/BaseTile.SIZE,
                //Смещение(для плавности перехода от тайла к тайлу)
            offsetX=player.posX-(wScreenX*BaseTile.SIZE),
            offsetY=player.posY-(wScreenY*BaseTile.SIZE);

        player.posRenderX=vScreenX/2*BaseTile.SIZE;
        player.posRenderY=vScreenY/2*BaseTile.SIZE;
            //[левая|правый] край
        if(player.posX/BaseTile.SIZE<vScreenX/2){
            wScreenX=vScreenX/2;
            player.posRenderX=player.posX;
            offsetX=0;
        } else{
            if(player.posX>(mapW-vScreenX/2)*BaseTile.SIZE){
                wScreenX=mapW-vScreenX/2;
                player.posRenderX=BaseTile.SIZE*(vScreenX-1)-(mapW*BaseTile.SIZE-player.posX);
                offsetX=0;
            }
        }
            //[верхний|нижний]край
        if(player.posY/BaseTile.SIZE<vScreenY/2){
            wScreenY=vScreenY/2;
            player.posRenderY=player.posY;
            offsetY=0;
        }else{
            if(player.posY>(mapH-vScreenY/2)*BaseTile.SIZE){
                wScreenY=mapH-vScreenY/2;
                player.posRenderY=(vScreenY-mapH-1)*BaseTile.SIZE+player.posY;
                offsetY=0;
            }
        }
        BaseTile tile;
            //для корректного смещения
        int startTileY=(wScreenY-vScreenY/2),startTileX=(wScreenX-vScreenX/2);
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
    private boolean tileIsWalkable(int x, int y) {
        BaseTile tile=BaseTile.getTileById(map.getTileId(x,y));
        return (tile!=null && tile.isWalkable);
    }
    private boolean accessMove() {
        int left ,right, top,down;
        left =  (player.posX+player.speed*player.directionX)/BaseTile.SIZE;
        right=  (player.posX+player.width+player.speed*player.directionX-1)/BaseTile.SIZE;
        top  =  (player.posY+player.speed*player.directionY)/BaseTile.SIZE;
        down =  (player.posY+player.height+player.speed*player.directionY-1)/BaseTile.SIZE;
        if(player.directionY == -1 && !(tileIsWalkable(left,top) && tileIsWalkable(right,top))) {
            return false;
        } else  {
            if(player.directionY == 1 && !(tileIsWalkable(left,down) && tileIsWalkable(right,down)))
                return false;
        }
        if(player.directionX == -1 && !(tileIsWalkable(left,top) && tileIsWalkable(left,down))) {
            return false;
        } else{
            if(player.directionX == 1 && !(tileIsWalkable(right,top) && tileIsWalkable(right,down)))
                return false;
        }
        return true;
    }


    //Установить обработчики событий
    private void setListener() {
        canvas.setFocusable(true);//нужно указать для получения событий с клавиатуры
        canvas.addKeyListener(keyListener);
    }
    //Удалить обработчики событий.
    private void unSetListener() {
        canvas.setFocusable(false);//убираем  фокус
        canvas.removeKeyListener(keyListener);
    }
}
