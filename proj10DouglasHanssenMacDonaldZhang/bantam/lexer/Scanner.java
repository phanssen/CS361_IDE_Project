
/*
 * File: Scanner.java
 * F18 CS361 Project 10
 * Modified By: Kyle Douglas, Paige Hanssen, Wyett MacDonald, Tia Zhang
 * Originally By: Liwei Jiang, Tracy Quan, Danqing Zhao, Chris Marcello, Michael Coyne
 * Date: 12/6/2018
 * This file contains the Scanner class, which takes in a file and
 * splits it into proper tokens, reporting bugs when necessary.
 * We borrowed this file from the "originally by" group because we couldn't fix up our scanner in time
 */


package proj10DouglasHanssenMacDonaldZhang.bantam.lexer;

import proj10DouglasHanssenMacDonaldZhang.bantam.lexer.Token.Kind;
import proj10DouglasHanssenMacDonaldZhang.bantam.util.*;
import proj10DouglasHanssenMacDonaldZhang.bantam.util.Error;
import java.io.Reader;
import java.util.List;

/**
 * The Scanner class takes in a file and splits it into proper tokens,
 * reporting bugs when necessary.
 *
 * @author liweijiang
 * @author Tracy Quan
 * @author Danqing Zhao
 * @author Chris Marcello
 * @author Michael Coyne
 * @author Tia Zhang
 */
public class Scanner {

     //The SourceFile object from which tokens are created
    private SourceFile sourceFile;

    //the ErrorHandler object which will keep track of any errors that arise during scanning
    private ErrorHandler errorHandler;

     //the currentChar from the sourceFile that's being processed into part of a token
    private char currentChar;

    //The next two fields are stored as Objects and not primitives so I can store a null as a default if not used.

    //Stores the next char after a token is done to avoid it being lost
    private Character nextChar = null;

    //Used to store the original line position for multiline tokens
    private Integer startPosition = null;




    /**
     * A constructor of the Scanner class.
     *
     * @param handler an ErrorHandler object that will be used to register any errors from scanning
     */
    public Scanner(ErrorHandler handler) {
        this.errorHandler = handler;
        this.currentChar = ' ';
        this.sourceFile = null;
    }

    /**
     * A constructor of the Scanner class.
     *
     * @param filename a filename as a String
     * @param handler an ErrorHandler object that will be used to register any errors from scanning
     */
    public Scanner(String filename, ErrorHandler handler) {
        this.errorHandler = handler;
        this.sourceFile = new SourceFile(filename);
        this.currentChar = ' ';
    }

    /**
     *Changes the source file to be tokenized to be the source file the user gives
     *If no file is passed into the constructed, a source file must be set with this method
     * @param sourceFile is the source file which will become the source for tokenization
     */
    public void setSourceFile(SourceFile sourceFile){
        this.sourceFile = sourceFile;
    }

    /**
     * A constructor of the Scanner class.
     *
     * @param reader a reader input stream
     * @param handler an ErrorHandler object
     */
    public Scanner(Reader reader, ErrorHandler handler) {
        this.errorHandler = handler;
        this.sourceFile = new SourceFile(reader);
    }

    /**
     * Helper method to register an error to the error handler.
     *
     * @param kind the kind of the error
     * @param message the error message
     */
    private void registerError(Error.Kind kind, String message) {
        int position;
        if (startPosition != null) {
            position = startPosition;
        }
        else {
            position = this.sourceFile.getCurrentLineNumber();
        }
        if (this.currentChar == SourceFile.eol) {
            position--;
        }
        this.errorHandler.register(kind, this.sourceFile.getFilename(), position, message);
    }

    /**
     * Helper method to create a new Token.
     * Gets the next character as specified.
     *
     * @param kind the Kind of the Token
     * @param spelling the spelling of the Token as a String
     * @return the new Token object created
     */
    private Token createNewToken(Token.Kind kind, String spelling) {
        int position;
        if (startPosition != null) {
            position = startPosition;
            startPosition = null;
        }
        else {
            position = this.sourceFile.getCurrentLineNumber();
       }
        // if the current character is eol, then record the line number decremented by 1
        if (this.currentChar == SourceFile.eol) {
            position--;
        }
        Token newToken = new Token(kind, spelling, position);

        if(nextChar == null){
            currentChar = sourceFile.getNextChar();
        }
        else{
            currentChar = nextChar;
            nextChar = null;
        }
        return newToken;
    }

    /**
     * Helper method to create an ERROR Token and register this error to the error handler.
     *
     * @param message error message
     * @param spelling the spelling of the error token
     * @return the ERROR Token constructed
     */
    private Token createAndRegisterErrorToken(String message, String spelling) {
        this.registerError(Error.Kind.LEX_ERROR, message);
        return this.createNewToken(Kind.ERROR, message + ": " + spelling);
    }

