/**
 * File: Scanner.java
 * CS361 Project 9
 * Names:  Kyle Douglas, Paige Hanssen, Wyett MacDonald, and Tia Zhang
 * Date: 11/14/18
 */

package proj9DouglasHanssenMacDonaldZhang.bantam.lexer;

import proj9DouglasHanssenMacDonaldZhang.bantam.util.ErrorHandler;
import proj9DouglasHanssenMacDonaldZhang.bantam.lexer.Token.Kind;
import proj9DouglasHanssenMacDonaldZhang.bantam.util.*;
import proj9DouglasHanssenMacDonaldZhang.bantam.util.Error;
import java.io.*;

/**
 * Scanner that loops through a file and breaks it down into tokens,
 * classifying each token according to the rules of Bantam Java.
 *
 * @author Kyle Douglas, Paige Hanssen, Wyett MacDonald, Tia Zhang
 */
public class Scanner
{
    private SourceFile sourceFile;
    private ErrorHandler errorHandler;
    private String token;
    private char currentChar;
    private boolean tokenDone;
    private Kind type;
    private boolean isNotLetters;
    private boolean multilineCommentOpen;
    private boolean singlelineCommentOpen;
    private boolean stringOpen;
    private String lostChar;
    private int lineNum;

    /**
     * Constructor method fro Scanner that takes in just an ErrorHandler
     * @param handler the handler for the errors
     */
    public Scanner(ErrorHandler handler) {
        sourceFile = null;
        errorHandler = handler;
        this.scannerInit();
    }

    /**
     * Constructor for Scanner that takes a filename and an ErrorHandler
     * @param filename the name of the file
     * @param handler the handle for errors
     */
    public Scanner(String filename, ErrorHandler handler) {
        sourceFile = new SourceFile(filename);
        errorHandler = handler;
        this.scannerInit();
    }

    /**
     * Constructor for the Scanner class, takes in reader and error handler.
     * @param reader a reader to read the file
     * @param handler the handler for errors
     */
    public Scanner(Reader reader, ErrorHandler handler) {
        errorHandler = handler;
        sourceFile = new SourceFile(reader);
        this.scannerInit();
    }

    /**
     * Called in each constructor method. This removes a lot of duplicate code.
     */
    private void scannerInit() {
        currentChar = ' ';
        token = "";
        tokenDone = false;
        type = null;
        isNotLetters = true; //Used to help figure out if the token is ints
        stringOpen = false;
        multilineCommentOpen = false;
        lostChar = null; //Needed solely to avoid losing an char to division
    }

    /**
     * Access the last retrieved token
     */
    private char getLastTokenChar(){
        return token.charAt(token.length()-1);
    }

    /**
     * Create a new token
     */
    private Token makeNewToken(){
        // create new token and reset fields
        Token newToken = new Token(type, token, lineNum);
        type = null;
        isNotLetters = true;
        tokenDone = false;

        // check for lost character
        if(lostChar != null){
            token = lostChar;
            lostChar = null;
        } else {
            token = "";
        }
        
        return newToken;
    }

    /**
     * Call the register method in ErrorHandler to store the found error
     * @param error
     */
    private void notifyErrorHandler(Error error){
        this.errorHandler.register(error.getKind(), error.getFilename(),
                                   error.getLineNum(), error.getMessage());;
    }

