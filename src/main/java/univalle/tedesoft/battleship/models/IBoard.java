package univalle.tedesoft.battleship.models;

import univalle.tedesoft.battleship.exceptions.OutOfBoundsException;
import univalle.tedesoft.battleship.exceptions.OverlapException;
import univalle.tedesoft.battleship.models.enums.CellState;
import univalle.tedesoft.battleship.models.ships.Ship;

import java.util.List;

/**
 * Interfaz que define el contrato para cualquier tablero del juego Battleship.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public interface IBoard {
    /**
     * Intenta colocar un barco en el tablero.
     * @param ship El barquito que quieres poner.
     * @param startCoordinate La esquina donde empieza el barco.
     * @return true si se pudo colocar, false si no.
     * @throws OutOfBoundsException Si el barco se sale de los limites.
     * @throws OverlapException Si el barco choca con otro.
     */
    boolean placeShip(Ship ship, Coordinate startCoordinate) throws OutOfBoundsException, OverlapException;
    /**
     * Quita un barco del tablero.
     * @param shipToRemove El barco que quieres quitar.
     * @return true si se encontro y se quito, false si no.
     */
    boolean removeShip(Ship shipToRemove);
    /**
     * Procesa un disparo en una coordenada.
     * @param targetCoordinate La casilla a la que se le dispara.
     * @return El resultado del disparo (agua, tocado o hundido).
     * @throws OutOfBoundsException Si el disparo es fuera del tablero.
     * @throws OverlapException Si ya se habia disparado ahi.
     */
    ShotOutcome receiveShot(Coordinate targetCoordinate) throws OutOfBoundsException, OverlapException;
    /**
     * Te dice que hay en una casilla especifica.
     * @param row La fila.
     * @param col La columna.
     * @return El estado de la celda (agua, barco, etc.).
     * @throws OutOfBoundsException Si la coordenada no existe.
     */
    CellState getCellState(int row, int col) throws OutOfBoundsException;
    /**
     * Cambia el estado de una casilla.
     * @param row La fila.
     * @param col La columna.
     * @param state El nuevo estado que tendra la celda.
     * @throws OutOfBoundsException Si la coordenada no existe.
     */
    void setCellState(int row, int col, CellState state) throws OutOfBoundsException;
    /**
     * Revisa si todos los barcos del tablero ya fueron hundidos.
     * @return true si todos estan hundidos, false si no.
     */
    boolean areAllShipsSunk();
    /**
     * Te da la lista de todos los barcos que hay en el tablero.
     * @return Una lista con los barcos.
     */
    List<Ship> getShips();
    /**
     * Agrega un barco al tablero sin hacer validaciones.
     * Util principalmente para cargar una partida guardada.
     * @param ship El barco a agregar.
     */
    void addShipDirectly(Ship ship);
    /**
     * Te dice cual barco esta en una coordenada especifica.
     * @param row La fila.
     * @param col La columna.
     * @return El barco que esta en esa casilla, o null si no hay ninguno.
     */
    Ship getShipAt(int row, int col);
    /**
     * Te dice el tamaño del tablero (normalmente 10).
     * @return El tamaño del tablero.
     */
    int getSize();
    /**
     * Revisa si una coordenada (fila, columna) es valida.
     * @param row La fila.
     * @param col La columna.
     * @return true si la coordenada esta dentro del tablero.
     */
    boolean isValidCoordinate(int row, int col);
    /**
     * Revisa si una coordenada (como objeto) es valida.
     * @param coordinate El objeto coordenada a revisar.
     * @return true si la coordenada esta dentro del tablero.
     */
    boolean isValidCoordinate(Coordinate coordinate);
    /**
     * Limpia el tablero y lo deja como nuevo, sin barcos y todo en AGUA.
     */
    void resetBoard();
    /**
     * Quita todos los barcos de la lista, pero no limpia el estado de las casillas.
     * Util para cargar partidas.
     */
    void clearShipsOnly();
}
