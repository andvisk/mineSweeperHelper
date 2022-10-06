package minesweeperhelper;

import org.opencv.core.Scalar;

public class HsvGray extends HsvColor {
    public HsvGray() {
            super(new Scalar(255, 10, 255), // including white color
                    new Scalar(0, 0, 0));
    }
}
