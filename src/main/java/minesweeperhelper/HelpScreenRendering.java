package minesweeperhelper;

import java.util.concurrent.ExecutorService;

import javafx.concurrent.Task;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.WritableImage;
import javafx.scene.robot.Robot;
import javafx.stage.Screen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.Mat;

public class HelpScreenRendering {

    private static Logger log = LogManager.getLogger(HelpScreenRendering.class);

    private ControllerMain controllerMain;

    public HelpScreenRendering(ControllerMain controllerMain) {
        this.controllerMain = controllerMain;
    }

    public void redrawScreen() {

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        WritableImage writableImage = new WritableImage((int) screenBounds.getWidth(),
                (int) screenBounds.getHeight());

        new Robot().getScreenCapture(writableImage,
                new Rectangle2D(0, 0, screenBounds.getWidth(), screenBounds.getHeight()));

        Task<Mat> task = JFxTasksUtils.createTask(() -> {

            return ImageUtils.writableImageToMat(writableImage);
        });

        task.setOnSucceeded(e -> {
            controllerMain.updateImageView(controllerMain.getImageView(), ImageUtils.mat2Image(task.getValue()));
        });

        task.setOnFailed(e -> {
            log.error(task.getException().getMessage(), task.getException());
        });

        controllerMain.getExecutorService().execute(task);
    }
}
