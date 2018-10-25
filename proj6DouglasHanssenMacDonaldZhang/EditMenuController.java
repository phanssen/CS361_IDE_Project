/*
File: EditMenuController
CS361 Project 5
Names: Kevin Ahn, Lucas DeGraw, Wyett MacDonald, and Evan Savillo
Date: 10/12/18
*/

package proj6DouglasHanssenMacDonaldZhang;

import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private TabPane tabPane;

    private MenuItem undoMenuItem;
    private MenuItem redoMenuItem;
    private MenuItem cutMenuItem;
    private MenuItem copyMenuItem;
    private MenuItem pasteMenuItem;
    private MenuItem selectAllMenuItem;


    /**
     * Constructor for the Edit Menu Controller
     */
    public EditMenuController(Object[] editMenuFields) {
        this.tabPane = (TabPane) editMenuFields[0];
        undoMenuItem = (MenuItem) editMenuFields[1];
        redoMenuItem = (MenuItem) editMenuFields[2];
        cutMenuItem = (MenuItem) editMenuFields[3];
        copyMenuItem = (MenuItem) editMenuFields[4];
        pasteMenuItem = (MenuItem) editMenuFields[5];
        selectAllMenuItem = (MenuItem) editMenuFields[6];
    }

    /**
     * Handles the Undo button action.
     * Undo the actions in the text area.
     */
    public void handleUndoMenuItemAction()
    {
        TabPaneInfo.getCurCodeArea(this.tabPane).undo();
    }

    /**
     * Handles the Redo button action.
     * Redo the actions in the text area.
     */
    public void handleRedoMenuItemAction()
    {
        TabPaneInfo.getCurCodeArea(this.tabPane).redo();
    }

    /**
     * Handles the Cut button action.
     * Cuts the selected text.
     */
    public void handleCutMenuItemAction()
    {
        TabPaneInfo.getCurCodeArea(this.tabPane).cut();
    }

    /**
     * Handles the Copy button action.
     * Copies the selected text.
     */
    public void handleCopyMenuItemAction()
    {
        TabPaneInfo.getCurCodeArea(this.tabPane).copy();
    }

    /**
     * Handles the Paste button action.
     * Pastes the copied/cut text.
     */
    public void handlePasteMenuItemAction()
    {
        TabPaneInfo.getCurCodeArea(this.tabPane).paste();
    }

    /**
     * Handles the SelectAll button action.
     * Selects all texts in the text area.
     */
    public void handleSelectAllMenuItemAction()
    {
        TabPaneInfo.getCurCodeArea(this.tabPane).selectAll();
    }


    /**
     * Comments out highlighted code if not yet commented out and uncomments it if it's commented out
     * Behavior generally modelled off of Toggle Block Comment in SublimeText
     * If highlighted text has multiline /*, removes beginning and closing /*
     * This includes if /* is in the middle of the highlighted text block and not the beginning and end
     * If every line has single line comment "//", removes // from every line
     * Else, adds multiline comments in style of "/*" to beginning and end of text
     * Does not handle /** style comments because that's usually documentation and documentation shouldn't be toggled
     */
    public void handleToggleComments() {
        //System.out.println("A");
        CodeArea curCodeArea = TabPaneInfo.getCurCodeArea(this.tabPane);
        String selectedText = TabPaneInfo.getCurCodeArea(this.tabPane).getSelectedText();

        //Pattern for /* */ with any character (including whitespace) any number of times in between
        Pattern commentPattern = Pattern.compile("(/\\*)[\\s\\S]*(\\*/)");

        Matcher matcher = commentPattern.matcher(Pattern.quote(selectedText));

        //Not using String.matches() because it doesn't account for a section where the commented code is in the middle
        // of the highlighted selection.
        //If highlighted text contains /* and */ in that order:
        if( matcher.find() ){
            //Replace /* and */ with empty space.
            String uncommentedText = selectedText.replace("/*", "");
            uncommentedText = uncommentedText.replace("*/", "");
            curCodeArea.replaceSelection(uncommentedText);
            //System.out.println("B");
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
                //System.out.println("C");
            }

            else{ //In all other cases, put /* at beginning and */ at end of selection
                curCodeArea.replaceSelection("/*" + selectedText + "*/");
                //System.out.println("D");
            }


        }
    }

    /*
    *Indents all highlighted text by one tab per line
    */
    public void handleIndentText() {
        CodeArea curCodeArea = TabPaneInfo.getCurCodeArea(this.tabPane);
        String selectedText = TabPaneInfo.getCurCodeArea(this.tabPane).getSelectedText();
        //Using a lookbehind to keep the new line character in the split
        //String[] selectedTextByLine = selectedText.split("(?<=\n)");
        //String[] selectedTextByLine = selectedText.split("\n");
        String selectedTextTabbed = selectedText.replace("\n", "\n\t");
        //First line won't have a new line char, so add a tab there too
        curCodeArea.replaceSelection("\t" + selectedTextTabbed);

    }

    /*
     * Unindents all highlighted text by one tab per line if there is at least one tab on the line
     * If there's no tab, nothing happens on that line
     */
    public void handleUnindentText() {
        CodeArea curCodeArea = TabPaneInfo.getCurCodeArea(this.tabPane);
        String selectedText = TabPaneInfo.getCurCodeArea(this.tabPane).getSelectedText();
        String selectedTextUnTabbed = selectedText.replace("\n\t", "\n");
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
        CodeArea curCodeArea = TabPaneInfo.getCurCodeArea(this.tabPane);
        String selectedText = TabPaneInfo.getCurCodeArea(this.tabPane).getSelectedText();
        String selectedTextEntabbed = selectedText.replace("    ", "\t");
        curCodeArea.replaceSelection(selectedTextEntabbed);
    }


    /*
     * Each tab in the highlighted text becomes four spaces
     */
    public void handleDetab() {
        CodeArea curCodeArea = TabPaneInfo.getCurCodeArea(this.tabPane);
        String selectedText = TabPaneInfo.getCurCodeArea(this.tabPane).getSelectedText();
        String selectedTextDetabbed = selectedText.replace("\t", "    ");
        curCodeArea.replaceSelection("");
        try {
            curCodeArea.replaceSelection(selectedTextDetabbed);
        }
        catch (Exception e){
            System.out.println("Here's the exception!");
            e.printStackTrace();
        }
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
