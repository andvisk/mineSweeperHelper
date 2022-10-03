package minesweeperhelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


public class OcrScanner {

    private static Logger log = LogManager.getLogger(OcrScanner.class);

    public String getTextFromImage(Mat srcImg) {

        Mat mat = srcImg.clone();
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(mat, mat, new Size(3, 3), 0);
        Imgproc.threshold(mat, mat, 0, 255, Imgproc.THRESH_OTSU);
        
        return "aaa";
    }

}
