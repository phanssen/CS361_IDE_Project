/*
File: ProcessBuilderTask.java
CS361 Project 5
Names: Kevin Ahn, Lucas DeGraw, Wyett MacDonald, and Evan Savillo
Date: 10/12/18
*/

package proj6DouglasHanssenMacDonaldZhang;

import javafx.application.Platform;
import javafx.concurrent.Task;
import java.io.*;


/**
 * Task which takes in a ProcessBuilder to manage the process created in this Thread.
 *
 */
public class ProcessBuilderTask extends Task<String>
{
    private final ProcessBuilder processBuilder;
    private Process process;
    private String consoleOutput;


    ProcessBuilderTask(ProcessBuilder processBuilder)
    {
        this.processBuilder = processBuilder;
        this.process = null;
        this.consoleOutput = "";
    }

    /**
     * @return returns the output of the process
     * @throws Exception
     */
    @Override
    protected String call() throws Exception
    {
        InputStream inputStream;

        try
        {
            this.process = this.processBuilder.start();
        }
        catch (IOException e)
        {
            consoleOutput = "I/O error";
            return consoleOutput;
        }

        String commandType = this.processBuilder.command().get(0);
        if (commandType.equals("javac"))
            inputStream = this.process.getErrorStream();
        else
            inputStream = this.process.getInputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        int readCharacter;
        while (this.process.isAlive())
        {
            if (isCancelled())
            {
                consoleOutput += "\nProcess Obliterated\n\n";
                break;
            }

            if ((readCharacter = reader.read()) != -1)
            {
                consoleOutput += Character.toString((char)readCharacter);
            }
            Platform.runLater(() -> updateValue(consoleOutput));
        }
        reader.close();
        
        if (commandType.equals("javac"))
            consoleOutput += (this.process.exitValue() == 0) ? 
                    ("Compilation Successful!\n") :
                    ("Trying to compile was a mistake.\n");
        
        return consoleOutput;
    }

    /**
     * @return returns the process so that other classes can access it
     */
    public Process getProcess() {
        return this.process;
    }

    /**
     * @return returns the console output so that other classes can access it
     * @throws Exception
     */
    public String getConsoleOutput() {
        return this.consoleOutput;
    }
}

