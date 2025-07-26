package univalle.tedesoft.battleship.views.shapes;

import javafx.scene.Node;

/**
 * Interfaz que define el contrato para todas las representaciones visuales (barcos y marcadores)
 * Cada clase que implemente esta interfaz será responsable de crear una forma específica,
 * como un barco o un marcador, y devolver un nodo de JavaFX que lo represente.
 */
public interface IShape {

    /**
     * Tamaño de la celda en la que se dibujará la forma.
     */
    double CELL_SIZE = 30.0;

    /**
     * Crea y devuelve el nodo de JavaFX que representa visualmente el objeto que implementa esta interfaz.
     * @return Un nodo de JavaFX (generalmente un Group) que representa el barco o el marcador.
     */
    Node createShape();
}