    /**
     * Helper method to construct a Token for Special Characters that has another special character with the same first letter.
     * For example, // (line comment) and / (divide), >= (greater or equal to) and > (greater than)
     *
     * @param firstChar the first character of the special character(s)
     * @param secondChar the second character of the special character(s)
     * @param kind_long the kind of the special character(s) with length two, with the first character equals to firstChar
     *              and the second character equals to secondChar
     * @param kind_short the kind of the special character(s) with length one, with the first character equals to firstChar
     * @return the Token constructed
     */
    private Token constructSpecialCharWithSameFirstLetterToken(char firstChar, char secondChar, Token.Kind kind_long, Token.Kind kind_short) {
        if ((nextChar = this.sourceFile.getNextChar()) == secondChar) {
            nextChar = null;
            return this.createNewToken(kind_long, Character.toString(firstChar) + Character.toString(secondChar));
        }
        else {
            return this.createNewToken(kind_short, Character.toString(firstChar));
        }
    }

    /**
     * Helper method to construct an integer constant Token.
     * Creates an INTCONST Token if the constructed integer constant token is legal.
     * Creates an Error Token (and registers the error) if the constructed integer constant token is too large (> 2^32 - 1).
     *
     * @return the Token constructed
     */
    private Token constructIntConstantToken() {
        String integerConstant = Character.toString(this.currentChar);
        this.currentChar = this.sourceFile.getNextChar();
        while (Character.isDigit(this.currentChar)) {
            integerConstant += this.currentChar;
            this.currentChar = this.sourceFile.getNextChar();
        }
        try {
            Integer.parseInt(integerConstant);
        } catch (Exception e) {
            nextChar = currentChar;
            return createAndRegisterErrorToken("Integer Constant Too Large", integerConstant);
        }
        nextChar = currentChar;
        return this.createNewToken(Kind.INTCONST, integerConstant);
    }

    /**
     * Helper method to construct an IDENTIFIER token.
     * An identifier is any non-keyword that starts with an uppercase or lowercase letter
     * and is followed by a sequence of letters (upper or lowercase), digits, and underscore '_'.
     *
     * @return the Token constructed
     */
    private Token constructIdentifierToken() {
        String identifier = Character.toString(this.currentChar);
        this.currentChar = this.sourceFile.getNextChar();
        while (Character.isLetterOrDigit(this.currentChar) || this.currentChar == '_') {
            identifier += this.currentChar;
            this.currentChar = this.sourceFile.getNextChar();
        }
        nextChar = currentChar;
        return this.createNewToken(Kind.IDENTIFIER, identifier);
    }

    /**
     * Helper method to construct a line comment token.
     * A line comment Token contains // and everything after it within the same line
     *
     * @return the COMMENT Token constructed
     */
    private Token constructLineCommentToken() {
        String lineComment = "//";
        this.currentChar = this.sourceFile.getNextChar();

        while (this.currentChar != SourceFile.eol && this.currentChar != SourceFile.eof) {
            lineComment += this.currentChar;
            this.currentChar = this.sourceFile.getNextChar();
        }
        return this.createNewToken(Kind.COMMENT, lineComment);
    }

    /**
     * Helper method to construct a block comment token.
     * Creates a COMMENT Token if the block comment is properly terminated.
     * Creates an ERROR Token (and registers the error) if the block comment is not properly terminated.
     *
     * @return the COMMENT Token constructed
     */
    private Token constructBlockCommentToken() {
        String blockComment = "/*";
        startPosition = sourceFile.getCurrentLineNumber();
        this.currentChar = this.sourceFile.getNextChar();
        char lastChar;
        while (!(this.currentChar == '*' && this.sourceFile.getNextChar() == '/')) {
            blockComment += this.currentChar;
            if ((lastChar = this.sourceFile.getNextChar()) == SourceFile.eof) {
                return createAndRegisterErrorToken("Unterminated Block Comment", blockComment);
            }
            this.currentChar = lastChar;
        }
        blockComment += "*/";
        return this.createNewToken(Kind.COMMENT, blockComment);
    }

