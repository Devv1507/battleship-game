package univalle.tedesoft.battleship.views.shapes;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

/**
 * Representa la forma 2D de un Portaaviones (Air-Craft Carrier).
 * Hereda de ShipShape e implementa la lógica de dibujo específica para este tipo de barco.
 */
public class AircraftShape extends ShipShape {

    /**
     * Construye y devuelve la representación visual de un Portaaviones.
     * El tamaño y la posición de cada componente se basan en la constante CELL_SIZE.
     * @return Un nodo (Group) de JavaFX que contiene todas las formas del Portaaviones.
     */
    @Override
    public Node createShape() {
        Group group = new Group();

        // Línea de flotación roja
        Rectangle waterline = new Rectangle(CELL_SIZE * 4.5, CELL_SIZE * 0.15);
        waterline.setY(CELL_SIZE * 0.75);
        waterline.setFill(Color.DARKRED);

        // Casco principal gris oscuro
        Rectangle hull = new Rectangle(CELL_SIZE * 4.5, CELL_SIZE * 0.4);
        hull.setY(CELL_SIZE * 0.35);
        hull.setFill(Color.web("#2F2F2F"));

        // Proa triangular
        Polygon bow = new Polygon();
        bow.getPoints().addAll(
                CELL_SIZE * 4.5, CELL_SIZE * 0.4,
                CELL_SIZE * 4.8, CELL_SIZE * 0.55,
                CELL_SIZE * 4.5, CELL_SIZE * 0.7
        );
        bow.setFill(Color.web("#2F2F2F"));

        // Cubierta superior
        Rectangle deck = new Rectangle(CELL_SIZE * 4.2, CELL_SIZE * 0.25);
        deck.setX(CELL_SIZE * 0.15);
        deck.setY(CELL_SIZE * 0.1);
        deck.setFill(Color.web("#404040"));

        // Isla/Torre de control (término naval correcto para portaaviones)
        Rectangle island = new Rectangle(CELL_SIZE * 0.6, CELL_SIZE * 0.5);
        island.setX(CELL_SIZE * 0.8);
        island.setY(-CELL_SIZE * 0.15);
        island.setFill(Color.web("#404040"));

        // Chimenea
        Rectangle funnel = new Rectangle(CELL_SIZE * 0.15, CELL_SIZE * 0.3);
        funnel.setX(CELL_SIZE * 1.0);
        funnel.setY(-CELL_SIZE * 0.15);
        funnel.setFill(Color.web("#505050"));

        // Antenas/Mástiles
        Rectangle mast1 = new Rectangle(CELL_SIZE * 0.04, CELL_SIZE * 0.4);
        mast1.setX(CELL_SIZE * 0.6);
        mast1.setY(-CELL_SIZE * 0.25);
        mast1.setFill(Color.web("#1A1A1A"));

        Rectangle mast2 = new Rectangle(CELL_SIZE * 0.04, CELL_SIZE * 0.35);
        mast2.setX(CELL_SIZE * 1.5);
        mast2.setY(-CELL_SIZE * 0.2);
        mast2.setFill(Color.web("#1A1A1A"));

        // Estructuras adicionales
        Rectangle structure1 = new Rectangle(CELL_SIZE * 0.3, CELL_SIZE * 0.15);
        structure1.setX(CELL_SIZE * 0.3);
        structure1.setY(-CELL_SIZE * 0.05);
        structure1.setFill(Color.web("#404040"));

        Rectangle structure2 = new Rectangle(CELL_SIZE * 0.25, CELL_SIZE * 0.12);
        structure2.setX(CELL_SIZE * 3.8);
        structure2.setY(-CELL_SIZE * 0.02);
        structure2.setFill(Color.web("#404040"));

        group.getChildren().addAll(waterline, hull, bow, deck, island, funnel,
                mast1, mast2, structure1, structure2);
        return group;
    }
}
