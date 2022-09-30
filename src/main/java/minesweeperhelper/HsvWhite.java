package minesweeperhelper;

import org.opencv.core.Scalar;

public class HsvWhite extends HsvColor {
    public HsvWhite() {
            super(new Scalar(255, 38, 255),
                    new Scalar(0, 0, 150));
    }
}
