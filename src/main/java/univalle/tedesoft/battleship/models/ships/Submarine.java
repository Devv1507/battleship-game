package univalle.tedesoft.battleship.models.ships;

import univalle.tedesoft.battleship.models.enums.ShipType;

/**
 * Clase que representa la embarcacion de tipo "SUBMARINE".
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class Submarine extends Ship {
    /**Constructor de la clase*/
    public Submarine() {
        super(ShipType.SUBMARINE, 3);
    }
}
