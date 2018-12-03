/*
 * Authors: Haoyu Song and Dale Skrien
 * Date: Spring and Summer, 2018
 *
 * In the grammar below, the variables are enclosed in angle brackets.
 * The notation "::=" is used instead of "-->" to separate a variable from its rules.
 * The special character "|" is used to separate the rules for each variable.
 * All other symbols in the rules are terminals.
 * EMPTY indicates a rule with an empty right hand side.
 * All other terminal symbols that are in all caps correspond to keywords.
 */
package proj10DouglasHanssenMacDonaldZhang.bantam.parser;

import static proj10DouglasHanssenMacDonaldZhang.bantam.lexer.Token.Kind.*;
import proj10DouglasHanssenMacDonaldZhang.bantam.lexer.*;
import proj10DouglasHanssenMacDonaldZhang.bantam.util.*;
import proj10DouglasHanssenMacDonaldZhang.bantam.ast.*;
import proj10DouglasHanssenMacDonaldZhang.bantam.util.Error;
import proj10DouglasHanssenMacDonaldZhang.bantam.treedrawer.*;


/**
 * This class constructs an AST from a legal Bantam Java program.  If the
 * program is illegal, then one or more error messages are displayed.
 */
public class Parser
{
    // instance variables
    private Scanner scanner;
    private Token currentToken; // the lookahead token
    private ErrorHandler errorHandler;
    private String filename;

    // constructor
    public Parser(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }


    /**
     * parse the given file and return the root node of the AST
     * @param filename The name of the Bantam Java file to be parsed
     * @return The Program node forming the root of the AST generated by the parser
     */
    public Program parse(String filename) {
        this.filename = filename;
        errorHandler = new ErrorHandler();
        scanner = new Scanner(filename, errorHandler);
        currentToken = scanner.scan();
        Program program = this.parseProgram();

        return program;
    }


    /*
     * <Program> ::= <Class> | <Class> <Program>
     */
    private Program parseProgram() {
        int position = currentToken.position;
        ClassList classList = new ClassList(position);

        while (currentToken.kind != EOF) {
            Class_ aClass = parseClass();
            classList.addElement(aClass);
        }

        return new Program(position, classList);
    }


    /*
     * <Class> ::= CLASS <Identifier> <ExtendsClause> { <MemberList> }
     * <ExtendsClause> ::= EXTENDS <Identifier> | EMPTY
     * <MemberList> ::= EMPTY | <Member> <MemberList>
     */
    private Class_ parseClass() {
        int position = currentToken.position;
        String spelling = currentToken.getSpelling();
        MemberList memberList = new MemberList(position);

        String className = parseIdentifier();
        String parent = "";

        // move token on to check for extends clause
        this.currentToken = scanner.scan();
        if(this.currentToken.kind == EXTENDS) {
            this.currentToken = scanner.scan();

            parent = this.currentToken.spelling;

            // move onto class members
            this.currentToken = scanner.scan();
        }

        // while (currentToken.kind != EOF) {
        // while (/* there are still members */) {          ASK DALE
            Member member = parseMember();
            memberList.addElement(member);
        // }
        
        return new Class_(position, filename, className, parent, memberList);
    }


    /* Fields and Methods
     * <Member> ::= <Field> | <Method>
     * <Method> ::= <Type> <Identifier> ( <Parameters> ) <Block>
     * <Field> ::= <Type> <Identifier> <InitialValue> ;
     * <InitialValue> ::= EMPTY | = <Expression>
     */
    private Member parseMember() {
        int position = currentToken.position;

        String type = parseType();
        String id = parseIdentifier();
        if((currentToken = scanner.scan()).equals("(")) {
            FormalList params = parseParameters();
            StmtList stmnt = new StmtList(position);
            Method method = new Method(position, type, id, params, stmnt);
            return method;

        }
        else {
            currentToken = scanner.scan();
            String initValue = parseIdentifier();
            Expr expr;
            if((currentToken = scanner.scan()).equals("=")) {
                expr = parseExpression();
            }
            else {
                expr = null;
            }
            Field field = new Field(position, type, id, expr);
            return field;
        }
    }


