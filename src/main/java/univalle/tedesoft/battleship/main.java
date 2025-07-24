package univalle.tedesoft.battleship;

import javafx.application.Application;
import javafx.stage.Stage;
import univalle.tedesoft.battleship.views.gameView;

import java.io.IOException;

public class main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        gameView gameMainView = gameView.getInstance();
        gameMainView.show();
    }

    public static void main(String[] args) {
        launch();
    }
}