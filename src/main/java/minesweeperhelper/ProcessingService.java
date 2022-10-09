package minesweeperhelper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ProcessingService extends Service<Mat> {

    private Mat screenShot;
    private int minGridHorizontalMembers;
    private int minGridVerticalMembers;
    private BigDecimal gridPositionAndSizeTolleranceInPercent;

    public ProcessingService(Mat screenShot, int minGridHorizontalMembers, int minGridVerticalMembers,
            BigDecimal gridPositionAndSizeTolleranceInPercent) {
        this.screenShot = screenShot;
        this.minGridHorizontalMembers = minGridHorizontalMembers;
        this.minGridVerticalMembers = minGridVerticalMembers;
        this.gridPositionAndSizeTolleranceInPercent = gridPositionAndSizeTolleranceInPercent;
    }

    protected Task<Mat> createTask() {
        Task<Mat> task = new Task<Mat>() {

            @Override
            protected Mat call() throws Exception {

                return HelpScreen.process(screenShot, prepareData(),
                        gridPositionAndSizeTolleranceInPercent);
            }

        };

        return task;
    }

    public Map<BigDecimal, Map<BigDecimal, Map<BigDecimal, List<Grid>>>> prepareData() {
        Map<BigDecimal, Map<BigDecimal, Map<BigDecimal, List<Grid>>>> mapGridsByAreaWidthHeight = GridUtils
                .collectGrids(
                        screenShot,
                        minGridHorizontalMembers, minGridVerticalMembers,
                        gridPositionAndSizeTolleranceInPercent);

        if (App.debug) {
            Mat screenShotCpy = screenShot.clone();
            Random rng = new Random(12345);
            mapGridsByAreaWidthHeight.entrySet().stream()
                    .flatMap(p -> p.getValue().entrySet().stream())
                    .flatMap(p -> p.getValue().entrySet().stream())
                    .flatMap(p -> p.getValue().stream()).forEach(p -> {
                        Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
                        GridUtils.drawLocations(screenShotCpy, p, color);
                    });

            Imgcodecs.imwrite("debug_all_grids.jpg", screenShotCpy);
        }
        return mapGridsByAreaWidthHeight;
    }
}
