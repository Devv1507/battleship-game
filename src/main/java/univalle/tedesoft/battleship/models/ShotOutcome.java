package univalle.tedesoft.battleship.models;

import univalle.tedesoft.battleship.models.Enums.ShotResult;
import univalle.tedesoft.battleship.models.Ships.Ship;

/**
 * Representa el resultado completo de un disparo, incluyendo la coordenada
 * donde ocurrió y el resultado (agua, tocado, hundido).
 * Esta clase facilita la comunicación entre el modelo y el controlador.
 */
public class ShotOutcome {
    private final Coordinate coordinate;
    private final ShotResult result;
    private final Ship sunkenShip; // Puede ser null si no se hundió ningún barco

    public ShotOutcome(Coordinate coordinate, ShotResult result) {
        this(coordinate, result, null);
    }

    public ShotOutcome(Coordinate coordinate, ShotResult result, Ship sunkenShip) {
        this.coordinate = coordinate;
        this.result = result;
        this.sunkenShip = sunkenShip;
    }

    public Coordinate getCoordinate() {
        return this.coordinate;
    }

    public ShotResult getResult() {
        return this.result;
    }

    public Ship getSunkenShip() {
        return this.sunkenShip;
    }
}