    //-----------------------------------

    /* Statements
     *  <Stmt> ::= <WhileStmt> | <ReturnStmt> | <BreakStmt> | <DeclStmt>
     *              | <ExpressionStmt> | <ForStmt> | <BlockStmt> | <IfStmt>
     */
    private Stmt parseStatement() {
        Stmt stmt;

        switch (currentToken.kind) {
            case IF:
                stmt = parseIf();
                break;
            case LCURLY:
                stmt = parseBlock();
                break;
            case VAR:
                stmt = parseDeclStmt();
                break;
            case RETURN:
                stmt = parseReturn();
                break;
            case FOR:
                stmt = parseFor();
                break;
            case WHILE:
                stmt = parseWhile();
                break;
            case BREAK:
                stmt = parseBreak();
                break;
            default:
                stmt = parseExpressionStmt();
        }

        return stmt;
    }


    /*
     * <WhileStmt> ::= WHILE ( <Expression> ) <Stmt>
     */
    private Stmt parseWhile() {
        int position = currentToken.position;

        Expr expr = null;
        Stmt stmt = null;
        if( currentToken.kind != WHILE){
            String message = "Error in While";
            notifyErrorHandler(new Error(Error.Kind.PARSE_ERROR, filename, currentToken.position, message));
        }
        if((currentToken = scanner.scan()).equals("(")) {
            expr = parseExpression();
            if ((currentToken = scanner.scan()).equals(")")) {
                String message = "Error in While";
                notifyErrorHandler(new Error(Error.Kind.PARSE_ERROR, filename, currentToken.position, message));
            }
            stmt = parseStatement();
        }
        return new WhileStmt(position, expr, stmt);

    }

    /*
     * <ReturnStmt> ::= RETURN <Expression> ; | RETURN ;
     */
    private Stmt parseReturn() {
        int position = currentToken.position;

        if( currentToken.kind != RETURN) {
            String message = "Error in return";
            notifyErrorHandler(new Error(Error.Kind.PARSE_ERROR, filename, currentToken.position, message));
        }
        this.currentToken = scanner.scan();
        Expr expr = parseExpression();
        return new ReturnStmt(position, expr);

    }

    /*
     * BreakStmt> ::= BREAK ;
     */
    private Stmt parseBreak() {
        int position = currentToken.position;

        if( currentToken.kind != BREAK) {
            String message = "Error in break";
            notifyErrorHandler(new Error(Error.Kind.PARSE_ERROR, filename, currentToken.position, message));
        }
//        this.currentToken = scanner.scan();
        return new BreakStmt(position);
    }

    /*
     * <ExpressionStmt> ::= <Expression> ;
     */
    private ExprStmt parseExpressionStmt() {
        int position = currentToken.position;

        this.currentToken = scanner.scan();
        Expr expr = parseExpression();
        return new ExprStmt(position, expr);
    }


    /*
     * <DeclStmt> ::= VAR <Identifier> = <Expression> ;
     * every local variable must be initialized
     */
    private Stmt parseDeclStmt() {
        int position = currentToken.position;

        if( currentToken.kind != VAR){
            String message = "Error in DeclStmt";
            notifyErrorHandler(new Error(Error.Kind.PARSE_ERROR, filename, currentToken.position, message));
        }
        String name = parseIdentifier();
        if(!(currentToken = scanner.scan()).equals("=")) {
            String message = "Error in DeclStmt";
            notifyErrorHandler(new Error(Error.Kind.PARSE_ERROR, filename, currentToken.position, message));
        }
        parseOperator();
        currentToken = scanner.scan();
        Expr expr = parseExpression();
        return new DeclStmt(position, name, expr);

    }


