/*
File: Controller.java
CS361 Project 6
Names:  Kyle Douglas, Paige Hanssen, Wyett MacDonald, and Tia Zhang
Date: 10/27/18
*/

package proj9DouglasHanssenMacDonaldZhang;

import proj9DouglasHanssenMacDonaldZhang.Controllers.*;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleListProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.StyleClassedTextArea;

/**
 * Controller is the main controller for the application.
 * It itself doesn't handle much. What it does is delegate
 * tasks to either of the sub controllers, FileMenuController or
 * EditMenuController.
 *
 * @author Yi Feng
 * @author Iris Lian
 * @author Chris Marcello
 * @author Evan Savillo
 */
public class Controller
{

    @FXML private VBox vBox;

    // FXML Menu items from File, Edit, Help
    @FXML private MenuItem closeMenuItem;
    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem saveAsMenuItem;
    @FXML private MenuItem darkModeMenuItem;
    @FXML private MenuItem normalModeMenuItem;
    @FXML private MenuItem funModeMenuItem;
    @FXML private MenuItem hallowThemeItem;
    @FXML private MenuItem undoMenuItem;
    @FXML private MenuItem redoMenuItem;
    @FXML private MenuItem cutMenuItem;
    @FXML private MenuItem copyMenuItem;
    @FXML private MenuItem pasteMenuItem;
    @FXML private MenuItem selectAllMenuItem;
    @FXML private MenuItem toggleCommentsMenuItem;
    @FXML private MenuItem indentTextMenuItem;
    @FXML private MenuItem unindentTextMenuItem;
    @FXML private MenuItem entabTextMenuItem;
    @FXML private MenuItem detabTextMenuItem;
    @FXML private MenuItem findMenuItem;
    @FXML private MenuItem handleCheckWellFormed;

    @FXML private Menu prefMenu;

    // Toolbar menu items
    @FXML private Button scanButton;
    @FXML private TextField findTextField;

    @FXML private SplitPane splitPane;
    @FXML private StyleClassedTextArea consoleTextArea;
    @FXML private Stage primaryStage;

    private CodeAreaTabPane tabPane;
    private Map<Tab, File> tabFileMap;

    // All the sub-controller items (found in Controllers package)
    private FileMenuController fileMenuController;
    private EditMenuController editMenuController;
    private HelpMenuController helpMenuController;
    /**
     * ContextMenuController for handling right-click menu actions
     */
    private ContextMenuController contextMenuController;
    /**
     * ToolbarController for handling buttons on the toolbar
     */
    private ToolbarController toolbarController;

    /**
     * This function is called after the FXML fields are populated.
     * Initializes the tab file map with the default tab.
     * and passes necessary items
     */
    public void initialize()
    {
        tabPane = new CodeAreaTabPane();
        splitPane.getItems().add(0, tabPane);

        tabFileMap = new HashMap<Tab,File>();
    
        MenuItem[] menuFields = {
                this.closeMenuItem,
                this.saveMenuItem,
                this.saveAsMenuItem,
                this.undoMenuItem,
                this.redoMenuItem,
                this.cutMenuItem,
                this.copyMenuItem,
                this.pasteMenuItem,
                this.selectAllMenuItem,
                this.toggleCommentsMenuItem,
                this.indentTextMenuItem,
                this.unindentTextMenuItem,
                this.entabTextMenuItem,
                this.detabTextMenuItem,
                this.findMenuItem
        };

        // List Property for case of no tabs
        SimpleListProperty<Tab> tablessListProperty =
                new SimpleListProperty<>(this.tabPane.getTabs());

        // Initialize all controllers
        fileMenuController = new FileMenuController(this.tabPane, this.primaryStage, this.tabFileMap);
        editMenuController = new EditMenuController(this.tabPane, this.findTextField);
        helpMenuController = new HelpMenuController();
        toolbarController = new ToolbarController(this.scanButton);

        this.tabFileMap = fileMenuController.tabFileMap;
        this.setupContextMenuController();

        // Start program with one new tab
        this.handleNewMenuItemAction();

        // bind menu items to tabless list
        for (MenuItem fxmlItem : menuFields) {
            if (fxmlItem != null) {
                fxmlItem.disableProperty().bind(tablessListProperty.emptyProperty());
            }
        }

        // Undo/Redo bindings
        {
            // Boolean Property for availability of undo and redo
            CodeArea curCodeArea = this.tabPane.getCurCodeArea();

            BooleanExpression undoableProperty =
                    BooleanExpression.booleanExpression(
                            curCodeArea.undoAvailableProperty());

            BooleanExpression redoableProperty =
                    BooleanExpression.booleanExpression(
                          curCodeArea.redoAvailableProperty());

            this.undoMenuItem.disableProperty().bind(undoableProperty.not());
            this.redoMenuItem.disableProperty().bind(redoableProperty.not());
        }
    }

