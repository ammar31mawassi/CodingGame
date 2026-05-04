package com.codeescape.validation;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IfStatementValidator implements CodeValidator {
    private static final Set<String> VALID_OPERATORS = Set.of("==", "!=", ">", ">=", "<", "<=");
    private static final Pattern IF_STATEMENT_PATTERN = Pattern.compile("^if\\s*\\((.*)\\)\\s*\\{\\s*}$");

    @Override
    public ValidationResult validate(String code) {
        if (!hasValidStructure(code)) return ValidationResult.failure("Invalid structure");

        String trimmedCode = code.trim();
        Matcher matcher = IF_STATEMENT_PATTERN.matcher(trimmedCode);
        matcher.matches();

        String condition = matcher.group(1).trim();
        if (!isValidCondition(condition)) return ValidationResult.failure("Invalid condition");
        String[] condTokens = condition.split("\\s+");
        if (condTokens.length != 3) {
            if (condTokens.length == 1){
                VariableDeclarationValidator.getInstance();
                String varName = VariableDeclarationValidator.checkNameOfVariable(condTokens[0]);
                if (varName.equals("boolean")) return ValidationResult.success("Valid if");
                else return ValidationResult.failure("Invalid variable in the condition");
            }
            return ValidationResult.failure("Invalid token");
        }
        if (!isValidOperator(condTokens[1]) || condTokens[0].isBlank() || condTokens[2].isBlank()) return ValidationResult.failure("Invalid operator or condition");
        return  ValidationResult.success("Valid if");
    }

    private boolean hasValidStructure(String code) {
        return code != null && IF_STATEMENT_PATTERN.matcher(code.trim()).matches();
    }

    private boolean isValidCondition(String condition) {
        return condition != null && !condition.isBlank();
    }

    private boolean isValidOperator(String operator) {
        return VALID_OPERATORS.contains(operator);
    }
}
