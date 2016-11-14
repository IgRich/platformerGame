package platformer.game;

//Данный класс является основным холстом, где будет отображаться вся графическая составляющая игры.

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;




public class Canvas extends JPanel{
    /**
     * Основное изображение для рисования
     * Осуществляет буферизацию изображений (избавляет от мерцания)
     */
    protected Image bufer = null;
    protected Color backGround = Color.black;   //Фон холста по умолчанию
    protected ArrayList renders = new ArrayList();  //Список элементов, которые необходимо нарисовать на холсте

    /**
     * Добавить элемент для рисования
     * @param render
     */
    public void addRender(IRenderToConvas render) {
        renders.add(render);
    }

    //Очищает список обрисовываемых элементов
    public void removeRenders() {
        renders.clear();
    }

    /**
     * Отвечает за вывод  графики на компоненте
     * @param g
     */
    public void paintWorld(Graphics g) {
        g.clearRect(0, 0, getWidth(), getHeight());
        g.setColor(backGround);
        g.fillRect(0, 0, getWidth(), getHeight());

        IRenderToConvas render;
        for(int i=0;i<renders.size();i++){
            render=(IRenderToConvas)renders.get(i);
            render.render(g);
        }
    }

    /**
     * Переопределяем метод обрисовки компонента
     * @param g
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if(bufer == null) {
            bufer = createImage(getWidth(), getHeight());
        }
        //рисуем мир!
        paintWorld(bufer.getGraphics());
        g.drawImage(bufer, 0, 0, null);
    }
}
