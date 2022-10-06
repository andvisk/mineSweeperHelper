package minesweeperhelper;

import org.opencv.core.Scalar;

public class HsvWhite extends HsvColor {
    public HsvWhite() {
            super(new Scalar(255, 130, 255), //included whit with shadow (gray)
                    new Scalar(0, 0, 90));
    }
}
