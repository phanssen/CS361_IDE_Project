/*
File: CodeAreaTabPane.java
CS361 Project 9
Names: Liwei Jiang, Chris Marcello, Tracy Quan, Wyett MacDonald, Paige Hanssen, Tia Zhang, Kyle Douglas
Date: 11/20/2018
*/

package proj10DouglasHanssenMacDonaldZhang;

import org.fxmisc.richtext.CodeArea;
import javafx.scene.control.*;
import org.fxmisc.flowless.VirtualizedScrollPane;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;

/**
 * A class that holds a static method to access the active code area.
 * Used by the edit menu controller and the file menu controller.
 * @author Evan Savillo
 * @author Paige Hanssen
 * @author Tia Zhang
 */
public class CodeAreaTabPane extends TabPane {
    
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