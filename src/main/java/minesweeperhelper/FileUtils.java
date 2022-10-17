package minesweeperhelper;

import java.io.File;
import java.util.Arrays;

public class FileUtils {
    public static void checkDirExists(String dirName, boolean empty) {
        File dir = new File(dirName);
        if (!dir.exists())
            dir.mkdirs();
        else {
            if (empty)
                Arrays.asList(dir.listFiles()).stream().forEach(p -> deleteDirectory(p));
        }
    }

    public static boolean deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                deleteDirectory(file);
            }
        }
        return dir.delete();
    }
}
