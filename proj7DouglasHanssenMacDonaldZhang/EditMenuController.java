/*
File: EditMenuController
CS361 Project 6
Names:  Kyle Douglas, Paige Hanssen, Wyett MacDonald, and Tia Zhang
Date: 10/27/18
*/

package proj7DouglasHanssenMacDonaldZhang;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventType;
import javafx.scene.control.*;
import javafx.scene.control.TabPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.Selection;

import javax.xml.soap.Text;
import java.util.TooManyListenersException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import javafx.stage.Window;

/**
 * This class handles the Edit menu, as a helper to the main Controller.
 * This includes the individual handler methods for the MenuItems as 
 * well as logic for determining deactivating the buttons when
 * appropriate.
 *
 *  @author Yi Feng
 *  @author Iris Lian
 *  @author Chris Marcello
 *  @author Evan Savillo
 */
public class EditMenuController
{
    private CodeAreaTabPane tabPane;
    private TextField textField;

    /**
     * Constructor for the Edit Menu Controller
     */
    public EditMenuController(CodeAreaTabPane tabPane, TextField textField) {
        this.tabPane = tabPane;
        this.textField = textField;
    }

    /**
     * Handles the Undo button action.
     * Undo the actions in the text area.
     */
    public void handleUndoMenuItemAction()
    {
        tabPane.getCurCodeArea().undo();
    }

    /**
     * Handles the Redo button action.
     * Redo the actions in the text area.
     */
    public void handleRedoMenuItemAction()
    {
        tabPane.getCurCodeArea().redo();
    }

    /**
     * Handles the Cut button action.
     * Cuts the selected text.
     */
    public void handleCutMenuItemAction()
    {
        tabPane.getCurCodeArea().cut();
    }

    /**
     * Handles the Copy button action.
     * Copies the selected text.
     */
    public void handleCopyMenuItemAction()
    {
        tabPane.getCurCodeArea().copy();
    }

    /**
     * Handles the Paste button action.
     * Pastes the copied/cut text.
     */
    public void handlePasteMenuItemAction()
    {
        tabPane.getCurCodeArea().paste();
    }

    /**
     * Handles the SelectAll button action.
     * Selects all texts in the text area.
     */
    public void handleSelectAllMenuItemAction()
    {
        tabPane.getCurCodeArea().selectAll();
    }

    /**
     * Handles commenting and uncommenting of the selected text in the code area
     * @param selectedCodeArea
     */
    public void handleToggleCommenting(CodeArea selectedCodeArea)
    {
        // get the start paragraph and the end paragraph of the selection
        Selection<?, ?, ?> selection = selectedCodeArea.getCaretSelectionBind();
        int startIdx = selection.getStartParagraphIndex();
        int endIdx = selection.getEndParagraphIndex();

        // If there is one line that is not commented in the selected paragraphs,
        // comment all selected paragraphs.
        boolean shouldComment = false;
        for (int lineNum = startIdx; lineNum <= endIdx; lineNum++)
        {
            if (!(selectedCodeArea.getParagraph(lineNum).getText().startsWith("//")))
            {
                shouldComment = true;
            }
        }

        // If we should comment all paragraphs, comment all paragraphs.
        // If all selected the paragraphs are commented,
        // uncomment the selected paragraphs.
        if (shouldComment)
        {
            for (int lineNum = startIdx; lineNum <= endIdx; lineNum++)
            {
                selectedCodeArea.insertText(lineNum, 0, "//");
            }
        }
        else
        {
            for (int lineNum = startIdx; lineNum <= endIdx; lineNum++)
            {
                selectedCodeArea.deleteText(lineNum, 0, lineNum, 2);
            }
        }
    }

    /*
    *Indents all highlighted text by one tab per line
    */
    public void handleIndentText() {
        CodeArea curCodeArea = tabPane.getCurCodeArea();
        String selectedText = tabPane.getCurCodeArea().getSelectedText();
        String selectedTextTabbed = selectedText.replace("\n", "\n\t");
        curCodeArea.replaceSelection("\t" + selectedTextTabbed);
    }

    /*
     * Unindents all highlighted text by one tab per line if there is at least one tab on the line
     * If there's no tab, nothing happens on that line
     */
    public void handleUnindentText() {
        CodeArea curCodeArea = tabPane.getCurCodeArea();
        String selectedText = tabPane.getCurCodeArea().getSelectedText();
        String selectedTextUnTabbed = selectedText.replace("\n\t", "\n");
        //The first line won't have a new line char and has to be handled separately
        String firstLine = selectedText.split("(?<=\n)")[0];
        int firstLineLength = firstLine.length();
        //replaceFirst in case there are multiple tabs on the first line
        String firstLineUntabbed = firstLine.replaceFirst("\t", "");
        selectedTextUnTabbed = firstLineUntabbed + selectedTextUnTabbed.substring(firstLineLength);
        curCodeArea.replaceSelection(selectedTextUnTabbed);
    }


    /*
    * Each section of four spaces in the highlighted text become a tab
    */
    public void handleEntab() {
        CodeArea curCodeArea = tabPane.getCurCodeArea();
        String selectedText = tabPane.getCurCodeArea().getSelectedText();
        String selectedTextEntabbed = selectedText.replace("    ", "\t");
        curCodeArea.replaceSelection(selectedTextEntabbed);
    }


    /*
     * Each tab in the highlighted text becomes four spaces
     */
    public void handleDetab() {
        CodeArea curCodeArea = tabPane.getCurCodeArea();
        String selectedText = tabPane.getCurCodeArea().getSelectedText();
        String selectedTextDetabbed = selectedText.replace("\t", "    ");
        //There appears to be a bug in replaceSelection, and this line has fixed it
        curCodeArea.replaceSelection("");
        curCodeArea.replaceSelection(selectedTextDetabbed);

    }

    /**
     * Finds text in the current code area that matches the text in the
     * textField
     */
    public void handleFind() {
        CodeArea curCodeArea = this.tabPane.getCurCodeArea();
        this.textField.setVisible(true);
        this.textField.requestFocus();
        this.textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                ArrayList<Integer> matchIndices = matchesString(newValue);
                if(matchIndices != null) {
                    for (Integer index : matchIndices) {
                        curCodeArea.setStyleClass(index, index + newValue.length(), "find");
                    }
                }
            }
        });
    }

    /**
     * Helper method for handleFind
     * Finds matching text in the CodeArea by getting the index
     * of the text that matches the search input.
     * @param input
     * @return ArrayList of type Integer with indices of matching text
     */

    public ArrayList<Integer> matchesString(String input) {
        ArrayList<Integer> inputArray = new ArrayList<Integer>();
        CodeArea curCodeArea = this.tabPane.getCurCodeArea();

        if(input.length() == 0) {
            return null;
        }

        if (curCodeArea.getText().contains(input)) {
            int index = curCodeArea.getText().indexOf(input);
            while (index != -1) {
                inputArray.add(index);
                index = curCodeArea.getText().indexOf(input, index + 1);
            }
        }
        return inputArray;
    }

    /**
     * Simple helper method
     *
     * @return true if there aren't currently any tabs open, else false
     */
    private boolean isTabless()
    {
        return this.tabPane.getTabs().isEmpty();
    }
}
