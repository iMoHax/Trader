package ru.trader.view.support;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.collections.ObservableList;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;

public class ViewUtils {

    //Scroll to row if invisible
    public static void show(TableView tableView, int index){
        ObservableList kids = ((TableViewSkin) tableView.getSkin()).getChildren();
        if (kids == null || kids.isEmpty()) { return; }
        VirtualFlow flow = (VirtualFlow)kids.get(1);
        flow.show(index);
    }

    // Edit next cell
    public static <S> void editNext(TableView<S> tableView){
        TableView.TableViewSelectionModel<S> sm = tableView.getSelectionModel();
        sm.selectNext();
        ObservableList<TablePosition> pos = sm.getSelectedCells();
        for (TablePosition p : pos) {
            if (p.getTableColumn().isEditable()) {
                show(tableView, p.getRow() > 0 ? p.getRow() : 0);
                //noinspection unchecked
                tableView.edit(p.getRow(), p.getTableColumn());
                return;
            }
        }
        editNext(tableView);
    }
}
