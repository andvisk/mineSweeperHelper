package minesweeperhelper;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.List;
import java.util.Map;

public class GridCell {

    private Rect rect;
    private boolean mine = false;
    private int number = 0;
    private boolean empty = false;

    public GridCell(Mat srcImage, Rect rect, Map<Integer, List<Rect>> numbersLocations) {
        this.rect = rect;
        ImageProcessing.processGridCell(srcImage,this, numbersLocations);
    }

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }
}
