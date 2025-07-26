package univalle.tedesoft.battleship.views.shapes.ships;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;
import univalle.tedesoft.battleship.views.shapes.IShape;

/**
 * Representa la forma 2D de un Submarino.
 * Hereda de ShipShape e implementa la lógica de dibujo específica para este tipo de barco.
 */
public class SubmarineShape implements IShape {

    /**
     * Construye y devuelve la representación visual de un Submarino.
     * Utiliza gradientes y efectos de sombra para un mayor realismo.
     * @return Un nodo (Group) de JavaFX que contiene todas las formas del Submarino.
     */
    @Override
    public Node createShape() {
        Group group = new Group();

        // Casco principal con gradiente submarino
        Ellipse hull = new Ellipse(CELL_SIZE * 1.5, CELL_SIZE * 0.5, CELL_SIZE * 1.5, CELL_SIZE * 0.4);
        LinearGradient hullGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1E3A8A")),
                new Stop(0.5, Color.web("#1E40AF")),
                new Stop(1, Color.web("#1E1B4B")));
        hull.setFill(hullGradient);
        hull.setStroke(Color.BLACK);
        hull.setStrokeWidth(1.5);

        // Efecto de profundidad del casco
        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setColor(Color.web("#000080", 0.3));
        innerShadow.setOffsetX(2);
        innerShadow.setOffsetY(2);
        hull.setEffect(innerShadow);

        // Líneas de remaches en el casco
        for (int i = 0; i < 6; i++) {
            Circle rivet = new Circle(CELL_SIZE * (0.5 + i * 0.4), CELL_SIZE * 0.3, CELL_SIZE * 0.02);
            rivet.setFill(Color.SILVER);
            rivet.setStroke(Color.DARKGRAY);
            rivet.setStrokeWidth(0.5);
            group.getChildren().add(rivet);
        }

        // Torre de mando (vela) con gradiente
        Rectangle conningTower = new Rectangle(CELL_SIZE * 1.2, -CELL_SIZE * 0.1, CELL_SIZE * 0.25, CELL_SIZE * 0.4);
        LinearGradient towerGradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1E3A8A")),
                new Stop(1, Color.web("#1E1B4B")));
        conningTower.setFill(towerGradient);
        conningTower.setStroke(Color.BLACK);
        conningTower.setStrokeWidth(1);

        // Periscopio con detalles
        Line periscope = new Line(CELL_SIZE * 1.32, -CELL_SIZE * 0.1, CELL_SIZE * 1.32, -CELL_SIZE * 0.5);
        periscope.setStroke(Color.DARKSLATEGRAY);
        periscope.setStrokeWidth(3);

        // Lente del periscopio
        Circle lens = new Circle(CELL_SIZE * 1.32, -CELL_SIZE * 0.5, CELL_SIZE * 0.04);
        lens.setFill(Color.LIGHTBLUE);
        lens.setStroke(Color.DARKBLUE);
        lens.setStrokeWidth(1);

        // Hélice con gradiente dorado
        Ellipse propeller = new Ellipse(CELL_SIZE * 0.15, CELL_SIZE * 0.5, CELL_SIZE * 0.15, CELL_SIZE * 0.25);
        LinearGradient propellerGradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.GOLD),
                new Stop(0.5, Color.GOLDENROD),
                new Stop(1, Color.DARKGOLDENROD));
        propeller.setFill(propellerGradient);
        propeller.setStroke(Color.DARKGOLDENROD);
        propeller.setStrokeWidth(1);

        // Aspas de la hélice
        Line blade1 = new Line(CELL_SIZE * 0.05, CELL_SIZE * 0.35, CELL_SIZE * 0.25, CELL_SIZE * 0.65);
        Line blade2 = new Line(CELL_SIZE * 0.05, CELL_SIZE * 0.65, CELL_SIZE * 0.25, CELL_SIZE * 0.35);
        blade1.setStroke(Color.DARKGOLDENROD);
        blade2.setStroke(Color.DARKGOLDENROD);
        blade1.setStrokeWidth(2);
        blade2.setStrokeWidth(2);

        // Sombra
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.4));
        shadow.setOffsetX(2);
        shadow.setOffsetY(2);
        shadow.setRadius(3);

        group.getChildren().addAll(hull, conningTower, periscope, lens, propeller, blade1, blade2);
        group.setEffect(shadow);
        return group;
    }
}
