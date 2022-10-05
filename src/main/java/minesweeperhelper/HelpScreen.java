package minesweeperhelper;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.WritableImage;
import javafx.scene.robot.Robot;
import javafx.stage.Screen;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

        public static Mat getScreenShot(ControllerHelpScreen controllerHelpScreen) {
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

                return ImageUtils.writableImageToMat(writableImage);
        }

        public static Mat process(Mat screenShot, Mat whiteColors,
                        Map<BigDecimal, Map<BigDecimal, List<Grid>>> mapGridsByWidthAndHeight,
                        BigDecimal tolleranceInPercent) {

                List<Board> boards = new ImageProcessing().collectBoards(screenShot, mapGridsByWidthAndHeight,
                                tolleranceInPercent);

                if (boards.size() > 0) {

                        for (Board board : boards) {
                                Mat screenShotBlured = screenShot.clone();

                                GridLocation gridLocation = new GridLocation(board.getGrid());

                                board.processGrid(screenShot);

                                Imgproc.GaussianBlur(screenShotBlured, screenShotBlured, new Size(21, 21), 0);

                                Mat squareMat = new Mat(screenShotBlured.height(), screenShotBlured.width(),
                                                CvType.CV_8UC4, new Scalar(0, 0, 0, 0));
                                Imgproc.rectangle(squareMat,
                                                new Point(gridLocation.minX - gridLocation.cellWidth / 2,
                                                                gridLocation.minY - gridLocation.cellHeight / 2),
                                                new Point(gridLocation.maxX + gridLocation.cellWidth * 1.5,
                                                                gridLocation.maxY + gridLocation.cellHeight * 1.5),
                                                new Scalar(255, 0, 0, 0), -1);

                                Mat drawing2gray = new Mat();
                                Imgproc.cvtColor(squareMat, drawing2gray, Imgproc.COLOR_BGR2GRAY);

                                Mat mask = new Mat();
                                Imgproc.threshold(drawing2gray, mask, 10, 255, Imgproc.THRESH_BINARY);

                                Mat maskInverted = new Mat();
                                Core.bitwise_not(mask, maskInverted);

                                Mat screenShotBackground = new Mat();
                                Core.bitwise_and(screenShotBlured, screenShotBlured, screenShotBackground,
                                                maskInverted);

                                Mat screenShotForeground = new Mat();
                                Core.bitwise_and(screenShot, screenShot, screenShotForeground, mask);

                                Mat dstImg = new Mat();
                                Core.add(screenShotBackground, screenShotForeground, dstImg);

                                return dstImg;
                        }
                }
                return null;
        }

        public static void showHelpScreen(Mat dstImg, ControllerHelpScreen controllerHelpScreen) {
                controllerHelpScreen.updateImageView(controllerHelpScreen.getImageView(),
                                ImageUtils.mat2Image(dstImg));
                controllerHelpScreen.getStage().show();
        }

}