    /*
     * <ForStmt> ::= FOR ( <Start> ; <Terminate> ; <Increment> ) <STMT>
     * <Start>     ::= EMPTY | <Expression>
     * <Terminate> ::= EMPTY | <Expression>
     * <Increment> ::= EMPTY | <Expression>
     */
    private Stmt parseFor() {
        int position = currentToken.position;
        Expr predExpr = null;
        Expr initExpr = null;
        Expr updateExpr = null;

        if( currentToken.kind != FOR){
            String message = "Error in parseFor";
            notifyErrorHandler(new Error(Error.Kind.PARSE_ERROR, filename, currentToken.position, message));
        }
        currentToken = scanner.scan();
        if (currentToken.spelling.equals("(")) {
            currentToken = scanner.scan();
            while ((currentToken.getSpelling() != ")")) {
                if((currentToken.getSpelling() != ";") & (predExpr == null)){
                    initExpr = parseExpression();
                }
                else{
                    if (predExpr == null) {
                        predExpr = parseExpression();
                    } else {
                        updateExpr = parseExpression();
                    }
                }
                currentToken = scanner.scan();
            }
        }

        Stmt stmt = parseStatement();
        return new ForStmt(position, initExpr, predExpr, updateExpr, stmt);

    }


    /*
     * <BlockStmt> ::= { <Body> }
     * <Body> ::= EMPTY | <Stmt> <Body>
     */
    private Stmt parseBlock() {
        int position = currentToken.position;

        if (this.currentToken.spelling.equals("{")) {
            if (currentToken.kind == null) {
                return null;
            }
            currentToken = scanner.scan();
            while ((this.currentToken.getSpelling() != "}")){
                Stmt stmt = parseStatement();
            }
        }

        StmtList list = new StmtList(position);
        return new BlockStmt(position, list);

    }


    /*
     * <IfStmt> ::= IF ( <Expr> ) <Stmt> | IF ( <Expr> ) <Stmt> ELSE <Stmt>
     */
    private Stmt parseIf() {
        int position = currentToken.position;

        Stmt elseStmt = null;
        Expr predExpr = null;
        Stmt thenStmt = null;

        if( currentToken.kind != IF){
            String message = "Error in parseIf";
            notifyErrorHandler(new Error(Error.Kind.PARSE_ERROR, filename, currentToken.position, message));
        }
        currentToken = scanner.scan();
        if (currentToken.spelling.equals("(")) {
            currentToken = scanner.scan();
            while (!(currentToken.spelling.equals(")"))) {
                predExpr = parseExpression();
            }
        }
        thenStmt = parseStatement();
        currentToken = scanner.scan();
        if (currentToken.kind == ELSE) {
            currentToken = scanner.scan();
            elseStmt = parseStatement();
        }

        return new IfStmt(position, predExpr, thenStmt, elseStmt);

    }



    //-----------------------------------------
    // Expressions
    //Here we introduce the precedence to operations

    /*
     * <Expression> ::= <LogicalOrExpr> <OptionalAssignment>
     * <OptionalAssignment> ::= EMPTY | = <Expression>
     */
    private Expr parseExpression() {
        int position = currentToken.position;

        Expr left = parseOrExpr();
        if((this.currentToken = scanner.scan()).equals("=")) {
            parseOperator();
            Expr right = parseExpression();
            left = new BinaryLogicOrExpr(position, left, right);
        }

        return left;
    }


    /*
     * <LogicalOR> ::= <logicalAND> <LogicalORRest>
     * <LogicalORRest> ::= EMPTY |  || <LogicalAND> <LogicalORRest>
     */
    private Expr parseOrExpr() {
        int position = currentToken.position;

        Expr left = parseAndExpr();
        while (this.currentToken.spelling.equals("||")) {
            this.currentToken = scanner.scan();
            Expr right = parseAndExpr();
            left = new BinaryLogicOrExpr(position, left, right);
        }

        return left;
    }


    /*
     * <LogicalAND> ::= <ComparisonExpr> <LogicalANDRest>
     * <LogicalANDRest> ::= EMPTY |  && <ComparisonExpr> <LogicalANDRest>
     */
    private Expr parseAndExpr() {
        int position = currentToken.position;

        Expr left = parseRelationalExpr();
        while (this.currentToken.spelling.equals("&&")) {
            this.currentToken = scanner.scan();
            Expr right = parseRelationalExpr();
            left = new BinaryLogicAndExpr(position, left, right);
        }

        return left;
    }


