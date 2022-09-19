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
import org.opencv.imgproc.Imgproc;

public class HelpScreen {

        private Logger log = LogManager.getLogger(this.getClass());

        public void showHelpScreen(ControllerHelpScreen controllerHelpScreen, boolean debug) {

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

                Grid grid = new ImageProcessing().processView(screenShot);

                if (grid != null) {
                        Mat screenShotBlured = new Mat();
                        screenShot.copyTo(screenShotBlured);

                        int minX = -1;
                        int maxX = -1;
                        int minY = -1;
                        int maxY = -1;
                        int cellHeight = -1;
                        int cellWidth = -1;
                        for (int i = 0; i < grid.getGrid().length; i++) {
                                for (int y = 0; y < grid.getGrid()[i].length; y++) {
                                        if (cellHeight < 0) {
                                                cellHeight = grid.getGrid()[i][y].getRect().height;
                                                cellWidth = grid.getGrid()[i][y].getRect().width;
                                        }
                                        if (minX < 0 || minX > grid.getGrid()[i][y].getRect().x) {
                                                minX = grid.getGrid()[i][y].getRect().x;
                                        }
                                        if (minY < 0 || minY > grid.getGrid()[i][y].getRect().y) {
                                                minY = grid.getGrid()[i][y].getRect().y;
                                        }
                                        if (maxX < 0 || maxX < grid.getGrid()[i][y].getRect().x) {
                                                maxX = grid.getGrid()[i][y].getRect().x;
                                        }
                                        if (maxY < 0 || maxY < grid.getGrid()[i][y].getRect().y) {
                                                maxY = grid.getGrid()[i][y].getRect().y;
                                        }
                                }
                        }

                        grid.processGrid(screenShot);

                        Imgproc.GaussianBlur(screenShotBlured, screenShotBlured, new Size(21, 21), 0);

                        Mat squareMat = new Mat(screenShotBlured.height(), screenShotBlured.width(),
                                        CvType.CV_8UC4, new Scalar(0, 0, 0, 0));
                        Imgproc.rectangle(squareMat, new Point(minX - cellWidth / 2, minY - cellHeight / 2),
                                        new Point(maxX + cellWidth * 1.5, maxY + cellHeight * 1.5),
                                        new Scalar(255, 0, 0, 0), -1);

                        Mat drawing2gray = new Mat();
                        Imgproc.cvtColor(squareMat, drawing2gray, Imgproc.COLOR_BGR2GRAY);

                        Mat mask = new Mat();
                        Imgproc.threshold(drawing2gray, mask, 10, 255, Imgproc.THRESH_BINARY);

                        Mat maskInverted = new Mat();
                        Core.bitwise_not(mask, maskInverted);

                        Mat screenShotBackground = new Mat();
                        Core.bitwise_and(screenShotBlured, screenShotBlured, screenShotBackground, maskInverted);

                        Mat screenShotForeground = new Mat();
                        Core.bitwise_and(screenShot, screenShot, screenShotForeground, mask);

                        Mat dstImg = new Mat();
                        Core.add(screenShotBackground, screenShotForeground, dstImg);

                        controllerHelpScreen.updateImageView(controllerHelpScreen.getImageView(),
                                        ImageUtils.mat2Image(dstImg));

                        controllerHelpScreen.getStage().show();
                }
        }
}
