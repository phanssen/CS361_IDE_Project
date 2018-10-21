/*
 * File: ToolbarController.java
 * Names: Kevin Ahn, Matt Jones, Jackie Hang, Kevin Zhou
 * Class: CS 361
 * Project 4
 * Date: October 2, 2018
 * ---------------------------
 * Edited By: Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou
 * Project 5
 * Date: October 12, 2018
 */

package proj5AbulhabHanssenSlagerZhou;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.io.*;

import java.util.concurrent.*;
import java.util.Optional;


/**
 * This class is the controller for all of the toolbar functionality.
 * Specifically, the compile, compile and run, and stop buttons
 *
 * @author  Kevin Ahn, Jackie Hang, Matt Jones, Kevin Zhou
 * @author  Zena Abulhab, Paige Hanssen, Kyle Slager Kevin Zhou
 * @version 2.0
 * @since   10-3-2018
 *
 */
public class ToolbarController {

    private FutureTask<Boolean> curFutureTask;
    private Console console;
    private Button stopButton;
    private Button compileButton;
    private Button compileRunButton;
    private TabPane tabPane;

    ToolbarController(Console console, Button stopButton, Button compileButton, Button compileRunButton, TabPane tabPane){
        this.console = console;
        this.tabPane = tabPane;
        this.stopButton = stopButton;
        this.compileButton = compileButton;
        this.compileRunButton = compileRunButton;
    }

    /**
     *  Compiles the code currently open, assuming it has been saved.
     * @param filename the name of the file to compile
     */
    public void handleCompile(String filename){
        Thread compileThread = new Thread(()->compileFile(filename));
        compileThread.start();
    }

    /**
     * Calls compile and runs the code
     * @param filename the name of the file to compile and run
     */
    public void handleCompileAndRun(String filename){
        Thread compileRunThread = new Thread(() -> compileRunFile(filename));
        compileRunThread.start();
    }

    /**
     * Stops all currently compiling files and any currently running Java programs
     */
    public void handleStop(){
        if(curFutureTask!=null) {
            this.curFutureTask.cancel(true);
            this.console.WriteLineToConsole("Process terminated.");
        }
    }

    /**
     * Tells the Console that a user-input command was given
     * @param ke Reads in the key pressed
     */
    public void handleNewCommand(KeyEvent ke){
        // check if a program is running
        if (this.curFutureTask == null||this.curFutureTask.isDone()){
            return;
        }
        // if enter key was pressed
        if(ke.getCode() == KeyCode.ENTER){
            this.console.setConsoleCommand(true);
        }
    }