    /*
     * <ComparisonExpr> ::= <RelationalExpr> <equalOrNotEqual> <RelationalExpr> |
     *                     <RelationalExpr>
     * <equalOrNotEqual> ::=  == | !=
     */
    private Expr parseEqualityExpr() {
        int position = currentToken.position;

        Expr left = parseRelationalExpr();
        while (this.currentToken.spelling.equals("==") || this.currentToken.spelling.equals("!=")) {
            this.currentToken = scanner.scan();
            parseOperator();
            Expr right = parseRelationalExpr();
            left = new BinaryCompEqExpr(position, left, right);
        }

        return left;
    }


    /*
     * <RelationalExpr> ::=<AddExpr> | <AddExpr> <ComparisonOp> <AddExpr>
     * <ComparisonOp> ::=  < | > | <= | >= | INSTANCEOF
     */
	private Expr parseRelationalExpr() {
	    int position = currentToken.position;

	    Expr left = parseAddExpr();
//	    String op = parseOperator();
	    if((currentToken = scanner.scan()).kind == Token.Kind.COMPARE) {
//	        this.currentToken = scanner.scan();
	        parseOperator();
            Expr right = parseAddExpr();
            left = new BinaryCompEqExpr(position, left, right);
        }

        return left;
    }


    /*
     * <AddExpr>::＝ <MultExpr> <MoreMultExpr>
     * <MoreMultExpr> ::= EMPTY | + <MultExpr> <MoreMultExpr> | - <MultExpr> <MoreMultExpr>
     */
    private Expr parseAddExpr() {
        int position = currentToken.position;

        Expr left = parseMultExpr();

        if((this.currentToken = scanner.scan()).equals("+")) {
            Expr right = parseMultExpr();
            left = new BinaryArithPlusExpr(position, left, right);
        }
        else if((this.currentToken = scanner.scan()).equals("-")) {
            Expr right = parseMultExpr();
            left = new BinaryArithMinusExpr(position, left, right);
        }
        else {
            String message = "Did not find correct operator";
            notifyErrorHandler(new Error(Error.Kind.PARSE_ERROR, filename, currentToken.position, message));
        }

        return left;
    }


    /*
     * <MultiExpr> ::= <NewCastOrUnary> <MoreNCU>
     * <MoreNCU> ::= * <NewCastOrUnary> <MoreNCU> |
     *               / <NewCastOrUnary> <MoreNCU> |
     *               % <NewCastOrUnary> <MoreNCU> |
     *               EMPTY
     */
    private Expr parseMultExpr() {
        int position = currentToken.position;

        Expr left = parseNewCastOrUnary();
        if((this.currentToken = scanner.scan()).equals("*")) {
            Expr right = parseNewCastOrUnary();
            left = new BinaryArithPlusExpr(position, left, right);
        }
        else if((this.currentToken = scanner.scan()).equals("/")) {
            Expr right = parseNewCastOrUnary();
            left = new BinaryArithMinusExpr(position, left, right);
        }
        else if((this.currentToken = scanner.scan()).equals("%")) {
            Expr right = parseNewCastOrUnary();
            left = new BinaryArithMinusExpr(position, left, right);
        }
        else {
            // return error
        }
        return left;
    }

    /*
     * <NewCastOrUnary> ::= < NewExpression> | <CastExpression> | <UnaryPrefix>
     */
    private Expr parseNewCastOrUnary() {
        if(currentToken.kind == Token.Kind.NEW) {
            return parseNew();
        } else if(currentToken.kind == Token.Kind.CAST) {
            return parseCast();
        } else {
            return parseUnaryPrefix();
        }
    }


    /*
     * <NewExpression> ::= NEW <Identifier> ( ) | NEW <Identifier> [ <Expression> ]
     */
    private Expr parseNew() {
        int position = currentToken.position;

	    String id = parseIdentifier();
	    if ((currentToken = scanner.scan()).equals("[")) {
	        parseExpression();
	        if (!(currentToken = scanner.scan()).equals("]")) {
                String message = "No closing bracket";
                notifyErrorHandler(new Error(Error.Kind.PARSE_ERROR, filename, currentToken.position, message));
            }
        }
        if ((currentToken = scanner.scan()).equals("(")) {
            if (!(currentToken = scanner.scan()).equals(")")) {
                String message = "No closing parentheses";
                notifyErrorHandler(new Error(Error.Kind.PARSE_ERROR, filename, currentToken.position, message));
            }
        }
        Expr newExpr = new NewExpr(position, id);
        return newExpr;
    }


