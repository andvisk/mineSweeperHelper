package minesweeperhelper;

import java.time.LocalTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class OcrScannerTest {

    @Test
    public void testOcr(){

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        OcrScanner ocrScanner = new OcrScanner(System.getProperty("tesseractDataDir"));
        Mat patternImage = Imgcodecs.imread("src/main/resources/" + 1 + ".png");


        LocalTime start = LocalTime.now();
        String text = ocrScanner.getTextFromImage(patternImage);
        LocalTime stop = LocalTime.now();

        int time = stop.getNano() - start.getNano();

        ocrScanner.destructor();

        Assertions.assertNotNull(text);
        Assertions.assertEquals("1", text);
    }

}