    /**
     * Creates a reference to the ContextMenuController and passes in window items and other sub Controllers when necessary.
     */
    private void setupContextMenuController() {
        this.contextMenuController = new ContextMenuController();
        this.contextMenuController.setFileMenuController(this.fileMenuController);
        this.contextMenuController.setEditMenuController(this.editMenuController);
        this.fileMenuController.setContextMenuController(this.contextMenuController);
    }

    /**
     * Handles the About button action.
     * Creates a dialog window that displays the authors' names.
     */
    @FXML
    private void handleAboutMenuItemAction() {
        fileMenuController.handleAboutMenuItemAction();
    }

    /**
     * Handles the Help Menu Items.
     * Will open a URL with default browser.
     * Supports Windows, Linux and Mac OS
     */
    @FXML
    private void handleHelpMenuItemAction() { helpMenuController.handleHelpMenuItemAction(); }
    
    @FXML
    private void handleUrlMenuItemAction() { helpMenuController.handleUrlMenuItemAction(); }

    /**
     * Handles the New button action.
     * Opens a text area embedded in a new tab.
     * Sets the newly opened tab to the the topmost one.
     */
    @FXML
    private void handleNewMenuItemAction()
    {
        fileMenuController.handleNewMenuItemAction();
    }

    /**
     * Handles the open button action.
     * Opens a dialog in which the user can select a file to open.
     * If the user chooses a valid file, a new tab is created and the file
     * is loaded into the text area.
     * If the user cancels, the dialog disappears without doing anything.
     */
    @FXML
    private void handleOpenMenuItemAction()
    {
        fileMenuController.handleOpenMenuItemAction();
    }

    /**
     * Handles the close button action.
     * If the current text area has already been saved to a file, then
     * the current tab is closed.
     * If the current text area has been changed since it was last saved to a file,
     * a dialog appears asking whether you want to save the text before closing it.
     */
    @FXML
    private void handleCloseMenuItemAction(ActionEvent event)
    {
        fileMenuController.handleCloseMenuItemAction(event);
    }

    /**
     * Handles the Save As button action.
     * Shows a dialog in which the user is asked for the name of the file into
     * which the contents of the current text area are to be saved.
     * If the user enters any legal name for a file and presses the OK button
     * in the dialog,
     * then creates a new text file by that name and write to that file all the current
     * contents of the text area so that those contents can later be reloaded.
     * If the user presses the Cancel button in the dialog, then the dialog closes
     * and no saving occurs.
     */
    @FXML
    private void handleSaveAsMenuItemAction()
    {
        fileMenuController.handleSaveAsMenuItemAction();
    }

    /**
     * Handles the save button action.
     * If a text area was not loaded from a file nor ever saved to a file,
     * behaves the same as the save as button.
     * If the current text area was loaded from a file or previously saved
     * to a file, then the text area is saved to that file.
     */
    @FXML
    private void handleSaveMenuItemAction()
    {
        fileMenuController.handleSaveMenuItemAction();
    }

    /**
     * Handles the Exit button action.
     * Exits the program when the Exit button is clicked.
     */
    @FXML
    void handleExitMenuItemAction(Event e)
    {
        fileMenuController.handleExitMenuItemAction(e);
    }

    /**
     * Handles the Undo button action.
     * Undo the actions in the text area.
     */
    @FXML
    private void handleUndoMenuItemAction()
    {
        editMenuController.handleUndoMenuItemAction();
    }

    /**
     * Handles the Redo button action.
     * Redo the actions in the text area.
     */
    @FXML
    private void handleRedoMenuItemAction()
    {
        editMenuController.handleRedoMenuItemAction();
    }

    /**
     * Handles the Cut button action.
     * Cuts the selected text.
     */
    @FXML
    private void handleCutMenuItemAction()
    {
        editMenuController.handleCutMenuItemAction();
    }

    /**
     * Handles the Copy button action.
     * Copies the selected text.
     */ 
    @FXML
    private void handleCopyMenuItemAction()
    {
        editMenuController.handleCopyMenuItemAction();
    }

    /**
     * Handles the Paste button action.
     * Pastes the copied/cut text.
     */
    @FXML
    private void handlePasteMenuItemAction()
    {
        editMenuController.handlePasteMenuItemAction();
    }

