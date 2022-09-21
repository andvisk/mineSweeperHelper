package minesweeperhelper;

import org.opencv.core.Rect;

public class MineSweeperGridCell extends GridCell {
    private CellTypeEnum cellTypeEnum;
    private int number = -1;

    public MineSweeperGridCell(CellTypeEnum cellTypeEnum, Rect rect, int number) {
        super(rect);
        this.cellTypeEnum = cellTypeEnum;
        this.number = number;
    }

    public CellTypeEnum getCellTypeEnum() {
        return cellTypeEnum;
    }

    public void setCellTypeEnum(CellTypeEnum cellTypeEnum) {
        this.cellTypeEnum = cellTypeEnum;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String toString() {
        return /* cellTypeEnum.name() + " number: " +  */number + " x: " + getRect().x + " y: " + getRect().y + " id: " + getId().toString();
    }

}
