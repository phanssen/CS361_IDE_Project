/*
File: ProcessBuilderTask.java
CS361 Project 6
Names:  Kyle Douglas, Paige Hanssen, Wyett MacDonald, and Tia Zhang
Date: 10/27/18
*/

package proj7DouglasHanssenMacDonaldZhang;

import javafx.application.Platform;
import javafx.concurrent.Task;
import java.io.*;

/**
 * Task which takes in a ProcessBuilder to manage the process created in this Thread.
 * @author Kevin Ahn, Lucas DeGraw, Wyett MacDonald, Evan Savillo
 * @author Kyle Douglas, Wyett MacDonald Paige Hanssen, Tia Zhang
 * @version 2.0
 */
public class ProcessBuilderTask extends Task<String>
{
    private final ProcessBuilder processBuilder;
    private Process process;
    private String consoleOutput;
    private String consoleOutputType;


    ProcessBuilderTask(ProcessBuilder processBuilder)
    {
        this.processBuilder = processBuilder;
        this.process = null;
        this.consoleOutput = "";
        this.consoleOutputType = "";
    }

    /**
     * @return returns the output of the process
     * @throws Exception
     */
    @Override
    protected String call() throws Exception
    {
        // redirect any errors
        //processBuilder.redirectErrorStream(true);

        // try to start the processBuilder, send error message to console if it fails
        try
        {
            this.process = this.processBuilder.start();
        }
        catch (IOException e)
        {
            consoleOutput = "I/O error";
            consoleOutputType = "Error";
            return consoleOutput;
        }

        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        // get input stream from the process
        InputStream inputStream = this.process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        int readCharacter;
        int errCharacter;
        while (this.process.isAlive())
        {
            // check if the process has been stopped
            /*if (isCancelled()){
                consoleOutputType = "ProcessInfo";
                consoleOutput += "\nProcess Obliterated\n\n";
                //updateValue(consoleOutput);
                break;
                //return consoleOutput;
            }*/

            // check for user input and assign it to the consoleOutput variable
            /*if(reader.readLine() != null){
                consoleOutput += reader.readLine() +"\n";
        }*/
            if ((readCharacter = reader.read()) != -1) {
                consoleOutputType = "Output";
                consoleOutput += Character.toString((char)readCharacter);
                //System.out.println("X");
            }

            //If you use an if, then the output won't print
            else if((errCharacter = errorReader.read()) != -1){
                System.out.println("Error");
                consoleOutput+= Character.toString((char)errCharacter);
                //Have to add char and the line together because for some reason,
                 //it doesn't process the rest of the line in the loop so it won't print after the first char
                //Adding the first char and readLine in the same line messed up the colors
                consoleOutput += errorReader.readLine() + "\n";
                consoleOutputType = "Error";
            }


            // print output to console
            //if (consoleOutput!=null)Platform.runLater(() -> updateValue(consoleOutput));
            Platform.runLater(() -> updateValue(consoleOutput));

        }
        reader.close();
        
        String commandType = this.processBuilder.command().get(0);
        if (commandType.equals("javac")) {
            consoleOutputType = "ProcessInfo";
            consoleOutput += (this.process.exitValue() == 0) ?
                    ("Compilation Successful!\n") :
                    ("Trying to compile was a mistake.\n");

        }
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

    public String getConsoleOutputType(){ return consoleOutputType;}

    protected void cancelled(){
        super.cancelled();
        System.out.println("Canceling");
        consoleOutputType = "ProcessInfo";
        consoleOutput = "\nProcess Obliterated\n\n"; //Not concatenation because then everything before goes green too
        process.destroy();
        //process.destroyForcibly();
        /*try{
            process.waitFor();
            process.destroy();}
        catch(InterruptedException e){
            System.out.println("Blah");
        }*/
    }
}