    /**
     * Primary method in this file! Loops through all the characters
     * in the Source File and creates tokens as appropriate, based
     * on Bantam Java sytax.
     */
    public Token scan() {
        //If there's already a finished token caught on the last round but a previous token had to be handled
        if(tokenDone) {
            return finishToken();
        }
        else {
            try {
                while ((currentChar = sourceFile.getNextChar()) != '\u0000') {
                    // check that we are not in a string or comment
                    if ((!stringOpen) && (!multilineCommentOpen) && (!singlelineCommentOpen)) {
                        // check for digit
                        if (Character.isDigit(currentChar)) {
                            Token completeToken = handleDigit();
                            if (completeToken != null) return completeToken;
                        }
                        // check for letter
                        else if (Character.isLetter(currentChar)) {
                            Token completeToken = handleLetter();
                            if (completeToken != null) return completeToken;
                        }
                        else {
                            Token completeToken = null;
                            // begin of switch statement to find what kind of char it is
                            switch (currentChar) {
                                case '{':   // if char is an opening brace 
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        completeToken = finishToken();
                                    }
                                    token = "{";
                                    type = Kind.LCURLY;
                                    tokenDone = true;
                                    lineNum = sourceFile.getCurrentLineNumber();
                                    if(completeToken!= null) return completeToken;

                                    break;

                                case '}':   // if char is a closing brace
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        completeToken = finishToken();
                                    }
                                    token = "}";
                                    type = Kind.RCURLY;
                                    tokenDone = true;
                                    lineNum = sourceFile.getCurrentLineNumber();
                                    if(completeToken!= null) return completeToken;

                                    break;

                                case '(':   // if opening parenthesis
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        completeToken  = finishToken();
                                    }
                                    token = "(";
                                    type = Kind.LPAREN;
                                    tokenDone = true;
                                    lineNum = sourceFile.getCurrentLineNumber();
                                    if(completeToken!= null) return completeToken;

                                    break;

                                case ')':   // if closing parenthesis
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        completeToken = finishToken();
                                    }
                                    token = ")";
                                    type = Kind.RPAREN;
                                    tokenDone = true;
                                    lineNum = sourceFile.getCurrentLineNumber();
                                    if(completeToken!= null) return completeToken;

                                    break;

                                case '[':   // if opening bracket
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        completeToken = finishToken();
                                    }
                                    token += "[";
                                    type = Kind.LBRACKET;
                                    tokenDone = true;
                                    lineNum = sourceFile.getCurrentLineNumber();
                                    if(completeToken!= null) return completeToken;

                                    break;

                                case ']':   // if closing bracket
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        completeToken = finishToken();
                                    }
                                    token += "]";
                                    type = Kind.RBRACKET;
                                    tokenDone = true;
                                    lineNum = sourceFile.getCurrentLineNumber();
                                    if(completeToken!= null) return completeToken;

                                    break;

                                case ':':   // if colon
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        completeToken = finishToken();
                                    }
                                    token += ":";
                                    type = Kind.COLON;
                                    tokenDone = true;
                                    lineNum = sourceFile.getCurrentLineNumber();
                                    if(completeToken!= null) return completeToken;

                                    break;

