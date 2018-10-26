/*
File: CodeAreaTabPane.java
CS361 Project 6
Names:  Kyle Douglas, Paige Hanssen, Wyett MacDonald, and Tia Zhang
Date: 10/27/18
*/


package proj6DouglasHanssenMacDonaldZhang;

import org.fxmisc.richtext.CodeArea;
import javafx.scene.control.*;
import org.fxmisc.flowless.VirtualizedScrollPane;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TabPane;
import java.io.IOException;

/**
 * A class that holds a static method to access the active code area.
 * Used by the edit menu controller and the file menu controller.
 * @author Evan Savillo
 * @author Paige Hanssen
 * @author Tia Zhang
 */
public class CodeAreaTabPane extends TabPane {
    //@FXML private TabPane tabPane;

    public CodeAreaTabPane(){
        super();
    }

    /**
     * Returns the currently active code area given a TabPane object
     * @return the CodeArea object in the currently selected Tab of the input TabPane
     */
    public CodeArea getCurCodeArea( ) {
        if (this.getTabs().size()>0) {
            Tab selectedTab = getCurTab();
            VirtualizedScrollPane vsp = (VirtualizedScrollPane) selectedTab.getContent();
            return (CodeArea) vsp.getContent();
        }
        else return null;
    }

    /**
     * Returns the currently active tabPane given a TabPane object
     * @return the tapPane object in the currently selected Tab of the input TabPane
     */
    public Tab getCurTab() {
        if (this.getTabs().size()>0) {
            return this.getSelectionModel().getSelectedItem();
        }
        else {
            return null;
        }
    }
}