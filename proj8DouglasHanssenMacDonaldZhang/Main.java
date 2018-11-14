/*
File: Main.java
CS361 Project 6
Names:  Kyle Douglas, Paige Hanssen, Wyett MacDonald, and Tia Zhang
Date: 10/27/18
*/

package proj8DouglasHanssenMacDonaldZhang;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;


/**
 * Creates a JavaFX application that contains an area for
 * 1) a TabPane,
 * which will contain  tabs at the user's discretion, the tabs themselves
 * being populated with CodeAreas inside of VirtualizedScrollPanes
 * and 2) a MenuBar with File and Edit menus,
 * which contain MenuItems that facilitate the modification of the Tabs,
 * or the textual contents therein.
 *
 * @author Liwei Jiang
 * @author Iris Lian
 * @author Tracy Quan
 * @author Evan Savillo
 * @author Chris Marcello
 * @author Yi Feng
 */
public class Main extends Application
{
    private static final int SCENE_WIDTH = 640;
    private static final int SCENE_HEIGHT = 480;
    private static final String STAGE_TITLE =
            "Kyle Douglas, Paige Hanssen, Wyett MacDonald, and Tia Zhang's Project 7";

    /**
     * Takes in a stage and loads the FXML, creates the controller,
     * and initializes a scene. 
     *
     * @param stage The stage that contains the window content
     */
    @Override
    public void start(Stage stage) throws Exception
    {
        // load the fxml file to create the stage and get the root
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("Main.fxml"));
        Parent root = loader.load();

        // set controller and stage
        Controller controller = loader.getController();
        controller.setPrimaryStage(stage);

        // eats the command to quit from the window itself
        // minor behavior issue currently but not a real bug
        // when quitting is prompts dialog twice on cancel after confirmed save
        stage.setOnCloseRequest(event -> {
            event.consume();
            controller.handleExitMenuItemAction(event);
        });

        // initialize a scene and add features specified in the css file to the scene
        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
        scene.getStylesheets().add("/proj8DouglasHanssenMacDonaldZhang/javaKeywords.css");

        // configure the stage
        stage.setTitle(STAGE_TITLE);

        stage.sizeToScene();
        stage.setScene(scene);
        stage.show();
    }

    /**
     * main function of Main class, launches application
     *
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        launch(args);
    }
}
