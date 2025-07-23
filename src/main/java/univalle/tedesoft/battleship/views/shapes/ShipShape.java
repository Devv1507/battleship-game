package univalle.tedesoft.battleship.views.shapes;

import javafx.scene.Node;

/**
 * Clase base abstracta para todas las representaciones visuales (formas 2D) de los barcos.
 * Define el contrato que todas las formas de barco deben seguir, asegurando que cada una
 * pueda generar su propia representación gráfica en JavaFX.
 * Esta abstracción permite tratar a todas las formas de manera polimórfica.
 */
public abstract class ShipShape {

    /**
     * Tamaño base de una celda del tablero.
     * Todas las dimensiones de las formas se calculan en relación a este valor
     * para asegurar una escala consistente y facilitar el ajuste del tamaño
     * de todos los barcos desde un único lugar.
     */
    protected static final double CELL_SIZE = 30.0;

    /**
     * Metodo abstracto que las clases concretas deben implementar.
     * Su responsabilidad es construir y devolver el conjunto de nodos de JavaFX
     * que componen la representación visual del barco.
     *
     * @return Un nodo de JavaFX (generalmente un Group) que representa el barco.
     */
    public abstract Node createShape();
}
