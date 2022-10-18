package minesweeperhelper;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class ProcessingServiceTest {
        @Test
        void runTest() {

                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

                Mat screenShot = Imgcodecs.imread("src/test/resources/src3.png");

                ProcessingService service = new ProcessingService(screenShot,
                                ControllerMain.MIN_WIDTH, ControllerMain.MIN_HEIGHT,
                                ControllerMain.TOLLERANCE_IN_PERCENT, null);
                ProcessingData processingData = service.prepareData(null);

                Mat finalResult = HelpScreen.process(screenShot, processingData.listScreenShotAreas(),
                                processingData.map(),
                                ControllerMain.TOLLERANCE_IN_PERCENT, null);

                Imgcodecs.imwrite(ProcessingService.debugDir + File.separatorChar + "debug_final.png", finalResult);

                int ooo = 0;

        }

}
