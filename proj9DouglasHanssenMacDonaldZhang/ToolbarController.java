/*
File: ToolbarController.java
CS361 Project 9
Names:  Kyle Douglas, Paige Hanssen, Wyett MacDonald, and Tia Zhang
Date: 11/14/18
*/
package proj9DouglasHanssenMacDonaldZhang.Controllers;
import proj9DouglasHanssenMacDonaldZhang.bantam.lexer.Scanner;

import javafx.scene.control.*;

import java.io.IOException;

public class ToolbarController {
    Button scanButton;
    Scanner scanner;

    //constructor method
    public ToolbarController(Button scanButton, Scanner scanner) {
        this.scanButton = scanButton;
        this.scanner = scanner;
    }

    public void handleScanButton() throws IOException {
        scanner.scan();
    }

    // scan control
}