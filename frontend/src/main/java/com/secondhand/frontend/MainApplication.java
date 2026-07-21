package com.secondhand.frontend;

import com.secondhand.frontend.view.LoginView;
import javafx.application.Application;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point of the JavaFX client.
 * Run it with: mvn javafx:run
 */
public class MainApplication extends Application {

    private static Stage stage;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("سامانه خرید و فروش کالای دست دوم");
        switchTo(LoginView.build(), 460, 600);
        stage.show();
    }

    /** Replaces the current scene with a new page. */
    public static void switchTo(Parent root, double width, double height) {
        // right-to-left layout for Persian
        root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        Scene scene = new Scene(root, width, height);
        // one shared CSS file gives the whole application the same look
        scene.getStylesheets().add(
                MainApplication.class.getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    public static Stage getStage() {
        return stage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
