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
import org.opencv.imgproc.Imgproc;

public class ImageProcessing {

        private static final Logger logger = LogManager.getLogger(ImageProcessing.class);

        public List<Board> collectBoards(Mat srcImg,
                        Map<BigDecimal, Map<BigDecimal, List<Grid>>> mapGridsByWidthAndHeight,
                        BigDecimal tolleranceInPercent) {

                List<Board> listBoards = new ArrayList<>();

                OcrScanner ocrScanner = new OcrScanner(System.getProperty("tesseractDataDir"));

                for (BigDecimal width : mapGridsByWidthAndHeight.keySet()) {
                        for (BigDecimal height : mapGridsByWidthAndHeight.get(width).keySet()) {
                                for (Grid grid : mapGridsByWidthAndHeight.get(width).get(height)) {

                                        GridLocation gridLocation = new GridLocation(grid.getGrid());
                                        List<MineSweeperGridCell> boardCells = new ArrayList<>();

                                        for (RectArea rectArea : grid.getGridCells()) {

                                                CellTypeEnum cellTypeEnum = null;

                                                switch (rectArea.color) {
                                                        case BLUE:
                                                                cellTypeEnum = CellTypeEnum.UNCHECKED;
                                                                break;
                                                        case YELLOW:
                                                                cellTypeEnum = CellTypeEnum.FLAG;
                                                                break;
                                                        case WHITE:
                                                                cellTypeEnum = CellTypeEnum.NUMBER;
                                                                break;

                                                }

                                                MineSweeperGridCell gridCell = new MineSweeperGridCell(
                                                                cellTypeEnum, rectArea.rectangle, 0, rectArea.color);

                                                boardCells.add(gridCell);
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

                                                        if (boardCell.color.equals(ColorsEnum.WHITE)) {
                                                                Mat imageToOcr = srcImg.submat(boardCell.rectangle);
                                                                String text = ocrScanner.getTextFromImage(imageToOcr);

                                                                if (text != null && text.length() > 0) {
                                                                        boardCell.setNumber(Integer.parseInt(text));
                                                                        boardCell.setCellTypeEnum(CellTypeEnum.NUMBER);
                                                                } else {
                                                                        boardCell.setNumber(0);
                                                                        boardCell.setCellTypeEnum(CellTypeEnum.EMPTY);
                                                                }
                                                        }

                                                        board.setCell(boardCell.positionInGridX,
                                                                        boardCell.positionInGridY, boardCell);
                                                }

                                                listBoards.add(board);
                                        }
                                }
                        }
                }
                ocrScanner.destructor();
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
