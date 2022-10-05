package minesweeperhelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

        public List<Board> collectBoards(Mat srcImg,
                        Map<BigDecimal, Map<BigDecimal, List<Grid>>> mapGridsByWidthAndHeight,
                        BigDecimal tolleranceInPercent) {

                List<Board> listBoards = new ArrayList<>();

                OcrScanner ocrScanner = new OcrScanner(System.getProperty("tesseractDataDir"));

                Mat ocrImage = srcImg.clone();
                Imgproc.cvtColor(ocrImage, ocrImage, Imgproc.COLOR_BGR2GRAY);
                Imgproc.GaussianBlur(ocrImage, ocrImage, new Size(3, 3), 0);
                Imgproc.threshold(ocrImage, ocrImage, 0, 255, Imgproc.THRESH_OTSU);

                for (BigDecimal width : mapGridsByWidthAndHeight.keySet()) {
                        for (BigDecimal height : mapGridsByWidthAndHeight.get(width).keySet()) {
                                for (Grid grid : mapGridsByWidthAndHeight.get(width).get(height)) {

                                        GridLocation gridLocation = new GridLocation(grid.getGrid());
                                        List<MineSweeperGridCell> boardCells = new ArrayList<>();

                                        Rect rectCrop = new Rect(gridLocation.minX, gridLocation.minY,
                                                        gridLocation.maxX + gridLocation.cellWidth - gridLocation.minX,
                                                        gridLocation.maxY + gridLocation.cellHeight
                                                                        - gridLocation.minY);

                                        for (int i = 0; i <= grid.getGrid().length; i++) {
                                                for (int k = 0; k <= grid.getGrid()[i].length; k++) {
                                                        RectArea rectArea = grid.getGrid()[i][k];
                                                        if (rectArea.color.equals(ColorsEnum.WHITE)) {
                                                                Mat imageToOcr = ocrImage.submat(rectArea.rectangle);
                                                                String text = ocrScanner.getTextFromImage(imageToOcr);

                                                                if(text != null && text.length() > 0){

                                                                }else{
                                                                        rectArea.
                                                                }
                                                        }
                                                }
                                        }

                                        Mat srcForOutput = srcImg.clone();

                                        for (int i = 10; i >= 0; i--) {

                                                Mat resizedPatternImage = new Mat();
                                                double scaleWidthFasctor = width.doubleValue() / patternImageWidth;
                                                Imgproc.resize(numberImgInit, resizedPatternImage, new Size(),
                                                                scaleWidthFasctor,
                                                                scaleWidthFasctor,
                                                                Imgproc.INTER_AREA);

                                                Imgproc.cvtColor(resizedPatternImage, resizedPatternImage,
                                                                Imgproc.COLOR_BGR2BGRA);

                                                Mat contoursMat = new Mat();
                                                Imgproc.matchTemplate(imageToMatchTemplate, resizedPatternImage,
                                                                contoursMat, machMethod);

                                                logger.debug("match finish for " + i);

                                                Core.MinMaxLocResult mmr = null;

                                                while (true) {
                                                        mmr = Core.minMaxLoc(contoursMat);
                                                        if (mmr.maxVal >= 0.65) {
                                                                Rect rect = new Rect(
                                                                                new Point(mmr.maxLoc.x
                                                                                                + gridLocation.minX,
                                                                                                mmr.maxLoc.y + gridLocation.minY),
                                                                                new Point(mmr.maxLoc.x
                                                                                                + resizedPatternImage
                                                                                                                .cols()
                                                                                                + gridLocation.minX,
                                                                                                mmr.maxLoc.y + resizedPatternImage
                                                                                                                .rows()
                                                                                                                + gridLocation.minY));
                                                                numbersLocations.add(rect);

                                                                Imgproc.rectangle(srcForOutput,
                                                                                new Point(mmr.maxLoc.x
                                                                                                + gridLocation.minX,
                                                                                                mmr.maxLoc.y + gridLocation.minY),
                                                                                new Point(mmr.maxLoc.x
                                                                                                + resizedPatternImage
                                                                                                                .cols()
                                                                                                + gridLocation.minX,
                                                                                                mmr.maxLoc.y + resizedPatternImage
                                                                                                                .rows()
                                                                                                                + gridLocation.minY),
                                                                                new Scalar(0, 255, 0, 255));

                                                                Imgproc.circle(contoursMat,
                                                                                new Point(mmr.maxLoc.x, mmr.maxLoc.y),
                                                                                (resizedPatternImage.width()
                                                                                                + resizedPatternImage
                                                                                                                .height())
                                                                                                / 4,
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

                                                                MineSweeperGridCell gridCell = new MineSweeperGridCell(
                                                                                cellTypeEnum, rect,
                                                                                (i >= 0 && i < 9) ? i : -1);
                                                                boardCells.add(gridCell);

                                                        } else {
                                                                break;
                                                        }
                                                }

                                                logger.debug(i + " " + numbersLocations.size());

                                        }

                                        List<RectArea> gridCells = grid.getGridCells();

                                        if (boardCells.size() == gridCells.size()) {
                                                for (MineSweeperGridCell boardCell : boardCells) {
                                                        RectArea gridCell = gridCells.stream().filter(
                                                                        p -> {
                                                                                BigDecimal bdX = BigDecimal
                                                                                                .valueOf(p.rectangle.x)
                                                                                                .setScale(2,
                                                                                                                RoundingMode.HALF_EVEN);
                                                                                BigDecimal bdY = BigDecimal
                                                                                                .valueOf(p.rectangle.y)
                                                                                                .setScale(2,
                                                                                                                RoundingMode.HALF_EVEN);
                                                                                BigDecimal bdWidth = BigDecimal.valueOf(
                                                                                                p.rectangle.width)
                                                                                                .setScale(2,
                                                                                                                RoundingMode.HALF_EVEN);
                                                                                BigDecimal bdHeight = BigDecimal
                                                                                                .valueOf(p.rectangle.height)
                                                                                                .setScale(2,
                                                                                                                RoundingMode.HALF_EVEN);

                                                                                BigDecimal bdBX = BigDecimal.valueOf(
                                                                                                boardCell.rectangle.x)
                                                                                                .setScale(2,
                                                                                                                RoundingMode.HALF_EVEN);
                                                                                BigDecimal bdBY = BigDecimal.valueOf(
                                                                                                boardCell.rectangle.y)
                                                                                                .setScale(2,
                                                                                                                RoundingMode.HALF_EVEN);

                                                                                return bdX.subtract(bdBX).abs()
                                                                                                .compareTo(
                                                                                                                bdWidth.divide(BigDecimal
                                                                                                                                .valueOf(100),
                                                                                                                                2,
                                                                                                                                RoundingMode.HALF_EVEN)
                                                                                                                                .multiply(tolleranceInPercent)) <= 0
                                                                                                &&
                                                                                                bdY.subtract(bdBY).abs()
                                                                                                                .compareTo(
                                                                                                                                bdHeight.divide(BigDecimal
                                                                                                                                                .valueOf(100),
                                                                                                                                                2,
                                                                                                                                                RoundingMode.HALF_EVEN)
                                                                                                                                                .multiply(tolleranceInPercent)) <= 0;

                                                                        }).findAny().orElse(null);

                                                        if (gridCell != null) {
                                                                boardCell.positionInGridX = gridCell.positionInGridX;
                                                                boardCell.positionInGridY = gridCell.positionInGridY;
                                                        }
                                                }
                                                int maxXpos = boardCells.stream()
                                                                .max((a, b) -> Integer.compare(a.positionInGridX,
                                                                                b.positionInGridX))
                                                                .get().positionInGridX;
                                                int maxYpos = boardCells.stream()
                                                                .max((a, b) -> Integer.compare(a.positionInGridY,
                                                                                b.positionInGridY))
                                                                .get().positionInGridY;
                                                Board board = new Board(maxXpos + 1, maxYpos + 1, gridLocation);

                                                for (MineSweeperGridCell boardCell : boardCells) {
                                                        board.setCell(boardCell.positionInGridX,
                                                                        boardCell.positionInGridY, boardCell);
                                                }

                                                listBoards.add(board);
                                        }
                                }
                        }
                }
                return listBoards;
        }

        public static Mat detectColor(Mat mat, HsvColor color) {

                Mat hsv = new Mat();
                Imgproc.cvtColor(mat, hsv, Imgproc.COLOR_BGR2HSV);
                Mat mask = new Mat();
                Core.inRange(hsv, color.lower, color.upper, mask);
                Mat dest = new Mat();
                Core.bitwise_and(mat, mat, dest, mask);

                Mat dest2gray = new Mat();
                Imgproc.cvtColor(dest, dest2gray, Imgproc.COLOR_BGR2GRAY);

                Imgproc.threshold(dest2gray, mask, 10, 255, Imgproc.THRESH_BINARY);

                return dest2gray;
        }

}
