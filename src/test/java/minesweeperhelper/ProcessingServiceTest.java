package minesweeperhelper;

import org.junit.jupiter.api.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class ProcessingServiceTest {
    @Test
    void runTest() {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Mat screenShot = Imgcodecs.imread("debug_aaa1_screenshot.png");

        ProcessingService service = new ProcessingService(screenShot, ControllerMain.MIN_WIDTH, ControllerMain.MIN_HEIGHT, ControllerMain.TOLLERANCE_IN_PERCENT);
        service.prepareData();
    }
}
