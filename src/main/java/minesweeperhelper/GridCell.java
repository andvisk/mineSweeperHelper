package minesweeperhelper;

import java.util.UUID;

import org.opencv.core.Rect;

public class GridCell {

    private UUID id;
    private Rect rect;
    private int positionInGridX = -1;
    private int positionInGridY = -1;

    public GridCell(Rect rect) {
        this.rect = rect;
        this.id = UUID.randomUUID();
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public UUID getId(){
        return this.id;
    }

    public void setPositionInGridX(int pos){
        this.positionInGridX = pos;
    }
    public void setPositionInGridY(int pos){
        this.positionInGridY = pos;
    }

    public int getX(){
        return positionInGridX;
    }
    public int getY(){
        return positionInGridY;
    }
}
