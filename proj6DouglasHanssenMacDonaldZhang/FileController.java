/*
 * File: FileController.java
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

import javafx.event.Event;
import java.util.Scanner;
import java.util.Optional;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.HashMap;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

/**
 * This class contains the handlers for each of the menu options in the IDE.
 *
 * Keeps track of the tab pane, the current tab, the index of the current tab
 * within the pane, and the File objects of the current tabs.
 *
 * @author  Kevin Ahn, Jackie Hang, Matt Jones, Kevin Zhou
 * @author  Zena Abulhab, Paige Hanssen, Kyle Slager Kevin Zhou
 * @version 3.0
 * @since   10-3-2018
 */
public class FileController {

    private TabPane tabPane;

    // "True" means that the file has not been changed since its last save,
    // if any. False means that something has been changed in the file.
    private HashMap<Tab, Boolean> saveStatus;

    private HashMap<Tab, String> filenames;
    private VBox vBox;
    private MasterController mController;

    /**
     * Constructor for the class. Intializes the save status
     * and the filenames in a HashMap
     */
    public FileController(VBox vBox, TabPane tabPane,MasterController mController) {
        this.saveStatus = new HashMap<>();
        this.filenames = new HashMap<>();
        this.vBox = vBox;
        this.tabPane = tabPane;
        this.mController = mController;
    }

    /**
     * Returns the name of the file open in the current tab.
     * @return The name of the currently open file
     */
    protected String getFileName(){
        Tab curTab = this.tabPane.getSelectionModel().getSelectedItem();
        return filenames.get(curTab);
    }

    /**
     * Handler for the "About" menu item in the "File" menu.
     * Creates an Information alert dialog to display author and information of this program
     */
    public void handleAbout() {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("About");
        dialog.setHeaderText(null);
        dialog.setContentText("Authors: Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou\n" +
                "This application is a basic IDE with syntax highlighting.");
        dialog.showAndWait();
    }

    /**
     * Handler for the "New" menu item in the "File" menu.
     * Adds a new Tab to the TabPane, adds null to the filenames HashMap,
     * and false to the saveStatus HashMap
     */
    public Tab handleNew() {
        Tab newTab = this.makeNewTab(null, tabPane);
        saveStatus.put(newTab, false);
        filenames.put(newTab,null);
        return newTab;
    }

