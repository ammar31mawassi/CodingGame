package com.codeescape.validation;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.Statement;
import java.util.Optional;

public class PrintStatementValidator implements CodeValidator {
    private final String expectedArgument;

    public PrintStatementValidator() {
        this(null);
    }

    public PrintStatementValidator(String expectedArgument) {
        this.expectedArgument = expectedArgument;
    }

    @Override
    public ValidationResult validate(String code) {
        Optional<Statement> statement = JavaSyntaxValidator.parseStatementAst(code);
        if (statement.isEmpty() || !statement.get().isExpressionStmt()) {
            return ValidationResult.failure("Build one print statement.");
        }

        Expression expression = statement.get().asExpressionStmt().getExpression();
        if (!isExpectedPrintCall(expression)) {
            return ValidationResult.failure("Use System.out.println with the expected value.");
        }

        return ValidationResult.success("Correct print statement.");
    }

    boolean isExpectedPrintCall(Expression expression) {
        if (!(expression instanceof MethodCallExpr methodCall)) {
            return false;
        }
        if (!methodCall.getNameAsString().equals("println")
                || methodCall.getScope().map(scope -> !scope.toString().equals("System.out")).orElse(true)
                || methodCall.getArguments().size() != 1) {
            return false;
        }

        return expectedArgument == null || methodCall.getArgument(0).toString().equals(expectedArgument);
    }
}
