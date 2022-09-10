package minesweeperhelper;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class ControllerMain {

    private Logger log = LogManager.getLogger(this.getClass());

    private BorderPane rootElement;

    private Stage stage;

    private ScreenMonitoringService screenMonitoringService;

    protected void init(Stage stage, BorderPane rootElement) {

        this.rootElement = rootElement;

        this.stage = stage;

        Scene scene = new Scene(rootElement, 1300, 800);

        ImageView imageView = new ImageView();
        String initImage = System.getProperty("user.dir") + File.separatorChar + "mineSweeper.png";

        log.error(initImage + " ===========================================================");

        Mat srcImage = Imgcodecs.imread(initImage);
        ImageProcessing imageProcessing = new ImageProcessing();
        Grid grid = imageProcessing.processView(srcImage);

        //updateImageView(imageView, ImageProcessing.mat2Image(mat));

        rootElement.setCenter(imageView);

        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();

        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());

        screenMonitoringService = new ScreenMonitoringService(this);
        screenMonitoringService.restart();

    }

    private void updateImageView(ImageView view, javafx.scene.image.Image image) {
        onFXThread(view.imageProperty(), image);
    }

    public static <T> void onFXThread(final ObjectProperty<T> property, final T value) {
        Platform.runLater(() -> {
            property.set(value);
        });
    }

}