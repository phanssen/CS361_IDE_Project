/*
File: ToolbarController.java
CS361 Project 9
Names:  Kyle Douglas, Paige Hanssen, Wyett MacDonald, and Tia Zhang
Date: 11/14/18
*/
package proj9DouglasHanssenMacDonaldZhang.Controllers;
import proj9DouglasHanssenMacDonaldZhang.bantam.lexer.Scanner;
import proj9DouglasHanssenMacDonaldZhang.bantam.util.ErrorHandler;
import javafx.scene.control.*;

import java.io.IOException;

public class ToolbarController {
    private ErrorHandler errorHandler;

    //constructor method
    public ToolbarController() {
        // initialize error handler to be used for Scanner
        this.errorHandler = new ErrorHandler();
    }

    /**
     * Scans a file by first checking to see if file requested to
     * be scanned exists, then either creates a Scanner object
     * and scans, or displays error message.
     * @param curFile is the current file open in the tab pane
     * @throws IOException
     */
    public void handleScanButton(File curFile) throws IOException {
        // check that current file is not null, in the event that a new, unsaved file is passed in
        if(curFile != null) {
            // create scanner and scan file
            Scanner scanner = new Scanner(curFile.getPath(), this.errorHandler);
            while(scanner.scan().kind != Token.Kind.EOF){
                scanner.scan();
            }
        } else {
            // Display File Not Found error dialog for the user
            UserErrorDialog errorDialog = new UserErrorDialog(UserErrorDialog.ErrorType.FNF_ERROR);
            errorDialog.showAndWait();
        }
        
    }
}