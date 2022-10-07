package minesweeperhelper;

import java.io.File;
import java.util.Arrays;

public class FileUtils {
    public static void checkDirExistsAndEmpty(String dirName) {
        File dir = new File(dirName);
        if (!dir.exists())
            dir.mkdirs();
        else {
            Arrays.asList(dir.listFiles()).stream().forEach(p -> p.delete());
        }
    }
}
