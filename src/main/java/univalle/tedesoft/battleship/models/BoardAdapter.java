package univalle.tedesoft.battleship.models;

import univalle.tedesoft.battleship.exceptions.OutOfBoundsException;
import univalle.tedesoft.battleship.exceptions.OverlapException;
import univalle.tedesoft.battleship.models.enums.CellState;
import univalle.tedesoft.battleship.models.ships.Ship;

import java.util.Collections;
import java.util.List;

/**
 * Clase adaptadora abstracta que proporciona una implementación por defecto
 * (vacía) para la interfaz IBoard. Permite a las subclases sobreescribir
 * solo los métodos que necesitan.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public abstract class BoardAdapter implements IBoard {
    /** Implementacion por defecto. No hace nada y devuelve false. */
    @Override
    public boolean placeShip(Ship ship, Coordinate startCoordinate) throws OutOfBoundsException, OverlapException {
        return false;
    }
    /** Implementacion por defecto. No hace nada y devuelve false. */
    @Override
    public boolean removeShip(Ship shipToRemove) {
        return false;
    }
    /** Implementacion por defecto. No hace nada y devuelve null. */
    @Override
    public ShotOutcome receiveShot(Coordinate targetCoordinate) throws OutOfBoundsException, OverlapException {
        return null;
    }
    /** Implementacion por defecto. No hace nada y devuelve null. */
    @Override
    public CellState getCellState(int row, int col) throws OutOfBoundsException {
        return null;
    }
    /** Implementacion por defecto. No hace nada. */
    @Override
    public void setCellState(int row, int col, CellState state) throws OutOfBoundsException {
        // Implementación vacía
    }
    /** Implementacion por defecto. No hace nada y devuelve false. */
    @Override
    public boolean areAllShipsSunk() {
        return false;
    }
    /** Implementacion por defecto. Devuelve una lista vacia para evitar errores. */
    @Override
    public List<Ship> getShips() {
        return Collections.emptyList(); // Devuelve una lista vacía por defecto
    }
    /** Implementacion por defecto. No hace nada. */
    @Override
    public void addShipDirectly(Ship ship) {
        // Implementación vacía
    }
    /** Implementacion por defecto. No hace nada y devuelve null. */
    @Override
    public Ship getShipAt(int row, int col) {
        return null;
    }
    /** Implementacion por defecto. Devuelve 0. */
    @Override
    public int getSize() {
        return 0;
    }
    /** Implementacion por defecto. */
    @Override
    public boolean isValidCoordinate(int row, int col) {
        return false;
    }
    /** Implementacion por defecto. No hace nada y devuelve false. */
    @Override
    public boolean isValidCoordinate(Coordinate coordinate) {
        return false;
    }
    /** Implementacion por defecto. No hace nada. */
    @Override
    public void resetBoard() {
        // Implementación vacía
    }
    /** Implementacion por defecto. No hace nada. */
    @Override
    public void clearShipsOnly() {
        // Implementación vacía
    }
}
