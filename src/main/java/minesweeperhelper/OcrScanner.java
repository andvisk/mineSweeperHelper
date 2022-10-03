package minesweeperhelper;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.tesseract.TessBaseAPI;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class OcrScanner {

    private static Logger log = LogManager.getLogger(OcrScanner.class);
    private TessBaseAPI api;

    public OcrScanner() {
        TessBaseAPI api = new TessBaseAPI();
        if (api.Init(null, "eng") != 0) {
            log.error("Could not initialize tesseract.");
            System.exit(1);
        }
    }

    public String getTextFromImage(Mat srcImg) {

        Mat mat = srcImg.clone();
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(mat, mat, new Size(3, 3), 0);
        Imgproc.threshold(mat, mat, 0, 255, Imgproc.THRESH_OTSU);

        try {
            byte[] data = ((java.awt.image.DataBufferByte) ImageUtils.mat2BufferedImage(mat).getRaster()
                    .getDataBuffer()).getData();

            api.SetImage(data, mat.cols(), mat.rows(), mat.channels(), (int) mat.step1());

            BytePointer outText = api.GetUTF8Text();
            
            return outText.getString();
        } catch (IOException e) {

        }
        return null;
    }
}
