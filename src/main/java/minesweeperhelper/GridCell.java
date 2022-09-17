package minesweeperhelper;

import java.util.UUID;

import org.opencv.core.Rect;

public class GridCell {

    private UUID id;
    private Rect rect;
    private CellTypeEnum cellTypeEnum;
    private int number = -1;

    public GridCell(CellTypeEnum cellTypeEnum, Rect rect, int number) {
        this.cellTypeEnum = cellTypeEnum;
        this.rect = rect;
        this.number = number;
        this.id = UUID.randomUUID();
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

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public String toString() {
        return /* cellTypeEnum.name() + " number: " +  */number + " x: " + rect.x + " y: " + rect.y + " id: " + id.toString();
    }

    public UUID getId(){
        return this.id;
    }
}
