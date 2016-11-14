package platformer.game;

import java.awt.Graphics;

/**
 * Интерфейс дает гарантию реализации в объекте метода обрисовки на холсте
 */
public interface IRenderToConvas {
    void render(Graphics g);
}
