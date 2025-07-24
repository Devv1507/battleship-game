package univalle.tedesoft.battleship.models.enums;

/**
 * Enum que representa el estado del juego.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public enum gamePhase {
    /** Inicio*/
    INITIAL,
    /** Jugador colocando barcos*/
    PLACEMENT,
    /** Fase de disparos*/
    FIRING,
    /** Juego terminado*/
    GAME_OVER
}
