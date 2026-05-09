package com.codeescape.validation;

public class VariableThenIfValidator implements CodeValidator {
    private final VariableDeclarationValidator variableDeclarationValidator;
    private final IfStatementValidator ifStatementValidator;
    private final ConditionEvaluator conditionEvaluator;

    public VariableThenIfValidator() {
        variableDeclarationValidator = VariableDeclarationValidator.getInstance();
        ifStatementValidator = new IfStatementValidator();
        conditionEvaluator = new ConditionEvaluator();
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

        ValidationResult declarationResult = variableDeclarationValidator.validate(declaration);
        if (!declarationResult.isValid()) {
            return declarationResult;
        }
        if (!VariableDeclarationValidator.checkNameOfVariable("x").equals("int")) {
            return ValidationResult.failure("Declare int x first.");
        }

        ValidationResult ifResult = ifStatementValidator.validate(ifStatement);
        if (!ifResult.isValid()) {
            return ifResult;
        }

        String condition = conditionEvaluator.extractCondition(ifStatement);
        if (!conditionEvaluator.usesVariable(condition, "x")) {
            return ValidationResult.failure("The if-statement must use x.");
        }

        ValidationResult conditionResult = conditionEvaluator.evaluateCondition(condition);
        if (!conditionResult.isValid()) {
            return ValidationResult.failure("The if-statement must be true.");
        }

        return ValidationResult.success("Correct two-line solution.");
    }
}
