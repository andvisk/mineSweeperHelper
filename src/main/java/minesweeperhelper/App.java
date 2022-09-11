package minesweeperhelper;

import javafx.application.Application;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.opencv.core.Core;

public class App extends Application {

    private static Logger log = LogManager.getLogger(App.class);

    private ControllerMain controller;

    public static void main(String[] args) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.TRACE);

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {

            primaryStage.initStyle(StageStyle.TRANSPARENT);
            primaryStage.setAlwaysOnTop(true);
            primaryStage.setX(5);
            primaryStage.setY(5);

            BorderPane rootElement = new BorderPane();
            rootElement.setBackground(Background.EMPTY);
            controller = new ControllerMain();
            controller.init(primaryStage, rootElement);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}