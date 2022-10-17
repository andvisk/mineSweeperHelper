package minesweeperhelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImageUtils {

    private static final Logger logger = LogManager.getLogger(ImageUtils.class);

    public static Mat bufferedImage2Mat(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", byteArrayOutputStream);
        byteArrayOutputStream.flush();
        return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.IMREAD_UNCHANGED);
    }

    public static BufferedImage mat2BufferedImage(Mat matrix) throws IOException {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, mob);
        return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
    }

    public static Image mat2Image(Mat src) {
        try {
            return bufferedImageToFxImage(ImageUtils.mat2BufferedImage(src));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private static Image bufferedImageToFxImage(BufferedImage image) {
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

    public static Mat writableImageToMat(WritableImage writableImage) {
        Image image = new ImageView(writableImage).getImage();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        byte[] buffer = new byte[width * height * 4];

        PixelReader reader = image.getPixelReader();
        WritablePixelFormat<ByteBuffer> format = WritablePixelFormat.getByteBgraInstance();
        reader.getPixels(0, 0, width, height, format, buffer, 0, width * 4);

        Mat mat = new Mat(height, width, CvType.CV_8UC4);
        mat.put(0, 0, buffer);
        return mat;
    }

    public static Mat gammaCorrection(Mat matImgSrc, double gammaValue) {
        Mat lookUpTable = new Mat(1, 256, CvType.CV_8U);
        byte[] lookUpTableData = new byte[(int) (lookUpTable.total() * lookUpTable.channels())];
        for (int i = 0; i < lookUpTable.cols(); i++) {
            lookUpTableData[i] = saturate(Math.pow(i / 255.0, gammaValue) * 255.0);
        }
        lookUpTable.put(0, 0, lookUpTableData);
        Mat img = new Mat();
        Core.LUT(matImgSrc, lookUpTable, img);
        return img;
    }

    public static Mat contrastAndBrightnessCorrection(Mat matImgSrc, double contrast, int brightness) {
        Mat imgClone = matImgSrc.clone();
        matImgSrc.convertTo(imgClone, -1, contrast, brightness);
        return imgClone;
    }

    public static byte saturate(double val) {
        int iVal = (int) Math.round(val);
        iVal = iVal > 255 ? 255 : (iVal < 0 ? 0 : iVal);
        return (byte) iVal;
    }

    public static Mat equalizeHistForColorImg(Mat srcImage) {

        Mat ycrcb = new Mat();
        Imgproc.cvtColor(srcImage, ycrcb, Imgproc.COLOR_BGR2YCrCb);

        List<Mat> channels = new ArrayList<>();
        Core.split(ycrcb, channels);

        Imgproc.equalizeHist(channels.get(0), channels.get(0));

        Core.merge(channels, ycrcb);

        Mat imgClone = srcImage.clone();
        Imgproc.cvtColor(ycrcb, imgClone, Imgproc.COLOR_YCrCb2BGR);

        return imgClone;
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
