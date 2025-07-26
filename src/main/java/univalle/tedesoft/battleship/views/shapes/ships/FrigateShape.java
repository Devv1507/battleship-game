package univalle.tedesoft.battleship.views.shapes.ships;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;
import univalle.tedesoft.battleship.views.shapes.IShape;

/**
 * Representa la forma 2D de una Fragata.
 * Hereda de ShipShape e implementa la lógica de dibujo específica para este tipo de barco.
 */
public class FrigateShape implements IShape {

    /**
     * Construye y devuelve la representación visual de una Fragata.
     * Es la embarcación más pequeña, con detalles como un mástil con bandera.
     * @return Un nodo (Group) de JavaFX que contiene todas las formas de la Fragata.
     */
    @Override
    public Node createShape() {
        Group group = new Group();

        // Casco principal con gradiente
        Rectangle hull = new Rectangle(CELL_SIZE, CELL_SIZE * 0.5);
        hull.setY(CELL_SIZE * 0.25);
        LinearGradient hullGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#4682B4")),
                new Stop(0.5, Color.web("#5F9EA0")),
                new Stop(1, Color.web("#2F4F4F")));
        hull.setFill(hullGradient);
        hull.setStroke(Color.BLACK);
        hull.setStrokeWidth(1.5);

        // Proa con gradiente
        Polygon bow = new Polygon();
        bow.getPoints().addAll(
                CELL_SIZE * 1.0, CELL_SIZE * 0.3,
                CELL_SIZE * 1.25, CELL_SIZE * 0.5,
                CELL_SIZE * 1.0, CELL_SIZE * 0.7
        );
        bow.setFill(hullGradient);
        bow.setStroke(Color.BLACK);
        bow.setStrokeWidth(1.5);

        // Línea de flotación
        Line waterline = new Line(0, CELL_SIZE * 0.6, CELL_SIZE * 1.25, CELL_SIZE * 0.6);
        waterline.setStroke(Color.DARKRED);
        waterline.setStrokeWidth(2);

        // Cabina con gradiente
        Rectangle cabin = new Rectangle(CELL_SIZE * 0.25, CELL_SIZE * 0.05, CELL_SIZE * 0.5, CELL_SIZE * 0.2);
        LinearGradient cabinGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#B0C4DE")),
                new Stop(0.5, Color.web("#87CEEB")),
                new Stop(1, Color.web("#4682B4")));
        cabin.setFill(cabinGradient);
        cabin.setStroke(Color.BLACK);
        cabin.setStrokeWidth(1);

        // Ventanas de la cabina
        Rectangle window1 = new Rectangle(CELL_SIZE * 0.3, CELL_SIZE * 0.1, CELL_SIZE * 0.08, CELL_SIZE * 0.05);
        Rectangle window2 = new Rectangle(CELL_SIZE * 0.42, CELL_SIZE * 0.1, CELL_SIZE * 0.08, CELL_SIZE * 0.05);
        Rectangle window3 = new Rectangle(CELL_SIZE * 0.54, CELL_SIZE * 0.1, CELL_SIZE * 0.08, CELL_SIZE * 0.05);
        window1.setFill(Color.LIGHTCYAN);
        window2.setFill(Color.LIGHTCYAN);
        window3.setFill(Color.LIGHTCYAN);
        window1.setStroke(Color.DARKBLUE);
        window2.setStroke(Color.DARKBLUE);
        window3.setStroke(Color.DARKBLUE);

        // Mástil con gradiente
        Rectangle mast = new Rectangle(CELL_SIZE * 0.48, -CELL_SIZE * 0.25, CELL_SIZE * 0.04, CELL_SIZE * 0.3);
        LinearGradient mastGradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#8B4513")),
                new Stop(0.5, Color.web("#A0522D")),
                new Stop(1, Color.web("#654321")));
        mast.setFill(mastGradient);
        mast.setStroke(Color.BLACK);
        mast.setStrokeWidth(1);

        // Bandera ondeando
        Polygon flag = new Polygon();
        flag.getPoints().addAll(
                CELL_SIZE * 0.5, -CELL_SIZE * 0.25,
                CELL_SIZE * 0.8, -CELL_SIZE * 0.15,
                CELL_SIZE * 0.75, -CELL_SIZE * 0.1,
                CELL_SIZE * 0.5, -CELL_SIZE * 0.18
        );
        LinearGradient flagGradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.RED),
                new Stop(0.5, Color.DARKRED),
                new Stop(1, Color.FIREBRICK));
        flag.setFill(flagGradient);
        flag.setStroke(Color.DARKRED);
        flag.setStrokeWidth(0.5);

        // Cables del mástil (aparejos)
        Line rigging1 = new Line(CELL_SIZE * 0.5, -CELL_SIZE * 0.1, CELL_SIZE * 0.2, CELL_SIZE * 0.05);
        Line rigging2 = new Line(CELL_SIZE * 0.5, -CELL_SIZE * 0.1, CELL_SIZE * 0.8, CELL_SIZE * 0.05);
        rigging1.setStroke(Color.DARKGRAY);
        rigging2.setStroke(Color.DARKGRAY);
        rigging1.setStrokeWidth(1);
        rigging2.setStrokeWidth(1);

        // Pequeño cañón
        Circle gun = new Circle(CELL_SIZE * 0.15, CELL_SIZE * 0.35, CELL_SIZE * 0.05);
        gun.setFill(Color.DARKKHAKI);
        gun.setStroke(Color.BLACK);

        // Sombra
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.3));
        shadow.setOffsetX(1);
        shadow.setOffsetY(1);
        shadow.setRadius(2);

        group.getChildren().addAll(hull, bow, waterline, cabin,
                window1, window2, window3, mast, flag,
                rigging1, rigging2, gun);
        group.setEffect(shadow);
        return group;
    }
}
