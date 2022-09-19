package minesweeperhelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javafx.scene.image.WritableImage;

public class ImageProcessing {

    private static final Logger logger = LogManager.getLogger(ImageProcessing.class);

    public Grid processView(Mat srcImage) {

        List<GridCell> cells = findCells(srcImage);

        return GridUtils.collectGrid(cells);
    }

    private List<GridCell> findCells(Mat srcImage) {
        List<GridCell> list = new ArrayList<>();

        int machMethod = Imgproc.TM_CCOEFF_NORMED;

        Mat srcForOutput = srcImage.clone();

        for (int i = 10; i >= 0; i--) {

            List<Rect> numbersLocations = new ArrayList<>();

            Mat numberImage = Imgcodecs.imread("src/main/resources/" + i + ".png");
            Imgproc.cvtColor(numberImage, numberImage, Imgproc.COLOR_BGR2BGRA);

            Mat outputImage = new Mat();
            Imgproc.matchTemplate(srcImage, numberImage, outputImage, machMethod);

            logger.debug("match finish for " + i);

            Core.MinMaxLocResult mmr = null;

            while (true) {
                mmr = Core.minMaxLoc(outputImage);
                if (mmr.maxVal >= 0.8) {
                    Rect rect = new Rect(mmr.maxLoc,
                            new Point(mmr.maxLoc.x + numberImage.cols(), mmr.maxLoc.y + numberImage.rows()));
                    numbersLocations.add(rect);

                    Imgproc.rectangle(srcForOutput, mmr.maxLoc,
                            new Point(mmr.maxLoc.x + numberImage.cols(), mmr.maxLoc.y + numberImage.rows()),
                            new Scalar(0, 255, 0, 255));

                    Imgproc.circle(outputImage, new Point(mmr.maxLoc.x, mmr.maxLoc.y),
                            (numberImage.width() + numberImage.height()) / 4,
                            new Scalar(0, 0, 0), -1);

                    CellTypeEnum cellTypeEnum = null;

                    if (i > 0 && i < 9)
                        cellTypeEnum = CellTypeEnum.NUMBER;
                    if (i == 0)
                        cellTypeEnum = CellTypeEnum.EMPTY;
                    if (i == 9)
                        cellTypeEnum = CellTypeEnum.FLAG;
                    if (i == 10)
                        cellTypeEnum = CellTypeEnum.UNCHECKED;

                    GridCell gridCell = new GridCell(cellTypeEnum, rect, (i >= 0 && i < 9) ? i : -1);

                    list.add(gridCell);

                } else {
                    break;
                }
            }

            logger.debug(i + " " + numbersLocations.size());

        }

        // todo remove
        Imgcodecs.imwrite(System.getProperty("user.dir") + File.separatorChar + "mineSweeperOut.png", srcForOutput);

        return list;
    }

    // todo remove
    public Mat drawEllipse(WritableImage writableImage) {
        Mat helpScreenDrawing = new Mat((int) writableImage.getHeight(), (int) writableImage.getWidth(),
                CvType.CV_8UC4, new Scalar(0, 0, 0, 0));

        int thickness = 20;
        int lineType = 8;
        int shift = 0;
        Imgproc.ellipse(helpScreenDrawing,
                new Point(writableImage.getHeight() / 2, writableImage.getHeight() / 2),
                new Size(writableImage.getHeight() / 4, writableImage.getHeight() / 16),
                45,
                0.0,
                360.0,
                new Scalar(255, 0, 0, 0),
                thickness,
                lineType,
                shift);

        Mat drawing2gray = new Mat();
        Imgproc.cvtColor(helpScreenDrawing, drawing2gray, Imgproc.COLOR_BGR2GRAY);

        Mat mask = new Mat();
        Imgproc.threshold(drawing2gray, mask, 10, 255, Imgproc.THRESH_BINARY);

        Mat maskInverted = new Mat();
        Core.bitwise_not(mask, maskInverted);

        /*
         * Mat screenShotBackground = new Mat();
         * Core.bitwise_and(screenShot, screenShot, screenShotBackground, maskInverted);
         * 
         * Mat dstImg = new Mat();
         * Core.add(screenShotBackground, helpScreenDrawing, dstImg);
         */
        return null;
    }

}
