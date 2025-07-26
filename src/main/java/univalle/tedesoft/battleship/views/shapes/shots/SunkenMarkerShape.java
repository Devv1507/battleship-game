package univalle.tedesoft.battleship.views.shapes.shots;

import javafx.scene.Node;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import univalle.tedesoft.battleship.views.shapes.IShape;

/**
 * Representa la forma visual para un disparo que HUNDE un barco (SUNKEN).
 * Esta versión se enfoca únicamente en crear la forma y el color correctos,
 * dejando el escalado y posicionamiento a la vista principal.
 */
public class SunkenMarkerShape implements IShape {
    double CELL_SIZE = 40;

    /**
     * Construye la representación visual de un barco hundido con efecto de llama.
     * @return Un nodo Path de JavaFX que representa la llama a tamaño completo (40x40).
     */
    @Override
    public Node createShape() {
        Path flamePath = new Path();

        MoveTo startPoint = new MoveTo(CELL_SIZE * 0.5, CELL_SIZE);
        CubicCurveTo leftCurve = new CubicCurveTo(
                CELL_SIZE * 0.1, CELL_SIZE * 0.95,
                0, CELL_SIZE * 0.6,
                CELL_SIZE * 0.5, CELL_SIZE * 0.05
        );
        CubicCurveTo rightCurve = new CubicCurveTo(
                CELL_SIZE, CELL_SIZE * 0.6,
                CELL_SIZE * 0.9, CELL_SIZE * 0.95,
                CELL_SIZE * 0.5, CELL_SIZE
        );
        flamePath.getElements().addAll(startPoint, leftCurve, rightCurve);

        // El parámetro 'proportional' se establece en 'true' para que los valores de
        // centro y radio se interpreten como porcentajes del tamaño de la forma.
        RadialGradient gradient = new RadialGradient(
                0, 0,
                0.5, 0.5, // Centro del gradiente (50% x, 50% y)
                0.6,      // Radio del 60% del tamaño de la forma
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.YELLOW),         // Relleno amarillo en el centro
                new Stop(0.7, Color.ORANGERED),
                new Stop(1.0, Color.CRIMSON)         // Rojo intenso en el borde
        );
        flamePath.setFill(gradient);

        flamePath.setStroke(Color.DARKRED.darker());
        flamePath.setStrokeWidth(1.5);
        flamePath.setEffect(new Glow(0.6));
        flamePath.setMouseTransparent(true);

        return flamePath;
    }
}
