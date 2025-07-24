package univalle.tedesoft.battleship.views.shapes;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

/**
 * Representa la forma visual para un disparo que cae en el AGUA.
 * Crea un cuadrado azul semitransparente con una 'X' blanca encima.
 */
public class WaterMarkerShape implements IMarkerShape {

    /**
     * Construye la representación visual de un fallo en el agua.
     * @return Un nodo (Group) de JavaFX que contiene el fondo y la 'X'.
     */
    @Override
    public Node createMarker() {
        Group group = new Group();

        // Fondo azul claro semitransparente para el efecto de agua.
        // Opacidad del 40% (se ve el fondo en un 60%).
        Rectangle background = new Rectangle(0, 0, CELL_SIZE, CELL_SIZE);
        background.setFill(Color.web("#87CEEB", 0.4)); // LightSkyBlue con opacidad
        background.setArcWidth(10);
        background.setArcHeight(10);

        double padding = CELL_SIZE * 0.2; // Un pequeño margen

        Line line1 = new Line(padding, padding, CELL_SIZE - padding, CELL_SIZE - padding);
        Line line2 = new Line(padding, CELL_SIZE - padding, CELL_SIZE - padding, padding);

        // Estilo de la 'X'
        Color crossColor = Color.web("#FFFFFF", 0.7); // Blanco con opacidad
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