    /*
     * <CastExpression> ::= CAST ( <Type> , <Expression> )
     */
    private Expr parseCast() {
        int position = currentToken.position;
        String type = parseIdentifier();
        Expr expr = parseExpression();
        Expr castExpr = new CastExpr(position, type, expr);

        return castExpr;
    }


    /*
     * <UnaryPrefix> ::= <PrefixOp> <UnaryPrefix> | <UnaryPostfix>
     * <PrefixOp> ::= - | ! | ++ | --
     * (Postfix handles any variables, like !blah, in which case ! is a prefix op and blah is the unary prefix that = postfix)
     */
    private Expr parseUnaryPrefix() {
        int position = currentToken.position;
        String spelling = currentToken.getSpelling();

        // I'm confused here - UnaryExpr (and similar) classes
        // need to get passed an expr, but what is this expr??
        // Just a normal Expr, as it is below?
        //Additional note from Tia: if the expression is meant to store the operation, like ++ or /, why pass it in the constructor?
        //The operation won't change - a UnaryNegExpr will always contain -. Why not just have the unary expression class make it?

        //Expr operatorExpr = new Expr(position);

        // additionally, do I need to make a call to parseUnaryPrefix()
        // after (or instead of) creating a new object and returning that
        // object?
        if("-".equals(spelling)) {
            return new UnaryNegExpr(position, null);
        } else if("!".equals(spelling)) {
            return new UnaryNotExpr(position, null);
        } else if("++".equals(spelling)) {
            return new UnaryIncrExpr(position, null, false);
        } else if("--".equals(spelling)) {
            return new UnaryDecrExpr(position, null, false);
        } else {
            return parseUnaryPostfix();
        }
    }


    /*
     * <UnaryPostfix> ::= <Primary> <PostfixOp>
     * <PostfixOp> ::= ++ | -- | EMPTY
     */
    private Expr parseUnaryPostfix() {
        int position = currentToken.position;
        Expr primaryExpr = parsePrimary();

        this.currentToken = scanner.scan();

        if("++".equals(currentToken.getSpelling()) ) {
            return new UnaryIncrExpr(position, primaryExpr, true);
        } else if("--".equals(this.currentToken.spelling)){
            return new UnaryDecrExpr(position, primaryExpr, true);
        } else {
            return primaryExpr;
        }
    }



    /*
     * <Primary> ::= ( <Expression> ) | <IntegerConst> | <BooleanConst> |
     *                               <StringConst> | <VarExpr> | <DispatchExpr>
     * <VarExpr> ::= <VarExprPrefix> <Identifier> <VarExprSuffix>
     * <VarExprPrefix> ::= SUPER . | THIS . | EMPTY
     * <VarExprSuffix> ::= [ <Expr> ] | EMPTY
     * <DispatchExpr> ::= <DispatchExprPrefix> <Identifier> ( <Arguments> )
     * <DispatchExprPrefix> ::= <Primary> . | EMPTY
     */
    private Expr parsePrimary() {
        Expr expr;
        int position = currentToken.position;
        if( currentToken.kind == Token.Kind.LPAREN){
            expr = parseExpression();
            if(currentToken.kind != Token.Kind.RPAREN){
                String message = "Missing right parenthesis";
                notifyErrorHandler(new Error(Error.Kind.PARSE_ERROR, filename, position, message));
            }
        }
        else if(currentToken.kind == Token.Kind.INTCONST){
            expr = parseIntConst();
        }
        else if(currentToken.kind == Token.Kind.BOOLEAN){
            expr = parseBoolean();
        }
        else if(currentToken.kind == Token.Kind.STRCONST){
            expr = parseStringConst();
        }
        else if(currentToken.kind == Token.Kind.IDENTIFIER){
            expr = parseVarOrDispatchExpr();
        }

        else{
            String message = "This is not a primary";
            notifyErrorHandler(new Error(Error.Kind.PARSE_ERROR, filename, currentToken.position, message));
            expr = null;
        }

        if(currentToken.kind == Token.Kind.DOT){ //I'm assuming the other methods moved it to the next token
            String methodName = parseVarOrDispatchIdentifier();
            ExprList args = processDispatchArgs();
            expr = new DispatchExpr(position, expr, methodName, args);
        }
        return expr;

    }


