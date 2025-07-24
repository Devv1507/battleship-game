package univalle.tedesoft.battleship.models.enums;

/**
 * Enum que representa el estado de una casilla dentro del juego.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public enum cellState {
    /** Estado en el que la celda se encuentra ocupada por un tipo de barco o parte de uno*/
    SHIP,
    /** Estado en el que la celda se encuentra vacia*/
    EMPTY,
    /** Estado en el que una celda vacia, se encuentra al recibir un ataque*/
    SHOT_LOST_IN_WATER,
    /** Estado en el que la celda se encuentra ocupada por una embarcacion impactada por un ataque enemigo*/
    HIT_SHIP,
    /** Estado en el que una celda con un barco o parte de uno impactado se encuentra, al declararse hundido*/
    SUNK_SHIP_PART
}
