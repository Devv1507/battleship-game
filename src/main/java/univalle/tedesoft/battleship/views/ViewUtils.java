package univalle.tedesoft.battleship.views;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

/**
 * Clase de utilidad que proporciona métodos estáticos comunes para la interfaz de usuario.
 * Esta clase no puede ser instanciada y agrupa funcionalidades reutilizables
 * como mostrar alertas o aplicar efectos visuales a los componentes.
 */
public final class ViewUtils {

    /**
     * Constructor privado para prevenir la instanciación de la clase de utilidad.
     */
    private ViewUtils() {}

    /**
     * Muestra una alerta en la pantalla.
     * @param alertType El tipo de alerta (ERROR, WARNING, INFORMATION).
     * @param title El título de la ventana de alerta.
     * @param content El mensaje principal de la alerta.
     */
    public static void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Aplica un efecto de "hover" a un botón específico, haciendo que se agrande y su sombra cambie.
     * @param button El botón al cual se le aplicará el efecto.
     */
    public static void applyHoverScaleEffect(Button button) {
        if (button != null) {
            final DropShadow shadow = (DropShadow) button.getEffect();

            // Guardamos todas las propiedades originales del efecto.
            final double originalRadius = shadow.getRadius();
            final double originalOffsetX = shadow.getOffsetX();
            final double originalOffsetY = shadow.getOffsetY();

            // Evento que se dispara cuando el mouse entra en el área del botón.
            button.setOnMouseEntered(event -> {
                button.setScaleX(1.05);
                button.setScaleY(1.05);
                if(button.getEffect() instanceof DropShadow){
                    ((DropShadow) button.getEffect()).setRadius(originalRadius * 2);
                    ((DropShadow) button.getEffect()).setOffsetX(originalOffsetX * 2);
                    ((DropShadow) button.getEffect()).setOffsetY(originalOffsetY * 2);
                }
            });

            // Evento que se dispara cuando el mouse sale del área del botón.
            button.setOnMouseExited(event -> {
                button.setScaleX(1.0);
                button.setScaleY(1.0);
                if(button.getEffect() instanceof DropShadow){
                    ((DropShadow) button.getEffect()).setRadius(originalRadius);
                    ((DropShadow) button.getEffect()).setOffsetX(originalOffsetX);
                    ((DropShadow) button.getEffect()).setOffsetY(originalOffsetY);
                }
            });
        }
    }

    /**
     * Traduce las fases del juego a texto más amigable para el usuario.
     * @param phase La fase del juego en inglés.
     * @return La traducción en español.
     */
    public static String translateGamePhase(String phase) {
        if (phase == null) return "Desconocida";
        return switch (phase.toUpperCase()) {
            case "INITIAL" -> "Inicial";
            case "PLACEMENT" -> "Colocación de Barcos";
            case "FIRING", "BATTLE" -> "En Batalla"; // Agregado BATTLE por si acaso.
            case "GAME_OVER" -> "Juego Terminado";
            default -> phase;
        };
    }
}