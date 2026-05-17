package com.codeescape.engine;

import com.codeescape.validation.ClassConstructorMethodValidator;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PracticeDrillEvaluator {
    private PracticeDrillEvaluator() {
    }

    public static boolean matches(PracticePrompt prompt, String submission) {
        if (prompt == null || submission == null) {
            return false;
        }

        Boolean semanticMatch = switch (prompt.notebookEntryId()) {
            case "variable-declaration" -> matchesVariableDeclaration(submission);
            case "print-statement" -> matchesPrintStatement(prompt, submission);
            case "if-block" -> matchesIfBlock(prompt, submission);
            case "if-else-branch" -> matchesIfElseBranch(prompt, submission);
            case "string-char" -> matchesStringChar(prompt, submission);
            case "void-method" -> matchesVoidMethod(prompt, submission);
            case "return-method" -> matchesReturnMethod(prompt, submission);
            case "while-loop" -> matchesWhileLoop(prompt, submission);
            case "for-loop" -> matchesForLoop(prompt, submission);
            case "class-fields" -> matchesClassFields(prompt, submission);
            case "constructor-method" -> matchesConstructorMethod(prompt, submission);
            case "object-call" -> matchesObjectCall(prompt, submission);
            default -> null;
        };
        if (semanticMatch != null) {
            return semanticMatch;
        }

        String normalizedSubmission = normalize(submission);
        return prompt.acceptedAnswers().stream()
                .map(PracticeDrillEvaluator::normalize)
                .anyMatch(normalizedSubmission::equals);
    }

    private static boolean matchesVariableDeclaration(String code) {
        return parseStatement(code)
                .filter(Statement::isExpressionStmt)
                .map(statement -> statement.asExpressionStmt().getExpression())
                .filter(Expression::isVariableDeclarationExpr)
                .map(Expression::asVariableDeclarationExpr)
                .filter(declaration -> declaration.getVariables().size() == 1)
                .map(declaration -> declaration.getVariable(0))
                .filter(variable -> "int".equals(variable.getType().asString()))
                .filter(variable -> "keys".equals(variable.getNameAsString()))
                .filter(variable -> variable.getInitializer().isPresent())
                .map(variable -> isIntegerValue(variable.getInitializer().get()))
                .orElse(false);
    }

    private static boolean matchesPrintStatement(PracticePrompt prompt, String code) {
        String expected = prompt.title().contains("Debug") ? "Gate open" : "Door unlocked";
        return parseStatement(code)
                .filter(Statement::isExpressionStmt)
                .map(statement -> statement.asExpressionStmt().getExpression())
                .filter(Expression::isMethodCallExpr)
                .map(Expression::asMethodCallExpr)
                .map(call -> isSystemPrintln(call, expected))
                .orElse(false);
    }

    private static boolean matchesIfBlock(PracticePrompt prompt, String code) {
        int limit = prompt.title().contains("Debug") ? 7 : 5;
        return parseStatement(code)
                .filter(Statement::isIfStmt)
                .map(Statement::asIfStmt)
                .filter(ifStmt -> ifStmt.getElseStmt().isEmpty())
                .filter(ifStmt -> isComparison(ifStmt.getCondition(), "score", BinaryExpr.Operator.GREATER, Integer.toString(limit)))
                .map(ifStmt -> blockPrints(ifStmt.getThenStmt(), "open"))
                .orElse(false);
    }

    private static boolean matchesIfElseBranch(PracticePrompt prompt, String code) {
        boolean debug = prompt.title().contains("Debug");
        String variable = debug ? "health" : "energy";
        String limit = debug ? "3" : "0";
        BinaryExpr.Operator operator = debug ? BinaryExpr.Operator.LESS : BinaryExpr.Operator.GREATER;
        String trueText = debug ? "heal" : "move";
        String falseText = debug ? "hold" : "rest";
        return parseStatement(code)
                .filter(Statement::isIfStmt)
                .map(Statement::asIfStmt)
                .filter(ifStmt -> isComparison(ifStmt.getCondition(), variable, operator, limit))
                .filter(ifStmt -> blockPrints(ifStmt.getThenStmt(), trueText))
                .map(ifStmt -> ifStmt.getElseStmt().filter(elseStmt -> blockPrints(elseStmt, falseText)).isPresent())
                .orElse(false);
    }

    private static boolean matchesStringChar(PracticePrompt prompt, String code) {
        boolean debug = prompt.title().contains("Debug");
        String stringName = debug ? "title" : "playerName";
        String charName = debug ? "rank" : "grade";
        return parseBlock(code)
                .map(block -> hasVariable(block, "String", stringName, Expression::isStringLiteralExpr)
                        && hasVariable(block, "char", charName, Expression::isCharLiteralExpr))
                .orElse(false);
    }

    private static boolean matchesVoidMethod(PracticePrompt prompt, String code) {
        boolean debug = prompt.title().contains("Debug");
        String methodName = debug ? "alarm" : "cheer";
        String expectedText = debug ? "Run!" : "Go!";
        return parseBodyDeclaration(code)
                .filter(body -> body instanceof MethodDeclaration)
                .map(body -> (MethodDeclaration) body)
                .filter(method -> methodName.equals(method.getNameAsString()))
                .filter(method -> method.getType().isVoidType())
                .filter(method -> method.getParameters().isEmpty())
                .flatMap(MethodDeclaration::getBody)
                .map(body -> body.findAll(MethodCallExpr.class).stream().anyMatch(call -> isSystemPrintln(call, expectedText)))
                .orElse(false);
    }

    private static boolean matchesReturnMethod(PracticePrompt prompt, String code) {
        boolean debug = prompt.title().contains("Debug");
        String methodName = debug ? "livesLeft" : "bonus";
        String expectedValue = debug ? "1" : "2";
        return parseBodyDeclaration(code)
                .filter(body -> body instanceof MethodDeclaration)
                .map(body -> (MethodDeclaration) body)
                .filter(method -> methodName.equals(method.getNameAsString()))
                .filter(method -> "int".equals(method.getType().asString()))
                .filter(method -> method.getParameters().isEmpty())
                .flatMap(MethodDeclaration::getBody)
                .map(body -> body.findAll(ReturnStmt.class).stream()
                        .anyMatch(returnStmt -> returnStmt.getExpression()
                                .map(expression -> expectedValue.equals(expression.toString()))
                                .orElse(false)))
                .orElse(false);
    }

    private static boolean matchesWhileLoop(PracticePrompt prompt, String code) {
        boolean debug = prompt.title().contains("Debug");
        String variable = debug ? "energy" : "count";
        String initial = debug ? "1" : "0";
        String limit = debug ? "4" : "3";
        return parseBlock(code)
                .filter(block -> block.getStatements().size() == 2)
                .filter(block -> isIntDeclaration(block.getStatement(0), variable, initial))
                .map(block -> block.getStatement(1))
                .filter(Statement::isWhileStmt)
                .map(Statement::asWhileStmt)
                .filter(whileStmt -> isComparison(whileStmt.getCondition(), variable, BinaryExpr.Operator.LESS, limit))
                .map(whileStmt -> bodyIncrements(whileStmt.getBody(), variable))
                .orElse(false);
    }

    private static boolean matchesForLoop(PracticePrompt prompt, String code) {
        boolean debug = prompt.title().contains("Debug");
        String variable = debug ? "step" : "i";
        String initial = debug ? "1" : "0";
        String limit = debug ? "3" : "3";
        BinaryExpr.Operator operator = debug ? BinaryExpr.Operator.LESS_EQUALS : BinaryExpr.Operator.LESS;
        return parseStatement(code)
                .filter(Statement::isForStmt)
                .map(Statement::asForStmt)
                .filter(forStmt -> forStmt.getInitialization().size() == 1)
                .filter(forStmt -> isIntDeclaration(forStmt.getInitialization().get(0), variable, initial))
                .filter(forStmt -> forStmt.getCompare().filter(compare -> isComparison(compare, variable, operator, limit)).isPresent())
                .filter(forStmt -> forStmt.getUpdate().stream().anyMatch(update -> isIncrement(update, variable)))
                .map(forStmt -> bodyPrintsExpression(forStmt.getBody(), variable))
                .orElse(false);
    }

    private static boolean matchesClassFields(PracticePrompt prompt, String code) {
        boolean debug = prompt.title().contains("Debug");
        String className = debug ? "Potion" : "Chest";
        Map<String, String> fields = debug
                ? Map.of("label", "String", "charges", "int")
                : Map.of("name", "String", "coins", "int");
        return parseClass(code)
                .filter(declaration -> className.equals(declaration.getNameAsString()))
                .map(declaration -> hasFieldsOnly(declaration, fields))
                .orElse(false);
    }

    private static boolean matchesConstructorMethod(PracticePrompt prompt, String code) {
        if (!prompt.title().contains("Debug")) {
            return new ClassConstructorMethodValidator().validate(code).isValid();
        }

        return parseClass(code)
                .filter(declaration -> "Door".equals(declaration.getNameAsString()))
                .filter(declaration -> hasFields(declaration, Map.of("code", "String", "uses", "int")))
                .filter(PracticeDrillEvaluator::hasDoorConstructor)
                .map(PracticeDrillEvaluator::hasUnlockMethod)
                .orElse(false);
    }

    private static boolean matchesObjectCall(PracticePrompt prompt, String code) {
        boolean debug = prompt.title().contains("Debug");
        String type = debug ? "Chest" : "Item";
        String variable = debug ? "loot" : "key";
        String method = debug ? "open" : "use";
        return parseBlock(code)
                .filter(block -> block.getStatements().size() == 2)
                .filter(block -> isObjectCreation(block.getStatement(0), type, variable))
                .map(block -> block.getStatement(1))
                .filter(Statement::isExpressionStmt)
                .map(statement -> statement.asExpressionStmt().getExpression())
                .filter(Expression::isMethodCallExpr)
                .map(Expression::asMethodCallExpr)
                .map(call -> method.equals(call.getNameAsString())
                        && call.getScope().map(scope -> variable.equals(scope.toString())).orElse(false)
                        && call.getArguments().isEmpty())
                .orElse(false);
    }

    private static boolean hasVariable(BlockStmt block, String type, String name, java.util.function.Predicate<Expression> initializerCheck) {
        return block.getStatements().stream()
                .filter(Statement::isExpressionStmt)
                .map(statement -> statement.asExpressionStmt().getExpression())
                .filter(Expression::isVariableDeclarationExpr)
                .map(Expression::asVariableDeclarationExpr)
                .flatMap(declaration -> declaration.getVariables().stream())
                .anyMatch(variable -> type.equals(variable.getType().asString())
                        && name.equals(variable.getNameAsString())
                        && variable.getInitializer().filter(initializerCheck).isPresent());
    }

    private static boolean hasFieldsOnly(ClassOrInterfaceDeclaration declaration, Map<String, String> expectedFields) {
        List<FieldDeclaration> fields = declaration.getFields();
        return fields.size() == expectedFields.size()
                && declaration.getMembers().size() == fields.size()
                && hasFields(declaration, expectedFields);
    }

    private static boolean hasFields(ClassOrInterfaceDeclaration declaration, Map<String, String> expectedFields) {
        for (Map.Entry<String, String> expected : expectedFields.entrySet()) {
            boolean found = declaration.getFields().stream()
                    .flatMap(field -> field.getVariables().stream())
                    .anyMatch(variable -> expected.getKey().equals(variable.getNameAsString())
                            && expected.getValue().equals(variable.getType().asString()));
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasDoorConstructor(ClassOrInterfaceDeclaration declaration) {
        return declaration.getConstructors().stream().anyMatch(constructor ->
                "Door".equals(constructor.getNameAsString())
                        && constructor.getParameters().size() == 1
                        && "String".equals(constructor.getParameter(0).getType().asString())
                        && "code".equals(constructor.getParameter(0).getNameAsString())
                        && constructor.getBody().findAll(AssignExpr.class).stream().anyMatch(assignment ->
                        assignment.getOperator() == AssignExpr.Operator.ASSIGN
                                && assignment.getTarget().toString().equals("this.code")
                                && assignment.getValue().toString().equals("code")));
    }

    private static boolean hasUnlockMethod(ClassOrInterfaceDeclaration declaration) {
        return declaration.getMethodsByName("unlock").stream().anyMatch(method ->
                method.getType().isVoidType()
                        && method.getParameters().isEmpty()
                        && method.getBody().map(body -> body.findAll(Expression.class).stream()
                                .anyMatch(expression -> isIncrement(expression, "uses")
                                        || expression instanceof AssignExpr assignment
                                        && assignment.getTarget().toString().equals("uses")
                                        && assignment.getValue().isBinaryExpr()
                                        && assignment.getValue().asBinaryExpr().getOperator() == BinaryExpr.Operator.PLUS
                                        && assignment.getValue().asBinaryExpr().getLeft().toString().equals("uses")
                                        && assignment.getValue().asBinaryExpr().getRight().toString().equals("1")))
                        .orElse(false));
    }

    private static boolean isObjectCreation(Statement statement, String type, String variableName) {
        if (!statement.isExpressionStmt()) {
            return false;
        }
        Expression expression = statement.asExpressionStmt().getExpression();
        if (!expression.isVariableDeclarationExpr()) {
            return false;
        }
        VariableDeclarationExpr declaration = expression.asVariableDeclarationExpr();
        if (declaration.getVariables().size() != 1) {
            return false;
        }
        VariableDeclarator variable = declaration.getVariable(0);
        if (!type.equals(variable.getType().asString()) || !variableName.equals(variable.getNameAsString())) {
            return false;
        }
        return variable.getInitializer()
                .filter(Expression::isObjectCreationExpr)
                .map(Expression::asObjectCreationExpr)
                .map(creation -> type.equals(creation.getType().asString()))
                .orElse(false);
    }

    private static boolean isIntDeclaration(Statement statement, String name, String value) {
        if (!statement.isExpressionStmt()) {
            return false;
        }
        return isIntDeclaration(statement.asExpressionStmt().getExpression(), name, value);
    }

    private static boolean isIntDeclaration(Expression expression, String name, String value) {
        if (!expression.isVariableDeclarationExpr()) {
            return false;
        }
        VariableDeclarationExpr declaration = expression.asVariableDeclarationExpr();
        if (declaration.getVariables().size() != 1) {
            return false;
        }
        VariableDeclarator variable = declaration.getVariable(0);
        return "int".equals(variable.getType().asString())
                && name.equals(variable.getNameAsString())
                && variable.getInitializer().map(initializer -> value.equals(initializer.toString())).orElse(false);
    }

    private static boolean blockPrints(Statement statement, String expectedText) {
        return statement.findAll(MethodCallExpr.class).stream()
                .anyMatch(call -> isSystemPrintln(call, expectedText));
    }

    private static boolean bodyPrintsExpression(Statement statement, String expectedExpression) {
        return statement.findAll(MethodCallExpr.class).stream()
                .anyMatch(call -> call.getNameAsString().equals("println")
                        && call.getScope().map(scope -> scope.toString().equals("System.out")).orElse(false)
                        && call.getArguments().size() == 1
                        && expectedExpression.equals(call.getArgument(0).toString()));
    }

    private static boolean bodyIncrements(Statement statement, String variable) {
        return statement.findAll(Expression.class).stream()
                .anyMatch(expression -> isIncrement(expression, variable));
    }

    private static boolean isSystemPrintln(MethodCallExpr call, String expectedText) {
        return call.getNameAsString().equals("println")
                && call.getScope().map(scope -> scope.toString().equals("System.out")).orElse(false)
                && call.getArguments().size() == 1
                && call.getArgument(0).isStringLiteralExpr()
                && expectedText.equals(call.getArgument(0).asStringLiteralExpr().asString());
    }

    private static boolean isComparison(Expression expression, String variable, BinaryExpr.Operator operator, String value) {
        Expression condition = unwrap(expression);
        return condition.isBinaryExpr()
                && condition.asBinaryExpr().getOperator() == operator
                && variable.equals(condition.asBinaryExpr().getLeft().toString())
                && value.equals(condition.asBinaryExpr().getRight().toString());
    }

    private static boolean isIncrement(Expression expression, String variable) {
        if (expression instanceof UnaryExpr unary) {
            return variable.equals(unary.getExpression().toString())
                    && (unary.getOperator() == UnaryExpr.Operator.POSTFIX_INCREMENT
                    || unary.getOperator() == UnaryExpr.Operator.PREFIX_INCREMENT);
        }
        if (expression instanceof AssignExpr assignment) {
            return assignment.getOperator() == AssignExpr.Operator.PLUS
                    && variable.equals(assignment.getTarget().toString())
                    && "1".equals(assignment.getValue().toString());
        }
        return false;
    }

    private static boolean isIntegerValue(Expression expression) {
        Expression value = unwrap(expression);
        if (value.isIntegerLiteralExpr()) {
            return true;
        }
        return value instanceof UnaryExpr unary
                && unary.getOperator() == UnaryExpr.Operator.MINUS
                && unary.getExpression().isIntegerLiteralExpr();
    }

    private static Expression unwrap(Expression expression) {
        if (expression.isEnclosedExpr()) {
            return unwrap(expression.asEnclosedExpr().getInner());
        }
        return expression;
    }

    private static Optional<Statement> parseStatement(String code) {
        try {
            return Optional.of(StaticJavaParser.parseStatement(code.trim()));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private static Optional<BlockStmt> parseBlock(String code) {
        try {
            return Optional.of(StaticJavaParser.parseBlock("{\n" + code.trim() + "\n}"));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private static Optional<BodyDeclaration<?>> parseBodyDeclaration(String code) {
        try {
            return Optional.of(StaticJavaParser.parseBodyDeclaration(code.trim()));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private static Optional<ClassOrInterfaceDeclaration> parseClass(String code) {
        try {
            CompilationUnit compilationUnit = StaticJavaParser.parse(code.trim());
            if (compilationUnit.getTypes().size() != 1 || !(compilationUnit.getType(0) instanceof ClassOrInterfaceDeclaration declaration)) {
                return Optional.empty();
            }
            return declaration.isInterface() ? Optional.empty() : Optional.of(declaration);
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private static String normalize(String code) {
        return code == null ? "" : code.replaceAll("\\s+", "");
    }
}
