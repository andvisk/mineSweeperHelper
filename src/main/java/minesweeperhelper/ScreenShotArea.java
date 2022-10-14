package minesweeperhelper;

import java.util.UUID;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public record ScreenShotArea(Rect area, Mat mat, UUID id) {
}
