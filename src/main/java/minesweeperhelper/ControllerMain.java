package minesweeperhelper;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class ControllerMain {

    private Logger log = LogManager.getLogger(this.getClass());

    private StackPane rootElement;

    private Stage stage;

    private ScreenMonitoringService screenMonitoringService;

    private SwitchButton switchButton;

    private int stageSmallWidth = 200;
    private int stageSmallHeight = 100;

    private Stage helpScreenStage;

    protected void init(Stage stage, StackPane rootElement, Stage helpScreenStage, ControllerHelpScreen controllerHelpScreen) {

        this.rootElement = rootElement;

        this.stage = stage;
        
        this.helpScreenStage = helpScreenStage;
        
        Scene scene = new Scene(rootElement, stageSmallWidth, stageSmallHeight);
        scene.setFill(Color.TRANSPARENT);

        this.switchButton = new SwitchButton();
        
        this.switchButton.getValue().addListener((observable, oldValue, newValue) -> {

            if (newValue) {
                helpScreenStage.show();

            } else {
                //updateImageView(imageView, null);

            }
        });

        rootElement.getChildren().add(this.switchButton);

        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();

        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());

        screenMonitoringService = new ScreenMonitoringService(this, controllerHelpScreen);
        screenMonitoringService.restart();

    }

    public ScreenMonitoringService getScreenMonitoringService() {
        return screenMonitoringService;
    }

    public void updateImageView(ImageView view, javafx.scene.image.Image image) {
        onFXThread(view.imageProperty(), image);
    }

    public static <T> void onFXThread(final ObjectProperty<T> property, final T value) {
        Platform.runLater(() -> {
            property.set(value);
        });
    }

    public Stage getStage() {
        return stage;
    }

    public SwitchButton getSwitchButton() {
        return switchButton;
    }

}