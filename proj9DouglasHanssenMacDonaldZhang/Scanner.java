package proj9DouglasHanssenMacDonaldZhang.bantam.lexer;

import proj9DouglasHanssenMacDonaldZhang.bantam.util.ErrorHandler;

import java.io.*;

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
    boolean commentOpen;
    boolean stringOpen;
    String tokenString;

    public Scanner(ErrorHandler handler) {
        errorHandler = handler;
        currentChar = ' ';
        sourceFile = null;
        tokenString = "";
        token = "";
        tokenDone = false;
        type = null;
        isNotLetters = true; //Used to help figure out if the token is ints
        stringOpen = false;
        commentOpen = false;
    }

    public Scanner(String filename, ErrorHandler handler) {
        errorHandler = handler;
        currentChar = ' ';
        sourceFile = new SourceFile(filename);
        tokenString = "";
        token = "";
        tokenDone = false;
        type = null;
        isNotLetters = true; //Used to help figure out if the token is ints
        stringOpen = false;
        commentOpen = false;
    }

    public Scanner(Reader reader, ErrorHandler handler) {
        errorHandler = handler;
        sourceFile = new SourceFile(reader);
        tokenString = "";
        token = "";
        tokenDone = false;
        type = null;
        isNotLetters = true; //Used to help figure out if the token is ints
        stringOpen = false;
        commentOpen = false;
    }


    public String getTokenString(){
        return tokenString;
    }

    private char getLastTokenChar(){
        return token.charAt(token.length()-1);
    }


    private Token makeNewToken(){
        //System.out.println("New token " + type + " " + token + " line num: " + sourceFile.getCurrentLineNumber());
        Token newToken = new Token(type, token, sourceFile.getCurrentLineNumber());
        type = null;
        isNotLetters = true;
        tokenDone = false;
        //System.out.println(token);
        token = "";
        System.out.println(newToken.toString());
        tokenString += newToken.toString();
        return newToken;
    }


    private void notifyErrorHandler(Kind kind){
        return;
    }

    public Token scan() {
        if(tokenDone){ //If there's already a finished token caught on the last round
            return finishToken();
        }
        else {
            try {
                //Reader reader = new BufferedReader(new FileReader("Users\\Tear\\Downloads\\CS361_IDE_Project-master-9-V2\\CS361_IDE_Project-master\\proj9DouglasHanssenMacDonaldZhang\\A.java"));
                Token completeToken = null;
                while ((currentChar = sourceFile.getNextChar()) != '\u0000') {
                    //System.out.println(currentChar);
                    if ((!stringOpen) && (!commentOpen)) {
                        if (Character.isDigit(currentChar)) {
                            //Manual check to see if this terminated a +,-, =. ++, -- etc should've already been terminated
                            //the types should've already been set by the case statements
                            if (token.length() == 1) {
                                if ((token.charAt(0) == '+') | (token.charAt(0) == '-') | (token.charAt(0) == '=')) {
                                    completeToken = makeNewToken();
                                }
                            }
                            token += Character.toString(currentChar);
                            if (completeToken != null) return completeToken;
                        } else if (Character.isLetter(currentChar)) {
                            if (token.length() == 1) {
                                if ((token.charAt(0) == '+') | (token.charAt(0) == '-') | (token.charAt(0) == '=')) {
                                    completeToken = makeNewToken();
                                }
                            }
                            token += Character.toString(currentChar);
                            if (isNotLetters) {
                                isNotLetters = false;
                            }
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
                        token += Character.toString(currentChar);
                        //System.out.println("Current token: " + token);
                        if (stringOpen) {
                            handleStringProcessing();
                        } else if (commentOpen) {
                            handleCommentProcessing();
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

                if (commentOpen) {
                    notifyErrorHandler(Kind.ERROR);
                    return new Token(Kind.ERROR, token, sourceFile.getCurrentLineNumber());
                    //make an error token
                }

                if (stringOpen) {
                    notifyErrorHandler(Kind.ERROR);
                    return new Token(Kind.ERROR, token, sourceFile.getCurrentLineNumber());
                    //make an error token
                }

                type = Kind.EOF; //Once the SourceFile only sends the end of file char, then only this section should be triggered
                Token eofToken = makeNewToken();
                return eofToken;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        type = Kind.EOF; //Once the SourceFile only sends the end of file char, then only this section should be triggered
        Token eofToken = makeNewToken();
        return eofToken;
    }

    private void handleForwardSlash(){
            char nextNextChar = sourceFile.getNextChar();
            if(nextNextChar == '/'){
                type = Kind.COMMENT;
                token += nextNextChar;
                while(nextNextChar != '\n') { //TODO replace this with a boolean and add to comment handling
                    nextNextChar = sourceFile.getNextChar();
                    token += nextNextChar;
                    tokenDone = true;
                }
            }
            else if (nextNextChar == '*'){
                token += "*";
                type = Kind.COMMENT;
                commentOpen = true;
            }
            else{ //How do you avoid eating the next char here? Lost char variable?
                //Tia just added this
                type = Kind.MULDIV;
                tokenDone = true;
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
                commentOpen = false;
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
