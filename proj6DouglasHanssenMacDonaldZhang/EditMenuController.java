/*
File: EditMenuController
CS361 Project 6
Names:  Kyle Douglas, Paige Hanssen, Wyett MacDonald, and Tia Zhang
Date: 10/27/18
*/

package proj6DouglasHanssenMacDonaldZhang;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventType;
import javafx.scene.control.*;
import javafx.scene.control.TabPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

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
     * Comments out highlighted code if not yet commented out and uncomments it if it's commented out
     * Behavior generally modelled off of Toggle Block Comment in SublimeText
     * If highlighted text has multiline /*, removes beginning and closing /*
     * This includes if /* is in the middle of the highlighted text block and not the beginning and end
     * If every line has single line comment "//", removes // from every line
     * Else, adds multiline comments in style of "/*" to beginning and end of text
     * Does not handle /** style comments because that's usually documentation and
     * documentation shouldn't be toggled
     */
    public void handleToggleComments() {

        CodeArea curCodeArea = tabPane.getCurCodeArea();
        String selectedText = tabPane.getCurCodeArea().getSelectedText();

        //Pattern for /* */ with any character (including whitespace) any number of times in between
        Pattern commentPattern = Pattern.compile("(/\\*)[\\s\\S]*(\\*/)");

        Matcher matcher = commentPattern.matcher(Pattern.quote(selectedText));

        //Not using String.matches() because it doesn't account for a section where the
        //commented code is in the middle
        // of the highlighted selection.
        //If highlighted text contains /* and */ in that order:
        if( matcher.find() ){
            //Replace /* and */ with empty space.
            String uncommentedText = selectedText.replace("/*", "");
            uncommentedText = uncommentedText.replace("*/", "");
            curCodeArea.replaceSelection(uncommentedText);

        }

        else{
            //If every line in the block has // at the beginning
            String[] selectedTextByLine = selectedText.split("/n");
            boolean commentedOut = true;
            for(int i = 0; i < selectedTextByLine.length; i++){
                if(! ("//").equals( selectedTextByLine[i].trim().substring(0, 2) ) ){
                    commentedOut = false;
                    break;
                }
            }
            //Then remove all the //
            if(commentedOut){
                String uncommentedText = selectedText.replace("//", "");
                curCodeArea.replaceSelection(uncommentedText);

            }

            else{ //In all other cases, put /* at beginning and */ at end of selection
                curCodeArea.replaceSelection("/*" + selectedText + "*/");

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
        CodeArea curCodeArea = TabPaneInfo.getCurCodeArea(this.tabPane);
        this.textField.setVisible(true);
        this.textField.requestFocus();
        this.textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                ArrayList<String> matches = matchesString(newValue);
                if(matches != null) {
                        System.out.println(matchesString(newValue).toString());
                }
            }
        });
    }

    /**
     * Helper method for handleFind
     * Iterates through codeArea to find matching text
     * @param input
     * @return ArrayList of type String with matching text
     */

    public ArrayList<String> matchesString(String input) {
        ArrayList<String> inputArray = new ArrayList<String>();
        CodeArea curCodeArea = TabPaneInfo.getCurCodeArea(this.tabPane);

        if(input.length() == 0) {
            return null;
        }

        String myString = "";
        int inputCount = 0;
        for(int i = 0; i < curCodeArea.getLength(); i++) {
            if(Character.toString(curCodeArea.getText().charAt(i)).equalsIgnoreCase(
                    Character.toString(input.charAt(inputCount)))) {
                inputCount++;
                myString += Character.toString(curCodeArea.getText().charAt(i));
                if(inputCount == input.length()) {
                    inputArray.add(myString);
                    inputCount = 0;
                    myString = "";
                }
            }
            else {
                inputCount = 0;
                myString = "";
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
