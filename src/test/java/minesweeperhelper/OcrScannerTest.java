package minesweeperhelper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class OcrScannerTest {
    
    @Test
    public void testOcr(){
        OcrScanner ocrScanner = new OcrScanner();
        Mat patternImage = Imgcodecs.imread("src/main/resources/" + 1 + ".png");
        String text = ocrScanner.getTextFromImage(patternImage);

        Assertions.assertNull(text);
        Assertions.assertSame("1", text);
    }
}
