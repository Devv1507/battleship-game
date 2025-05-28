package univalle.tedesoft.battleship.models.Ships;

import univalle.tedesoft.battleship.models.Coordinate;
import univalle.tedesoft.battleship.models.Enums.Orientation;
import univalle.tedesoft.battleship.models.Enums.ShipType;
import java.util.List;

/**
 * Clase que representa las embarcaciones de la batalla naval.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public abstract class Ship {
    /**Tipo de embarcacion que se esta creando*/
    public ShipType shipType;
    /**La Orientacion de la embarcacion*/
    public Orientation orientation;
    /**Coordenadas que ocupa la embarcacion*/
    private List<Coordinate> occupiedCoordinates;
    /**Ataques recibidos por parte del enemigo*/
    private int hitCount;
    /**booleano que indica si la embarcacion se ha hundido*/
    private boolean sunk;
    /**Cantidad de casillas que emplea la embarcacion*/
    private final int valueShip;

    /**
     * Constructor de la clase.
     * @param shipType tipo de embarcacion.
     * @param valueShip cantidad de casillas que va a ocupar.
     */
    public Ship(ShipType shipType, int valueShip) {
        this.shipType = shipType;
        this.orientation= Orientation.VERTICAL;
        this.sunk = false;
        this.valueShip = valueShip;
    }
    /**
     * Metodo que retorna el tipo de barco.
     * @return tipo de embarcacion.
     */
    public ShipType getShipType() {
        return shipType;
    }
    /**
     * Metodo que retorna la orientacion del barco.
     * @return orientacion de la embarcacion.
     */
    public Orientation getOrientation() {
        return orientation;
    }
    /**
     * Metodo que retorna las casillas ocupadas por la embarcacion.
     * @return arreglo con las casillas que ocupa la embarcacion.
     */
    public List<Coordinate> getOccupiedCoordinates() {
        return occupiedCoordinates;
    }
    /**
     * Metodo que retorna la cantidad de veces que la embarcacion ha recibido un ataque.
     * @return cantidad de ataques recibidos almacenados en hitCount.
     */
    public int getHitCount() {
        return hitCount;
    }
    /**
     * Metodo que retorna el booleano, que indica si el barco se ha hundido.
     * @return un booleano que por defecto es falso, pero si se da el caso se declara como true al ser atacadas todas sus coordenadas.
     */
    public boolean isSunk() {
        return sunk;
    }
    /**
     * Metodo que cambia es valor del booleano sunk a true.
     */
    public void sunkTheShip() {
       sunk= true;
    }
    /**
     * Metodo que retorna la cantidad de coordenadas que ocupa la embarcacion(puede que se borre, no le veo uso).
     * @return Cantidad de casillas que ocupa la embarcacion.
     */
    public int getValueShip() {
        return valueShip;
    }
}
