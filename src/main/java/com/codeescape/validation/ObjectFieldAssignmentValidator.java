package com.codeescape.validation;

import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.stmt.Statement;
import java.util.Optional;

public class ObjectFieldAssignmentValidator implements CodeValidator {
    @Override
    public ValidationResult validate(String code) {
        Optional<Statement> statement = JavaSyntaxValidator.parseStatementAst(code);
        if (statement.isEmpty() || !statement.get().isExpressionStmt()) {
            return ValidationResult.failure("Set the ironChest locked field to false.");
        }

        Expression expression = statement.get().asExpressionStmt().getExpression();
        if (!(expression instanceof AssignExpr assignment)
                || assignment.getOperator() != AssignExpr.Operator.ASSIGN
                || !isIronChestLocked(assignment.getTarget())
                || !assignment.getValue().isBooleanLiteralExpr()
                || assignment.getValue().asBooleanLiteralExpr().getValue()) {
            return ValidationResult.failure("Use: ironChest.locked = false;");
        }

        return ValidationResult.success("ironChest unlocked");
    }

    private boolean isIronChestLocked(Expression expression) {
        return expression instanceof FieldAccessExpr fieldAccess
                && "locked".equals(fieldAccess.getNameAsString())
                && fieldAccess.getScope().isNameExpr()
                && "ironChest".equals(fieldAccess.getScope().asNameExpr().getNameAsString());
    }
}
