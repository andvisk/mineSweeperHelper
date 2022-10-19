package minesweeperhelper;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.*;
import org.bytedeco.tesseract.*;
import org.bytedeco.tesseract.global.tesseract;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class OcrScanner {

    private static Logger log = LogManager.getLogger(OcrScanner.class);
    private TessBaseAPI api;

    public OcrScanner(String tessDataPath) {
        api = new TessBaseAPI();

        if (api.Init(tessDataPath, "eng", tesseract.OEM_DEFAULT) != 0) {
            log.error("Could not initialize tesseract.");
            System.exit(1);
        }
        api.SetVariable("tessedit_char_whitelist", "12345678");
        api.SetVariable("classify_bln_numeric_mode", "1");
        api.SetVariable("load_system_dawg", "false");
        api.SetVariable("load_freq_dawg", "false");
        api.SetVariable("classify_enable_learning ", "0");
    }

    public String getNumberFromImage(Mat srcImg) {

        Mat mat = srcImg.clone();

        double scaleWidthFasctor = (double) 50 / mat.width();

        Imgproc.resize(mat, mat, new Size(),
                scaleWidthFasctor,
                scaleWidthFasctor,
                Imgproc.INTER_AREA);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        Imgproc.morphologyEx(mat, mat, Imgproc.MORPH_OPEN, kernel);

        Imgproc.GaussianBlur(mat, mat, new Size(5, 5), 0);

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
        
        Imgproc.threshold(mat, mat, 0, 255, Imgproc.THRESH_OTSU);

        int kernelSize = 2;
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2 * kernelSize + 1, 2 * kernelSize + 1),
                new Point(kernelSize, kernelSize));
        Imgproc.dilate(mat, mat, element); //background white for OCR, number black: dilate white background -> erode black number

        byte[] buffer = new byte[(int) mat.total() * mat.channels()];
        mat.get(0, 0, buffer);
        api.SetImage(buffer, mat.width(), mat.height(), mat.channels(), (int) mat.step1());

        BytePointer outText = api.GetUTF8Text();

        String text = outText.getString().trim();
        text = text.replaceAll("[^\\d.]", "");

        outText.deallocate();

        return text;

    }

    public void destructor() {
        api.End();
    }

}
