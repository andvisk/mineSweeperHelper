package minesweeperhelper;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class TestTest {
    @Test
    void runTest() {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        final int MIN_WIDTH = 9;
        final int MIN_HEIGHT = 9;
        final BigDecimal TOLLERANCE_IN_PERCENT = BigDecimal.valueOf(65);

        Mat screenShot = Imgcodecs.imread("debug_aaa1_screenshot.jpg");

        ProcessingService service = new ProcessingService(screenShot, MIN_WIDTH, MIN_HEIGHT, TOLLERANCE_IN_PERCENT);
        service.restart();
    }
}
