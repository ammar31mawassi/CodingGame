package com.codeescape.validation;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import java.util.Optional;

public class VariableThenPrintValidator implements CodeValidator {
    private final String expectedType;
    private final String expectedName;

    public VariableThenPrintValidator(String expectedType, String expectedName) {
        this.expectedType = expectedType;
        this.expectedName = expectedName;
    }

    @Override
    public ValidationResult validate(String code) {
        Optional<BlockStmt> block = JavaSyntaxValidator.parseBlockAst(code);
        if (block.isEmpty() || block.get().getStatements().size() != 2) {
            return ValidationResult.failure("Declare the variable, then print it.");
        }

        Statement declarationStatement = block.get().getStatement(0);
        if (!declarationStatement.isExpressionStmt()) {
            return ValidationResult.failure("Start with a variable declaration.");
        }

        Expression declarationExpression = declarationStatement.asExpressionStmt().getExpression();
        if (!declarationExpression.isVariableDeclarationExpr()
                || declarationExpression.asVariableDeclarationExpr().getVariables().size() != 1) {
            return ValidationResult.failure("Start with one variable declaration.");
        }

        VariableDeclarator variable = declarationExpression.asVariableDeclarationExpr().getVariables().get(0);
        if (!variable.getType().asString().equals(expectedType)
                || !variable.getNameAsString().equals(expectedName)) {
            return ValidationResult.failure("Declare " + expectedType + " " + expectedName + " first.");
        }

        VariableDeclarationValidator.resetVariables();
        ValidationResult declarationResult = VariableDeclarationValidator.getInstance().validate(declarationStatement.toString());
        if (!declarationResult.isValid()) {
            return declarationResult;
        }

        ValidationResult printResult = new PrintStatementValidator(expectedName).validate(block.get().getStatement(1).toString());
        if (!printResult.isValid()) {
            return ValidationResult.failure("Print the variable named " + expectedName + ".");
        }

        return ValidationResult.success("Correct variable and print code.");
    }
}
