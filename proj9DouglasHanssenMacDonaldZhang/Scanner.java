package proj9DouglasHanssenMacDonaldZhang.bantam.lexer;

import proj9DouglasHanssenMacDonaldZhang.bantam.util.ErrorHandler;

import java.io.Reader;

public class Scanner
{
    private SourceFile sourceFile;
    private ErrorHandler errorHandler;
    private char currentChar;


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

    public Token scan()
    {
       return null;  // REMOVE THIS LINE AND REPLACE IT WITH YOUR CODE
    }

}