    /**
     * Helper method to construct a string constant Token.
     * Creates a STRCONST Token if it is within 5000 characters, only contains the following special symbols:
     * \n (newline), \t (tab), \" (double quote), \\ (backslash), and \f (form feed), and if it is properly terminated.
     * Creates an unterminated string ERROR Token if the string is not properly terminated.
     * Creates a contains illegal escape characters ERROR Token if the string contains illegal escape characters.
     * Creates a string too long ERROR Token if the string exceeds 5000 characters.
     *
     * @return the STRCONST Token constructed
     */
    private Token constructStringConstantToken() {
        String stringConstant = Character.toString(this.currentChar);
        //In case of multiline strings, store the starting line for the error token
        startPosition = sourceFile.getCurrentLineNumber();
        boolean containIllegalEscapeChar = false;
        this.currentChar = this.sourceFile.getNextChar();

        boolean illegalMultiline = false;
        while (!(this.currentChar == '\"' && !this.isEscaped(stringConstant))) {
            //I've changed this case from Liwei's group's version because their immediate return at the end of the line
            //meant the beginning of the next line (which was still in the illegal string) was processed as a valid token
            //It would then think the ending " was the beginning of another unterminated string.
            //That gave us three+ tokens and two errors for one multiline String
            if (this.currentChar == SourceFile.eol) {
                illegalMultiline = true;
                stringConstant += this.currentChar;
                this.currentChar = this.sourceFile.getNextChar();
                continue;
            }
            else if (this.currentChar == SourceFile.eof) {
                return createAndRegisterErrorToken("Unterminated String Constant or illegal multline string", stringConstant);
            }
            else if (this.currentChar == '\\' ) {
                if (!this.isLegalEscapeChars(stringConstant)) {
                    containIllegalEscapeChar = true;
                }
            }
            else {
                stringConstant += this.currentChar;
                this.currentChar = this.sourceFile.getNextChar();
            }
        }
        stringConstant += "\"";

        if(illegalMultiline){
            return createAndRegisterErrorToken("Unterminated String Constant", stringConstant);
        }
        if (containIllegalEscapeChar) {
            return createAndRegisterErrorToken("String Contains Illegal Escape Characters", stringConstant);
        }
        if (stringConstant.length() > 5002) {
            return createAndRegisterErrorToken("String Exceeds 5000 Characters", stringConstant);
        }


        return this.createNewToken(Kind.STRCONST, stringConstant);
    }

    /**
     * Helper method to check whether a sequence of characters starting with / are legal escape characters.
     * And appends the characters being checked to the string constant along the way of examining them.
     *
     * @return  true if they are legal escape characters;
     *          false if they are not legal escape characters
     */
    private boolean isLegalEscapeChars(String stringConstant) {
        int countBackslash = 0;
        while (this.currentChar == '\\') {
            countBackslash++;
            stringConstant += this.currentChar;
            this.currentChar = this.sourceFile.getNextChar();
        }
        if (countBackslash%2 == 0) return true;
        return  (this.currentChar == 'n') ||
                (this.currentChar == 't') ||
                (this.currentChar == '\"') ||
                (this.currentChar == 'f');
    }

    /**
     * Helper method to determine whether a given character has been escaped in the given source string
     *
     * @return a boolean indicating whether or not the character has been escaped
     */
    private boolean isEscaped(String stringConstant) {
        int countBackslash = 0;
        int tmpIndex = stringConstant.length() - 1;

        // count the number of consecutive backslashes before the given character
        while (tmpIndex >= 0 && stringConstant.charAt(tmpIndex) == '\\') {
            countBackslash++;
            tmpIndex--;
        }
        // if the number of backslashes is odd, then the character is escaped
        if (countBackslash%2 == 1) return true;
        return false;
    }

