/*
File: CompilationController.java
CS361 Project 6
Names:  Kyle Douglas, Paige Hanssen, Wyett MacDonald, and Tia Zhang
Date: 10/27/18
*/

package proj7DouglasHanssenMacDonaldZhang;

import java.io.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.fxmisc.richtext.StyleClassedTextArea;

/**
 * Controller that handles the compilation and
 * execution of java programs. Uses a process builder
 * to begin the compilation and then creates a new thread
 * to start the process. The running of the program is handled
 * through the process builder using the java command as well as
 * a new thread being executed.
 *
 * @author Evan Savillo
 * @author Lucas DeGraw
 * @author Wyett MacDonald
 * @author Kevin Ahn
 * @author Paige Hanssen
 * @author Tia Zhang
 */
public class CompilationController
{
    private Button compileButton;
    private Button compileAndRunButton;
    private Button haltButton;
    private CodeAreaTabPane tabPane;
    private StyleClassedTextArea consoleTextArea;
    private ProcessBuilder processBuilder;
    private ProcessBuilderTask currentProcessBuilderTask;
    private Thread currentThread;
    private Map<Tab, File> tabFileMap;
    private BooleanProperty isAnythingRunning;
    private BufferedWriter writer;
    /**
     * ByteArrayOutputstream outputStream holds user input data from the console
     */
    private ByteArrayOutputStream outputStream;

    /**
     * constructor for the Compilation Controller Class
     *
     * @param tabPane - where all the open tabs are stored
     * @param toolBarFields - button array holding the toolbar buttons
     * @param consoleTextArea - the textArea holding an open file's contents
     * @param tabFileMap - map of all open files
     */
    public CompilationController(CodeAreaTabPane tabPane,
                                 Button[] toolBarFields,
                                 StyleClassedTextArea consoleTextArea,
                                 Map<Tab, File> tabFileMap,
                                 SimpleListProperty<Tab> tablessListProperty)
    {
        this.tabPane = tabPane;
        this.compileButton = toolBarFields[0];
        this.compileAndRunButton = toolBarFields[1];
        this.haltButton = toolBarFields[2];
        this.processBuilder = new ProcessBuilder();

        String cwd = System.getProperty("user.dir");
        this.processBuilder.directory(new File(cwd));

        this.consoleTextArea = consoleTextArea;
        this.tabFileMap = tabFileMap;

        this.isAnythingRunning = new SimpleBooleanProperty(false);

        //Bindings
        {
            this.compileButton.disableProperty().bind(
                    this.isAnythingRunningProperty().or(tablessListProperty.emptyProperty()));
            this.compileAndRunButton.disableProperty().bind(
                    this.isAnythingRunningProperty().or(tablessListProperty.emptyProperty()));
            this.haltButton.disableProperty().bind(
                    this.isAnythingRunningProperty().not());
        }

        this.outputStream = new ByteArrayOutputStream();
    }

    /**
     * handles the compilation of the java file using the javac compiler
     * gets the currently selected tab and asks the user if they would
     * like to save before compiling. Uses the most recently saved version.
     * Uses a processBuilder and threads to start the compilation
     *
     * @return false if compilation couldn't be completed or was unsuccessful.
     */
    public boolean handleCompileAction()
    {
        ArrayList<String> commandInput = new ArrayList<>();

        Tab currentTab = tabPane.getCurTab();
        String filename = this.tabFileMap.get(currentTab).getPath();

        commandInput.add("javac");
        commandInput.add(filename);

        // Delete the filename from the full path string
        {
            int nameLength = currentTab.getText().length();
            this.processBuilder.directory(new File(filename.substring(0,
                    filename.length() - nameLength)));
        }

        this.processBuilder.command(commandInput);

        this.currentProcessBuilderTask = new ProcessBuilderTask(this.processBuilder
        );
        this.currentThread = new Thread(this.currentProcessBuilderTask);
        //Set it as a daemon so if the app is closed and process is still running,
        // processBuilder is cancelled.
        this.currentThread.setDaemon(true);

        // Adds Listeners for relevant thread properties
        {
            this.currentProcessBuilderTask.valueProperty().addListener(
                    (observable, oldValue, newValue) ->
                            this.consoleTextArea.replaceText(newValue)
            );
            this.currentProcessBuilderTask.runningProperty().addListener(
                    (observable, oldValue, newValue) ->
                            this.setIsAnythingRunning(newValue)
            );
        }

        try
        {
            if (this.writer != null)
                this.writer.close();
            this.writer = null;
        }
        catch (IOException e)
        {
            Alert alert = new Alert(Alert.AlertType.WARNING,  "IO exception from closing");
            alert.setTitle("Warning");
        }
        this.currentThread.start();
        return true;
    }

