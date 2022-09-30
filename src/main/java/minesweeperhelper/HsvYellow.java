package minesweeperhelper;

import org.opencv.core.Scalar;

public class HsvYellow extends HsvColor {
    public HsvYellow() {
        super(new Scalar(40, 255, 255),
                new Scalar(20, 100, 0));
    }
}
