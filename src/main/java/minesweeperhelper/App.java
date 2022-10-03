package minesweeperhelper;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.opencv.core.Core;

public class App extends Application {

    private static Logger log = LogManager.getLogger(App.class);

    public static final boolean debug = true;

    private ControllerMain controller;

    public static void main(String[] args) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Configurator.initialize(null, System.getProperty("user.dir") + File.separator
                + "src/main/resources/log4j2.properties");

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {

            initPrimaryStage(primaryStage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPrimaryStage(Stage primaryStage) {
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.setX(0);
        primaryStage.setY(0);

        StackPane rootElement = new StackPane();
        rootElement.setAlignment(Pos.TOP_LEFT);
        rootElement.setBackground(Background.EMPTY);

        controller = new ControllerMain();
        controller.init(primaryStage, rootElement);
    }

}