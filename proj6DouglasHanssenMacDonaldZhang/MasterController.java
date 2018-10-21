/*
 * File: MasterController.java
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


import javafx.beans.property.SimpleListProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.event.Event;


/**
 * This is the master controller for the program. it references
 * the other controllers for proper menu functionality.
 *
 * @author  Kevin Ahn, Jackie Hang, Matt Jones, Kevin Zhou
 * @author  Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou
 * @version 2.0
 * @since   10-3-2018
 */
public class MasterController {
    @FXML private Menu editMenu;
    @FXML private TabPane tabPane;
    @FXML private VBox vBox;
    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem saveAsMenuItem;
    @FXML private MenuItem closeMenuItem;
    @FXML private Console console;
    @FXML private Button stopButton;
    @FXML private Button compileButton;
    @FXML private Button compileRunButton;
    private EditController eController;
    private FileController fController;
    private ToolbarController tController;

    @FXML
    public void initialize(){
        eController = new EditController(tabPane);
        fController = new FileController(vBox,tabPane,this);
        tController = new ToolbarController(console,stopButton,compileButton,compileRunButton,tabPane);
        SimpleListProperty<Tab> listProperty = new SimpleListProperty<Tab> (tabPane.getTabs());
        editMenu.disableProperty().bind(listProperty.emptyProperty());
        saveMenuItem.disableProperty().bind(listProperty.emptyProperty());
        saveAsMenuItem.disableProperty().bind(listProperty.emptyProperty());
        closeMenuItem.disableProperty().bind(listProperty.emptyProperty());
        disableToolbar();
    }

    /**
     * Calls handleNewCommand() from the Toolbar Controller if the user
     * presses the enter key.
     * @param ke the key event
     */
    @FXML public void handleNewCommand(KeyEvent ke){
        tController.handleNewCommand(ke);
    }

    /**
     * Handler for the Compile in the toolbar. Checks if the current file
     * has been saved. If it has not, prompts the user to save, if so,
     * compiles the program. If user chooses not to save, compiles last
     * version of the file.
     */
    @FXML public void handleCompile(){
        disableCompRun();
        if(!fController.getSaveStatus()) {
            String saveResult = tController.handleCompileSaveDialog();
            if (saveResult == "yesButton") {
                fController.handleSave();
                tController.handleCompile(fController.getFileName());
            } else if (saveResult == "noButton") {
                if(fController.getFileName() == null){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Cannot compile a file with no previous saved version.");
                    alert.showAndWait();
                }
                tController.handleCompile(fController.getFileName());
            }else{ return;}
        } else {
            tController.handleCompile(fController.getFileName());
        }
        if(this.tabPane.getTabs().isEmpty()){
            disableToolbar();
        }
    }

    /**
     * Handler for the Compile and Run button in the toolbar.
     * Checks if the current file has been saved. If it has not,
     * prompts the user to save, if so, compiles and runs the program.
     * If user chooses not to save, compiles and runs the last
     * version of the file.
     */
    @FXML public void handleCompileAndRun() {
        disableCompRun();
        if(!fController.getSaveStatus()) {
            String saveResult = tController.handleCompileSaveDialog();
            if (saveResult.equals("yesButton")) {
                fController.handleSave();
                tController.handleCompileAndRun(fController.getFileName());
            } else if (saveResult == "noButton") {
                if(fController.getFileName() == null){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Cannot compile a file with no previous saved version.");
                    alert.showAndWait();
                }
                tController.handleCompileAndRun(fController.getFileName());
            }
            else{enableCompRun();
                return;
            }
        } else {
            tController.handleCompileAndRun(fController.getFileName());
        }
        if(this.tabPane.getTabs().isEmpty()){
            disableToolbar();
        }
    }

    /**
     * Handler for the Stop button in the toolbar.
     * Calls the handleStop() method from Toolbar Controller and re-enables the toolbar buttons.
     */
    @FXML public void handleStop(){
        tController.handleStop();
        if(this.tabPane.getTabs().isEmpty()) {
            this.stopButton.setDisable(true);
            return;
        }
        enableCompRun();
    }

