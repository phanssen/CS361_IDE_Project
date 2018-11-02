/*
 * File: ContextMenuController.java
 * F18 CS361 Project 6
 * Names: Liwei Jiang, Chris Marcello, Tracy Quan
 * Date: 10/23/2018
 * This file contains the ContextMenuController class, handling context menu related actions.
 */

package proj7DouglasHanssenMacDonaldZhang;

import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseButton;
import javafx.scene.Node;
import javafx.scene.control.SeparatorMenuItem;
import org.fxmisc.richtext.StyleClassedTextArea;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleListProperty;

/**
 * ContextMenu Controller, handling context menu related actions.
 */
public class ContextMenuController {
    /**
     * CompilationController for handling toolbar actions
     */
    private CompilationController compilationController;
    /**
     * FileMenuController handling File menu actions
     */
    private FileMenuController fileMenuController;
    /**
     * EditMenuController handling Edit menu actions
     */
    private EditMenuController editMenuController;

    /**
     * Sets the CompilationController.
     *
     * @param compilationController CompilationController initialized in main Controller.
     */
    public void setCompilationController(CompilationController compilationController) { this.compilationController = compilationController; }

    /**
     * Sets file menu controller.
     *
     * @param fileMenuController FileMenuController initialized in main Controller.
     */
    public void setFileMenuController(FileMenuController fileMenuController) { this.fileMenuController = fileMenuController; }

    /**
     * Sets the edit menu controller.
     *
     * @param editMenuController EditMenuController initialized in main Controller.
     */
    public void setEditMenuController(EditMenuController editMenuController) { this.editMenuController = editMenuController; }

    /**
     * Helper method to set on shown action.
     * Will be called when a context menu node shows up.
     * By clicking on the left mouse button, the context menu will disappear.
     * By clicking on the right mouse button, the context menu moves to the clicked position.
     */
    private void setOnShown(ContextMenu rightClickMenu, final Node node) {
        rightClickMenu.setOnShown(onShowEvent -> {
            node.setOnMouseReleased(e -> {
                // clicking on the left button hides the context menu
                if (e.getButton() == MouseButton.PRIMARY) {
                    rightClickMenu.hide();
                }
                // clicking on the right button moves the context menu to the clicked position
                else if (e.getButton() == MouseButton.SECONDARY) {
                    rightClickMenu.show(node, e.getScreenX(), e.getScreenY());
                }
            });
        });
    }


    /**
     * Handles the right click context menu action for a Tab.
     * Pops up a context menu when right-clicking on the specified Tab.
     * The context menu contains Save, SaveAs, New, Open, Close items.
     * After the context menu pops up, left mouse click will make the context menu disappear;
     * right mouse click will move the context menu to the location of the mouse click.
     *
     * @param tab Tab being clicked on
     */
    public void setupTabContextMenuHandler(Tab tab) {
        ContextMenu rightClickMenu = new ContextMenu();
        rightClickMenu.getStyleClass().add("contextMenu");

        MenuItem SaveItem = new MenuItem("Save");
        SaveItem.setOnAction(e -> this.fileMenuController.handleSaveMenuItemAction());

        MenuItem SaveAsItem = new MenuItem("SaveAs");
        SaveAsItem.setOnAction(e -> this.fileMenuController.handleSaveAsMenuItemAction());

        MenuItem NewItem = new MenuItem("New");
        NewItem.setOnAction(e -> this.fileMenuController.handleNewMenuItemAction());

        MenuItem OpenItem = new MenuItem("Open");
        OpenItem.setOnAction(e -> this.fileMenuController.handleOpenMenuItemAction());

        MenuItem CloseItem = new MenuItem("Close");
        CloseItem.setOnAction(e -> this.fileMenuController.handleCloseMenuItemAction(e));

        rightClickMenu.getItems().addAll(SaveItem, SaveAsItem, new SeparatorMenuItem(),
                NewItem, OpenItem, new SeparatorMenuItem(), CloseItem);
        tab.setContextMenu(rightClickMenu);
    }


