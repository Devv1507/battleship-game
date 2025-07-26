package univalle.tedesoft.battleship.models.ships;

import univalle.tedesoft.battleship.models.enums.ShipType;

/**
 * Clase que representa la embarcacion de tipo "FRIGATE".
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class Frigate extends Ship {
    /**Constructor de la clase*/
    public Frigate() {
        super(ShipType.FRIGATE, 1);
    }
}
