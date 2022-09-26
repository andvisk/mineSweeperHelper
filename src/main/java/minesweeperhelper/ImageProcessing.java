package minesweeperhelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageProcessing {

    private static final Logger logger = LogManager.getLogger(ImageProcessing.class);

    @Deprecated
    public Board processView(Mat srcImage, Map<BigDecimal, Map<BigDecimal, List<Grid>>> mapGridsByWidthAndHeight,
            BigDecimal tolleranceInPercent) {

        List<MineSweeperGridCell> cells = findCells(srcImage);

        return GridUtils.collectGrid(cells, tolleranceInPercent);
    }

    public Board collectBoard(Mat srcImage, Map<BigDecimal, Map<BigDecimal, List<Grid>>> mapGridsByWidthAndHeight,
            BigDecimal tolleranceInPercent) {

        Mat patternImage = Imgcodecs.imread("src/main/resources/" + 0 + ".png");
        int patternImageWidth = patternImage.cols();

        Imgcodecs.imwrite("src/test/resources/" + "srcImage" + ".png", srcImage);

        for (BigDecimal width : mapGridsByWidthAndHeight.keySet()) {
            for (BigDecimal height : mapGridsByWidthAndHeight.get(width).keySet()) {

                int machMethod = Imgproc.TM_CCOEFF_NORMED;

                Mat srcForOutput = srcImage.clone();

                for (int i = 10; i >= 0; i--) {

                    List<Rect> numbersLocations = new ArrayList<>();

                    Mat numberImgInit = Imgcodecs.imread("src/main/resources/" + i + ".png");

                    Mat resizedPatternImage = new Mat();
                    double scaleWidthFasctor = width.doubleValue() / patternImageWidth;
                    Imgproc.resize(numberImgInit, resizedPatternImage, new Size(), scaleWidthFasctor, scaleWidthFasctor,
                            Imgproc.INTER_AREA);

                    Imgproc.cvtColor(resizedPatternImage, resizedPatternImage, Imgproc.COLOR_BGR2BGRA);

                    Mat outputImage = new Mat();
                    Imgproc.matchTemplate(srcImage, resizedPatternImage, outputImage, machMethod);

                    logger.debug("match finish for " + i);

                    Core.MinMaxLocResult mmr = null;

                    while (true) {
                        mmr = Core.minMaxLoc(outputImage);
                        if (mmr.maxVal >= 0.8) {
                            Rect rect = new Rect(mmr.maxLoc,
                                    new Point(mmr.maxLoc.x + resizedPatternImage.cols(),
                                            mmr.maxLoc.y + resizedPatternImage.rows()));
                            numbersLocations.add(rect);

                            Imgproc.rectangle(srcForOutput, mmr.maxLoc,
                                    new Point(mmr.maxLoc.x + resizedPatternImage.cols(),
                                            mmr.maxLoc.y + resizedPatternImage.rows()),
                                    new Scalar(0, 255, 0, 255));

                            Imgproc.circle(outputImage, new Point(mmr.maxLoc.x, mmr.maxLoc.y),
                                    (resizedPatternImage.width() + resizedPatternImage.height()) / 4,
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

                            MineSweeperGridCell gridCell = new MineSweeperGridCell(cellTypeEnum, rect,
                                    (i >= 0 && i < 9) ? i : -1);

                        } else {
                            break;
                        }
                    }

                    logger.debug(i + " " + numbersLocations.size());

                }

                Board board = new Board(1, 1);
                return board;

            }
        }
        return null;
    }

    private List<MineSweeperGridCell> findCells(Mat srcImage) {
        List<MineSweeperGridCell> list = new ArrayList<>();

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

                    MineSweeperGridCell gridCell = new MineSweeperGridCell(cellTypeEnum, rect,
                            (i >= 0 && i < 9) ? i : -1);

                    list.add(gridCell);

                } else {
                    break;
                }
            }

            logger.debug(i + " " + numbersLocations.size());

        }

        return list;
    }

}
