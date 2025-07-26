package univalle.tedesoft.battleship.views.shapes.shots;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import univalle.tedesoft.battleship.views.shapes.IShape;

/**
 * Representa la forma visual para un disparo que IMPACTA un barco (TOUCHED).
 * Siguiendo la nueva especificación, crea un cuadrado rojo semitransparente con una 'X' blanca encima.
 */
public class TouchedMarkerShape implements IShape {
    double CELL_SIZE = 40;

    /**
     * Construye la representación visual de un impacto.
     * @return Un nodo (Group) de JavaFX que contiene el fondo rojo y la 'X'.
     */
    @Override
    public Node createShape() {
        Group group = new Group();

        // Fondo rojo semitransparente. Opacidad del 40% (se ve el fondo en un 60%).
        Rectangle background = new Rectangle(0, 0, CELL_SIZE, CELL_SIZE);
        background.setFill(Color.web("#DC143C", 0.4)); // Crimson con opacidad
        background.setArcWidth(10);
        background.setArcHeight(10);

        // La 'X' blanca para indicar claramente un impacto.
        double padding = CELL_SIZE * 0.2;

        Line line1 = new Line(padding, padding, CELL_SIZE - padding, CELL_SIZE - padding);
        Line line2 = new Line(padding, CELL_SIZE - padding, CELL_SIZE - padding, padding);

        // Estilo de la 'X'
        Color crossColor = Color.web("#FFFFFF", 0.7);
        double strokeWidth = 4.0;

        line1.setStroke(crossColor);
        line1.setStrokeWidth(strokeWidth);
        line1.setStrokeLineCap(StrokeLineCap.ROUND);

        line2.setStroke(crossColor);
        line2.setStrokeWidth(strokeWidth);
        line2.setStrokeLineCap(StrokeLineCap.ROUND);

        group.getChildren().addAll(background, line1, line2);
        group.setMouseTransparent(true);

        return group;
    }
}
