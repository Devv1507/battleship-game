package univalle.tedesoft.battleship.models.ships;

import univalle.tedesoft.battleship.models.enums.ShipType;

/**
 * Clase que representa la embarcacion de tipo "DESTROYER".
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class Destroyer extends Ship {
    /**Constructor de la clase*/
    public Destroyer() {
        super(ShipType.DESTROYER, 2);
    }
}
