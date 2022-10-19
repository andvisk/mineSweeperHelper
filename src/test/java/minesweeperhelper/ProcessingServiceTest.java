package minesweeperhelper;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class ProcessingServiceTest {
        @Test
        void runTest() {

                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

                Mat screenShot = Imgcodecs.imread("src/test/resources/debug_aaa1_screenshot.png");
                // Mat screenShot = Imgcodecs.imread("src/test/resources/src3.png");

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

        private int[][] getBoardNumbers() {
                return new int[][] {
                        new int[] { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,2,0,2,0,0,0,1,0,3,2,1,1,0,2 },
                        new int[] { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,3,0,3,1,1,1,1,3,0,0,2,1,0,0 },
                        new int[] { 0,0,0,0,0,0,0,0,0,0,0,0,0,4,3,1,2,0,2,1,0,1,0,2,0,0,2,1,2,2 },
                        new int[] { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,2,2,2,1,1,1,0,1,2,2,1,0,0,0 },
                        new int[] { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,1,1,0,0,0,1,7,1,0,0,0,0,0,1,1 },
                        new int[] { 0,0,0,0,0,0,2,2,2,0,0,0,3,2,1,0,1,1,1,0,2,0,2,0,0,0,0,1,2,0 },
                        new int[] { 0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,4,1,1,0,0,2,0,2,0,1,1,1,1,0,3 },
                        new int[] { 0,0,0,4,0,3,1,0,1,3,0,6,0,4,2,3,0,2,0,0,2,3,3,2,2,0,2,2,3,0 },
                        new int[] { 0,0,3,0,2,1,0,0,0,1,2,0,0,0,0,3,0,2,0,0,1,0,0,2,0,3,0,2,4,0 },
                        new int[] { 0,0,0,2,1,0,0,0,1,1,2,3,4,4,2,2,1,1,0,1,2,4,3,3,1,3,3,0,3,0 },
                        new int[] { 0,0,3,3,1,1,0,0,1,0,1,1,0,1,0,0,0,0,0,1,0,2,0,1,0,1,0,2,2,1 },
                        new int[] { 0,0,0,2,0,1,1,1,3,2,2,1,1,1,0,0,0,0,0,2,2,3,1,1,0,2,3,4,3,2 },
                        new int[] { 0,0,3,2,1,1,2,0,3,0,1,0,0,0,0,0,0,0,0,1,0,1,0,0,0,1,0,0,0,0 },
                        new int[] { 0,0,2,0,1,2,5,0,6,3,3,1,1,1,1,1,0,1,2,4,3,2,0,1,1,2,2,4,0,3 },
                        new int[] { 0,0,2,1,1,0,0,0,0,0,3,0,1,1,0,1,0,1,0,0,0,1,0,1,0,1,0,1,1,1 },
                        new int[] { 0,0,0,1,1,3,0,0,0,0,3,1,0,1,1,1,0,1,2,3,2,1,0,1,1,1,0,0,0,0 }
                };
        }

}
