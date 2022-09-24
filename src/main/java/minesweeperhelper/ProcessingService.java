package minesweeperhelper;

import org.opencv.core.Mat;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ProcessingService extends Service<Mat> {

    private Mat screenShot;
    private int tolleranceInPercent;

    public ProcessingService(Mat screenShot, int tolleranceInPercent){
        this.screenShot = screenShot;
    }

    protected Task<Mat> createTask() {
        Task<Mat> task = new Task<Mat>() {

            @Override
            protected Mat call() throws Exception {
                
                return HelpScreen.process(screenShot, tolleranceInPercent);
            }

        };

        return task;
    }
}