    /**
     * Iterates through the file and returns the next Token each time being called.
     * When it reaches the end of the file, any calls to scan() result in a Token of kind EOF.
     *
     * @return the next Token
     */
    public Token scan() {
            // ignore spaces, tabs, or newlines
            while (this.currentChar == ' ' || this.currentChar == '\t' || this.currentChar == SourceFile.eol) {
                this.currentChar = this.sourceFile.getNextChar();
            }
            // -------------------- EOF
            if (this.currentChar == SourceFile.eof) {
                return this.createNewToken(Token.Kind.EOF, "End of File");
            }

            // -------------------- Line Comment
            else if (this.currentChar == '/' && (nextChar = this.sourceFile.getNextChar()) == '/') {
                nextChar =  null; //Reset nextChar to null if the char won't be lost;
                return this.constructLineCommentToken();
            }

            // -------------------- Block Comment
            else if (this.currentChar == '/' && nextChar == '*') {
                nextChar = null; //Reset nextChar to null if the char won't be lost;
                return this.constructBlockCommentToken();
            }

            // -------------------- Identifiers
            else if (Character.isLetter(this.currentChar)) {
                return this.constructIdentifierToken();
            }

            // -------------------- Integer constants
            else if (Character.isDigit(this.currentChar)) {
                return this.constructIntConstantToken();
            }

            // ----------------- String constants
            else if(this.currentChar == '\"') {
                return this.constructStringConstantToken();
            }

            // -------------------- Special Characters
            // &&
            else if (this.currentChar == '&' && (this.sourceFile.getNextChar()) == '&') {
                return this.createNewToken(Token.Kind.BINARYLOGIC, "&&");
            }
            // ||
            else if (this.currentChar == '|' && (this.sourceFile.getNextChar()) == '|') {
                return this.createNewToken(Token.Kind.BINARYLOGIC, "||");
            }
            // -- / -
            else if (this.currentChar == '-') {
                return this.constructSpecialCharWithSameFirstLetterToken('-', '-', Token.Kind.UNARYDECR, Token.Kind.PLUSMINUS);
            }
            // ++ / +
            else if (this.currentChar == '+') {
                return this.constructSpecialCharWithSameFirstLetterToken('+', '+', Token.Kind.UNARYINCR, Token.Kind.PLUSMINUS);
            }
            // != / !
            else if (this.currentChar == '!') {
                return this.constructSpecialCharWithSameFirstLetterToken('!', '=', Token.Kind.COMPARE, Token.Kind.UNARYNOT);
            }
            // == / =
            else if (this.currentChar == '=') {
                return this.constructSpecialCharWithSameFirstLetterToken('=', '=', Token.Kind.COMPARE, Token.Kind.ASSIGN);
            }
            // <= / <
            else if (this.currentChar == '<') {
                return this.constructSpecialCharWithSameFirstLetterToken('<', '=', Token.Kind.COMPARE, Token.Kind.COMPARE);
            }
            // >= / >
            else if (this.currentChar == '>') {
                return this.constructSpecialCharWithSameFirstLetterToken('>', '=', Token.Kind.COMPARE, Token.Kind.COMPARE);
            }
            // *
            else if (this.currentChar == '*') {
                return this.createNewToken(Token.Kind.MULDIV, "*");
            }
            // /
            else if (this.currentChar == '/') {
                return this.createNewToken(Token.Kind.MULDIV, "/");
            }
            // %
            else if (this.currentChar == '%') {
                return this.createNewToken(Token.Kind.MULDIV, "%");
            }
            // {
            else if (this.currentChar == '{') {
                return this.createNewToken(Token.Kind.LCURLY, "{");
            }
            // }
            else if (this.currentChar == '}') {
                return this.createNewToken(Token.Kind.RCURLY, "}");
            }
            // [
            else if (this.currentChar == '[') {
                return this.createNewToken(Token.Kind.LBRACKET, "[");
            }
            // ]
            else if (this.currentChar == ']') {
                return this.createNewToken(Token.Kind.RBRACKET, "]");
            }
            // (
            else if (this.currentChar == '(') {
                return this.createNewToken(Token.Kind.LPAREN, "(");
            }
            // )
            else if (this.currentChar == ')') {
                return this.createNewToken(Token.Kind.RPAREN, ")");
            }
            // .
            else if (this.currentChar == '.') {
                return this.createNewToken(Token.Kind.DOT, ".");
            }
            // ,
            else if (this.currentChar == ',') {
                return this.createNewToken(Token.Kind.COMMA, ",");
            }
            // ;
            else if (this.currentChar == ';') {
                return this.createNewToken(Token.Kind.SEMICOLON, ";");
            }
            // :
            else if (this.currentChar == ':') {
                return this.createNewToken(Token.Kind.COLON, ":");
            }
            // Illegal Special Characters
            else {
                return createAndRegisterErrorToken("Illegal Special Character", Character.toString(this.currentChar));
            }

    }

    /**
     * Scans the file and returns a String containing all tokens of the given file, including the error tokens.
     *
     * @return a String containing all tokens of the given file, each on a separate line, including the error tokens.
     */
    public String scanFile() {
        Token curToken = null; // the current token
        String tokenResult = ""; // a String a String containing all tokens of the given file

        // scan the file from the beginning to the end of the file
        while (curToken == null || (curToken.kind != Token.Kind.EOF)) {
            curToken = this.scan();
            tokenResult += curToken.toString();
        }
        return tokenResult;
    }

    /**
     * Main function for testing the Scanner class.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // command line arguments we used for testing purpose
        // test/test1.java test/test2.java test/test3.java test/test4.java test/badtest.java
        for (int i=0; i < args.length; i++) {
            String filename = args[i];
            System.out.println("\n------------------ " + filename + " ------------------" + "\n");
            try {
                ErrorHandler handler = new ErrorHandler();
                Scanner scanner = new Scanner(filename, handler);
                System.out.println(scanner.scanFile());
                List<Error> errorList = handler.getErrorList();
                for (Error err: errorList){
                    System.out.println(err.toString());
                }
                if (errorList.size()==0){
                    System.out.println("Scanning was successful!");
                }
                else if (errorList.size()==1){
                    System.out.println("\n1 illegal token was found.");
                }
                else{
                    System.out.println("\n" + errorList.size() + " illegal tokens were found.");
                }
            }
            catch (Exception e) {
                System.out.println("ERROR: Scanning " + filename + " failed!");
            }
        }
    }
}


