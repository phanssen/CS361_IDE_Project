/*
File: ToolbarController.java
CS361 Project 9
Names:  Kyle Douglas, Paige Hanssen, Wyett MacDonald, and Tia Zhang
Date: 11/14/18
*/
package proj9DouglasHanssenMacDonaldZhang.Controllers;
import proj9DouglasHanssenMacDonaldZhang.CodeAreaTabPane;
import proj9DouglasHanssenMacDonaldZhang.UserErrorDialog;
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


public class ToolbarController {
    private ErrorHandler errorHandler;
    private CodeAreaTabPane tabPane;
    private StyleClassedTextArea console;

    //constructor method
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
           // create scanner and scan file
           Token token;
           Scanner scanner = new Scanner(filename, this.errorHandler);
           while ( ( token= scanner.scan()).kind != Token.Kind.EOF) {
               tokenString += token.toString() + "\n";
           }
           if(token.kind == Token.Kind.EOF) {
               tokenString += token.toString();
           }
           //System.out.println(tokenString);
           if (this.errorHandler.errorsFound()) {
               List<Error> errorList = this.errorHandler.getErrorList();
               for (Error error : errorList) {
                   this.console.appendText(error.toString() + "\n");
               }
               this.console.appendText("Errors found: " + errorList.size() + "\n");
               errorHandler.clear();
           }
           this.tabPane.getCurCodeArea().replaceText(tokenString);
       }
       catch(CompilationException e){
           System.out.println("Compilation Exception");
       }
    }
}