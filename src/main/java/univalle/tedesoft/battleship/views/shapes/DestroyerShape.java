package univalle.tedesoft.battleship.views.shapes;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;

/**
 * Representa la forma 2D de un Destructor.
 * Hereda de ShipShape e implementa la lógica de dibujo específica para este tipo de barco.
 */
public class DestroyerShape extends ShipShape {

    /**
     * Construye y devuelve la representación visual de un Destructor.
     * Incluye detalles como cañones, chimeneas con humo y superestructuras.
     * @return Un nodo (Group) de JavaFX que contiene todas las formas del Destructor.
     */
    @Override
    public Node createShape() {
        Group group = new Group();

        // Casco principal con gradiente militar
        Rectangle hull = new Rectangle(CELL_SIZE * 2, CELL_SIZE * 0.6);
        hull.setY(CELL_SIZE * 0.2);
        LinearGradient hullGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#708090")),
                new Stop(0.5, Color.web("#556B70")),
                new Stop(1, Color.web("#2F4F4F")));
        hull.setFill(hullGradient);
        hull.setStroke(Color.BLACK);
        hull.setStrokeWidth(1.5);

        // Proa con gradiente
        Polygon bow = new Polygon();
        bow.getPoints().addAll(
                CELL_SIZE * 2.0, CELL_SIZE * 0.25,
                CELL_SIZE * 2.5, CELL_SIZE * 0.5,
                CELL_SIZE * 2.0, CELL_SIZE * 0.75
        );
        bow.setFill(hullGradient);
        bow.setStroke(Color.BLACK);
        bow.setStrokeWidth(1.5);

        // Línea de flotación
        Line waterline = new Line(0, CELL_SIZE * 0.65, CELL_SIZE * 2.5, CELL_SIZE * 0.65);
        waterline.setStroke(Color.DARKRED);
        waterline.setStrokeWidth(3);

        // Superestructura con gradiente
        Rectangle superstructure = new Rectangle(CELL_SIZE * 0.6, -CELL_SIZE * 0.15, CELL_SIZE * 0.8, CELL_SIZE * 0.35);
        LinearGradient superstructureGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#D3D3D3")),
                new Stop(0.5, Color.web("#A9A9A9")),
                new Stop(1, Color.web("#696969")));
        superstructure.setFill(superstructureGradient);
        superstructure.setStroke(Color.BLACK);
        superstructure.setStrokeWidth(1);

        // Ventanas del puente
        for (int i = 0; i < 4; i++) {
            Rectangle window = new Rectangle(CELL_SIZE * (0.65 + i * 0.15), -CELL_SIZE * 0.08,
                    CELL_SIZE * 0.08, CELL_SIZE * 0.05);
            window.setFill(Color.LIGHTCYAN);
            window.setStroke(Color.DARKBLUE);
            window.setStrokeWidth(0.5);
            group.getChildren().add(window);
        }

        // Cañones principales con gradiente
        Ellipse mainGun1 = new Ellipse(CELL_SIZE * 0.4, CELL_SIZE * 0.1, CELL_SIZE * 0.15, CELL_SIZE * 0.08);
        Ellipse mainGun2 = new Ellipse(CELL_SIZE * 1.6, CELL_SIZE * 0.1, CELL_SIZE * 0.15, CELL_SIZE * 0.08);
        LinearGradient gunGradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#556B2F")),
                new Stop(0.5, Color.web("#6B8E23")),
                new Stop(1, Color.web("#228B22")));
        mainGun1.setFill(gunGradient);
        mainGun2.setFill(gunGradient);
        mainGun1.setStroke(Color.BLACK);
        mainGun2.setStroke(Color.BLACK);

        // Cañones de artillería
        Rectangle gunBarrel1 = new Rectangle(CELL_SIZE * 0.32, CELL_SIZE * 0.06, CELL_SIZE * 0.16, CELL_SIZE * 0.08);
        Rectangle gunBarrel2 = new Rectangle(CELL_SIZE * 1.52, CELL_SIZE * 0.06, CELL_SIZE * 0.16, CELL_SIZE * 0.08);
        gunBarrel1.setFill(Color.DARKSLATEGRAY);
        gunBarrel2.setFill(Color.DARKSLATEGRAY);
        gunBarrel1.setStroke(Color.BLACK);
        gunBarrel2.setStroke(Color.BLACK);

        // Chimeneas con humo
        Rectangle funnel1 = new Rectangle(CELL_SIZE * 0.9, -CELL_SIZE * 0.4, CELL_SIZE * 0.12, CELL_SIZE * 0.25);
        Rectangle funnel2 = new Rectangle(CELL_SIZE * 1.1, -CELL_SIZE * 0.35, CELL_SIZE * 0.1, CELL_SIZE * 0.2);
        funnel1.setFill(Color.DARKRED);
        funnel2.setFill(Color.FIREBRICK);
        funnel1.setStroke(Color.BLACK);
        funnel2.setStroke(Color.BLACK);

        // Humo
        Circle smoke1 = new Circle(CELL_SIZE * 0.96, -CELL_SIZE * 0.45, CELL_SIZE * 0.08);
        Circle smoke2 = new Circle(CELL_SIZE * 0.98, -CELL_SIZE * 0.55, CELL_SIZE * 0.06);
        Circle smoke3 = new Circle(CELL_SIZE * 1.15, -CELL_SIZE * 0.4, CELL_SIZE * 0.05);
        smoke1.setFill(Color.web("#696969", 0.6));
        smoke2.setFill(Color.web("#696969", 0.4));
        smoke3.setFill(Color.web("#696969", 0.5));

        // Sombra
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.4));
        shadow.setOffsetX(2);
        shadow.setOffsetY(2);
        shadow.setRadius(4);

        group.getChildren().addAll(hull, bow, waterline, superstructure,
                mainGun1, mainGun2, gunBarrel1, gunBarrel2,
                funnel1, funnel2, smoke1, smoke2, smoke3);
        group.setEffect(shadow);
        return group;
    }
}
