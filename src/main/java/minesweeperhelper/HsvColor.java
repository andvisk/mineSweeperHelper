package minesweeperhelper;

import org.opencv.core.Scalar;

public class HsvColor {
    public Scalar upper;
    public Scalar lower;

    public HsvColor(Scalar upper, Scalar lower){
        this.upper = upper;
        this.lower = lower;
    }
}
