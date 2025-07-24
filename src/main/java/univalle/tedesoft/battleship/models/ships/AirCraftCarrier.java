package univalle.tedesoft.battleship.models.ships;

import univalle.tedesoft.battleship.models.enums.ShipType;

/**
 * Clase que representa la embarcacion de tipo "AIR CRAFT CARRIER".
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class AirCraftCarrier extends Ship {
    /**Constructor de la clase*/
    public AirCraftCarrier() {
        super(ShipType.AIR_CRAFT_CARRIER, 4);
    }
}
