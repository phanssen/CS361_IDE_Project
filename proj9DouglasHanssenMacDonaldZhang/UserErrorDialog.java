/*
File: UserErrorDialog.java
CS361 Project 9
Names: Liwei Jiang, Chris Marcello, Tracy Quan, Wyett MacDonald, Paige Hanssen, Tia Zhang, Kyle Douglas
Date: 11/20/2018
*/

package proj9DouglasHanssenMacDonaldZhang;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Specialization of the Alert class for making customizable user error notifications,
 * cleanly.
 *
 * @author Evan Savillo
 * @see javafx.scene.control.Alert
 */
public class UserErrorDialog extends Alert
{ 
    /**
     * enumeration which defines the contents of error alerts of certain types
     */
    public enum ErrorType
    {
        SAVING_ERROR("Error",
                "Saving Error",
                "File %s could not be saved!"),
        READING_ERROR("Error",
                "Reading Error",
                "File %s could not be read!"),
        FNF_ERROR("Error",
                "File Not Found Error",
                "File %s could not be found!"),
        IO_ERROR("Error",
                "I/O Error",
                "Process %s could not be started!");

        final String TITLE;
        final String HEADER;
        final String CONTENT;

        ErrorType(final String title, final String header, final String content)
        {
            TITLE = title;
            HEADER = header;
            CONTENT = content;
        }
    }

    /**
     * Constructor which requires a predefined ErrorType in order to
     * construct itself with predefined contents.
     *
     * @param type     enum of variety ErrorType
     * @param filename optionally pass the name of the file with which the error
     *                 occurred.
     */
    public UserErrorDialog(ErrorType type, String filename)
    {
        super(AlertType.NONE);

        this.getButtonTypes().add(ButtonType.OK);

        this.setTitle(type.TITLE);
        this.setHeaderText(type.HEADER);
        this.setContentText(String.format(type.CONTENT, filename));
    }

    /**
     * Fallback constructor
     */
    public UserErrorDialog(ErrorType type)
    {
        this(type, "in question");
    }
}
