package minesweeperhelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.*;

public class ImageProcessing {

    private static final Logger logger = LogManager.getLogger(ImageProcessing.class);

    public Grid processView(Mat srcImage) {

        Mat dest = srcImage.clone();
        Imgproc.cvtColor(srcImage, dest, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(dest, dest, new Size(5, 5), 0);
        Imgproc.adaptiveThreshold(dest, dest, 255, 1, 1, 11, 2);

        List<MatOfPoint> contours = new ArrayList<>();

        Imgproc.findContours(dest, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        return GridUtils.getGrid(srcImage, contours, findNumberLocations(srcImage));
    }

    private Map<Integer, List<Rect>> findNumberLocations(Mat srcImage) {
        Map<Integer, List<Rect>> map = new HashMap<>();

        int machMethod = Imgproc.TM_CCOEFF_NORMED;

        // Mat inTestImage = Imgcodecs.imread(System.getProperty("user.dir") +
        // File.separatorChar + "mineSweeper.png");

        Mat srcForOutput = srcImage.clone();

        for (int i = 0; i <= 10; i++) {

            List<Rect> numbersLocations = new ArrayList<>();

            Mat numberImage = Imgcodecs.imread("src/main/resources/" + i + ".png");
            Imgproc.cvtColor(numberImage, numberImage, Imgproc.COLOR_BGR2BGRA);

            Mat outputImage = new Mat();
            Imgproc.matchTemplate(srcImage, numberImage, outputImage, machMethod);

            logger.info("match finish for " + i);

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
                } else {
                    break;
                }
            }

            logger.info(i + " " + numbersLocations.size());

            map.put(i, numbersLocations);

        }

        Imgcodecs.imwrite(System.getProperty("user.dir") + File.separatorChar + "mineSweeperOut.png", srcForOutput);

        logger.info("finish");

        return map;
    }

    public static void processGridCell(Mat srcImage, GridCell gridCell, Map<Integer, List<Rect>> numbersLocations) {
        Mat cellImage = new Mat(srcImage, gridCell.getRect());

        /*
         * rectImage.copyTo(result.rowRange(i.y, i.y + i.height).colRange(i.x, i.x +
         * i.width));
         * Imgproc.rectangle(result, i, new Scalar(0, 255, 0));
         */

        /*
         * Mat dest = srcImage.clone();
         * Imgproc.cvtColor(cellImage, dest, Imgproc.COLOR_BGR2GRAY);
         * Imgproc.GaussianBlur(dest, dest, new Size(5, 5), 0);
         * 
         * MatOfDouble mu = new MatOfDouble();
         * MatOfDouble sigma = new MatOfDouble();
         * Core.meanStdDev(dest, mu, sigma);
         * 
         * double d = mu.get(0,0)[0];
         */

        Mat dst = cellImage.clone();

        // Core.inRange(srcImage, new Scalar(65,125,230), new Scalar(120,230,255), dst);
        Core.inRange(cellImage, new Scalar(175, 175, 175), new Scalar(255, 255, 255), dst);

    }

}
