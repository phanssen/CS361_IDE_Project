/*
File: CodeAreaTabPane.java
CS361 Project 6
Names:  Kyle Douglas, Paige Hanssen, Wyett MacDonald, and Tia Zhang
Date: 10/27/18
*/


package proj8DouglasHanssenMacDonaldZhang;

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

    /*
     *
     * 
     */
    public void handleCheckWellFormed() {
        CodeArea activeCodeArea = this.getCurCodeArea();
        // get the text of the current codeArea
        String text = activeCodeArea.getText();
        // now go through the text to check if it is malformed
        long nOpenBraces = text.chars().filter(ch -> ch == '{').count();
        long nCloseBraces= text.chars().filter(ch -> ch == '}').count();
        long nOpenParens = text.chars().filter(ch -> ch == '(').count();
        long nCloseParens = text.chars().filter(ch -> ch == ')').count();
        long nOpenBrackets = text.chars().filter(ch -> ch == '[').count();
        long nCloseBrackets = text.chars().filter(ch -> ch == ']').count();
        String bracesMessage;
        String parensMessage;
        String bracketsMessage;
        // determine if brackets are well formed
        if (nOpenBraces > nCloseBraces) {
            bracesMessage = "Missing " + (nOpenBraces - nCloseBraces) + " close braces\n";
        }
        else if (nOpenBraces < nCloseBraces) {
            bracesMessage = "Missing " + (nCloseBraces - nOpenBraces) + " open braces\n";
        }
        else {
            bracesMessage = "Braces are well formed\n";
        }
         // determine if parenthesis are well formed
        if (nOpenParens > nCloseParens) {
            parensMessage = "Missing " + (nOpenParens - nCloseParens) + " close parenthesis\n";
        }
        else if (nOpenParens < nCloseParens) {
            parensMessage = "Missing " + (nCloseParens - nOpenParens) + " open parenthesis\n";
        }
        else {
            parensMessage = "Parenthesis are well formed\n";
        }
         // determine if brackets are well formed
        if (nOpenBrackets > nCloseBrackets) {
            bracketsMessage = "Missing " + (nOpenBrackets - nCloseBrackets) + " close brackets\n";
        }
        else if (nOpenBrackets < nCloseBrackets) {
            bracketsMessage = "Missing " + (nCloseBrackets - nOpenBrackets) + " open brackets\n";
        }
        else {
            bracketsMessage = "Brackets are well formed\n";
        }
         // show messages
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Code formation report");
        alert.setHeaderText("Checking brackets, parentheses and braces");
        alert.setContentText(bracesMessage + parensMessage + bracketsMessage);
        alert.showAndWait();
    }
}