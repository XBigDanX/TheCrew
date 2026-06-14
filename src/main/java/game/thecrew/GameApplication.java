package game.thecrew;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GameApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("HomeScreen.fxml"));
        Scene scene = new Scene(loader.load(), 1500, 800);
        stage.setTitle("The Crew");
        stage.setScene(scene);
        stage.show();
    }
}
