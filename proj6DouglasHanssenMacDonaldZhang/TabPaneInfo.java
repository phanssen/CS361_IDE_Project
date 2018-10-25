package proj6DouglasHanssenMacDonaldZhang;

import org.fxmisc.richtext.CodeArea;
import javafx.scene.control.*;
import org.fxmisc.flowless.VirtualizedScrollPane;

/**
 * A class that holds a static method to access the active code area.
 * Used by the edit menu controller and the file menu controller.
 * @author Evan Savillo
 */
public class TabPaneInfo {

    /**
     * Returns the currently active code area given a TabPane object
     * @param tabPane the master TabPane object used to manage all tabs of the IDE
     * @return the CodeArea object in the currently selected Tab of the input TabPane
     */
    public static CodeArea getCurCodeArea(TabPane tabPane) {
        if (tabPane.getTabs().size()>0) {
            Tab selectedTab = getCurTab(tabPane);
            VirtualizedScrollPane vsp = (VirtualizedScrollPane) selectedTab.getContent();
            return (CodeArea) vsp.getContent();
        }
        else return null;
    }

    /**
     * Returns the currently active tabPane given a TabPane object
     * @param tabPane the master TabPane object used to manage all tabs of the IDE
     * @return the tapPane object in the currently selected Tab of the input TabPane
     */
    public static Tab getCurTab(TabPane tabPane) {
        if (tabPane.getTabs().size()>0) {
            return tabPane.getSelectionModel().getSelectedItem();
        }
        else {
            return null;
        }
    }
}