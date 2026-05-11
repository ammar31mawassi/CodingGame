package com.codeescape.validation;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.Statement;
import java.util.Optional;

public class ForLoopValidator implements CodeValidator {
    private final String variableName;
    private final String limit;
    private final String expectedPrintArgument;

    public ForLoopValidator(String variableName, String limit) {
        this(variableName, limit, null);
    }

    public ForLoopValidator(String variableName, String limit, String expectedPrintArgument) {
        this.variableName = variableName;
        this.limit = limit;
        this.expectedPrintArgument = expectedPrintArgument;
    }

    @Override
    public ValidationResult validate(String code) {
        Optional<Statement> statement = JavaSyntaxValidator.parseStatementAst(code);
        if (statement.isEmpty() || !statement.get().isForStmt()) {
            return ValidationResult.failure("Build a for loop.");
        }

        ForStmt forStatement = statement.get().asForStmt();
        if (!forStatement.getBody().isBlockStmt()) {
            return ValidationResult.failure("Put the for body inside braces.");
        }
        if (!hasExpectedInitialization(forStatement)
                || !hasExpectedCondition(forStatement)
                || !hasExpectedUpdate(forStatement)) {
            return ValidationResult.failure("Use for (int " + variableName + " = 0; "
                    + variableName + " < " + limit + "; " + variableName + "++).");
        }
        if (expectedPrintArgument != null && !printsExpectedArgument(forStatement)) {
            return ValidationResult.failure("Print the expected value inside the loop.");
        }

        return ValidationResult.success("Correct for loop.");
    }

    private boolean hasExpectedInitialization(ForStmt forStatement) {
        if (forStatement.getInitialization().size() != 1) {
            return false;
        }
        Expression initialization = forStatement.getInitialization().get(0);
        if (!(initialization instanceof VariableDeclarationExpr declaration)
                || declaration.getVariables().size() != 1) {
            return false;
        }

        var variable = declaration.getVariables().get(0);
        return variable.getType().asString().equals("int")
                && variable.getNameAsString().equals(variableName)
                && variable.getInitializer().map(Expression::toString).orElse("").equals("0");
    }

    private boolean hasExpectedCondition(ForStmt forStatement) {
        if (forStatement.getCompare().isEmpty()) {
            return false;
        }

        Expression condition = unwrap(forStatement.getCompare().get());
        return condition.isBinaryExpr()
                && condition.asBinaryExpr().getOperator() == BinaryExpr.Operator.LESS
                && condition.asBinaryExpr().getLeft().toString().equals(variableName)
                && condition.asBinaryExpr().getRight().toString().equals(limit);
    }

    private boolean hasExpectedUpdate(ForStmt forStatement) {
        return forStatement.getUpdate().stream().anyMatch(update ->
                update instanceof UnaryExpr unary
                        && unary.getExpression().toString().equals(variableName)
                        && (unary.getOperator() == UnaryExpr.Operator.POSTFIX_INCREMENT
                        || unary.getOperator() == UnaryExpr.Operator.PREFIX_INCREMENT));
    }

    private boolean printsExpectedArgument(ForStmt forStatement) {
        return forStatement.getBody().findAll(MethodCallExpr.class).stream()
                .anyMatch(methodCall -> methodCall.getNameAsString().equals("println")
                        && methodCall.getScope().map(scope -> scope.toString().equals("System.out")).orElse(false)
                        && methodCall.getArguments().size() == 1
                        && methodCall.getArgument(0).toString().equals(expectedPrintArgument));
    }

    private Expression unwrap(Expression expression) {
        if (expression.isEnclosedExpr()) {
            return unwrap(expression.asEnclosedExpr().getInner());
        }
        return expression;
    }
}
