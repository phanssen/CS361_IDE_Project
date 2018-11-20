package proj9DouglasHanssenMacDonaldZhang.bantam.lexer;

import proj9DouglasHanssenMacDonaldZhang.bantam.util.ErrorHandler;

import java.io.*;

import proj9DouglasHanssenMacDonaldZhang.bantam.lexer.Token.Kind;
import proj9DouglasHanssenMacDonaldZhang.bantam.util.Error;


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
    //String tokenString;
    private String lostChar;
    //private Error error;
    //Token completeToken;
    int lineNum;

    public Scanner(ErrorHandler handler) {
        errorHandler = handler;
        currentChar = ' ';
        sourceFile = null;
        //tokenString = "";
        token = "";
        tokenDone = false;
        type = null;
        isNotLetters = true; //Used to help figure out if the token is ints
        stringOpen = false;
        multilineCommentOpen = false;
        lostChar = null; //Needed solely to avoid losing an char to division

    }

    public Scanner(String filename, ErrorHandler handler) {
        errorHandler = handler;
        currentChar = ' ';
        sourceFile = new SourceFile(filename);
        //tokenString = "";
        token = "";
        tokenDone = false;
        type = null;
        isNotLetters = true; //Used to help figure out if the token is ints
        stringOpen = false;
        multilineCommentOpen = false;
        lostChar = null; //Needed solely to avoid losing an char to division
    }

    public Scanner(Reader reader, ErrorHandler handler) {
        errorHandler = handler;
        sourceFile = new SourceFile(reader);
        //tokenString = "";
        token = "";
        tokenDone = false;
        type = null;
        isNotLetters = true; //Used to help figure out if the token is ints
        stringOpen = false;
        multilineCommentOpen = false;
        lostChar = null; //Needed solely to avoid losing an char to division
    }


    /*public String getTokenString(){
        return tokenString;
    }*/

    private char getLastTokenChar(){
        return token.charAt(token.length()-1);
    }


    private Token makeNewToken(){
        //System.out.println("New token " + type + " " + token + " line num: " + sourceFile.getCurrentLineNumber());
        Token newToken = new Token(type, token, lineNum);
        type = null;
        isNotLetters = true;
        tokenDone = false;
        //linNnum can't be reset because it's a primitive, but resetting for it would only be a safety precaution anyways
        //System.out.println(token);
        if(lostChar!=null){
            token = lostChar;
            lostChar = null;
        }
        else {
            token = "";
        }
        //System.out.println(newToken.toString());
        //tokenString += newToken.toString();
        return newToken;
    }


    private void notifyErrorHandler(Error error){
        this.errorHandler.register(error.getKind(), error.getFilename(), error.getLineNum(), error.getMessage());;
    }

    public Token scan() {
        if(tokenDone){ //If there's already a finished token caught on the last round but a previous token had to be handled
            return finishToken();
        }
        else {
            try {
                 //Reader reader = new BufferedReader(new FileReader("Users\\Tear\\Downloads\\CS361_IDE_Project-master-9-V2\\CS361_IDE_Project-master\\proj9DouglasHanssenMacDonaldZhang\\A.java"));

                while ((currentChar = sourceFile.getNextChar()) != '\u0000') {
                    //System.out.println(currentChar);
                    if ((!stringOpen) && (!multilineCommentOpen) && (!singlelineCommentOpen)) {
                        if (Character.isDigit(currentChar)) {
                            Token completeToken = handleDigit();
                            if (completeToken != null) return completeToken;
                        } else if (Character.isLetter(currentChar)) {
                            Token completeToken = handleLetter();
                            if (completeToken != null) return completeToken;
                        } else {
                            Token completeToken = null;
                            switch (currentChar) {
                                case '{':
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

                                case '}':
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

                                case '(':
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

                                case ')':
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

                                case '[':
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

                                case ']':
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

                                case ':':
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

                                case ';':
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

                                case ',':
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

                                case '_': //Part of constants' identifiers
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        completeToken = finishToken();
                                    }
                                    token += "_";
                                    type = Kind.IDENTIFIER;
                                    lineNum = sourceFile.getCurrentLineNumber();
                                    if(completeToken!= null) return completeToken;
                                    break;

                                case '!':
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        completeToken = finishToken();
                                    }
                                    token = "!";
                                    type = Kind.UNARYNOT;
                                    //If it begins a !=, that's comparison, not unary not, and it won't be done yet
                                    if(completeToken!= null) return completeToken;
                                    break;

                                case '=':
                                    if (token.length() > 0) {
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

                                case '+':
                                    if (token.length() > 0) {
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


                                case '-':
                                    if (token.length() > 0) {
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

                                case '/':
                                    if (token.length() > 0) {
                                        completeToken = finishToken();
                                    }
                                    token += "/";
                                    handleForwardSlash();
                                    if(completeToken!= null) return completeToken;
                                    break;

                                case '\"':
                                    if (token.length() > 0) {
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        completeToken = finishToken();
                                    }
                                    token = "\"";
                                    stringOpen = true; //Closing open strings is handled elsewhere
                                    type = Kind.STRCONST;
                                    if(completeToken!= null) return completeToken;
                                    break;

                                case '.':
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

                                case '*':
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

                                case '<': //<= is not legal, so don't need to account for that
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

                                case '>':
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

                                case '%':
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
                                case ' ': //Empty space means any current token is over, then we handle crap
                                    if (token.length() > 0) {
                                        System.out.println("Done, space, token = " + token);
                                        tokenDone = true;
                                        lineNum = sourceFile.getCurrentLineNumber();
                                    }
                                    break;
                                case '\n':
                                    if (token.length() > 0) {
                                        System.out.println("Done, newline, token = " + token + lineNum);
                                        tokenDone = true;
                                        lineNum = sourceFile.getCurrentLineNumber()-1;
                                    }
                                    //count++;
                                    break;
                                case '\t':
                                    if (token.length() > 0) {
                                        System.out.println("Done, tab, token = " + token);
                                        lineNum = sourceFile.getCurrentLineNumber();
                                        tokenDone = true;
                                    }
                                    //count++;
                                    break;
                                case '$': //Only can be used inside identifiers
                                    token = "$";
                                    type = Kind.IDENTIFIER;

                                default:
                                    type = Kind.ERROR;
                                    token += Character.toString(currentChar);
                                    Error error = new Error(Error.Kind.LEX_ERROR, sourceFile.getFilename(), sourceFile.getCurrentLineNumber(), "Illegal character(s)");
                                    this.notifyErrorHandler(error);
                                    tokenDone = true;
                                    lineNum = sourceFile.getCurrentLineNumber();
                            } //Close switch statement for char

                            if (tokenDone) {
                                return finishToken();

                            }


                        }


                    } else { //This is in a comment or string
                        //System.out.println("Current token: " + token);
                        if (stringOpen) {
                            token += Character.toString(currentChar);
                            handleStringProcessing();
                        }
                        else if (multilineCommentOpen) {
                            token += Character.toString(currentChar);
                            handleCommentProcessing();
                        }
                        else if(singlelineCommentOpen) {
                            if(currentChar == '\n'){ //Don't add the new line char to a single line comment
                                singlelineCommentOpen = false;
                                lineNum = sourceFile.getCurrentLineNumber()-1;
                                tokenDone = true;
                            }
                            else{
                                token += Character.toString(currentChar);
                            }
                        }

                        // make token for string or comment. Type should've already been set by the case statements
                        if (tokenDone) {
                            Token newToken = makeNewToken();
                            System.out.println("Made a new string or comment token " + newToken);
                            return newToken;
                            //System.out.println(newToken.toString());
//                          return newToken;
                        }


                    }

                } //Close while loop



                if ((multilineCommentOpen) | (singlelineCommentOpen) | (stringOpen)) {
                    Error error = new Error(Error.Kind.LEX_ERROR, sourceFile.getFilename(), sourceFile.getCurrentLineNumber(), "Found end of file before program was properly closed.");
                    this.notifyErrorHandler(error);
                    return new Token(Kind.ERROR, token, sourceFile.getCurrentLineNumber());
                    //make an error token(stringOpen) {
                }

                /*type = Kind.EOF; //Once the SourceFile only sends the end of file char, then only this section should be triggered
                Token eofToken = makeNewToken();
                return eofToken;*/

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        type = Kind.EOF; //Once the SourceFile only sends the end of file char, then only this section should be triggered
        Token eofToken = makeNewToken();
        return eofToken;
    }


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

    private Token handleDigit(){
        Token completeToken = null;
        //Manual check to see if this terminated a +,-, =. ++, -- etc should've already been terminated
        //the types should've already been set by the case statements, so they don't have to be set
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

    private void handleForwardSlash(){
            char nextNextChar = sourceFile.getNextChar();
            if(nextNextChar == '/'){
                type = Kind.COMMENT;
                token += nextNextChar;
                singlelineCommentOpen = true;
            }
            else if (nextNextChar == '*'){
                token += "*";
                type = Kind.COMMENT;
                multilineCommentOpen = true;
            }
            else{
                //Tia just added this
                type = Kind.MULDIV;
                tokenDone = true;
                if(!Character.isWhitespace(nextNextChar)){
                    lostChar = Character.toString(nextNextChar);
                }
            }
    }

    private void handleStringProcessing(){
        if (currentChar == '\\') { //get the next char and check for illegal special chars
            char nextNextChar = sourceFile.getNextChar();
            token += nextNextChar;
            //if not \n (newline), \t (tab), \" (double quote), \\ (backslash), and \f
            if( (nextNextChar != 't') && (nextNextChar!= 'n') && (nextNextChar != '\\') &&
                    (nextNextChar !='\"') && (nextNextChar != 'f')){
                type = Kind.ERROR;
                Error error = new Error(Error.Kind.LEX_ERROR, sourceFile.getFilename(), sourceFile.getCurrentLineNumber(), "Illegal character(s)");
                this.notifyErrorHandler(error);
            }

        } else if (currentChar == '\"') {
            stringOpen = false;
            tokenDone = true;
            lineNum = sourceFile.getCurrentLineNumber();
            if (token.length() > 5000) {
                type = Kind.ERROR;
                Error error = new Error(Error.Kind.LEX_ERROR, sourceFile.getFilename(), sourceFile.getCurrentLineNumber(), "String exceeds 5000 characters");
                this.notifyErrorHandler(error);
            }
        } else if (currentChar == '\n') {
            type = Kind.ERROR;
            Error error = new Error(Error.Kind.LEX_ERROR, sourceFile.getFilename(), sourceFile.getCurrentLineNumber(), "String not properly closed");
            this.notifyErrorHandler(error);
            }
        }

    private void handleCommentProcessing(){
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

    private Token finishToken(){
        if (type == null) { //It terminated by whitespace, also, move this into a function
            if (isNotLetters) { //Kind of bad but this boolean isn't used unless there isn't an alt type
                type = Kind.INTCONST;
                int num = Integer.parseInt(token);
                int intRoof = (int) Math.pow(2, 32);
                if( (0 > num) | (num> intRoof) ){
                    type = Kind.ERROR;
                    Error error = new Error(Error.Kind.LEX_ERROR, sourceFile.getFilename(), sourceFile.getCurrentLineNumber(), "Integer is too large.");
                    this.notifyErrorHandler(error);
                }
            }
            else{  //Tia just added this
                type = Kind.IDENTIFIER;
            }
        }
        //This is not an else if so it catches both identifiers from the if type == null and anything caught with _ or $
        if (type == Kind.IDENTIFIER) {
            if (!Character.isLetter(token.charAt(0))) { //if the first letter of the identifier isn't a letter
                type = Kind.ERROR;
                Error error = new Error(Error.Kind.LEX_ERROR, sourceFile.getFilename(), sourceFile.getCurrentLineNumber(), "Not a legal identifier.");
                this.notifyErrorHandler(error);
                //illegal identifier token
            }
        }


        //make token into a Token here
        Token newToken = makeNewToken();
        return newToken;
        //System.out.println(newToken.toString());

    }

}
