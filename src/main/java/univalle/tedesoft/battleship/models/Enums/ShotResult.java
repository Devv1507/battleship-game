package univalle.tedesoft.battleship.models.Enums;
/**
 * Enum para los resultados de un ataque.
 * @author Santiago David Guerrero
 * @author David Esteban Valencia
 * @author Juan Pablo Escamilla
 */
public enum ShotResult {
    /**Disparo al agua*/
    WATER,
    /**Disparo toca una embarcacion*/
    TOUCHED,
    /**Disparo a una casilla ya golpeada*/
    ALREADY_HIT
}
