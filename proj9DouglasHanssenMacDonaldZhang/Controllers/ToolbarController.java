/*
File: ToolbarController.java
CS361 Project 9
Names: Liwei Jiang, Chris Marcello, Tracy Quan, Wyett MacDonald, Paige Hanssen, Tia Zhang, Kyle Douglas
Date: 11/20/2018
*/
package proj9DouglasHanssenMacDonaldZhang.Controllers;
import proj9DouglasHanssenMacDonaldZhang.CodeAreaTabPane;
import proj9DouglasHanssenMacDonaldZhang.UserErrorDialog;
import proj9DouglasHanssenMacDonaldZhang.UserErrorDialog.ErrorType;
import proj9DouglasHanssenMacDonaldZhang.bantam.lexer.Scanner;
import proj9DouglasHanssenMacDonaldZhang.bantam.util.CompilationException;
import proj9DouglasHanssenMacDonaldZhang.bantam.util.ErrorHandler;
import proj9DouglasHanssenMacDonaldZhang.bantam.lexer.Token;
import proj9DouglasHanssenMacDonaldZhang.bantam.util.Error;
import javafx.scene.control.*;
import java.io.IOException;
import java.io.File;
import java.util.List;
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

    // constructor method
    public ToolbarController(CodeAreaTabPane tabPane, StyleClassedTextArea console) {
        // initialize error handler to be used for Scanner
        this.errorHandler = new ErrorHandler();
        this.tabPane = tabPane;
        this.console = console;
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
           UserErrorDialog userErrorDialog = new UserErrorDialog(ErrorType.FNF_ERROR, filename);
           userErrorDialog.showAndWait();
       }
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