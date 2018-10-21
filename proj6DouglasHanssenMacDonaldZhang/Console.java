/*
 * File: Controller.java
 * Names: Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou
 * Project 5
 * Date: October 12, 2018
 */

package proj5AbulhabHanssenSlagerZhou;

import org.fxmisc.richtext.StyleClassedTextArea;

/**
 * This class is used to support console functionality.
 * It can be used to write new lines of text to the console.
 * It can also be used to check whether user input been given,
 * and what the command string was.
 *
 * @author  Zena Abulhab, Paige Hanssen, Kyle Slager Kevin Zhou
 * @version 1.0
 * @since   10-12-2018
 *
 */
public class Console extends StyleClassedTextArea {
    // Whether or not a user-input command has been received
    private boolean receivedCommand;

    // Constructor, using StyleClassedTextArea default
    public Console(){
        super();
        receivedCommand = false;
    }

    /**
     * Sets ReceivedCommand to true or false
     * @param newCommand whether or not the console has received a command
     */
    public void setConsoleCommand(boolean newCommand){
        this.receivedCommand = newCommand;
    }

    /**
     * Checks for whether the console has received a command
     * @return whether or not a command was received in this console
     */
    public boolean getConsoleHasCommand(){
        return this.receivedCommand;
    }

    /**
     * Gets the command text that the user has input to this console.
     * @return String that the user has input to the console
     */
    public String getConsoleCommand(){
        String[] lines = this.getText().split("\n");
        int newLineIndex = lines.length-1;
        int caretPos = this.getCaretPosition();

        // Reset ReceivedCommand to false
        this.setConsoleCommand(false);

        // if user gave no input
        if(this.getText().substring(caretPos-2).equals("\n\n")){
            return "";
        }
        return lines[newLineIndex]+"\n";
    }

    /**
     * Adds a new, separate line of text to this console.
     * Used in ToolbarController when printing to the console.
     * @param newLine the string to add to the new line
     */
    public void WriteLineToConsole(String newLine){
        String separator = System.getProperty("line.separator");
        this.appendText(newLine);
        this.appendText(separator);
        this.moveTo(this.getText().length());
        this.requestFollowCaret();
    }
}
