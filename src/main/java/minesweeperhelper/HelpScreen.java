package minesweeperhelper;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.WritableImage;
import javafx.scene.robot.Robot;
import javafx.stage.Screen;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

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

        public static HelpScreenResult process(Mat mainScreenShot, List<ScreenShotArea> screenShotList,
                        Map<UUID, Map<BigDecimal, Map<BigDecimal, Map<BigDecimal, List<Grid>>>>> mapGridsByAreaWidthHeight,
                        BigDecimal tolleranceInPercent, Consumer<String> updateMessageConsumer) {

                Map<UUID, List<Board>> mapBoards = new HashMap<>();

                for (ScreenShotArea screenShotArea : screenShotList) {
                        UUID id = screenShotArea.id();

                        if (mapGridsByAreaWidthHeight.get(id) != null) {
                                List<Board> boards = Board.collectBoards(screenShotArea,
                                                mapGridsByAreaWidthHeight.get(id),
                                                tolleranceInPercent, updateMessageConsumer);

                                if (boards.size() > 0) {
                                        mapBoards.put(id, boards);
                                        for (Board board : boards) {
                                                board.processGrid(screenShotArea.mat());
                                        }
                                }
                        }
                }

                Mat screenShotBlured = mainScreenShot.clone();
                Imgproc.GaussianBlur(screenShotBlured, screenShotBlured, new Size(21, 21), 0);

                Mat squareMat = new Mat(screenShotBlured.height(), screenShotBlured.width(),
                                CvType.CV_8UC4, new Scalar(0, 0, 0, 0));

                Mat dstImg = new Mat();

                for (Map.Entry<UUID, List<Board>> boardsEntry : mapBoards.entrySet()) {
                        List<Board> boards = boardsEntry.getValue();
                        UUID id = boardsEntry.getKey();

                        for (Board board : boards) {

                                ScreenShotArea screenShotArea = screenShotList.stream()
                                                .filter(p -> p.id().compareTo(id) == 0).findAny().get();

                                if (App.debug) {
                                        Mat screenShotMat = screenShotArea.mat();

                                        board.printDebugInfo(screenShotMat);

                                        Imgcodecs.imwrite(ProcessingService.debugDir + File.separatorChar
                                                        + "debug_result_"
                                                        + id + ".jpg", screenShotMat);
                                } else {
                                        board.printNumberValuesOnBoardCells(screenShotArea.mat());
                                }

                                GridLocation gridLocation = new GridLocation(board.getGrid(), screenShotArea.area().x,
                                                screenShotArea.area().y);

                                Imgproc.rectangle(squareMat,
                                                new Point(gridLocation.minX - gridLocation.cellWidth / 2,
                                                                gridLocation.minY - gridLocation.cellHeight / 2),
                                                new Point(gridLocation.maxX + gridLocation.cellWidth * 1.5,
                                                                gridLocation.maxY + gridLocation.cellHeight * 1.5),
                                                new Scalar(255, 0, 0, 0), -1);

                        }

                }

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
                Core.bitwise_and(mainScreenShot, mainScreenShot, screenShotForeground, mask);

                Core.add(screenShotBackground, screenShotForeground, dstImg);

                return new HelpScreenResult(dstImg, mapBoards);

        }

        public static void showHelpScreen(Mat dstImg, ControllerHelpScreen controllerHelpScreen) {
                controllerHelpScreen.updateImageView(controllerHelpScreen.getImageView(),
                                ImageUtils.mat2Image(dstImg));
                controllerHelpScreen.getStage().show();
        }

}