    /**
     * Handler for the "Open" menu item in the "File" menu.
     * Creates a FileChooser to select a file
     * Use scanner to read the file and write it into a new tab.
     */
    public Tab handleOpen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open");
        Window stage = this.vBox.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file == null){
            return null;
        }
        Tab newTab =  makeNewTab(file, tabPane);

        saveStatus.put(newTab, true);
        filenames.put(newTab, file.getPath());
        return newTab;
    }


    /**
     * Handler for the "Close" menu item in the "File" menu.
     * Checks to see if the file has been changed since the last save.
     * If changes have been made, redirect to askSaveAndClose and then close the tab.
     * Otherwise, just close the tab.
     */
    public void handleClose(Event event) {
        Tab curTab = this.tabPane.getSelectionModel().getSelectedItem();
        if (filenames.get(curTab) != null) {
            // check if any changes were made
            if (saveStatus.get(curTab))
                this.closeTab();
            else
                this.askSaveAndClose(curTab.getText(),event);
        } else {
            if(!filenames.isEmpty()) {
                this.askSaveAndClose(curTab.getText(),event);
            }
        }
    }

    /**
     * Handler for the "Save" menu item in the "File" menu.
     * If the current tab has been saved before, writes out the content to its corresponding
     * file in storage.
     * Else if the file has never been saved, opens a pop-up window that allows the user to
     * choose a filename and directory and then store the content of the tab to storage.
     */
    public boolean handleSave() {
        Tab curTab = this.tabPane.getSelectionModel().getSelectedItem();
        if (filenames.get(curTab) != null){
            File file = new File(filenames.get(curTab));
            writeFile(file);
            saveStatus.replace(curTab, true);
            return true;
        }
        else
            return this.handleSaveAs();
    }

    /**
     * Handler for the "Save as..." menu item in the "File" menu.
     * Opens a pop-up window that allows the user to choose a filename and directory.
     * Calls writeFile to save the file to memory.
     * Changes the name of the current tab to match the newly saved file's name.
     */
    public boolean handleSaveAs() {
        Tab curTab = this.tabPane.getSelectionModel().getSelectedItem();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save as...");
        Window stage = this.vBox.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file == null){
            return false;
        }
        else{
            writeFile(file);
            filenames.replace(curTab,file.getPath());
            saveStatus.replace(curTab, true);
        }
        curTab.setText(file.getName());
        return true;
    }

    /**
     * Handler for the "Exit" menu item in the "File" menu.
     * Closes all the tabs using handleClose()
     * Returns when the user cancels exiting any tab.
     */
    public void handleExit(Event event) {
        int numTabs = filenames.size();

        // Close each tab using handleClose()
        // Check if current number of tabs decreased by one to know if the user cancelled.
        for (int i = 0; i < numTabs; i++ ) {
            this.handleClose(event);
            if (filenames.size() == (numTabs - i))
                return;
        }
        Platform.exit();
    }

    /**
     * Creates a pop-up window which allows the user to select whether they wish to save
     * the current file or not.
     * Used by handleClose.
     *
     * @param filename The filename of the file to be saved (or not) at the user's discretion
     */
    private void askSaveAndClose(String filename,Event event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Save Changes?");
        alert.setHeaderText("Do you want to save the changes you made to " + filename + "?");
        alert.setContentText("Your changes will be lost if you don't save them.");

        // the option buttons for the user to click
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == cancelButton) {
                event.consume();
                return;
            } else if (result.get() == yesButton) {
                boolean isNotCancelled = this.handleSave();
                if(isNotCancelled) {
                    this.closeTab();
                } else {
                    event.consume();
                }
                return;
            }
            this.closeTab();
        }
    }

    /**
     * Saves the text present in the current tab to a given filename.
     * Used by handleSave, handleSaveAs.
     *
     * @param file The file object to which the text is written to.
     */
    private void writeFile(File file) {
        Tab curTab = this.tabPane.getSelectionModel().getSelectedItem();
        VirtualizedScrollPane<CodeArea> scrollPane = (VirtualizedScrollPane<CodeArea>) curTab.getContent();
        CodeArea codeArea = scrollPane.getContent();
        String text = codeArea.getText();

        // use a BufferedWriter object to write out the string to a file
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            writer.write(text);
            writer.close();
        }
        catch (IOException e) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText("File Not Found");
            alert.setContentText("Cannot find file. Please select a new file or try again.");
            alert.showAndWait();
            return;
        }

        // update File array
        filenames.replace(curTab, file.getPath());
        saveStatus.replace(curTab, true);
    }

    /**
     * A method to create a new tab with a codeArea inside of it and add it to the tabPane
     * if there exists a file, a scanner will read in the content
     * and write it to the codeArea.
     *
     * @param file the file opened into the new tab
     * @param tabPane the tabPane of the IDE
     * @return a Tab for the master controller to reference
     */
    private Tab makeNewTab(File file, TabPane tabPane){
        // Make a new tab with a TextArea containing the content String,
        // Make it the first tab and select it
        String content = "";
        String filename = "Untitled-" + Integer.toString(filenames.size()+1);

        if (file != null){
            filename = file.getName();
            try {
                Scanner scanner = new Scanner(file).useDelimiter("\\Z");
                if (scanner.hasNext())
                    content = scanner.next();
            }
            catch (FileNotFoundException | NullPointerException e) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setHeaderText("File Not Found");
                alert.setContentText("Please select a new file.");
                alert.showAndWait();
            }
        }

        // creation of the codeArea
        JavaCodeArea codeArea = new JavaCodeArea();
        codeArea.setOnKeyPressed(event -> setSaveStatus());
        codeArea.replaceText(content);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        // creation of the tab
        Tab newTab = new Tab(filename, new VirtualizedScrollPane<>(codeArea,
                ScrollPane.ScrollBarPolicy.ALWAYS,
                ScrollPane.ScrollBarPolicy.ALWAYS));
        tabPane.getTabs().add(0,newTab);
        tabPane.getSelectionModel().select(newTab);
        newTab.setOnCloseRequest(event -> mController.handleClose(event));
        return newTab;
    }

    /**
     * Executes process for when a tab is closed, which is to remove the filename and saveStatus at
     * the corresponding HashMaps, and then remove the Tab object from TabPane
     *
     * NOTE: the following three lines has to be in this order removing the tab first would
     * result in calling handleUpdateCurrentTab() because the currently selected tab will
     * change, and thus the wrong File will be removed from the HashMaps
     */
    private void closeTab() {
        Tab curTab = this.tabPane.getSelectionModel().getSelectedItem();
        saveStatus.remove(curTab);
        filenames.remove(curTab);
        tabPane.getTabs().remove(curTab);
    }

   /**
    * gets the saved status of the current tab.
    * @return the saved status
    */
    public boolean getSaveStatus(){
        Tab curTab = this.tabPane.getSelectionModel().getSelectedItem();
        return this.saveStatus.get(curTab);
    }

    /**
     * Set the current save status of the current
     * tab to false.
     */
    private void setSaveStatus() {
        Tab curTab = this.tabPane.getSelectionModel().getSelectedItem();
        saveStatus.replace(curTab, false);
    }
}

