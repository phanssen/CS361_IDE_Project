package proj9DouglasHanssenMacDonaldZhang.bantam.lexer;

import proj9DouglasHanssenMacDonaldZhang.CodeAreaTabPane;
import proj9DouglasHanssenMacDonaldZhang.bantam.util.ErrorHandler;

import java.io.*;

import proj9DouglasHanssenMacDonaldZhang.bantam.lexer.Token.Kind;
import java.util.Arrays;

public class Scanner
{
    private SourceFile sourceFile;
    private ErrorHandler errorHandler;
    private String token; 
    //Do we need instanceof? 
    //no interfaces, final, static, packages, abstract, other loops, switch statement, prims aside from boolean
//    private String[] keywords = {"class", "while", "for", "int", "boolean", "if",
//    "else", "extends", "import", "public", "return", "super", "this", "void", "throw", "throws"};


    public Scanner(ErrorHandler handler) {
        errorHandler = handler;
        char currentChar = ' ';
        sourceFile = null;
    }

    public Scanner(String filename, ErrorHandler handler) {
        errorHandler = handler;
        char currentChar = ' ';
        sourceFile = new SourceFile(filename);
    }

    public Scanner(Reader reader, ErrorHandler handler) {
        errorHandler = handler;
        sourceFile = new SourceFile(reader);
    }


    private char getLastTokenChar(){
        int tokenLength = token.length();
        return token.charAt(tokenLength-2);
    }


    private void notifyErrorHandler(Kind kind){
        return;
    }

    public void scan() throws IOException {
        token = "";
        boolean tokenDone = false;
        Kind type = null;
        boolean isNotLetters = true; //Used to help figure out if the token is ints
        boolean stringOpen = false;
        boolean commentOpen = false;
        String filename;

        Reader reader = new BufferedReader(new FileReader("Token.java"));

        int count = 0;
        int c;

        while((c = reader.read()) != -1) {
        char character = (char) c;

        if(Character.isDigit(character)){
            token += Character.toString(character);
        }
        else if (Character.isLetter(c)) {
            token += Character.toString(character);
            if(isNotLetters){
                isNotLetters = false;
            }
        }


        else {
            if((!stringOpen) && (!commentOpen) ) {
                switch(character) {
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
                        if(getLastTokenChar() == '='){
                            tokenDone = true;
                        }
                        break;

                    case '+':
                        token += "+";
                        type = Kind.PLUSMINUS;
                        if(getLastTokenChar() == '+'){
                            tokenDone = true;
                        }
                        break;
                        

                    case '-':
                        token = "-";
                        type = Kind.PLUSMINUS;
                        if(getLastTokenChar() == '-'){
                            tokenDone = true;
                        }

                        break;
                        
                    case '/':
                        token += "/"; //Part of a comment
                        type = Kind.COMMENT;
                        if(getLastTokenChar() == '/') {
                            token += ((BufferedReader) reader).readLine(); // figure out how to read line with reader
                            tokenDone = true;
                        }
                        else if(getLastTokenChar() == '*') {
                            commentOpen = false;
                            tokenDone = true;
                        }
                        else{
                            type = Kind.ERROR;
                            notifyErrorHandler(Kind.ERROR);
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
                        if(getLastTokenChar()  == '/'){
                            token += "*";
                            type = Kind.COMMENT;
                            commentOpen = true;
                        }
                        else{
                            token = "*";
                            type = Kind.MULDIV;
                            tokenDone = true;
                        }

                        break;

                    case '<': //<= is not legal
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
                        count++;
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
                        if (token.equals("true") ^ token.equals("false")){
                            type = Kind.BOOLEAN;
                        }
                        else{
                            if (isNotLetters) { //Kind of bad but this boolean isn't used unless there isn't an alt type
                                type = Kind.INTCONST;
                            }
                        }
                    }
/*
else{
boolean isKeyword = Arrays.stream(keywords).anyMatch(token::equals);
if(!isKeyword):
type = "Identifier";
}
*/
                    else if(("Special Symbol".equals(type)) && (token.length() > 1)){ //Then the symbol might've ended a previous token
                        int tokenLength = token.length();
                        String prevToken;
                        //If it's one of the two character symbols
                        if( ("==".equals(token.substring(tokenLength-2, tokenLength) ) ) ||
                            ("--".equals(token.substring(tokenLength-2, tokenLength) ) ) ||
                            ("==".equals(token.substring(tokenLength-2, tokenLength) ) ) ||
                            ("!=".equals(token.substring(tokenLength-2, tokenLength) ) ) ) {
                                if(tokenLength > 2){ 
                                    prevToken = token.substring(0, tokenLength-2);
                                    token = token.substring(tokenLength-2, tokenLength);
                                    
                                    //check if it's identifier, integer, string, or boolean by calling a function, 
                                    //which should just be part of the above under the if tokenDone, but moved into a function
                                    //make prevToken into a token here
                                }


                        }
                        else{ 
                            //It's a one character punctuation and it's currently lumped with the previous token
                            prevToken = token.substring(0, tokenLength-2);
                            //check if it's identifier, integer, string, or boolean by calling a function, 
                            //which should just be part of the above under the if tokenDone, but moved into a function
                            //make prevToken into a token here 
                        }
                        
                        token = token.substring(tokenLength-2);
                        

                    }
                    //This is not an else if so it catches both identifiers from the if type == null and anything caught with _ or $
                    if(type == Kind.IDENTIFIER){
                        if(!Character.isLetter(token.charAt(0))){ //if the first letter of the identifier isn't a letter
                            type = Kind.ERROR;
                            //illegal identifier token
                            }
                    }

                        
                    //make token into a Token here
                    type = null;
                    boolean isAllInts = true;
                    tokenDone = false;
                    token = "";


                }

                else{ //This is in a comment or string, who cares about it
                    token += Character.toString(character);
                    if(stringOpen) {
                        if(character == '\\'){ //get the next char and check for illegal special chars
                            char nextNextChar = (char) reader.read();

                            
                        }
                        else if (character == '\"'){
                            stringOpen = false;
                            tokenDone = true;
                            if (token.length() > 5000){
                                type = Kind.ERROR; //illegal String token
                                notifyErrorHandler(Kind.ERROR);
                            }
                        }
                        else if(character == '\n'){
                            type = Kind.ERROR;
                            notifyErrorHandler(Kind.ERROR);
                        }
                    }
                    else if(commentOpen){
                        if(character == '*'){
                            char nextNextChar = (char) reader.read();
                            if(nextNextChar == '/'){
                                commentOpen = false;
                            } 
                            else{
                                token += nextNextChar;
                            }
                        } 
                    }


                    if(tokenDone){
                        // make token for string or comment
                        Token newToken = new Token(type, token, count);
                        System.out.println(newToken.toString());
//                        return newToken;
                    }
                    

                }

            }


        } //Close while loop
        }
        if(commentOpen){
            notifyErrorHandler(Kind.ERROR);
            //make an error token
        }

        if(stringOpen){
            notifyErrorHandler(Kind.ERROR);
            //make an error token
        }




//    return newToken;
//        return newToken;
    }

}
