package minesweeperhelper;

import java.util.List;
import java.util.Map;

import org.opencv.core.Mat;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ProcessingService extends Service<Mat> {

    private Mat screenShot;
    private int minGridHorizontalMembers;
    private int minGridVerticalMembers;
    private int gridPositionAndSizeTolleranceInPercent;

    public ProcessingService(Mat screenShot, int minGridHorizontalMembers, int minGridVerticalMembers,
            int gridPositionAndSizeTolleranceInPercent) {
        this.screenShot = screenShot;
        this.minGridHorizontalMembers = minGridHorizontalMembers;
        this.minGridVerticalMembers = minGridVerticalMembers;
        this.gridPositionAndSizeTolleranceInPercent = gridPositionAndSizeTolleranceInPercent;
    }

    protected Task<Mat> createTask() {
        Task<Mat> task = new Task<Mat>() {

            @Override
            protected Mat call() throws Exception {

                Map<Integer, Map<Integer, List<Grid>>> mapGridsByWidthAndHeight = GridUtils.collectGrids(screenShot,
                minGridHorizontalMembers, minGridVerticalMembers, gridPositionAndSizeTolleranceInPercent);

                /*
                 * mapGridsByWidthAndHeight.entrySet().stream().flatMap(p ->
                 * p.getValue().entrySet().stream())
                 * .flatMap(p -> p.getValue().stream()).forEach(p ->
                 * GridUtils.drawLocations(screenShot, p));
                 * 
                 * Imgcodecs.imwrite("C:/andrius/test.jpg", screenShot);
                 */

                return HelpScreen.process(screenShot, mapGridsByWidthAndHeight, gridPositionAndSizeTolleranceInPercent);
            }

        };

        return task;
    }
}
