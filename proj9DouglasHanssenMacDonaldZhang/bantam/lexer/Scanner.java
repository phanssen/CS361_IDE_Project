package proj9DouglasHanssenMacDonaldZhang.bantam.lexer;

import proj9DouglasHanssenMacDonaldZhang.Controllers.FileMenuController;
import proj9DouglasHanssenMacDonaldZhang.Controllers.ToolbarController;
import proj9DouglasHanssenMacDonaldZhang.bantam.util.ErrorHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import proj9DouglasHanssenMacDonaldZhang.bantam.lexer.Token.Kind;

public class Scanner
{
    private SourceFile sourceFile;
    private ErrorHandler errorHandler;
    private String token;
    char currentChar;
    boolean tokenDone;
    Kind type;
    boolean isNotLetters;
    boolean multilineCommentOpen;
    boolean singlelineCommentOpen;
    boolean stringOpen;
    //String tokenString;
    String lostChar;
    Token completeToken;
    List<String> tokenList;

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
        tokenList = new ArrayList<>();
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
        tokenList = new ArrayList<>();
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
        tokenList = new ArrayList<>();
    }


    /*public String getTokenString(){
        return tokenString;
    }*/

    private char getLastTokenChar(){
        return token.charAt(token.length()-1);
    }

    public List<String> getTokens() {
        return tokenList;
    }

    private Token makeNewToken(){
        //System.out.println("New token " + type + " " + token + " line num: " + sourceFile.getCurrentLineNumber());
        Token newToken = new Token(type, token, sourceFile.getCurrentLineNumber());
        type = null;
        isNotLetters = true;
        tokenDone = false;
        //System.out.println(token);
        if(lostChar!=null){
            token = lostChar;
            lostChar = null;
        }
        else {
            token = "";
        }
        System.out.println(newToken.toString());
        //tokenString += newToken.toString();
//        insertToken(newToken.toString());
        tokenList.add(newToken.toString());


        return newToken;
    }


    private void notifyErrorHandler(Kind kind){
        return;
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
                            switch (currentChar) {
                                case '{':
                                    if (token.length() > 0) {
                                        completeToken = finishToken();
                                    }
                                    token = "{";
                                    type = Kind.LCURLY;
                                    tokenDone = true;
                                    break;

                                case '}':
                                    if (token.length() > 0) {
                                        finishToken();
                                    }
                                    token = "}";
                                    type = Kind.RCURLY;
                                    tokenDone = true;
                                    break;

                                case '(':
                                    if (token.length() > 0) {
                                        finishToken();
                                    }
                                    token = "(";
                                    type = Kind.LPAREN;
                                    tokenDone = true;
                                    break;

                                case ')':
                                    if (token.length() > 0) {
                                        finishToken();
                                    }
                                    token = ")";
                                    type = Kind.RPAREN;
                                    tokenDone = true;
                                    break;

                                case '[':
                                    if (token.length() > 0) {
                                        finishToken();
                                    }
                                    token += "[";
                                    type = Kind.LBRACKET;
                                    tokenDone = true;
                                    break;

                                case ']':
                                    if (token.length() > 0) {
                                        finishToken();
                                    }
                                    token += "]";
                                    type = Kind.RBRACKET;
                                    tokenDone = true;
                                    break;

                                case ':':
                                    if (token.length() > 0) {
                                        finishToken();
                                    }
                                    token += ":";
                                    type = Kind.COLON;
                                    tokenDone = true;
                                    break;

                                case ';':
                                    if (token.length() > 0) {
                                        finishToken();
                                    }
                                    token += ";";
                                    type = Kind.SEMICOLON;
                                    tokenDone = true;
                                    break;

                                case ',':
                                    if (token.length() > 0) {
                                        finishToken();
                                    }
                                    token = ",";
                                    type = Kind.COMMA;
                                    tokenDone = true;
                                    break;

                                case '_': //Part of constants' identifiers
                                    if (token.length() > 0) {
                                        finishToken();
                                    }
                                    token += "_";
                                    type = Kind.IDENTIFIER;
                                    break;

                                case '!':
                                    if (token.length() > 0) {
                                        finishToken();
                                    }
                                    token = "!";
                                    type = Kind.UNARYNOT;
                                    //If it begins a !=, that's comparison, not unary not, and it won't be done yet
                                    break;

                                case '=':
                                    if (token.length() > 0) {
                                        if ((getLastTokenChar() != '!') && (getLastTokenChar() != '=')) {
                                            finishToken();
                                            token = "=";
                                            type = Kind.ASSIGN;
                                        } else {
                                            token += "=";
                                            type = Kind.COMPARE;
                                            tokenDone = true;
                                        }
                                    } else {
                                        token += "=";
                                        type = Kind.ASSIGN;
                                    }
                                    break;

                                case '+':
                                    if (token.length() > 0) {
                                        if (getLastTokenChar() == '+') {
                                            type = Kind.UNARYINCR;
                                            token = "++";
                                            tokenDone = true;
                                        } else {
                                            finishToken();
                                            token = "+";
                                            type = Kind.PLUSMINUS;
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
                                            tokenDone = true;
                                        } else {
                                            finishToken();
                                            token = "-";
                                            type = Kind.PLUSMINUS;
                                        }
                                    } else {
                                        token += "-";
                                        type = Kind.PLUSMINUS;
                                    }

                                    break;

                                case '/':
                                    if (token.length() > 0) {
                                        finishToken();
                                    }
                                    token += "/";
                                    handleForwardSlash();
                                    break;

                                case '\"':
                                    if (token.length() > 0) {
                                        finishToken();
                                    }
                                    token = "\"";
                                    stringOpen = true; //Closing open strings is handled elsewhere
                                    type = Kind.STRCONST;
                                    break;

                                case '.':
                                    if (token.length() > 0) {
                                        finishToken();
                                    }
                                    token = ".";
                                    type = Kind.DOT;
                                    tokenDone = true;
                                    break;

                                case '*':
                                    if (token.length() > 0) {
                                        finishToken();
                                    }
                                    token = "*";
                                    type = Kind.MULDIV;
                                    tokenDone = true;


                                    break;

                                case '<': //<= is not legal, so don't need to account for that
                                    if (token.length() > 0) {
                                        finishToken();
                                    }
                                    token = "<";
                                    type = Kind.COMPARE;
                                    tokenDone = true;
                                    break;

                                case '>':
                                    if (token.length() > 0) {
                                        finishToken();
                                    }
                                    token = ">";
                                    type = Kind.COMPARE;
                                    tokenDone = true;
                                    break;

                                case '%':
                                    if (token.length() > 0) {
                                        finishToken();
                                    }
                                    token = "%";
                                    type = Kind.BINARYLOGIC;
                                    tokenDone = true;
                                    break;
                                case ' ': //Empty space means any current token is over, then we handle crap
                                    if (token.length() > 0) {
                                        tokenDone = true;
                                    }
                                    break;
                                case '\n':
                                    if (token.length() > 0) {
                                        tokenDone = true;
                                    }
                                    //count++;
                                    break;
                                case '\t':
                                    if (token.length() > 0) {
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
                                    notifyErrorHandler(Kind.ERROR);
                                    tokenDone = true;
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
                                tokenDone = true;
                            }
                            else{
                                token += Character.toString(currentChar);
                            }
                        }

                        // make token for string or comment
                        if (tokenDone) {
                            Token newToken = makeNewToken();
                            return newToken;
                            //System.out.println(newToken.toString());
//                          return newToken;
                        }


                    }

                } //Close while loop



                if ((multilineCommentOpen) | (singlelineCommentOpen) | (stringOpen)) {
                    notifyErrorHandler(Kind.ERROR);
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
            if ((token.charAt(0) == '+') | (token.charAt(0) == '-') | (token.charAt(0) == '=')) {
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
        completeToken = null;
        //Manual check to see if this terminated a +,-, =. ++, -- etc should've already been terminated
        //the types should've already been set by the case statements, so they don't have to be set
        if (token.length() == 1) {
            if ((token.charAt(0) == '+') | (token.charAt(0) == '-') | (token.charAt(0) == '=')) {
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
        else{ //TODO How do you avoid eating the next char here? Lost char variable?
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
            }

        } else if (currentChar == '\"') {
            stringOpen = false;
            tokenDone = true;
            if (token.length() > 5000) {
                type = Kind.ERROR; //illegal String token
                notifyErrorHandler(Kind.ERROR);
            }
        } else if (currentChar == '\n') {
            type = Kind.ERROR;
            notifyErrorHandler(Kind.ERROR);
        }
    }

    private void handleCommentProcessing(){
        if (currentChar == '*') {
            char nextNextChar = sourceFile.getNextChar();
            token += nextNextChar;
            if (nextNextChar == '/') {
                multilineCommentOpen = false;
                tokenDone = true;
            }
        }
    }

    /*private Token handlePrevToken(){ //I think this just duplicates finishToken
            if (isNotLetters) { //Kind of bad but this boolean isn't used unless there isn't an alt type
                type = Kind.INTCONST;
            }
            else{  //Tia just added this
                type = Kind.IDENTIFIER;
            }
            return makeNewToken();

    }*/

    private Token finishToken(){
        if (type == null) { //It terminated by whitespace, also, move this into a function
            if (isNotLetters) { //Kind of bad but this boolean isn't used unless there isn't an alt type
                type = Kind.INTCONST;
            }
            else{  //Tia just added this
                type = Kind.IDENTIFIER;
            }
        }
        //This is not an else if so it catches both identifiers from the if type == null and anything caught with _ or $
        if (type == Kind.IDENTIFIER) {
            if (!Character.isLetter(token.charAt(0))) { //if the first letter of the identifier isn't a letter
                type = Kind.ERROR;
                //illegal identifier token
            }
        }


        //make token into a Token here
        Token newToken = makeNewToken();
        return newToken;
        //System.out.println(newToken.toString());

    }

}
