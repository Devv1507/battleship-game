package univalle.tedesoft.battleship.models;

import univalle.tedesoft.battleship.models.enums.shotResult;
import univalle.tedesoft.battleship.models.ships.ship;

/**
 * Representa el resultado completo de un disparo, incluyendo la coordenada
 * donde ocurrió y el resultado (agua, tocado, hundido).
 * Esta clase facilita la comunicación entre el modelo y el controlador.
 */
public class shotOutcome {
    private final coordinate coordinate;
    private final shotResult result;
    private final ship sunkenShip; // Puede ser null si no se hundió ningún barco

    public shotOutcome(coordinate coordinate, shotResult result) {
        this(coordinate, result, null);
    }

    public shotOutcome(coordinate coordinate, shotResult result, ship sunkenShip) {
        this.coordinate = coordinate;
        this.result = result;
        this.sunkenShip = sunkenShip;
    }

    public coordinate getCoordinate() {
        return this.coordinate;
    }

    public shotResult getResult() {
        return this.result;
    }

    public ship getSunkenShip() {
        return this.sunkenShip;
    }
}