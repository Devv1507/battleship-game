package univalle.tedesoft.battleship.models.Ships;

import univalle.tedesoft.battleship.models.Enums.ShipType;

/**
 * Clase que representa la embarcacion de tipo "SUBMARINE".
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public abstract class Submarine extends Ship {
    /**Constructor de la clase*/
    public Submarine() {
        super(ShipType.SUBMARINE, 3);
    }
}
