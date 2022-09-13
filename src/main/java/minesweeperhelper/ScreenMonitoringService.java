package minesweeperhelper;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.robot.Robot;
import javafx.stage.Screen;

import java.awt.image.BufferedImage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.math.BigDecimal;

public class ScreenMonitoringService extends Service<Void> {

    private Logger log = LogManager.getLogger(this.getClass());

    private ControllerHelpScreen controllerHelpScreen;
    private ControllerMain controllerMain;

    private ObjectProperty<Long> step = new SimpleObjectProperty<>(0L);

    public ScreenMonitoringService(ControllerMain controllerMain, ControllerHelpScreen controllerHelpScreen) {

        this.controllerMain = controllerMain;
        this.controllerHelpScreen = controllerHelpScreen;

        Robot robot = new Robot();

        step.addListener((observable, oldValue, newValue) -> {

            if (controllerMain.getSwitchButton().getValue().get()) {

                Rectangle2D screenBounds = Screen.getPrimary().getBounds();

                Bounds boundsStackPaneLocal = controllerHelpScreen.getRootElement().getBoundsInLocal();
                Bounds boundsStackPane = controllerHelpScreen.getRootElement().localToScreen(boundsStackPaneLocal);

                WritableImage writableImage = new WritableImage((int) Math.ceil(screenBounds.getWidth()),
                        (int) Math.ceil(screenBounds.getHeight()));

                //controllerHelpScreen.getImageView().setImage(null);

                Mat atom_image = new Mat((int) writableImage.getHeight(), (int) writableImage.getWidth(),
                        CvType.CV_8UC4, new Scalar(255, 255, 255, 0));
                        

                int thickness = 2;
                int lineType = 8;
                int shift = 0;
                Imgproc.ellipse(atom_image,
                        new Point(writableImage.getHeight() / 2, writableImage.getHeight() / 2),
                        new Size(writableImage.getHeight() / 4, writableImage.getHeight() / 16),
                        45,
                        0.0,
                        360.0,
                        new Scalar(255, 0, 0, 255),
                        thickness,
                        lineType,
                        shift);

                controllerHelpScreen.updateImageView(controllerHelpScreen.getImageView(),
                        ImageUtils.mat2Image(atom_image));

                /*
                 * robot.getScreenCapture(writableImage,
                 * new Rectangle2D(boundsStackPane.getMinX(),
                 * boundsStackPane.getMinY(),
                 * screenBounds.getWidth(),
                 * screenBounds.getHeight()));
                 * 
                 * controllerHelpScreen.updateImageView(controllerHelpScreen.getImageView(),
                 * ImageUtils.mat2Image(ImageUtils.writableImageToMat(writableImage)));
                 */

            }

        });
    }

    @Override
    protected Task<Void> createTask() {
        Task task = new Task<>() {

            long step = 0;

            protected Void call() {

                while (!isCancelled()) {

                    updateValue(++step);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException interrupted) {
                        if (isCancelled()) {
                            break;
                        }
                    }
                }
                return null;
            }

        };

        this.step.bind(Bindings.when(task.valueProperty().isNotNull()).then(task.valueProperty()).otherwise(0L));

        return task;
    }
}
