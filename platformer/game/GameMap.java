package platformer.game;

/**
 * Класс карты игры.
 * По сути, карта является многомерным числовым массивом.
 * Массив можно хранить в виде   переменной,
 * а можно загружать из файла.
 */
class GameMap {

    private int[][] map;
    public GameMap() {
        map = new int[][]{
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                {1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1},
                {1,2,1,1,1,2,1,1,1,1,1,1,1,1,1,1,1,1,2,1},
                {1,2,1,2,1,2,1,2,2,2,2,2,1,2,2,2,2,1,2,1},
                {1,2,1,2,2,2,1,1,1,1,2,2,1,2,2,2,2,1,2,1},
                {1,2,1,2,1,2,1,1,2,2,2,2,1,1,1,2,2,1,2,1},
                {1,2,1,2,2,2,2,2,2,1,2,2,2,2,1,2,2,1,1,1},
                {1,2,1,2,2,1,2,2,2,1,2,2,2,2,1,2,2,2,2,1},
                {1,2,1,2,2,1,2,1,2,1,2,1,2,2,2,2,2,2,2,1},
                {1,2,1,2,1,1,1,1,1,1,1,1,1,1,1,1,1,2,2,1},
                {1,2,2,2,2,1,2,2,2,1,2,2,2,2,2,2,2,2,2,1},
                {1,2,2,2,2,1,2,2,2,1,2,1,2,2,1,2,2,2,2,1},
                {1,2,1,1,2,1,1,2,2,1,2,1,2,2,1,2,1,2,2,1},
                {1,2,1,1,2,2,2,2,2,1,2,1,2,2,1,1,1,2,2,1},
                {1,2,2,1,1,1,2,1,1,1,2,1,1,1,1,2,2,2,2,1},
                {1,2,2,2,2,1,2,2,1,2,2,2,2,2,2,2,2,1,2,1},
                {1,2,1,2,2,1,2,2,1,2,2,2,2,2,2,2,2,1,2,1},
                {1,2,1,1,2,1,2,1,1,1,1,1,1,1,1,1,1,1,2,1},
                {1,2,2,2,2,1,2,2,2,2,2,2,2,2,2,2,2,2,2,1},
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
        };
    }
    /** Проверка наличия плитки на карте
     *  @param x
     *  @param y
     *  @return  */
    public boolean hasTile(int x, int y) {
        return (x >= 0 && y >= 0 && y <  getHeight() && x <  getWidth());
    }

    /** Получить ИД плитки
     *  @param x
     *  @param y
     *  @return  */
    public int getTileId(int x, int y) {
        return hasTile(x,y) ? map[y][x] : 0;
    }

    //Количество плиток на карте по высоте
    public int getHeight() {
        return map.length;
    }
    //Количество плиток на карте по ширине
    public int getWidth() {
        return map.length;
    }
}