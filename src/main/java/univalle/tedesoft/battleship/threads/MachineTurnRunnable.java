package univalle.tedesoft.battleship.threads;

import javafx.application.Platform;
import univalle.tedesoft.battleship.controllers.GameController;

/**
 * Runnable que gestiona el turno de la máquina en un hilo separado.
 * Introduce un retraso para simular el "pensamiento" de la máquina antes
 * de ejecutar la lógica del juego en el hilo de la interfaz de usuario de JavaFX.
 */
public class MachineTurnRunnable implements Runnable {

    private final GameController gameController;
    private final long thinkDelayMs;

    /**
     * Constructor para MachineTurnRunnable.
     * @param gameController La instancia del controlador del juego.
     * @param thinkDelayMs El tiempo en milisegundos que la máquina "pensará" antes de actuar.
     */
    public MachineTurnRunnable(GameController gameController, long thinkDelayMs) {
        this.gameController = gameController;
        this.thinkDelayMs = thinkDelayMs;
    }

    /**
     * Simula el pensamiento de la máquina y luego ejecuta su lógica de turno
     * en el hilo de la interfaz de usuario de JavaFX.
     */
    @Override
    public void run() {
        try {
            // Simular que la máquina está "pensando"
            Thread.sleep(this.thinkDelayMs);

            // La lógica del turno que modifica el estado y la UI
            // debe ejecutarse en el hilo de la aplicación JavaFX.
            Platform.runLater(() -> {
                if (!this.gameController.getGameState().isGameOver()) {
                    this.gameController.executeMachineTurnLogic();
                }
            });

        } catch (InterruptedException e) {
            // Si el hilo es interrumpido (por ejemplo, si el juego termina o se reinicia),
            // se termina la ejecución de forma segura.
            Thread.currentThread().interrupt();
            System.out.println("El hilo del turno de la máquina fue interrumpido.");
        }
    }
}
