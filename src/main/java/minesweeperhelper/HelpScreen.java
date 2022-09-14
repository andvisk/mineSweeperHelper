package minesweeperhelper;

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
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.math.BigDecimal;

public class HelpScreen {

    private Logger log = LogManager.getLogger(this.getClass());

    public static void showHelpScreen(ControllerHelpScreen controllerHelpScreen) {

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        Bounds boundsStackPaneLocal = controllerHelpScreen.getRootElement().getBoundsInLocal();
        Bounds boundsStackPane = controllerHelpScreen.getRootElement().localToScreen(boundsStackPaneLocal);

        WritableImage writableImage = new WritableImage((int) Math.ceil(screenBounds.getWidth()),
                (int) Math.ceil(screenBounds.getHeight()));

        new Robot().getScreenCapture(writableImage,
                new Rectangle2D(boundsStackPane.getMinX(),
                        boundsStackPane.getMinY(),
                        screenBounds.getWidth(),
                        screenBounds.getHeight()));

        Mat screenShot = ImageUtils.writableImageToMat(writableImage);

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

        Mat dstImg = new Mat();

        // Core.add(screenShot, atom_image,  dstImg);

        controllerHelpScreen.updateImageView(controllerHelpScreen.getImageView(),
                ImageUtils.mat2Image(screenShot));

        controllerHelpScreen.getStage().show();
    }
}
