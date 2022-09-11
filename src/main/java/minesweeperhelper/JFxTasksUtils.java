package minesweeperhelper;

import javafx.concurrent.Task;
import java.util.concurrent.Callable;

public class JFxTasksUtils {
    public static <T,R> Task<T> createTask(Callable<T> callable) {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                return callable.call();
            }
        };
    }
}
