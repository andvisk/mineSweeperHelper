package minesweeperhelper;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
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

        Mat srcCvt = srcImage.clone();

        Mat outImage = Imgcodecs.imread(System.getProperty("user.dir") + File.separatorChar + "mineSweeper.png");

        for (int i = 1; i <= 7; i++) {

            List<Rect> numbersLocations = new ArrayList<>();

            Mat numberImage = Imgcodecs.imread("src/main/resources/" + i + ".png");
            Mat outputImage = new Mat();
            Imgproc.matchTemplate(srcCvt, numberImage, outputImage, machMethod);
            //Imgproc.threshold(outputImage, outputImage, 0.8, 1, Imgproc.THRESH_TOZERO);

            Core.MinMaxLocResult mmr = null;

            while (true) {
                mmr = Core.minMaxLoc(outputImage);
                if (mmr.maxVal >= 0.8) {
                    Rect rect = new Rect(mmr.maxLoc, new Point(mmr.maxLoc.x + numberImage.cols(),mmr.maxLoc.y + numberImage.rows()));
                    numbersLocations.add(rect);

                    Imgproc.rectangle(outImage, mmr.maxLoc,
                            new Point(mmr.maxLoc.x + numberImage.cols(),mmr.maxLoc.y + numberImage.rows()),
                            new    Scalar(0,255,0));
                    

                    Imgproc.rectangle(outputImage, mmr.maxLoc,
                            new Point(mmr.maxLoc.x + numberImage.cols(),mmr.maxLoc.y + numberImage.rows()),
                            new    Scalar(0,255,0),-1);
                } else {
                    break;
                }
            }

            Imgcodecs.imwrite(System.getProperty("user.dir") + File.separatorChar + "mineSweeperOut.png", outImage);

            map.put(i, numbersLocations);

        }

        return map;
    }

    public static void processGridCell(Mat srcImage, GridCell gridCell, Map<Integer, List<Rect>> numbersLocations) {
        Mat cellImage = new Mat(srcImage, gridCell.getRect());
        System.out.println("get number if");
        /*rectImage.copyTo(result.rowRange(i.y, i.y + i.height).colRange(i.x, i.x + i.width));
        Imgproc.rectangle(result, i, new Scalar(0, 255, 0));*/

        /*Mat dest = srcImage.clone();
        Imgproc.cvtColor(cellImage, dest, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(dest, dest, new Size(5, 5), 0);

        MatOfDouble mu = new MatOfDouble();
        MatOfDouble sigma = new MatOfDouble();
        Core.meanStdDev(dest, mu, sigma);

        double d = mu.get(0,0)[0];*/

        Mat dst = cellImage.clone();

        //Core.inRange(srcImage, new Scalar(65,125,230), new Scalar(120,230,255), dst);
        Core.inRange(cellImage, new Scalar(175, 175, 175), new Scalar(255, 255, 255), dst);

    }

    public static Image mat2Image(Mat src) {
        return convertToFxImage(matToBufferedImage(src));
    }

    private static Image convertToFxImage(BufferedImage image) {
        WritableImage wr = null;
        if (image != null) {
            wr = new WritableImage(image.getWidth(), image.getHeight());
            PixelWriter pw = wr.getPixelWriter();
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    pw.setArgb(x, y, image.getRGB(x, y));
                }
            }
        }
        return new ImageView(wr).getImage();
    }

    private static BufferedImage matToBufferedImage(Mat original) {
        // init
        BufferedImage image = null;
        int width = original.width(), height = original.height(), channels = original.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        original.get(0, 0, sourcePixels);

        if (original.channels() > 1) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

        return image;
    }
}
