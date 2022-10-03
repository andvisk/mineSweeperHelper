package minesweeperhelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ListReactArea {
    public RectArea mainMember;
    public List<RectArea> list;

    public ListReactArea(RectArea mainMember){
        this.mainMember = mainMember;
        list = new ArrayList<>();
    }

    public ListReactArea(RectArea mainMember, List<RectArea>lst){
        this.mainMember = mainMember;
        list = lst;
    }

}
