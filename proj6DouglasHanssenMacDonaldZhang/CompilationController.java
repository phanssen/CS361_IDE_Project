/*
File: CompilationController.java
CS361 Project 5
Names: Kevin Ahn, Lucas DeGraw, Wyett MacDonald, and Evan Savillo
Date: 10/12/18
*/

package proj6DouglasHanssenMacDonaldZhang;


import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

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
 */
public class CompilationController
{
    private Button compileButton;
    private Button compileAndRunButton;
    private Button haltButton;
    private TabPane tabPane;
    private StyleClassedTextArea consoleTextArea;
    private ProcessBuilder processBuilder;
    private ProcessBuilderTask currentProcessBuilderTask;
    private Thread currentThread;

    private Map<Tab, File> tabFileMap;

    private Stage primaryStage;

    private BooleanProperty isAnythingRunning;

    private ByteArrayOutputStream outputStream;

    private BufferedWriter writer;

    /**
     * constructor for the Compilation Controller Class
     *
     * @param toolBarFields
     * @param consoleTextArea
     * @param tabFileMap
     */
    public CompilationController(Object[] toolBarFields,
                                 StyleClassedTextArea consoleTextArea,
                                 Map<Tab, File> tabFileMap)
    {
        this.tabPane = (TabPane) toolBarFields[0];
        this.compileButton = (Button) toolBarFields[1];
        this.compileAndRunButton = (Button) toolBarFields[2];
        this.haltButton = (Button) toolBarFields[3];
        this.primaryStage = (Stage) toolBarFields[4];
        this.processBuilder = new ProcessBuilder();

        String cwd = System.getProperty("user.dir");
        this.processBuilder.directory(new File(cwd));
        System.out.println(cwd);

        this.consoleTextArea = consoleTextArea;
        this.tabFileMap = tabFileMap;

        this.isAnythingRunning = new SimpleBooleanProperty(false);

        //Bindings
        {
            this.compileButton.disableProperty().bind(
                    this.isAnythingRunningProperty());
            this.compileAndRunButton.disableProperty().bind(
                    this.isAnythingRunningProperty());
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
        // get selected tab and the code area
        Tab selectedTab = this.tabPane.getSelectionModel().getSelectedItem();
        CodeArea activeCodeArea = TabPaneInfo.getCurCodeArea(this.tabPane);

        // no tabs open
        if (isTabless())
            return false;

        if (tabNeedsSaving(selectedTab))
        {
            if (this.tabFileMap.get(selectedTab) == null)
            {
                // create a fileChooser and add file extension restrictions
                FileChooser fileChooser = new FileChooser();

                // file where the text content is to be saved
                File saveFile = fileChooser.showSaveDialog(this.primaryStage);
                if (saveFile != null)
                {

                    // save the content of the active text area to the selected file
                    this.saveFile(activeCodeArea.getText(), saveFile);

                    // set the title of the tab to the name of the saved file
                    selectedTab.setText(saveFile.getName());

                    // map the tab and the associated file
                    this.tabFileMap.put(selectedTab, saveFile);

                }
                // else return if file is not saved
                else
                {
                    return false;
                }
            }
            else
            {
                Alert alert = new Alert(
                        Alert.AlertType.CONFIRMATION,
                        "Want to save before compiling?",
                        ButtonType.YES,
                        ButtonType.NO,
                        ButtonType.CANCEL
                );
                alert.setTitle("Alert");

                Optional<ButtonType> result = alert.showAndWait();
                // if user presses Yes button, save the file and compile

                if (result.get() == ButtonType.YES)
                {
                    this.saveFile(activeCodeArea.getText(),
                            this.tabFileMap.get(selectedTab));
                }
                // if user presses No button, compile without saving
                else if (result.get() == ButtonType.NO)
                {
                }
                else if (result.get() == ButtonType.CANCEL)
                {
                    return false;
                }
            }
        }

        ArrayList<String> commandInput = new ArrayList<>();

        Tab currentTab = TabPaneInfo.getCurTab(this.tabPane);
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
            System.out.println("IO exception from closing");
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
    public void handleCompileAndRunAction()
    {
        this.consoleTextArea.requestFocus();

        if (!this.handleCompileAction())
            return;

        int timeout = 5000;
        while (this.currentThread.isAlive() && timeout > 0)
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                System.out.println("Interrupted");
            }
            timeout = timeout - 100;
        }
        if (timeout <= 0)
        {
            System.out.println("Failure");
        }

        File curFile = tabFileMap.get(TabPaneInfo.getCurTab(this.tabPane));
        String filename = curFile.getName();
        filename = filename.substring(0, filename.length() - 5); // remove ".class" from filename

        ArrayList<String> commandInput = new ArrayList<>();
        commandInput.add("java");
        commandInput.add(filename);

        this.processBuilder.command(commandInput);

        this.currentProcessBuilderTask = new ProcessBuilderTask(this.processBuilder
        );
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

        try
        {
            if (this.writer != null)
                this.writer.close();
        }
        catch (IOException e)
        {
            System.out.println("IO exception from closing");
        }
        this.currentThread.start();
    }


