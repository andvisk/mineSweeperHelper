package minesweeperhelper;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public record ScreenShotArea(Rect area, Mat mat) {
}