    /**
     * Called when trying to compile something that was unsaved
     * @return the text corresponding with which button the user chose
     */
    public String handleCompileSaveDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Do you want to save your changes?");
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() == yesButton){
            return "yesButton";
        }
        else if(result.get() == noButton){
            return "noButton";
        }
        else{
            return "cancelButton";
        }

    }

    /**
     * Compiles the specified file using the javac command
     * @param filename the name of the file to compile
     * @return whether or not compilation was successful
     */
    private boolean compileFile(String filename) {

        // create and run the compile process
        ProcessBuilder pb = new ProcessBuilder("javac", filename);
        CompileOrRunTask compileTask = new CompileOrRunTask(this.console, pb);
        this.curFutureTask = new FutureTask<Boolean>(compileTask);
        ExecutorService compileExecutor = Executors.newFixedThreadPool(1);
        compileExecutor.execute(curFutureTask);

        // Check if compile was successful, and if so, indicate this in the console
        Boolean compSuccessful = false;
        try {
            compSuccessful = curFutureTask.get();
            if (compSuccessful) {
                Platform.runLater(() ->
                        this.console.WriteLineToConsole("Compilation was Successful."));
            }
            compileExecutor.shutdown();
        } catch (ExecutionException | InterruptedException | CancellationException e) {
            compileTask.stop();
        }

        if (this.tabPane.getTabs().isEmpty()){
            this.stopButton.setDisable(true);
        }
        else{
            enableCompRun();
        }

        return compSuccessful;
    }

    /**
     * Compiles and runs the specified file using the java command
     * @param fileNameWithPath the file name, including its path
     */
    private void compileRunFile(String fileNameWithPath){

        // Try to compile
        boolean compSuccessful = compileFile(fileNameWithPath);
        if(!compSuccessful){
            return;
        }
        // Disable appropriate compile buttons
        disableCompRun();

        // set up the necessary file path elements
        int pathLength = fileNameWithPath.length();
        File file = new File(fileNameWithPath);
        String filename = file.getName();
        String filepath = fileNameWithPath.substring(0,pathLength-filename.length());
        int nameLength = filename.length();
        String classFilename = filename.substring(0, nameLength - 5);

        // Run the java program
        ProcessBuilder pb = new ProcessBuilder("java","-cp",filepath ,classFilename);
        CompileOrRunTask runTask = new CompileOrRunTask(console,pb);
        this.curFutureTask = new FutureTask<Boolean>(runTask);
        ExecutorService curExecutor = Executors.newFixedThreadPool(1);
        curExecutor.execute(this.curFutureTask);
        Boolean runSuccessful;

        try{
            runSuccessful = this.curFutureTask.get();
            curExecutor.shutdown();
        }
        // if the program is interrupted, stop running
        catch (InterruptedException|ExecutionException | CancellationException e){
            runSuccessful = false;
            runTask.stop();
        }

        if (this.tabPane.getTabs().isEmpty()){
            this.stopButton.setDisable(true);
        }
        else{
            enableCompRun();
        }
    }

    /**
     * An inner class used for a thread to execute the run task
     * Designed to be used for compilation or running.
     * Writes the input/output error to the console.
     */
    private class CompileOrRunTask implements Callable{
        private Process curProcess;
        private Console console;
        private ProcessBuilder pb;

        /**
         * Initializes this compile/run task
         * @param console where to write output to
         * @param pb the ProcessBuilder we have used to call javac/java
         */
        CompileOrRunTask(Console console, ProcessBuilder pb){
            this.console = console;
            this.pb = pb;
        }

        /**
         * Starts the process
         * @return will return false if there is an error, true otherwise.
         * @throws IOException error reading input/output to/from console
         */
        @Override
        public Boolean call() throws IOException{
            this.curProcess = pb.start();
            BufferedReader stdInput, stdError;
            BufferedWriter stdOutput;
            stdInput = new BufferedReader(new InputStreamReader(this.curProcess.getInputStream()));
            stdError = new BufferedReader(new InputStreamReader(this.curProcess.getErrorStream()));
            stdOutput = new BufferedWriter((new OutputStreamWriter(this.curProcess.getOutputStream())));

            // Input to the console from the program
            String inputLine;

            // Errors from the executing task
            String errorLine = null;

            // True if there are no errors
            Boolean taskSuccessful = true;

            // A separate thread that checks for user input to the console
            new Thread(()->{
                while(this.curProcess.isAlive()){
                    if(this.console.getConsoleHasCommand()){
                        try {
                            stdOutput.write(this.console.getConsoleCommand());
                            stdOutput.flush();
                        }catch (IOException e){this.stop();}
                    }
                }
            }).start();

            // While there is some input to the console, or errors that have occurred,
            // append them to the console for the user to see.
            while ((inputLine = stdInput.readLine()) != null || (errorLine = stdError.readLine()) != null){

                final String finalInputLine = inputLine;
                final String finalErrorLine = errorLine;

                if (finalInputLine != null) {
                    Platform.runLater(() -> this.console.WriteLineToConsole(finalInputLine));
                }
                if(finalErrorLine != null) {
                    taskSuccessful = false;
                    Platform.runLater(() -> this.console.WriteLineToConsole(finalErrorLine));
                }
                try {
                    Thread.sleep(50);
                }catch (InterruptedException e){
                    this.stop();
                    return taskSuccessful;
                }
            }
            stdError.close();
            stdInput.close();
            stdOutput.close();
            return taskSuccessful;
        }

        /**
         * Stop the current process
         */
        public void stop(){
            if(this.curProcess != null){
                curProcess.destroyForcibly();
            }
        }
    }

    /**
     * Check if the task is still running.
     * @return true if this task is running, and false otherwise
     */
    public boolean getTaskStatus(){
        if(this.curFutureTask == null){
            return false;
        }
        else{
            return !this.curFutureTask.isDone();
        }
    }


    /**
     * Disables the Compile and Compile and Run buttons, enables the Stop button.
     */
    public void disableCompRun() {
        this.compileButton.setDisable(true);
        this.compileRunButton.setDisable(true);
        this.stopButton.setDisable(false);
    }

    /**
     * Enables the Compile and Compile and Run buttons, disables the Stop button.
     */
    public void enableCompRun() {
        this.compileButton.setDisable(false);
        this.compileRunButton.setDisable(false);
        this.stopButton.setDisable(true);
    }

}