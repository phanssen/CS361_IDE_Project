/*
File: CodeAreaTabPane.java
CS361 Project 9
Names: Liwei Jiang, Chris Marcello, Tracy Quan, Wyett MacDonald, Paige Hanssen, Tia Zhang, Kyle Douglas
Date: 11/20/2018
*/

package proj10DouglasHanssenMacDonaldZhang;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.richtext.LineNumberFactory;
import org.reactfx.Subscription;
import java.time.Duration;
import java.util.regex.Pattern;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

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
        this.setParagraphGraphicFactory(LineNumberFactory.get(this));
        this.addTextListener();
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
        .subscribe(ignore -> this.setStyleSpans(0, computeHighlighting(this.getText())));
    }

    // a list of strings that contain the keywords for the IDE to identify.
    private static final String[] KEYWORDS = new String[]{
        "abstract", "assert", "boolean", "break", "byte",
        "case", "catch", "char", "class", "const",
        "continue", "default", "do", "double", "else",
        "enum", "extends", "final", "finally", "float",
        "for", "goto", "if", "implements", "import",
        "instanceof", "int", "interface", "long", "native",
        "new", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super",
        "switch", "synchronized", "this", "throw", "throws",
        "transient", "try", "void", "volatile", "while", "var"
    };

    // the regex rules for the ide
    private static final String IDENTIFIER_PATTERN = "[a-zA-Z]+[a-zA-Z0-9_]*";
    private static final String FLOAT_PATTERN = "(\\d+\\.\\d+)";
    private static final String INTCONST_PATTERN = "(?<![\\w])(?<![\\d.])[0-9]+(?![\\d.])(?![\\w])";
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<FLOAT>" + FLOAT_PATTERN + ")"
                    + "|(?<INTCONST>" + INTCONST_PATTERN + ")"
                    + "|(?<IDENTIFIER>" + IDENTIFIER_PATTERN + ")"

    );

    /**
     * Method to highlight all of the regex rules and keywords.
     * Code obtained from the RichTextFX Demo from GitHub.
     *
     * @param text a string analyzed for proper syntax highlighting
     */
    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass = matcher.group("KEYWORD") != null ? "keyword" :
                    matcher.group("PAREN") != null ? "paren" :
                            matcher.group("BRACE") != null ? "brace" :
                                    matcher.group("BRACKET") != null ? "bracket" :
                                            matcher.group("SEMICOLON") != null ? "semicolon" :
                                                    matcher.group("STRING") != null ? "string" :
                                                            matcher.group("COMMENT") != null ? "comment" :
                                                                    matcher.group("IDENTIFIER") != null ? "identifier" :
                                                                            matcher.group("INTCONST") != null ? "intconst" :
                                                                                    null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    /**
     * A listener that looks for when the user has typed either ( or {
     * in the current code area, and automatically adds a closing ) or }
     * to match the opening one.
     */
    private void addTextListener() {
        this.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.length() != 0) {
                    String lastChar = Character.toString(newValue.charAt(newValue.length() - 1));
                    if (lastChar.equalsIgnoreCase("(")) {
                        appendText(")");
                    }
                    else if(lastChar.equalsIgnoreCase("{")) {
                        appendText("\n}");
                    }
                }
            }
        });
    }
}