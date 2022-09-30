package minesweeperhelper;

import org.opencv.core.Scalar;

public class HsvBlue extends HsvColor {
    public HsvBlue() {
        super(new Scalar(140, 255, 255),
                new Scalar(100, 100, 0));
    }
}
