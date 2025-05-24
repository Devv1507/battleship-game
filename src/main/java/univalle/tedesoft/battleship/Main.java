package univalle.tedesoft.battleship;

import javafx.application.Application;
import javafx.stage.Stage;
import univalle.tedesoft.battleship.views.GameView;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        GameView gameMainView = GameView.getInstance();
        gameMainView.show();
    }

    public static void main(String[] args) {
        launch();
    }
}