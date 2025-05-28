package univalle.tedesoft.battleship.models.Ships;

import univalle.tedesoft.battleship.models.Enums.ShipType;

/**
 * Clase que representa la embarcacion de tipo "DESTROYER".
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public abstract class Destroyer extends Ship {
    /**Constructor de la clase*/
    public Destroyer() {
        super(ShipType.DESTROYER, 2);
    }
}
