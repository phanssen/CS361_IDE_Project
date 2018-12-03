/*
File: ToolbarController.java
CS361 Project 9
Names: Liwei Jiang, Chris Marcello, Tracy Quan, Wyett MacDonald, Paige Hanssen, Tia Zhang, Kyle Douglas
Date: 11/20/2018
*/
package proj10DouglasHanssenMacDonaldZhang.Controllers;
import proj10DouglasHanssenMacDonaldZhang.*;
import proj10DouglasHanssenMacDonaldZhang.bantam.lexer.*;
import proj10DouglasHanssenMacDonaldZhang.bantam.util.*;
import proj10DouglasHanssenMacDonaldZhang.bantam.util.Error;
import javafx.scene.control.*;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import javafx.scene.input.KeyCode;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.StyleClassedTextArea;

/**
 * ToolbarController handles the actions associated with
 * all items on the toolbar. This includes the Scan button.
 *
 * @author Kyle Douglas, Paige Hanssen, Wyett MacDonald, Tia Zhang
 */
public class ToolbarController {
    private ErrorHandler errorHandler;
    private CodeAreaTabPane tabPane;
    private StyleClassedTextArea console;
    private TextField textField;

    // constructor method
    public ToolbarController(CodeAreaTabPane tabPane, StyleClassedTextArea console, TextField textField) {
        // initialize error handler to be used for Scanner
        this.errorHandler = new ErrorHandler();
        this.tabPane = tabPane;
        this.console = console;
        this.textField = textField;
    }

    /**
     * Finds text in the current code area that matches the text in the
     * textField. Is case-sensitive.
     */
    public void handleFind() {
        this.textField.setVisible(true);
        this.textField.requestFocus();
        this.textField.setOnKeyPressed(keyEvent -> {
            // grab current code area here, to make sure the correct current area is updated
            CodeArea curCodeArea = this.tabPane.getCurCodeArea();
            KeyCode keyCode = keyEvent.getCode();
            String searchedText = this.textField.getText();

            // check for enter key and find search results
            // does not run search if the tab pane is empty
            if(keyCode.equals(KeyCode.ENTER) && !this.tabPane.getTabs().isEmpty()) {
                resetHighlighting(curCodeArea);

                ArrayList<Integer> matchIndices = this.matchesString(searchedText);
                if(matchIndices != null) {
                    for (Integer index : matchIndices) {
                        curCodeArea.setStyleClass(index, index + searchedText.length(), "find");
                    }
                }
            }
            // check if the search field is empty, reset text
            // this currently is delayed by one character
            if(searchedText.trim().isEmpty()) {
                resetHighlighting(curCodeArea);
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
    private ArrayList<Integer> matchesString(String input) {
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
     * Reset highlighting for current code area
     * @param curCodeArea is the current code area being viewed in the tab pane
     */
    private void resetHighlighting(CodeArea curCodeArea) {
        // reset the highlighting
        curCodeArea.setStyleClass(0, curCodeArea.getText().length(), "reset-found-words");
        // re-compute the text styling, because it gets removed when found words is reset
        curCodeArea.setStyleSpans(0, JavaCodeArea.computeHighlighting(curCodeArea.getText()));
    }

    /**
     * Scans a file by creating a a Scanner object and
     * scanning, printing out the tokens to a new file.
     * Checks for errors found and prints those to the console.
     * @param filename is the name of the current file open
     * @throws IOException
     */
    public void handleScanButton(String filename)  {
       try {
           String tokenString = "";
           Token token;
           Scanner scanner = new Scanner(filename, this.errorHandler);

           // scan file and grab tokens
           while ( ( token= scanner.scan()).kind != Token.Kind.EOF) {
               tokenString += token.toString() + "\n";
           }

           // handle end of file token that gets skipped by while loop
           if(token.kind == Token.Kind.EOF) {
               tokenString += token.toString();
           }

           // print tokens to new tab
           this.tabPane.getCurCodeArea().replaceText(tokenString);

           // get errors and print to console
           this.printConsoleErrors();
       }
       catch(CompilationException e){
           UserErrorDialog userErrorDialog = new UserErrorDialog(UserErrorDialog.ErrorType.FNF_ERROR, filename);
           userErrorDialog.showAndWait();
       }
    }

    /**
     * Handle Scan and Parse button
     */
    private void handleScanParse() {
        System.out.println("Handle scanning and parsing");
    }

    /**
     * Print ErrorHandler errors to the console
     */
    private void printConsoleErrors() {
        // check for any errors
        if(this.errorHandler.errorsFound()) {

            List<Error> errorList = this.errorHandler.getErrorList();
            for (Error error : errorList) {
                this.console.appendText(error.toString() + "\n");
            }

            // print number of errors to the console
            this.console.appendText("Illegal tokens found: " + errorList.size() + "\n");
            errorHandler.clear();
        }
    }
}