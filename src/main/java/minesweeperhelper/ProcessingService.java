package minesweeperhelper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ProcessingService extends Service<Mat> {

    private Mat mainScreenShot;
    private int minGridHorizontalMembers;
    private int minGridVerticalMembers;
    private BigDecimal gridPositionAndSizeTolleranceInPercent;

    public ProcessingService(Mat mainScreenShot, int minGridHorizontalMembers,
            int minGridVerticalMembers,
            BigDecimal gridPositionAndSizeTolleranceInPercent) {
        this.mainScreenShot = mainScreenShot;
        this.minGridHorizontalMembers = minGridHorizontalMembers;
        this.minGridVerticalMembers = minGridVerticalMembers;
        this.gridPositionAndSizeTolleranceInPercent = gridPositionAndSizeTolleranceInPercent;
    }

    protected Task<Mat> createTask() {
        Task<Mat> task = new Task<Mat>() {

            @Override
            protected Mat call() throws Exception {

                Map<UUID, Set<Intersection>> mapGroupedIntersections = GridUtils
                        .getMapGroupedByIntersections(mainScreenShot);

                Map<UUID, Rect> gridAreas = mapGroupedIntersections.entrySet().stream()
                        .collect(Collectors.toMap(k -> k.getKey(),
                                p -> GridUtils.getAreaByIntersections(p.getValue())));

                Map<UUID, ScreenShotArea> listScreenShotAreas = gridAreas.entrySet().stream()
                        .collect(Collectors.toMap(k -> k.getKey(),
                                p -> new ScreenShotArea(p.getValue(), mainScreenShot.submat(p.getValue()))));

                return HelpScreen.process(mainScreenShot, listScreenShotAreas, prepareData(),
                        gridPositionAndSizeTolleranceInPercent);
            }

        };

        return task;
    }

    public Map<UUID, Map<BigDecimal, Map<BigDecimal, Map<BigDecimal, List<Grid>>>>> prepareData() {

        Map<UUID, Map<BigDecimal, Map<BigDecimal, Map<BigDecimal, List<Grid>>>>> ret = new HashMap<>();

        for (Map.Entry<UUID, ScreenShotArea> screenShotEntry : screenShotList.entrySet()) {

            Map<BigDecimal, Map<BigDecimal, Map<BigDecimal, List<Grid>>>> mapGridsByAreaWidthHeight = GridUtils
                    .collectGrids(
                            screenShotEntry.getValue().mat(),
                            minGridHorizontalMembers, minGridVerticalMembers,
                            gridPositionAndSizeTolleranceInPercent);

            if (App.debug) {
                Mat screenShotCpy = screenShotEntry.getValue().mat().clone();
                Random rng = new Random(12345);
                mapGridsByAreaWidthHeight.entrySet().stream()
                        .flatMap(p -> p.getValue().entrySet().stream())
                        .flatMap(p -> p.getValue().entrySet().stream())
                        .flatMap(p -> p.getValue().stream()).forEach(p -> {
                            Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
                            GridUtils.drawLocations(screenShotCpy, p, color);
                        });

                Imgcodecs.imwrite("debug_all_grids_" + screenShotEntry.getKey() + ".jpg", screenShotCpy);
            }

            ret.put(screenShotEntry.getKey(), mapGridsByAreaWidthHeight);

        }
        return ret;
    }
}
