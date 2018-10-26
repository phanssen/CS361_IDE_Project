/*
File: CodeAreaTabPane.java
CS361 Project 6
Names:  Kyle Douglas, Paige Hanssen, Wyett MacDonald, and Tia Zhang
Date: 10/27/18
*/

package proj6DouglasHanssenMacDonaldZhang;

import org.fxmisc.richtext.CodeArea;
import org.reactfx.Subscription;
import java.time.Duration;

/**
 * This class handles the creation of CodeAreas, as well as keyword
 * highlighting and syntax recognition.
 * It contains the creation method createCodeArea and a helper method
 * computeHighlighting which handles the highlighting of the codeArea.
 * It also contains the java keywords and patterns to be colored in the CodeArea.
 *
 *  @author Yi Feng
 *  @author Iris Lian
 *  @author Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou
 */
public class JavaCodeArea extends CodeArea {

    public JavaCodeArea(){
        super();
        this.subscribe();
    }

    /* 
     * Method obtained from the RichTextFX Keywords Demo. Method allows
     * for syntax highlighting after a delay of 500ms after typing has ended.
     * This method was copied from JavaKeyWordsDemo
     * Original Author: Jordan Martinez
    */
    private void subscribe() {
        // recompute the syntax highlighting 500 ms after user stops editing area
        Subscription codeCheck = this
        // plain changes = ignore style changes that are emitted when syntax highlighting is reapplied
        // multi plain changes = save computation by not rerunning the code multiple times
        //   when making multiple changes (e.g. renaming a method at multiple parts in file)
        .multiPlainChanges()

        // do not emit an event until 500 ms have passed since the last emission of previous stream
        .successionEnds(Duration.ofMillis(500))

        // run the following code block when previous stream emits an event
        .subscribe(ignore -> this.setStyleSpans(0, JavaStyle.computeHighlighting(this.getText())));
    }
}