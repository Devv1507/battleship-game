package univalle.tedesoft.battleship.models.ships;

import univalle.tedesoft.battleship.models.enums.shipType;

/**
 * Fábrica para crear objetos Ship.
 * Implementa el patrón Factory Method para centralizar la lógica de creación.
 */
public class shipFactory {

    /**
     * Crea y devuelve una instancia de un barco basado en su tipo.
     * Este es el "metodo de fabrica".
     * @param type El tipo de barco a crear.
     * @return Una nueva instancia del barco solicitado.
     */
    public static ship createShip(shipType type) {
        switch (type) {
            case AIR_CRAFT_CARRIER:
                return new airCraftCarrier();
            case SUBMARINE:
                return new submarine();
            case DESTROYER:
                return new destroyer();
            case FRIGATE:
                return new frigate();
            default:
                throw new IllegalArgumentException("Tipo de barco desconocido: " + type);
        }
    }
}