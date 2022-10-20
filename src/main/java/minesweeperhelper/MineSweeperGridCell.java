package minesweeperhelper;

import java.util.UUID;

import org.opencv.core.Rect;

public class MineSweeperGridCell extends RectArea {
    private CellTypeEnum cellTypeEnum;
    private int number = -1;

    public MineSweeperGridCell(CellTypeEnum cellTypeEnum, Rect rect, int number, ColorsEnum colorsEnum) {
        super.rectangle = rect;
        super.color = colorsEnum;
        super.id = UUID.randomUUID();
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
        return /* cellTypeEnum.name() + " number: " +  */number + " x: " + rectangle.x + " y: " + rectangle.y + " id: " + id.toString();
    }

}
