package minesweeperhelper;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
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

    private BorderPane rootElement;

    private Stage stage;

    private ScreenMonitoringService screenMonitoringService;

    private ExecutorService executorService;

    private SwitchButton switchButton;

    private ImageView imageView;

    private int stageSmallWidth = 200;
    private int stageSmallHeight = 100;

    protected void init(Stage stage, BorderPane rootElement) {

        this.rootElement = rootElement;

        this.stage = stage;

        Scene scene = new Scene(rootElement, stageSmallWidth, stageSmallHeight);
        scene.setFill(Color.TRANSPARENT);

        imageView = new ImageView();
        imageView.setMouseTransparent(true);
        rootElement.setLeft(imageView);

        String initImage = System.getProperty("user.dir") + File.separatorChar + "mineSweeper.png";

        Mat srcImage = Imgcodecs.imread(initImage);
        ImageProcessing imageProcessing = new ImageProcessing();
        Grid grid = imageProcessing.processView(srcImage);

        startExecutorService();

        this.switchButton = new SwitchButton();
        
        this.switchButton.getValue().addListener((observable, oldValue, newValue) -> {

            if (newValue) {
                Rectangle2D screenBounds = Screen.getPrimary().getBounds();
                stage.setWidth(screenBounds.getWidth());
                stage.setHeight(screenBounds.getHeight());

                /*
                 * WritableImage writableImage = new WritableImage((int)
                 * screenBounds.getWidth(),
                 * (int) screenBounds.getHeight());
                 * 
                 * robot.getScreenCapture(writableImage,
                 * new Rectangle2D(0, 0, screenBounds.getWidth(), screenBounds.getHeight()));
                 * 
                 * imageView.imageProperty().set(new ImageView(writableImage).getImage());
                 */

                // updateImageView(imageView, ImageProcessing.mat2Image(srcImage));

            } else {
                stage.setHeight(stageSmallHeight);
                stage.setWidth(stageSmallWidth);
                updateImageView(imageView, null);
            }
        });

        rootElement.setTop(this.switchButton);

        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();

        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());

        screenMonitoringService = new ScreenMonitoringService(this);
        screenMonitoringService.restart();

    }

    public void startExecutorService() {

        if (executorService == null || executorService.isShutdown() || executorService.isTerminated()) {
            int poolSize = 1;
            int queueSize = 2;
            RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardOldestPolicy();

            executorService = new ThreadPoolExecutor(poolSize, poolSize,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(queueSize),
                    handler);

        }
    }

    public ScreenMonitoringService getScreenMonitoringService() {
        return screenMonitoringService;
    }

    public void shutdownExecutorService() {
        if (executorService != null && !executorService.isShutdown())
            executorService.shutdown();
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

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public SwitchButton getSwitchButton() {
        return switchButton;
    }

    public ImageView getImageView(){
        return imageView;
    }

    public BorderPane getRootElement(){
        return rootElement;
    }
}