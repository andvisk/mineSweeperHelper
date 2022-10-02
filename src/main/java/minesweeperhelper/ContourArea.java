package minesweeperhelper;

import org.opencv.core.MatOfPoint;

public class ContourArea {
    public MatOfPoint contour;
    public ColorsEnum color;

    public ContourArea(MatOfPoint contour, ColorsEnum color) {
        this.contour = contour;
        this.color = color;
    }
}