    /**
     * Handles the SelectAll button action.
     * Selects all texts in the text area.
     */
    @FXML
    private void handleSelectAllMenuItemAction()
    {
        editMenuController.handleSelectAllMenuItemAction();
    }

    /**
     * Changes the theme of the IDE to Dark
     */
    @FXML
    public void handleDarkMode(){
        handleThemeChange("proj9DouglasHanssenMacDonaldZhang/css_styles/DarkMode.css", darkModeMenuItem);
    }

    /**
     * Changes the theme of the IDE back to normal
     */
    @FXML
    public void handleNormalMode(){
        vBox.getStylesheets().remove(vBox.getStylesheets().size()-1);
        enableUnselectedThemes(normalModeMenuItem);
    }

    /**
     * Changes the theme of the IDE to Fun Mode
     */
    @FXML
    public void handleFunMode(){
        handleThemeChange("proj9DouglasHanssenMacDonaldZhang/css_styles/FunMode.css", funModeMenuItem);
    }

    /**
     * Changes the theme of the IDE to HallowTheme--
     * a fun Halloween extra!
     */
    @FXML
    public void handleHallowThemeMode(){
        handleThemeChange("proj9DouglasHanssenMacDonaldZhang/css_styles/HallowTheme.css", hallowThemeItem);
    }

    /**
     * Helper method to change the theme
     * @param themeCSS
     */
    private void handleThemeChange(String themeCSS, MenuItem menuItem){
        if(vBox.getStylesheets().size() > 1){
            vBox.getStylesheets().remove(vBox.getStylesheets().size()-1);
        }
        vBox.getStylesheets().add(themeCSS);
        enableUnselectedThemes(menuItem);
    }

    /**
     * Enables the menu items of themes that aren't currently used and
     * disables the menu item of the theme that is currently on
     * display
     *
     * @param menItem the menu item that needs to be disabled
     */
    private void enableUnselectedThemes(MenuItem menItem){
        for(MenuItem item: prefMenu.getItems()){
            if(!item.equals(menItem)){
                item.setDisable(false);
            }
            else{
                item.setDisable(true);
            }
        }
    }

    @FXML
    private void handleToggleComments()
    {
        editMenuController.handleToggleComments();
    }

    @FXML
    private void handleIndentText()
    {
        editMenuController.handleIndentText();
    }

    @FXML
    private void handleUnindentText()
    {
        editMenuController.handleUnindentText();
    }

    @FXML
    private void handleEntab()
    {
        editMenuController.handleEntab();
    }

    @FXML
    private void handleDetab()
    {
        editMenuController.handleDetab();
    }

    @FXML
    private void handleFind() {
        editMenuController.handleFind();
    }

    @FXML
    private void handleScan() {
        // call scan from Scanner
        System.out.println("Scanning!");
    }

    /**
     * Reads in the application's main stage.
     * For use in Filechooser dialogs
     */
    public void setPrimaryStage(Stage primaryStage)
    {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void handleCheckWellFormedAction() {
        tabPane.handleCheckWellFormed();
    }

    /**
     * Promts the user to save when if the current tab has not been
     * saved since the last change. If the tab has never been saved,
     * will automatically open a FileChooser window to save the file.
     * Returns true if the user chooses to save or not to save (Yes
     * or No buttons), returns false if the user selects Cancel.
     */
    private boolean promptCompileSave() {
        // get selected tab and the code area
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        CodeArea activeCodeArea = tabPane.getCurCodeArea();

        if (this.fileMenuController.tabNeedsSaving(selectedTab)) {
            if (this.tabFileMap.get(selectedTab) == null) {
                // create a fileChooser and add file extension restrictions
                FileChooser fileChooser = new FileChooser();

                // file where the text content is to be saved
                File saveFile = fileChooser.showSaveDialog(this.primaryStage);
                if (saveFile != null) {
                    // save the content of the active text area to the selected file
                    this.fileMenuController.saveFile(activeCodeArea.getText(), saveFile);

                    // set the title of the tab to the name of the saved file
                    selectedTab.setText(saveFile.getName());

                    // map the tab and the associated file
                    this.tabFileMap.put(selectedTab, saveFile);

                }
                // else return if file is not saved
                else {
                    return false;
                }
            } else {
                // prompt user with save dialog
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
                if (result.get() == ButtonType.YES) {
                    this.fileMenuController.saveFile(activeCodeArea.getText(), this.tabFileMap.get(selectedTab));
                    return true;
                } else if (result.get() == ButtonType.CANCEL) {
                    return false;
                }
            }
        }
        return true;
    }
}
