package univalle.tedesoft.battleship.models.Enums;

/**
 * Enum que representa el estado del juego.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public enum GamePhase {
    /** Inicio*/
    INITIAL,
    /** Jugador colocando barcos*/
    PLACEMENT,
    /** Fase de disparos*/
    FIRING,
    /** Juego terminado*/
    GAME_OVER
}
