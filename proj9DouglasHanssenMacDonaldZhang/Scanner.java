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

    public Scanner(ErrorHandler handler) {
        errorHandler = handler;
        currentChar = ' ';
        sourceFile = null;
    }

    public Scanner(String filename, ErrorHandler handler) {
        errorHandler = handler;
        currentChar = ' ';
        sourceFile = new SourceFile(filename);
    }

    public Scanner(Reader reader, ErrorHandler handler) {
        errorHandler = handler;
        sourceFile = new SourceFile(reader);
    }


    private char getLastTokenChar(){
        return token.charAt(token.length()-1);
    }


    private Token makeNewToken(){
        //System.out.println(type + " " + token + sourceFile.getCurrentLineNumber());
        Token newToken = new Token(type, token, sourceFile.getCurrentLineNumber());
        type = null;
        isNotLetters = true;
        tokenDone = false;
        System.out.println(token);
        token = "";
        System.out.println(newToken.toString());
        return newToken;
    }


    private void notifyErrorHandler(Kind kind){
        return;
    }

    public void scan() {
        token = "";
        tokenDone = false;
        type = null;
        isNotLetters = true; //Used to help figure out if the token is ints
        stringOpen = false;
        commentOpen = false;
        String filename;
        try {
            //Reader reader = new BufferedReader(new FileReader("Users\\Tear\\Downloads\\CS361_IDE_Project-master-9-V2\\CS361_IDE_Project-master\\proj9DouglasHanssenMacDonaldZhang\\A.java"));
            while ((currentChar = sourceFile.getNextChar())!= '\u0000') {
                System.out.println(currentChar);
                if (Character.isDigit(currentChar)) {
                    token += Character.toString(currentChar);
                } else if (Character.isLetter(currentChar)) {
                    token += Character.toString(currentChar);
                    if (isNotLetters) {
                        isNotLetters = false;
                    }
                } else {
                    if ((!stringOpen) && (!commentOpen)) {
                        switch (currentChar) {
                            case '{':
                                token += "{";
                                type = Kind.LCURLY;
                                tokenDone = true;
                                break;

                            case '}':
                                token += "}";
                                type = Kind.RCURLY;
                                tokenDone = true;
                                break;

                            case '(':
                                token += "(";
                                type = Kind.LPAREN;
                                tokenDone = true;
                                break;

                            case ')':
                                token += ")";
                                type = Kind.RPAREN;
                                tokenDone = true;
                                break;

                            case '[':
                                token += "[";
                                type = Kind.LBRACKET;
                                tokenDone = true;
                                break;

                            case ']':
                                token += "]";
                                type = Kind.RBRACKET;
                                tokenDone = true;
                                break;

                            case ':':
                                token += ":";
                                type = Kind.COLON;
                                tokenDone = true;
                                break;

                            case ';':
                                token += ";";
                                type = Kind.SEMICOLON;
                                tokenDone = true;
                                break;

                            case ',':
                                token = ",";
                                type = Kind.COMMA;
                                tokenDone = true;
                                break;

                            case '_': //Part of constants identifiers
                                token += "_";
                                type = Kind.IDENTIFIER;
                                break;

                            case '!':
                                token = "!";
                                type = Kind.UNARYNOT;
                                tokenDone = true;
                                break;

                            case '=':
                                token += "=";
                                type = Kind.ASSIGN;
                                if (getLastTokenChar() == '=') {
                                    type = Kind.COMPARE;
                                    tokenDone = true;
                                }
                                break;

                            case '+':
                                token += "+";
                                type = Kind.PLUSMINUS;
                                if (getLastTokenChar() == '+') {
                                    tokenDone = true;
                                }
                                break;


                            case '-':
                                token = "-";
                                type = Kind.PLUSMINUS;
                                if (getLastTokenChar() == '-') {
                                    tokenDone = true;
                                }

                                break;

                            case '/':
                                token += "/"; //Part of a comment
                                if(token.length() > 1){ //1 here because you've added / to the token already. Tia just added this
                                    if ( getLastTokenChar() == '/') {
                                        char nextNextChar = sourceFile.getNextChar();
                                        token += nextNextChar;
                                        type = Kind.COMMENT;
                                        while(nextNextChar != '\n') { //TODO replace this with a boolean and add to comment handling
                                            token += sourceFile.getNextChar();
                                        }
                                        tokenDone = true;
                                    } else if (getLastTokenChar() == '*') {
                                        commentOpen = false;
                                        tokenDone = true;
                                    }
                                    else{ //Tia just added this
                                        type = Kind.MULDIV;
                                        tokenDone = true;
                                    }
                                }
                                else {
                                    //How do you avoid eating a char if the next character isn't a /? Lost char variable?
                                    char nextNextChar = sourceFile.getNextChar();
                                    if(nextNextChar == '/'){
                                        type = Kind.COMMENT;
                                        while(nextNextChar != '\n') { //TODO replace this with a boolean and add to comment handling
                                            token += sourceFile.getNextChar();
                                            tokenDone = true;
                                        }
                                    }
                                    else{
                                        type = Kind.ERROR;
                                        tokenDone = true;
                                        notifyErrorHandler(Kind.ERROR);
                                    }
                                }
                                break;

                            case '\"':
                                token += "\"";
                                stringOpen = true; //Closing open strings is handled elsewhere
                                type = Kind.STRCONST;
                                break;

                            case '.':
                                token = ".";
                                type = Kind.DOT;
                                tokenDone = true;
                                break;

                            case '*':
                                System.out.println("Asterisk");
                                if (token.length() > 0){ //0 here because you haven't added the * to token. Tia just added this
                                    System.out.println("A");
                                    if (getLastTokenChar() == '/') {
                                        System.out.println("B");
                                        token += "*";
                                        type = Kind.COMMENT;
                                        commentOpen = true;
                                    } else {
                                        token = "*";
                                        type = Kind.MULDIV;
                                        tokenDone = true;
                                    }
                                }
                                else{
                                    System.out.println("Asterisk error");
                                    //Error because you can't have an asterisk without multiplication or /* legally
                                }


                                break;

                            case '<': //<= is not legal, so don't need to account for that
                                token = "<";
                                type = Kind.COMPARE;
                                tokenDone = true;
                                break;

                            case '>':
                                token = ">";
                                type = Kind.COMPARE;
                                tokenDone = true;
                                break;

                            case '%':
                                token = "%";
                                type = Kind.BINARYLOGIC;
                                tokenDone = true;
                                break;
                            case ' ': //Empty space means any current token is over, then we handle crap
                                tokenDone = true;
                                break;
                            case '\n':
                                token = "\n";
                                tokenDone = true;
                                //count++;
                                break;

                            case '$':
                                token = "$";
                                type = Kind.IDENTIFIER;

                            default:
                                type = Kind.ERROR;
                                notifyErrorHandler(Kind.ERROR);
                                tokenDone = true;
                        } //Close switch statement for char

                        if (tokenDone) {
                            if (type == null) { //It terminated by whitespace, also, move this into a function
                                if (isNotLetters) { //Kind of bad but this boolean isn't used unless there isn't an alt type
                                    type = Kind.INTCONST;
                                }
                                else{  //Tia just added this
                                    type = Kind.IDENTIFIER;
                                }
                            }
                            else if ((Kind.LPAREN.equals(type) || Kind.LBRACKET.equals(type) || Kind.DOT.equals(type)) && (token.length() > 1)) { //Then the symbol might've ended a previous token
                                int tokenLength = token.length();
                                String prevToken;
                                //If it's one of the two character symbols
                                if (("==".equals(token.substring(tokenLength - 2, tokenLength))) ||
                                        ("--".equals(token.substring(tokenLength - 2, tokenLength))) ||
                                        ("==".equals(token.substring(tokenLength - 2, tokenLength))) ||
                                        ("!=".equals(token.substring(tokenLength - 2, tokenLength)))) {
                                    if (tokenLength > 2) {
//                                        System.out.println(token);
                                        prevToken = token.substring(0, tokenLength - 2);
                                        token = token.substring(tokenLength - 2, tokenLength);

                                        //check if it's identifier, integer, string, or boolean by calling a function,
                                        //which should just be part of the above under the if tokenDone, but moved into a function
                                        //make prevToken into a token here
                                    }


                                } else {
                                    //It's a one character punctuation and it's currently lumped with the previous token
                                    prevToken = token.substring(0, tokenLength - 2);
                                    //check if it's identifier, integer, string, or boolean by calling a function,
                                    //which should just be part of the above under the if tokenDone, but moved into a function
                                    //make prevToken into a token here
                                    token = prevToken;
                                }

                                token = token.substring(tokenLength - 2);


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
                            //System.out.println(newToken.toString());


                        } else { //This is in a comment or string, who cares about it
                            token += Character.toString(currentChar);
                            if (stringOpen) {
                                if (currentChar == '\\') { //get the next char and check for illegal special chars
                                    char nextNextChar = sourceFile.getNextChar();


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
                            } else if (commentOpen) {
                                if (currentChar == '*') {
                                    char nextNextChar = sourceFile.getNextChar();
                                    if (nextNextChar == '/') {
                                        commentOpen = false;
                                    } else {
                                        token += nextNextChar;
                                    }
                                }
                            }



                                // make token for string or comment
                            Token newToken = makeNewToken();
                            //System.out.println(newToken.toString());
//                          return newToken;



                        }

                    }


                }

            } //Close while loop
            if (commentOpen) {
                notifyErrorHandler(Kind.ERROR);
                //make an error token
            }

            if (stringOpen) {
                notifyErrorHandler(Kind.ERROR);
                //make an error token
            }

            type = Kind.EOF; //Once the SourceFile only sends the end of file char, then only this section should be triggered
            makeNewToken();


        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}
