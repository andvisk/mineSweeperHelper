package minesweeperhelper;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

    public static String debugDir = "debug";
    public static String debugContoursDir = "debug_contours";
    public static String debugRemoveConformSeqDir = "debug_remove_conform_seq";

    public ProcessingService(Mat mainScreenShot, int minGridHorizontalMembers,
            int minGridVerticalMembers,
            BigDecimal gridPositionAndSizeTolleranceInPercent) {
        this.mainScreenShot = mainScreenShot;
        this.minGridHorizontalMembers = minGridHorizontalMembers;
        this.minGridVerticalMembers = minGridVerticalMembers;
        this.gridPositionAndSizeTolleranceInPercent = gridPositionAndSizeTolleranceInPercent;

        FileUtils.checkDirExists(debugDir, true);
        FileUtils.checkDirExists(debugContoursDir, true);
        FileUtils.checkDirExists(debugRemoveConformSeqDir, true);
    }

    protected Task<Mat> createTask() {
        Task<Mat> task = new Task<Mat>() {

            @Override
            protected Mat call() throws Exception {

                ProcessingData processingData = prepareData();

                return HelpScreen.process(mainScreenShot, processingData.listScreenShotAreas(), processingData.map(),
                        gridPositionAndSizeTolleranceInPercent);
            }

        };

        return task;
    }

    public ProcessingData prepareData() {

        Map<UUID, Map<BigDecimal, Map<BigDecimal, Map<BigDecimal, List<Grid>>>>> ret = new HashMap<>();

        Map<UUID, Set<Intersection>> mapGroupedIntersections = GridUtils
                .getMapGroupedByIntersections(mainScreenShot);

        Map<UUID, Rect> gridAreas = mapGroupedIntersections.entrySet().stream()
                .collect(Collectors.toMap(k -> k.getKey(),
                        p -> GridUtils.getAreaByIntersections(p.getValue())));

        List<ScreenShotArea> listScreenShotAreas = gridAreas.entrySet().stream()
                .map(p -> new ScreenShotArea(p.getValue(), mainScreenShot.submat(p.getValue()), p.getKey()))
                .collect(Collectors.toCollection(ArrayList::new));

        Iterator<ScreenShotArea> screenShotAreaIt = listScreenShotAreas.iterator();

        while (screenShotAreaIt.hasNext()) {

            ScreenShotArea screenShotArea = screenShotAreaIt.next();

            Map<BigDecimal, Map<BigDecimal, Map<BigDecimal, List<Grid>>>> mapGridsByAreaWidthHeight = GridUtils
                    .collectGrids(
                            screenShotArea,
                            minGridHorizontalMembers, minGridVerticalMembers,
                            gridPositionAndSizeTolleranceInPercent);

            if (mapGridsByAreaWidthHeight.entrySet().size() > 0 && App.debug) {
                Mat screenShotCpy = screenShotArea.mat().clone();
                Random rng = new Random(12345);
                mapGridsByAreaWidthHeight.entrySet().stream()
                        .flatMap(p -> p.getValue().entrySet().stream())
                        .flatMap(p -> p.getValue().entrySet().stream())
                        .flatMap(p -> p.getValue().stream()).forEach(p -> {
                            Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
                            GridUtils.drawLocations(screenShotCpy, p, color);
                        });

                Imgcodecs.imwrite(debugDir + File.separatorChar + "debug_all_grids_"
                        + screenShotArea.id() + ".jpg", screenShotCpy);
            }

            if (mapGridsByAreaWidthHeight.keySet().size() > 0)
                ret.put(screenShotArea.id(), mapGridsByAreaWidthHeight);
            else
                screenShotAreaIt.remove();

        }
        return new ProcessingData(ret, listScreenShotAreas);
    }
}