    /**
     * Handles the running of the java code after calling the
     * handleCompileAction() method. Runs the code by using a
     * processBuilder and the current thread as long as that
     * thread is still alive
     */
    public void handleCompileAndRunAction() throws InterruptedException
    {
        this.consoleTextArea.requestFocus();

        // check if handleCompileAction() has completed
        if (!this.handleCompileAction())
            return;

        // wait for thread that is currently running to finish
        this.currentThread.join();

        // get class to prepare to run the file
        File curFile = tabFileMap.get(tabPane.getCurTab());
        String filename = curFile.getName();
        filename = filename.substring(0, filename.length() - 5); // remove ".class" from filename

        // use the java command to run the process through the process builder
        ArrayList<String> commandInput = new ArrayList<>();
        commandInput.add("java");
        commandInput.add(filename);

        this.processBuilder.command(commandInput);

        // set up the current thread to prepare to run it
        this.currentProcessBuilderTask = new ProcessBuilderTask(this.processBuilder);
        this.currentThread = new Thread(this.currentProcessBuilderTask);
        this.currentThread.setDaemon(true);

        // Adds Listeners for relevant thread properties
        {
            this.currentProcessBuilderTask.valueProperty().addListener(
                    (observable, oldValue, newValue) ->
                    {
                        this.consoleTextArea.replaceText(newValue);
                        this.consoleTextArea.requestFollowCaret();
                    }
            );
            this.currentProcessBuilderTask.runningProperty().addListener(
                    (observable, oldValue, newValue) ->
                    {
                        this.setIsAnythingRunning(newValue);
                    }
            );
        }

        // try to close the input/output writer
        try
        {
            if (this.writer != null)
                this.writer.close();
        }
        catch (IOException e)
        {
            Alert alert = new Alert(Alert.AlertType.WARNING, "IO exception from closing");
            alert.setTitle("Warning");
        }
        // start the thread
        this.currentThread.start();
    }


    /**
     * Handles the canceling of the current process
     */
    public void handleHaltAction() {
        this.currentProcessBuilderTask.cancel();
        try
        {
            this.currentThread.join(3000);
        }
        catch (InterruptedException e)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not halt program. Please try again.");
            alert.setTitle("Error");
        }
    }

    /**
     * Collects all key presses into the console
     * and then writes them into file on RETURN press in a last ditch effort
     * to give it to standard input, which doesn't work
     *
     * @param event any key pressed
     */
    public void handleOnKeyPressedAction(javafx.scene.input.KeyEvent event)
    {
        // Wrapped in this conditional for pressing delete.
        if (event.getCharacter().length() > 0)
        {
            // check if enter key is pressed
            if (event.getCode().equals(KeyCode.ENTER) ||
                    event.getCharacter().getBytes()[0] == '\n' ||
                    event.getCharacter().getBytes()[0] == '\r')
            {
                try
                {
                    // send data to program console
                    this.outputStream.flush();

                    // get standard input
                    OutputStream stdin =
                            this.currentProcessBuilderTask.getProcess().getOutputStream();
                    
                    String content = this.outputStream.toString() + System.lineSeparator();
                    String consoleOutput = this.currentProcessBuilderTask.getConsoleOutput();
                    consoleOutput += System.lineSeparator();

                    if (this.writer == null)
                        this.writer = new BufferedWriter(new OutputStreamWriter(stdin));

                    // write content to program console
                    this.writer.write(content);
                    this.writer.newLine();
                    this.writer.flush();

                    this.outputStream.close();
                    this.outputStream = new ByteArrayOutputStream();
                }
                catch (IOException e)
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "IO exception from outputStream");
                    alert.setTitle("IO exception from outputStream");
                }

            }
            else
            {
                try
                {
                    this.outputStream.write(event.getCharacter().getBytes());
                }
                catch (IOException e)
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "IO exception from outputStream");
                    alert.setTitle("IO exception from outputStream");
                }
            }
        }
    }

    /**
     * Helper method to check if something is running
     *
     * @return returns true if there is a process or thread currently running
     */
    public BooleanProperty isAnythingRunningProperty()
    {
        return this.isAnythingRunning;
    }

    /**
     * sets the field isAnythingRunning to a boolean value
     *
     * @param bool used to set the field isAnythingRunning to true or false
     */
    public final void setIsAnythingRunning(boolean bool)
    {
        this.isAnythingRunning.set(bool);
    }

}
