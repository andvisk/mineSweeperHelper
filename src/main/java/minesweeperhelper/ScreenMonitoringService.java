package minesweeperhelper;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.stage.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;

public class ScreenMonitoringService extends Service<Void> {

    private Logger log = LogManager.getLogger(this.getClass());

    private ControllerMain controllerMain;

    public ScreenMonitoringService(ControllerMain controllerMain) {
        this.controllerMain = controllerMain;
    }

    @Override
    protected Task<Void> createTask() {
        Task task = new Task<>() {
            protected Void call() {

                while (!isCancelled()) {

                    Screen screen = Screen.getPrimary();
                    BigDecimal dpi = BigDecimal.valueOf(screen.getDpi());
                    updateValue(dpi);

                    log.info("screen monitoring is on");

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException interrupted) {
                        if (isCancelled()) {
                            break;
                        }
                    }
                }
                return null;
            }

        };

        //controllerMain.dpiProperty().bind(Bindings.when(task.valueProperty().isNotNull()).then(task.valueProperty()).otherwise(controllerMain.dpiProperty().getValue()));

        return task;
    }
}