    /**
     *
     * Handles the right click context menu action for a JavaCodeArea.
     * Pops up a context menu when right-clicking on the specified JavaCodeArea.
     * The context menu contains Undo, Redo, Cut, Copy, Paste, SelectAll,
     * Toggle Comment, Toggle Block Comment, Indent, Unindent items.
     * After the context menu pops up, left mouse click will make the context menu disappear;
     * right mouse click will move the context menu to the location of the mouse click.
     *
     * @param javaCodeArea JavaCodeArea being clicked on
     */
    public void setupJavaCodeAreaContextMenuHandler(JavaCodeArea javaCodeArea) {
        ContextMenu rightClickMenu = new ContextMenu();

        MenuItem UndoItem = new MenuItem("Undo");
        UndoItem.setOnAction(e -> javaCodeArea.undo());

        MenuItem RedoItem = new MenuItem("Redo");
        RedoItem.setOnAction(e -> javaCodeArea.redo());

        MenuItem CutItem = new MenuItem("Cut");
        CutItem.setOnAction(e -> javaCodeArea.cut());

        MenuItem CopyItem = new MenuItem("Copy");
        CopyItem.setOnAction(e -> javaCodeArea.copy());

        MenuItem PasteItem = new MenuItem("Paste");
        PasteItem.setOnAction(e -> javaCodeArea.paste());

        MenuItem SelectAllItem = new MenuItem("SelectAll");
        SelectAllItem.setOnAction(e -> javaCodeArea.selectAll());

        MenuItem ToggleCommentItem = new MenuItem("Toggle Comment");
        ToggleCommentItem.setOnAction(e -> this.editMenuController.handleToggleComments());

        // MenuItem ToggleBlockCommentItem = new MenuItem("Toggle Block Comment");
        // ToggleBlockCommentItem.setOnAction(e -> this.editMenuController.handleToggleBlockCommentAction());

        MenuItem indentItem = new MenuItem("Indent");
        indentItem.setOnAction(e -> this.editMenuController.handleIndentText());

        MenuItem unindentItem = new MenuItem("Unindent");
        unindentItem.setOnAction(e -> this.editMenuController.handleUnindentText());

        rightClickMenu.getItems().addAll(UndoItem, RedoItem, CutItem, CopyItem, PasteItem, SelectAllItem,
                new SeparatorMenuItem(), ToggleCommentItem, //ToggleBlockCommentItem,
                new SeparatorMenuItem(), indentItem, unindentItem);

        javaCodeArea.setOnContextMenuRequested(event -> {
            rightClickMenu.show(javaCodeArea, event.getScreenX(), event.getSceneY());
        });
        this.setOnShown(rightClickMenu, javaCodeArea);
    }


    /**
     *
     * Handles the right click context menu action for a StyleClassedTextArea console.
     * Pops up a context menu when right-clicking on the specified StyleClassedTextArea console.
     * The context menu contains ClearAll, Stop Program items.
     * After the context menu pops up, left mouse click will make the context menu disappear;
     * right mouse click will move the context menu to the location of the mouse click.
     *
     * @param console StyleClassedTextArea console being clicked on
     */
    public void setupConsoleContextMenuHandler(StyleClassedTextArea console,
                                               ReadOnlyBooleanProperty isAnythingRunning) {
        ContextMenu rightClickMenu = new ContextMenu();

        MenuItem StopItem = new MenuItem("Stop Program");
        StopItem.setOnAction(e -> this.compilationController.handleHaltAction());

        // set binding for stopItem
        StopItem.disableProperty().bind((isAnythingRunning.not()));

        rightClickMenu.getItems().addAll(StopItem);

        console.setOnContextMenuRequested(event -> {
            rightClickMenu.show(console, event.getScreenX(), event.getSceneY());
        });
        this.setOnShown(rightClickMenu, console);
    }
}
