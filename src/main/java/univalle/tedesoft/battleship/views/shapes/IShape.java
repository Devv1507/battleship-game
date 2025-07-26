package univalle.tedesoft.battleship.views.shapes;

import javafx.scene.Node;

/**
 * Interfaz que define el contrato para todas las representaciones visuales de los marcadores de disparo.
 * Cada clase que implemente esta interfaz será responsable de crear una forma específica
 * (agua, humo, llama) para representar el resultado de un disparo en el tablero.
 */
public interface IShape {

    /**
     * Tamaño de la celda en la que se dibujará el marcador.
     * Es útil tenerlo como una constante compartida.
     */
    double CELL_SIZE = 30.0;

    /**
     * Crea y devuelve el nodo de JavaFX que representa visualmente el marcador.
     *
     * @return Un nodo de JavaFX (generalmente un Group) que representa el marcador.
     */
    Node createShape();
}
