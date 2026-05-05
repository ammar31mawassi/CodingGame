package com.codeescape.validation;

import java.util.regex.Pattern;

public class VariableThenIfValidator implements CodeValidator {
    private static final Pattern REQUIRED_DECLARATION = Pattern.compile("^int\\s+x\\s*=\\s*5\\s*;$");
    private static final Pattern REQUIRED_TRUE_IF = Pattern.compile("^if\\s*\\(\\s*x\\s*>\\s*3\\s*\\)\\s*\\{\\s*}$");

    private final VariableDeclarationValidator variableDeclarationValidator;
    private final IfStatementValidator ifStatementValidator;

    public VariableThenIfValidator() {
        variableDeclarationValidator = VariableDeclarationValidator.getInstance();
        ifStatementValidator = new IfStatementValidator();
    }

    @Override
    public ValidationResult validate(String code) {
        if (code == null || code.isBlank()) {
            return ValidationResult.failure("Build a declaration and an if-statement.");
        }

        VariableDeclarationValidator.resetVariables();

        String trimmedCode = code.trim();
        int declarationEnd = trimmedCode.indexOf(';');
        if (declarationEnd < 0) {
            return ValidationResult.failure("The variable declaration needs to end with ;");
        }

        String declaration = trimmedCode.substring(0, declarationEnd + 1).trim();
        String ifStatement = trimmedCode.substring(declarationEnd + 1).trim();
        if (ifStatement.isBlank()) {
            return ValidationResult.failure("Add an if-statement after the declaration.");
        }

        if (!REQUIRED_DECLARATION.matcher(declaration).matches()) {
            return ValidationResult.failure("Declare int x = 5; first.");
        }

        ValidationResult declarationResult = variableDeclarationValidator.validate(declaration);
        if (!declarationResult.isValid()) {
            return declarationResult;
        }

        if (!REQUIRED_TRUE_IF.matcher(ifStatement).matches()) {
            return ValidationResult.failure("Use a true if-statement with x > 3.");
        }

        ValidationResult ifResult = ifStatementValidator.validate(ifStatement);
        if (!ifResult.isValid()) {
            return ifResult;
        }

        return ValidationResult.success("Correct two-line solution.");
    }
}