                                case ';':   // if semicolon
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        completeToken = finishToken();
                                    }
                                    token += ";";
                                    type = Kind.SEMICOLON;
                                    tokenDone = true;
                                    lineNum = sourceFile.getCurrentLineNumber();
                                    if(completeToken!= null) return completeToken;

                                    break;

                                case ',':   // if comma
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        completeToken = finishToken();
                                    }
                                    token = ",";
                                    type = Kind.COMMA;
                                    tokenDone = true;
                                    lineNum = sourceFile.getCurrentLineNumber();
                                    if(completeToken!= null) return completeToken;

                                    break;

                                case '_':   //If underscore
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        completeToken = finishToken();
                                    }
                                    token += "_";
                                    type = Kind.IDENTIFIER;
                                    lineNum = sourceFile.getCurrentLineNumber();
                                    if(completeToken!= null) return completeToken;

                                    break;

                                case '!':   // if exclamation mark
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        completeToken = finishToken();
                                    }
                                    token = "!";
                                    type = Kind.UNARYNOT;
                                    if(completeToken!= null) return completeToken;

                                    break;

                                case '=':   // if equals sign
                                    if (token.length() > 0) {
                                        // check for comparison usage
                                        if ((getLastTokenChar() != '!') && (getLastTokenChar() != '=')) {
                                            lineNum = sourceFile.getCurrentLineNumber();
                                            completeToken = finishToken();
                                            token = "=";
                                            type = Kind.ASSIGN;
                                        } else {
                                            token += "=";
                                            type = Kind.COMPARE;
                                            tokenDone = true;
                                            lineNum = sourceFile.getCurrentLineNumber();
                                        }
                                    } else {
                                        token += "=";
                                        type = Kind.ASSIGN;
                                    }
                                    if(completeToken!= null) return completeToken;

                                    break;

                                case '+':   // if plus sign
                                    if (token.length() > 0) {
                                        // check for incrementing vs. addition
                                        if (getLastTokenChar() == '+') {
                                            type = Kind.UNARYINCR;
                                            token = "++";
                                            lineNum = sourceFile.getCurrentLineNumber();
                                            tokenDone = true;
                                        } else {
                                            lineNum = sourceFile.getCurrentLineNumber();
                                            completeToken = finishToken();
                                            token = "+";
                                            type = Kind.PLUSMINUS;
                                            return completeToken;
                                        }
                                    } else {
                                        token += "+";
                                        type = Kind.PLUSMINUS;
                                    }

                                    break;

                                case '-':   // if minus sign
                                    if (token.length() > 0) {
                                        // check for decrement vs. subtract
                                        if (getLastTokenChar() == '-') {
                                            type = Kind.UNARYDECR;
                                            token = "--";
                                            lineNum = sourceFile.getCurrentLineNumber();
                                            tokenDone = true;
                                        } else {
                                            lineNum = sourceFile.getCurrentLineNumber();
                                            completeToken = finishToken();
                                            token = "-";
                                            type = Kind.PLUSMINUS;
                                            return completeToken;
                                        }
                                    } else {
                                        token += "-";
                                        type = Kind.PLUSMINUS;
                                    }

                                    break;

                                case '/':   // if forward slash
                                    if (token.length() > 0) {
                                        completeToken = finishToken();
                                    }
                                    token += "/";
                                    handleForwardSlash();
                                    if(completeToken!= null) return completeToken;

                                    break;

                                case '\"':  // if double quotation mark
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        completeToken = finishToken();
                                    }
                                    token = "\"";
                                    stringOpen = true; // Closing open strings is handled further on
                                    type = Kind.STRCONST;
                                    if(completeToken!= null) return completeToken;

                                    break;

                                case '.':   // if period
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        completeToken = finishToken();
                                    }
                                    token = ".";
                                    type = Kind.DOT;
                                    tokenDone = true;
                                    lineNum = sourceFile.getCurrentLineNumber();
                                    if(completeToken!= null) return completeToken;

                                    break;

                                case '*':   // if asterisk
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        completeToken = finishToken();
                                    }
                                    token = "*";
                                    type = Kind.MULDIV;
                                    tokenDone = true;
                                    lineNum = sourceFile.getCurrentLineNumber();
                                    if(completeToken!= null) return completeToken;

                                    break;

                                case '<':   // if less than
                                    //<= is not legal, so don't need to account for that
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        completeToken = finishToken();
                                    }
                                    token = "<";
                                    type = Kind.COMPARE;
                                    tokenDone = true;
                                    lineNum = sourceFile.getCurrentLineNumber();
                                    if(completeToken!= null) return completeToken;

                                    break;

                                case '>':   // if greater than
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        completeToken = finishToken();
                                    }
                                    token = ">";
                                    type = Kind.COMPARE;
                                    tokenDone = true;
                                    lineNum = sourceFile.getCurrentLineNumber();
                                    if(completeToken!= null) return completeToken;
                                    
                                    break;

                                case '%':   // if modulus
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        completeToken = finishToken();
                                    }
                                    token = "%";
                                    type = Kind.BINARYLOGIC;
                                    tokenDone = true;
                                    lineNum = sourceFile.getCurrentLineNumber();
                                    if(completeToken!= null) return completeToken;

                                    break;

                                case ' ':   // Empty space means any current token is over
                                    if (token.length() > 0) {
                                        tokenDone = true;
                                        lineNum = sourceFile.getCurrentLineNumber();
                                    }
                                    break;

                                case '\n':  // if new line character
                                    if (token.length() > 0) {
                                        tokenDone = true;
                                        lineNum = sourceFile.getCurrentLineNumber()-1;
                                    }

                                    break;

                                case '\t':  // if tab character
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        tokenDone = true;
                                    }

                                    break;

                                case '$': // if dollar sign - only can be used inside identifiers
                                    token = "$";
                                    type = Kind.IDENTIFIER;

                                default:    // anything else is not a legal character
                                    type = Kind.ERROR;
                                    token += Character.toString(currentChar);
            
                                    Error error = new Error(Error.Kind.LEX_ERROR, sourceFile.getFilename(), sourceFile.getCurrentLineNumber(), "Illegal character(s)");
                                    this.notifyErrorHandler(error);

                                    tokenDone = true;
                                    lineNum = sourceFile.getCurrentLineNumber();
                            }
                            // Close switch statement for char

                            if (tokenDone) {
                                return finishToken();
                            }
                        }
                    }
                    // Anything in a comment or string
                    else {
                        if (stringOpen) {
                            token += Character.toString(currentChar);
                            handleStringProcessing();
                        }
                        else if (multilineCommentOpen) {
                            token += Character.toString(currentChar);
                            handleCommentProcessing();
                        }
                        else if(singlelineCommentOpen) {
                            if(currentChar == '\n'){ // Don't add the new line char to a single line comment
                                singlelineCommentOpen = false;
                                lineNum = sourceFile.getCurrentLineNumber()-1;
                                tokenDone = true;
                            }
                            else{
                                token += Character.toString(currentChar);
                            }
                        }

                        // Make token for string or comment
                        if (tokenDone) {
                            Token newToken = makeNewToken();
                            return newToken;
                        }
                    }
                }
                // End while loop

                if ((multilineCommentOpen) || (singlelineCommentOpen) || (stringOpen)) {
                    Error error = new Error(Error.Kind.PARSE_ERROR, sourceFile.getFilename(), sourceFile.getCurrentLineNumber(), "Found end of file before program was properly closed.");
                    this.notifyErrorHandler(error);
                    type = Kind.ERROR; //Once the SourceFile only sends the end of file char, then only this section should be triggered
                    Token errToken = makeNewToken();
                    stringOpen = false;
                    multilineCommentOpen = false;
                    singlelineCommentOpen = false;
                    return errToken;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        type = Kind.EOF; //Once the SourceFile only sends the end of file char, then only this section should be triggered
        Token eofToken = makeNewToken();
        return eofToken;
    }

    /**
     * 
     * @return
     */
    private Token handleLetter(){
        Token completeToken = null;
        if (token.length() == 1) {
            if ((token.charAt(0) == '+') | (token.charAt(0) == '-') | (token.charAt(0) == '=') | (token.charAt(0) == '!')) {
                lineNum = sourceFile.getCurrentLineNumber();
                completeToken = makeNewToken();
            }
        }
        token += Character.toString(currentChar);
        if (isNotLetters) {
            isNotLetters = false;
        }
        return completeToken;
    }

    /**
     * Handler for when a digit is found.
     */
    private Token handleDigit(){
        Token completeToken = null;
        // Manual check to see if this terminated a +,-, =. ++, -- etc should've already been terminated
        // the types should've already been set by the case statements, so they don't have to be set
        if (token.length() == 1) {
            if ((token.charAt(0) == '+') | (token.charAt(0) == '-') | (token.charAt(0) == '=') ) {
                lineNum = sourceFile.getCurrentLineNumber();
                completeToken = makeNewToken();

            }
        }
        //The token can't get the current char added to it until the previous one has been processed
        token += Character.toString(currentChar);
        return completeToken;
    }

    /**
     * Handler for forward slash. Could be open single line comment,
     * multiline comment, or division symbol.
     */
    private void handleForwardSlash(){
            char nextNextChar = sourceFile.getNextChar();
            // If single line comment
            if(nextNextChar == '/'){
                type = Kind.COMMENT;
                token += nextNextChar;
                singlelineCommentOpen = true;
            }
            // If multiline comment
            else if (nextNextChar == '*'){
                token += "*";
                type = Kind.COMMENT;
                multilineCommentOpen = true;
            }
            // Division
            else {
                type = Kind.MULDIV;
                tokenDone = true;
                if(!Character.isWhitespace(nextNextChar)){
                    lostChar = Character.toString(nextNextChar);
                }
            }
    }

    /**
     * Handler for processing strings when they are open
     */
    private void handleStringProcessing() {
        // Found escaped char - get the next char & check for illegal special chars
        if (currentChar == '\\') {
            char nextNextChar = sourceFile.getNextChar();
            token += nextNextChar;

            // If any illegal character, make error
            if( (nextNextChar != 't') && (nextNextChar!= 'n') && (nextNextChar != '\\') &&
                    (nextNextChar !='\"') && (nextNextChar != 'f')){
                type = Kind.ERROR;
                Error error = new Error(Error.Kind.LEX_ERROR, sourceFile.getFilename(), sourceFile.getCurrentLineNumber(), "Illegal character(s)");
                this.notifyErrorHandler(error);
            }
        }
        // Found string closed
        else if (currentChar == '\"') {
            stringOpen = false;
            tokenDone = true;
            lineNum = sourceFile.getCurrentLineNumber();
            // Check if string is out of range, generate error if so
            if (token.length() > 5000) {
                type = Kind.ERROR;
                Error error = new Error(Error.Kind.LEX_ERROR, sourceFile.getFilename(), sourceFile.getCurrentLineNumber(), "String exceeds 5000 characters");
                this.notifyErrorHandler(error);
            }
        }
        // Found string carrying onto two lines - generate error
        else if (currentChar == '\n') {
            type = Kind.ERROR;
            Error error = new Error(Error.Kind.LEX_ERROR, sourceFile.getFilename(), sourceFile.getCurrentLineNumber(), "String not properly closed");
            this.notifyErrorHandler(error);
            }
        }

    /**
     * Handler for processing closing comments. Looks for an
     * asterisk and if found, checks for a /, which is the mark
     * of a closed comment.
     */
    private void handleCommentProcessing() {
        if (currentChar == '*') {
            char nextNextChar = sourceFile.getNextChar();
            token += nextNextChar;

            if (nextNextChar == '/') {
                multilineCommentOpen = false;
                tokenDone = true;
                lineNum = sourceFile.getCurrentLineNumber();
            }
        }
    }

    /**
     * Complete the token and officially create it
     */
    private Token finishToken(){
        if (type == null) { // Token terminated by whitespace
            if (isNotLetters) {
                type = Kind.INTCONST;
                int num = Integer.parseInt(token);
                int intRoof = (int) Math.pow(2, 31);

                // if the integer is not within the appropriate range, create error
                if( (0 > num) | (num > intRoof) ){
                    type = Kind.ERROR;
                    Error error = new Error(Error.Kind.LEX_ERROR, sourceFile.getFilename(), sourceFile.getCurrentLineNumber(), "Integer out of range.");
                    this.notifyErrorHandler(error);
                }
            }
            else {
                type = Kind.IDENTIFIER;
            }
        }

        // Catch both identifiers from if type == null and anything caught with _ or $
        if (type == Kind.IDENTIFIER) {
            //if the first letter of the identifier isn't a letter, illegal identifier token
            if (!Character.isLetter(token.charAt(0))) { 
                type = Kind.ERROR;
                Error error = new Error(Error.Kind.LEX_ERROR, sourceFile.getFilename(), sourceFile.getCurrentLineNumber(), "Not a legal identifier.");
                this.notifyErrorHandler(error);
                
            }
        }

        //make token into a Token
        Token newToken = makeNewToken();
        return newToken;
    }

    public static void main(String[] args) {
        if(args.length > 0) {
            for(int i = 0; i < args.length; i++) {
                System.out.println("Printing tokens for file :" + args[i]);
                String tokenString = "";
                String message = "";
                ErrorHandler errorHandler = new ErrorHandler();
                Scanner scanner = new Scanner(args[i], errorHandler);
                Token token;
                while ( ( token= scanner.scan()).kind != Token.Kind.EOF) {
                    tokenString += token.toString() + "\n";
                }
                if(token.kind == Token.Kind.EOF) {
                    tokenString += token.toString() + "\n";
                }
                if(errorHandler.errorsFound()) {
                    message = "There are " + errorHandler.getErrorList().size() + " errors.\n";
                }
                else {
                    message = "Scanning successful.\n";
                }
                System.out.println(tokenString);
                System.out.println(message);
            }
        }



    }
}
