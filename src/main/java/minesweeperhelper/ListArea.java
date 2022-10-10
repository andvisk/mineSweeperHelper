package minesweeperhelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ListArea<T> {
    public T mainMember;
    public List<T> list;

    public ListArea(T mainMember){
        this.mainMember = mainMember;
        list = new ArrayList<>();
    }

    public ListArea(T mainMember, List<T>lst){
        this.mainMember = mainMember;
        list = lst;
    }

}
