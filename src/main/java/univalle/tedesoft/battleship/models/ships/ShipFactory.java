package univalle.tedesoft.battleship.models.ships;

import univalle.tedesoft.battleship.models.enums.ShipType;

/**
 * Fábrica para crear objetos Ship.
 * Implementa el patrón Factory Method para centralizar la lógica de creación.
 */
public class ShipFactory {

    /**
     * Crea y devuelve una instancia de un barco basado en su tipo.
     * Este es el "metodo de fabrica".
     * @param type El tipo de barco a crear.
     * @return Una nueva instancia del barco solicitado.
     */
    public static Ship createShip(ShipType type) {
        switch (type) {
            case AIR_CRAFT_CARRIER:
                return new AirCraftCarrier();
            case SUBMARINE:
                return new Submarine();
            case DESTROYER:
                return new Destroyer();
            case FRIGATE:
                return new Frigate();
            default:
                throw new IllegalArgumentException("Tipo de barco desconocido: " + type);
        }
    }
}