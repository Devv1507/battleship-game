package univalle.tedesoft.battleship.models.board;

/**
 * Clase que representa una coordenada en el tablero de la batalla naval.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class Coordinate {
    /** Coordenada x*/
    private int x;
    /** Coordenada y*/
    private int y;
    /** Booleano con el objetivo de saber si la coordenada fue impactada*/
    private boolean isSunk;
    /** Constructor*/
    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
        this.isSunk = false;
    }

    /** Getter para obtener el eje x*/
    public int getX() {
        return x;
    }

    /** Getter para obtener el eje y*/
    public int getY() {
        return y;
    }

    /** Setter para obtener el eje x*/
    public void setX(int x) {
        this.x = x;
    }

    /** Setter para obtener el eje x*/
    public void setY(int y) {
        this.y = y;
    }

    /** Trae el valor de isSunk, que indica si la coordenada ha sido atacada */
    public boolean isSunk() {
        return isSunk;
    }

    /** Sirve para indicar que una casilla fue impactada con el ataque de un jugador*/
    public void hitInTheCoordinate() {
        isSunk = true;
    }

    /**
     * Convierte la coordenada numérica (ej. x=0, y=0) a notación algebraica (ej. "A1").
     * Las columnas 0-9 se mapean a A-J y las filas 0-9 a 1-10.
     * @return La representación de la coordenada en formato de texto.
     */
    public String toAlgebraicNotation() {
        // 'A' es el carácter 65 en ASCII. Sumamos la columna (x) para obtener la letra.
        char columnLetter = (char) ('A' + this.x);
        // Sumamos 1 a la fila (y) para que el rango sea 1-10 en lugar de 0-9.
        int rowNumber = this.y + 1;
        return String.format("%c%d", columnLetter, rowNumber);
    }
}
