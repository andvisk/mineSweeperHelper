package minesweeperhelper;

import org.opencv.core.Rect;

public class GridCell {

    private Rect rect;
    private CellTypeEnum cellTypeEnum;
    private int number = -1;

    public GridCell(CellTypeEnum cellTypeEnum, Rect rect, int number) {
        this.cellTypeEnum = cellTypeEnum;
        this.rect = rect;
        this.number = number;
    }

    public CellTypeEnum getCellTypeEnum(){
        return cellTypeEnum;
    }

    public void setCellTypeEnum(CellTypeEnum cellTypeEnum){
        this.cellTypeEnum = cellTypeEnum;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public String toString(){
        return cellTypeEnum.name() + " number: " + number + " x: " + rect.x;
    }
}