    /**
     * Handler for the "About" menu item in the "File" menu.
     * Creates an Information alert dialog to display author and information of this program
     */
    @FXML public void handleAbout() {
        fController.handleAbout();
    }

    /**
     * Handler for the "New" menu item in the "File" menu.
     * Adds a new Tab to the TabPane, and also adds null to the HashMap
     * Also sets the current tab for both the file and edit controllers.
     */
    @FXML public void handleNew() {
        fController.handleNew();
        if(!tController.getTaskStatus()) {
            enableCompRun();
        }
    }

    /**
     * Handler for the "Open" menu item in the "File" menu.
     * Creates a FileChooser to select a file
     * Use scanner to read the file and write it into a new tab.
     * Also sets the current tab for both the file and edit controllers.
     */
    @FXML public void handleOpen() {
        fController.handleOpen();
        if(!tController.getTaskStatus()) {
            enableCompRun();
        }
    }

    /**
     * Handler for the "Close" menu item in the "File" menu.
     * Checks to see if the file has been changed since the last save.
     * If changes have been made, redirect to askSave and then close the tab.
     * Otherwise, just close the tab.
     */
    @FXML public void handleClose(Event event) {
        fController.handleClose(event);
        if (this.tabPane.getTabs().isEmpty()&&!tController.getTaskStatus()){
            disableToolbar();
        }

    }

    /**
     * Handler for the "Save" menu item in the "File" menu.
     * If the current tab has been saved before, writes out the content to its corresponding
     * file in storage.
     * Else if the file has never been saved, opens a pop-up window that allows the user to
     * choose a filename and directory and then store the content of the tab to storage.
     */
    @FXML public void handleSave() {
        fController.handleSave();
    }

    /**
     * Handler for the "Save as..." menu item in the "File" menu.
     * Opens a pop-up window that allows the user to choose a filename and directory.
     * Calls writeFile to save the file to memory.
     * Changes the name of the current tab to match the newly saved file's name.
     */
    @FXML public void handleSaveAs( ) {
        fController.handleSaveAs();
    }

    /**
     * Handler for the "Exit" menu item in the "File" menu.
     * Closes all the tabs using handleClose()
     * Returns when the user cancels exiting any tab.
     */
    @FXML public void handleExit(Event event) {
        tController.handleStop();
        fController.handleExit(event);
    }

    /**
     * Handler for the "Undo" menu item in the "Edit" menu.
     */
    @FXML
    public void handleUndo() { eController.handleUndo(); }

    /**
     * Handler for the "Redo" menu item in the "Edit" menu.
     */
    @FXML
    public void handleRedo() {eController.handleRedo(); }

    /**
     * Handler for the "Cut" menu item in the "Edit" menu.
     */
    @FXML
    public void handleCut() {eController.handleCut(); }

    /**
     * Handler for the "Copy" menu item in the "Edit" menu.
     */
    @FXML
    public void handleCopy() {eController.handleCopy();}

    /**
     * Handler for the "Paste" menu item in the "Edit" menu.
     */
    @FXML
    public void handlePaste() {eController.handlePaste(); }

    /**
     * Handler for the "SelectAll" menu item in the "Edit" menu.
     */
    @FXML
    public void handleSelectAll() {eController.handleSelectAll(); }

   /**
    * Disables the Compile and Compile and Run buttons, enables the Stop button.
    */
   private void disableCompRun() {
        this.compileButton.setDisable(true);
        this.compileRunButton.setDisable(true);
        this.stopButton.setDisable(false);
   }

   /**
    * Enables the Compile and Compile and Run buttons, disables the Stop button.
    */
   private void enableCompRun() {
       this.compileButton.setDisable(false);
       this.compileRunButton.setDisable(false);
       stopButton.setDisable(true);
   }

    /**
     * Disables the Compile, Compile and Run, and Stop buttons in the toolbar
     */
   private void disableToolbar(){
       this.compileButton.setDisable(true);
       this.compileRunButton.setDisable(true);
       this.stopButton.setDisable(true);
   }


}