    /**
     * Handles the canceling of the current process
     */
    public void handleHaltAction()
    {
        this.currentProcessBuilderTask.cancel();
        try
        {
            this.currentThread.join(3000);

        }
        catch (InterruptedException e)
        {
            System.out.println("Interrupted Join");
        }
        System.out.println(this.currentThread.getState());
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
            if (event.getCode().equals(KeyCode.ENTER) ||
                    event.getCharacter().getBytes()[0] == '\n' ||
                    event.getCharacter().getBytes()[0] == '\r')
            {
                try
                {
                    System.out.println("return hit");
                    this.outputStream.flush();

                    OutputStream stdin =
                            this.currentProcessBuilderTask.process.getOutputStream();

                    String content = this.outputStream.toString() + System.lineSeparator();

                    this.currentProcessBuilderTask.consoleOutput += System.lineSeparator();

                    if (this.writer == null)
                        this.writer = new BufferedWriter(new OutputStreamWriter(stdin));

                    this.writer.write(content);
                    this.writer.newLine();
                    this.writer.flush();
                    //writer.close();

                    this.outputStream.close();
                    this.outputStream = new ByteArrayOutputStream();
                }
                catch (IOException e)
                {
                    System.out.println("IO exception from outputStream");
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
                    System.out.println("IO exception from outputStream");
                }
            }
            System.out.println("break");
        }
    }

    /**
     * Helper function to get the text content of a specified file.
     *
     * @param file File to get the text content from
     * @return the text content of the specified file
     */
    private String getFileContent(File file)
    {
        String content = "";
        try
        {
            content = new String(Files.readAllBytes(Paths.get(file.toURI())));
        }
        catch (IOException e)
        {
            UserErrorDialog userErrorDialog = new UserErrorDialog(
                    UserErrorDialog.ErrorType.READING_ERROR, file.getName());
            userErrorDialog.showAndWait();
            content = null;

        }
        return content;
    }

    /**
     * Helper function to save the input string to a specified file.
     *
     * @param content String that is saved to the specified file
     * @param file    File that the input string is saved to
     */
    private boolean saveFile(String content, File file)
    {
        if (!tabPane.getTabs().isEmpty())
        {
            try
            {
                // open a file, save the content to it, and close it
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(content);
                fileWriter.close();
                return true;
            }
            catch (IOException e)
            {
                UserErrorDialog userErrorDialog = new UserErrorDialog(
                        UserErrorDialog.ErrorType.SAVING_ERROR, file.getName());
                userErrorDialog.showAndWait();
                return false;
            }
        }

        return false;
    }

    /**
     * Helper function to check if the content of the specified TextArea
     * has changed from the specified File.
     *
     * @param codeArea TextArea to compare with the the specified File
     * @param file     File to compare with the the specified TextArea
     * @return Boolean indicating if the TextArea has changed from the File
     */
    private boolean ifContentsMatch(CodeArea codeArea, File file)
    {
        String codeAreaContent = codeArea.getText();
        String fileContent = this.getFileContent((file));
        return codeAreaContent.equals(fileContent);
    }

    /**
     * Helper function to handle closing tab action.
     * Checks if the text content within the tab window should be saved.
     *
     * @param tab Tab to be closed
     * @return true if the tab content has not been saved to any file yet,
     * or have been changed since last save.
     */
    private boolean tabNeedsSaving(Tab tab)
    {
        // check whether the embedded text has been saved or not
        if (this.tabFileMap.get(tab) == null)
        {
            return true;
        }
        // check whether the saved file has been changed or not
        else
        {
            VirtualizedScrollPane vsp = (VirtualizedScrollPane) tab.getContent();
            CodeArea codeArea = (CodeArea) vsp.getContent();
            return !this.ifContentsMatch(codeArea, this.tabFileMap.get(tab));
        }
    }

    /**
     * Simple helper method
     *
     * @return true if there aren't currently any tabs open, else false
     */
    boolean isTabless()
    {
        return this.tabPane.getTabs().isEmpty();
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
