package minesweeperhelper;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
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
    private ControllerHelpScreen controllerHelpScreen;

    public static void main(String[] args) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.TRACE);

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {

            Stage helpScreenStage = initHelpScreenStage();

            initPrimaryStage(primaryStage, helpScreenStage, controllerHelpScreen);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPrimaryStage(Stage primaryStage, Stage helpScreenStage, ControllerHelpScreen controllerHelpScreen){
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.setX(0);
        primaryStage.setY(0);

        StackPane rootElement = new StackPane();
        rootElement.setAlignment(Pos.TOP_LEFT);
        rootElement.setBackground(Background.EMPTY);

        controller = new ControllerMain();
        controller.init(primaryStage, rootElement, helpScreenStage, controllerHelpScreen);
    }

    private Stage initHelpScreenStage(){
        Stage helpScreenStage = new Stage();
        helpScreenStage.initStyle(StageStyle.TRANSPARENT);

        helpScreenStage.setMaximized(true);

        StackPane rootElementHelpScreen = new StackPane();
        rootElementHelpScreen.setAlignment(Pos.TOP_LEFT);
        rootElementHelpScreen.setBackground(Background.EMPTY);

        controllerHelpScreen = new ControllerHelpScreen();
        controllerHelpScreen.init(helpScreenStage, rootElementHelpScreen);

        return helpScreenStage;
    }

}