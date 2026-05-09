package com.codeescape.validation;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import java.util.Optional;

public class ObjectCreateAndCallValidator implements CodeValidator {
    @Override
    public ValidationResult validate(String code) {
        Optional<BlockStmt> block = JavaSyntaxValidator.parseBlockAst(code);
        if (block.isEmpty() || block.get().getStatements().size() != 2) {
            return ValidationResult.failure("Create one Item object, then call use on it.");
        }

        Optional<String> variableName = readItemVariable(block.get().getStatement(0));
        if (variableName.isEmpty()) {
            return ValidationResult.failure("Start with Item key = new Item(\"key\");");
        }
        if (!callsUseOnVariable(block.get().getStatement(1), variableName.get())) {
            return ValidationResult.failure("Call use() on the object you created.");
        }

        return ValidationResult.success("Valid object creation and method call");
    }

    private Optional<String> readItemVariable(Statement statement) {
        if (!statement.isExpressionStmt()) {
            return Optional.empty();
        }

        Expression expression = statement.asExpressionStmt().getExpression();
        if (!(expression instanceof VariableDeclarationExpr declaration)
                || declaration.getVariables().size() != 1) {
            return Optional.empty();
        }

        VariableDeclarator variable = declaration.getVariable(0);
        if (!"Item".equals(variable.getType().asString())
                || variable.getInitializer().isEmpty()
                || !(variable.getInitializer().get() instanceof ObjectCreationExpr objectCreation)
                || !"Item".equals(objectCreation.getType().asString())
                || objectCreation.getArguments().size() != 1
                || !objectCreation.getArgument(0).isStringLiteralExpr()
                || !"key".equals(objectCreation.getArgument(0).asStringLiteralExpr().asString())) {
            return Optional.empty();
        }

        return Optional.of(variable.getNameAsString());
    }

    private boolean callsUseOnVariable(Statement statement, String variableName) {
        if (!statement.isExpressionStmt()) {
            return false;
        }

        Expression expression = statement.asExpressionStmt().getExpression();
        return expression instanceof MethodCallExpr methodCall
                && "use".equals(methodCall.getNameAsString())
                && methodCall.getArguments().isEmpty()
                && methodCall.getScope().isPresent()
                && methodCall.getScope().get().isNameExpr()
                && variableName.equals(methodCall.getScope().get().asNameExpr().getNameAsString());
    }
}
