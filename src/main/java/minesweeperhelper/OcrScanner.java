package minesweeperhelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.*;
import org.bytedeco.tesseract.*;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class OcrScanner {

    private static Logger log = LogManager.getLogger(OcrScanner.class);
    private TessBaseAPI api;

    public OcrScanner(String tessDataPath) {
        api = new TessBaseAPI();
        if (api.Init(tessDataPath, "eng") != 0) {
            log.error("Could not initialize tesseract.");
            System.exit(1);
        }
        api.SetVariable("tessedit_char_whitelist", "12345678");
    }

    public String getNumberFromImage(Mat srcImg) {

        Mat mat = srcImg.clone();
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(mat, mat, new Size(5, 5), 0);
        Imgproc.threshold(mat, mat, 0, 255, Imgproc.THRESH_OTSU);

        byte[] buffer = new byte[(int) mat.total() * mat.channels()];
        mat.get(0, 0, buffer);
        api.SetImage(buffer, mat.width(), mat.height(), mat.channels(), (int) mat.step1());

        BytePointer outText = api.GetUTF8Text();

        String text = outText.getString().trim().replaceAll("[^\\d.]", "");;

        outText.deallocate();

        return text;

    }

    public void destructor() {
        api.End();
    }
}
