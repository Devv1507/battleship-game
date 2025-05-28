package univalle.tedesoft.battleship.models.Ships;

import univalle.tedesoft.battleship.models.Enums.ShipType;

/**
 * Clase que representa la embarcacion de tipo "FRIGATE".
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public abstract class Frigate extends Ship {
    /**Constructor de la clase*/
    public Frigate() {
        super(ShipType.FRIGATE, 1);
    }
}