    /*
     * <VarExpr> ::= <VarExprPrefix> <Identifier> <VarExprSuffix>
     * <VarExprPrefix> ::= SUPER . | THIS . | EMPTY
     * <VarExprSuffix> ::= [ <Expr> ] | EMPTY
     */
    private Expr parseVarOrDispatchExpr(){
        //First identifier will be "super" or "this" or neither
        // if neither, there is no prefix to the expression
        if( (currentToken.spelling.equals("super")) || (currentToken.spelling.equals("this"))){
            return parseVarExprWithPrefix();
        }
        else{ //If not "super" or "this", then the prefix is empty, either an empty DispatchExprPrefix or empty VarExprPrefix.
            return parseVarOrDispatchExprNoPrefix();
        }
    }

    private Expr parseVarExprWithPrefix() {
        String refVarName = currentToken.spelling;
        if ((scanner.scan()).kind != Token.Kind.DOT) { //"super" and "this" should both be followed by a dot
            String message = "Missing dot after reference name";
            notifyErrorHandler(new Error(Error.Kind.PARSE_ERROR, filename, currentToken.position, message));
            return null; //It throws an exception so returns nothing, the return is a moot pt but Java insists
        } else {
            currentToken = scanner.scan(); //Get the <identifier> after the dot
            String varName = parseVarOrDispatchIdentifier(); //How do I convert an id into an expression for ref?
            int position = currentToken.position;
            VarExpr referenceVar = new VarExpr(position, null, refVarName);
            Expr arrayIdx = parseVarSuffix(); //I don't know what to do with this, I think it's an array index
            if (arrayIdx != null) {
                return new ArrayExpr(position, referenceVar, varName, arrayIdx);
            } else {
                return new VarExpr(position, referenceVar, varName);
            }
        }
    }

    private String parseVarOrDispatchIdentifier(){
        if(currentToken.kind != Token.Kind.IDENTIFIER){
            String message = "Missing name of variable";
            notifyErrorHandler(new Error(Error.Kind.PARSE_ERROR, filename, currentToken.position, message));
            return null; //It throws an exception
        }
        else {
            return parseIdentifier(); //How do I convert an id into an expression for ref?
        }

    }

    private Expr parseVarOrDispatchExprNoPrefix(){
        String varName = parseIdentifier();
        Expr arrayIdx = parseVarSuffix(); //I don't know what to do with this, I think it's an array index
        int position = currentToken.position;
        if((currentToken = scanner.scan()).kind == Token.Kind.LPAREN) { //Then it's a DispatchExpr
            ExprList args = processDispatchArgs();
            return new DispatchExpr(position, arrayIdx, varName, args);
        }
        else{ //It's a VarExpr
            if(arrayIdx != null ){
                return new ArrayExpr(position, null, varName, arrayIdx);
            }
            else{
                return new VarExpr(position, null, varName);
            }
        }

    }

    /*
     * <VarExprSuffix> ::= [ <Expr> ] | EMPTY
     */
    private Expr parseVarSuffix(){
        //Move onto the suffix, which is either empty or has an expression in brackets.
        //I think [Expr] represents indexing into an array
        //If next token is not [, since it's been stored in currentToken, it shouldn't be lost
        if((currentToken = scanner.scan()).spelling.equals("[")){
            currentToken = scanner.scan();
            Expr expr = parseExpression();
            return expr;
        }
        else{
            return null;
        }
    }




