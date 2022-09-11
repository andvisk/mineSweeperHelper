package minesweeperhelper;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.stage.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;

public class ScreenMonitoringService extends Service<Void> {

    private Logger log = LogManager.getLogger(this.getClass());

    private ControllerMain controllerMain;

    private ObjectProperty<Long> step = new SimpleObjectProperty<>(0L);

    public ScreenMonitoringService(ControllerMain controllerMain) {
        this.controllerMain = controllerMain;
        step.addListener((observable, oldValue, newValue) -> {

            log.info("step: " + oldValue + " -> " + newValue);
            controllerMain.getStage().requestFocus();

        });
    }

    @Override
    protected Task<Void> createTask() {
        Task task = new Task<>() {

            long step = 0;

            protected Void call() {

                while (!isCancelled()) {

                    updateValue(++step);

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

        this.step.bind(Bindings.when(task.valueProperty().isNotNull()).then(task.valueProperty()).otherwise(0L));

        return task;
    }
}