    /*
     * Handles the case that it is a dispatch expression by handling its args
     * If not dispatch expression, will move the scanner onto the next token
     * <DispatchExpr> ::= <DispatchExprPrefix> <Identifier> ( <Arguments> )
     */
    private ExprList processDispatchArgs(){
        ExprList args = parseArguments(); //I don't know what to do with the arguments either
        if(scanner.scan().kind != Token.Kind.RPAREN) { //If it's not, then don't need to save the token anyways cause throw exception
            String message = "Missing right parenthesis";
            notifyErrorHandler(new Error(Error.Kind.PARSE_ERROR, filename, currentToken.position, message));
        }
        return args;
    }



    /*
     * <Arguments> ::= EMPTY | <Expression> <MoreArgs>
     * <MoreArgs>  ::= EMPTY | , <Expression> <MoreArgs>
     */
    private ExprList parseArguments() {
        if(currentToken.kind != Token.Kind.IDENTIFIER) return null;
        int position = currentToken.position;
        ExprList argList = new ExprList(position);
        Expr argExpression = parseExpression();
        argList.addElement(argExpression);
        while(currentToken.kind == Token.Kind.COMMA){
            argExpression = parseExpression();
            argList.addElement(argExpression);
        }
        return argList;
    }


    /*
     * <Parameters>  ::= EMPTY | <Formal> <MoreFormals>
     * <MoreFormals> ::= EMPTY | , <Formal> <MoreFormals
     */
    private FormalList parseParameters() {
        if(currentToken.kind != Token.Kind.IDENTIFIER) return null;
        int position = currentToken.position;
        FormalList paramList = new FormalList(position);
        Formal param = parseFormal();
        paramList.addElement(param);
        while(currentToken.kind == Token.Kind.COMMA){
            param = parseFormal();
            paramList.addElement(param);
        }
        return paramList;
    }


    /*
     * <Formal> ::= <Type> <Identifier>
     */
    private Formal parseFormal() {
        int position = currentToken.position;
        String type = parseType();
        return new Formal(position, type, currentToken.getSpelling());
    }


    /*
	 * <Type> ::= <Identifier> <Brackets>
     * <Brackets> ::= EMPTY | [ ]
     */
    //*/ //Tia commented out cause it's an error
    private String parseType() {
        String type = parseIdentifier();
        if(scanner.scan().kind == Token.Kind.LBRACKET ){
            type += "[";
            if(!(scanner.scan().kind == Token.Kind.RBRACKET)){
                String message = "Missing right bracket";
                notifyErrorHandler(new Error(Error.Kind.PARSE_ERROR, filename, currentToken.position, message)); //TODO REPLACE WITH ERROR REPORTING
            }
        }
        return type;
    }


    //----------------------------------------
    //Terminals

    private String parseOperator() {
        return currentToken.getSpelling();
    }


    private String parseIdentifier() {
        return currentToken.getSpelling();
    }

    private ConstStringExpr parseStringConst() {
        int position = currentToken.position;
        return new ConstStringExpr(position, currentToken.getSpelling());
    }

    private ConstIntExpr parseIntConst() {
        int position = currentToken.position;
        String spelling = currentToken.getSpelling();

        return new ConstIntExpr(position, spelling);
    }


    private ConstBooleanExpr parseBoolean() {
        int position = currentToken.position;
        String spelling = currentToken.getSpelling();

        return new ConstBooleanExpr(position, spelling);
    }

    /**
     * Call the register method in ErrorHandler to store the found error
     * @param error
     */
    private void notifyErrorHandler(Error error) throws CompilationException{
        this.errorHandler.register(error.getKind(), error.getFilename(),
                error.getLineNum(), error.getMessage());
        throw new CompilationException("There was an error while parsing");
    }

    public static void main(String args[]) {
        ErrorHandler errorHandler = new ErrorHandler();
        Parser parser = new Parser(errorHandler);
        Drawer drawer = new Drawer();

        for(int i = 0; i < args.length; i++) { //0 is the file name Parser
            Program program = parser.parse(args[i]);
            drawer.draw(args[i], program);
            System.out.println("Filename: " + args[i] + "\nFile size: " + errorHandler.getErrorList().size());
            errorHandler.clear();
        }
    }

}

