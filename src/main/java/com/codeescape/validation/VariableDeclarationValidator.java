package com.codeescape.validation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class VariableDeclarationValidator implements CodeValidator {
    private record VariableData(String type, String value) {
    }

    private static HashMap<String, VariableData> variables;
    private static volatile VariableDeclarationValidator instance;
    private static final Set<String> VALID_TYPES = Set.of("int", "double", "char", "String", "boolean");
    private static final Set<String> INVALID_WORDS = Set.of(
            "int", "double", "char", "String", "boolean", "while", "if", "for", "class"
    );

    private VariableDeclarationValidator() {
        variables = new HashMap<>();
    }

    public static VariableDeclarationValidator getInstance() {
        if (instance == null) {
            synchronized (VariableDeclarationValidator.class) {
                if (instance == null) {
                    instance = new VariableDeclarationValidator();
                }
            }
        }
        return instance;
    }
    public static String checkNameOfVariable(String variableName) {
        getInstance();
        VariableData variableData = variables.get(variableName);
        return variableData == null ? "Does not exist" : variableData.type();
    }
    public static String checkValueOfVariable(String variableName) {
        getInstance();
        VariableData variableData = variables.get(variableName);
        return variableData == null ? "Does not exist" : variableData.value();
    }
    public ValidationResult validate(String line){
        return validateDeclaration(line, true);
    }

    public ValidationResult validateFieldDeclaration(String line) {
        ValidationResult initializedFieldResult = validateDeclaration(line, false);
        if (initializedFieldResult.isValid()) {
            return initializedFieldResult;
        }

        return validateUninitializedFieldDeclaration(line);
    }

    private ValidationResult validateDeclaration(String line, boolean rememberVariable){
        if(line == null || line.isBlank()){
            return ValidationResult.failure("Empty variable declaration");
        }
        line = line.trim();
        if(!line.endsWith(";")){
            return ValidationResult.failure("Missing ; at the end of the line: " + line);
        }
        line = line.substring(0, line.length()-1);
        String[] words = line.trim().split("\\s+");
        if(words.length != 4)
            return ValidationResult.failure("Not Enough words in the code OR extra words in line: " + line);
        if(!words[2].equals("=")){
            return ValidationResult.failure("Missing = in the line: " + line);
        }
        if(!validVarName(words[1], rememberVariable)){ return ValidationResult.failure("Not valid Variable name in the line: " + line); }
        switch(words[0]){
            case "int":
                if(!validateInteger(words[3])){ return ValidationResult.failure("Not a valid Integer the line: " + line); }
                rememberVariable(words[1], "int", words[3], rememberVariable);
                return ValidationResult.success("Successful Integer Declaration");
            case "double":
                if(!validateDouble(words[3])){ return ValidationResult.failure("Not a valid Integer the line: " + line); }
                rememberVariable(words[1], "double", words[3], rememberVariable);
                return ValidationResult.success("Successful Double Declaration");
            case "char":
                if(!validateChar(words[3])){ return ValidationResult.failure("Not a valid Integer the line: " + line); }
                rememberVariable(words[1], "char", words[3], rememberVariable);
                return ValidationResult.success("Successful Char Declaration");
            case "String":
                if(!validateString(words[3])){ return ValidationResult.failure("Not a valid Integer the line: " + line); }
                rememberVariable(words[1], "String", words[3], rememberVariable);
                return ValidationResult.success("Successful String Declaration");
            case "boolean":
                if(!validateBoolean(words[3])){ return ValidationResult.failure("Not a valid Integer the line: " + line); }
                rememberVariable(words[1], "boolean", words[3], rememberVariable);
                return ValidationResult.success("Successful Boolean Declaration");
            default:
                return ValidationResult.failure("Not a valid TYPE in line: " + line);
        }
    }
    private boolean validVarName(String name){
        return validVarName(name, true);
    }

    private boolean validVarName(String name, boolean checkExistingVariable){
        if(name == null || !name.matches("[A-Za-z_$][A-Za-z0-9_$]*")){
            return false;
        }
        if(checkExistingVariable && variables.containsKey(name)){
            return  false;
        }

        return !INVALID_WORDS.contains(name);

    }
    private ValidationResult validateUninitializedFieldDeclaration(String line) {
        if(line == null || line.isBlank()){
            return ValidationResult.failure("Empty field declaration");
        }
        line = line.trim();
        if(!line.endsWith(";")){
            return ValidationResult.failure("Missing ; at the end of the field: " + line);
        }
        line = line.substring(0, line.length()-1);
        String[] words = line.trim().split("\\s+");
        if(words.length != 2){
            return ValidationResult.failure("Invalid field declaration: " + line);
        }
        if(!VALID_TYPES.contains(words[0])){
            return ValidationResult.failure("Not a valid field type: " + line);
        }
        if(!validVarName(words[1], false)){
            return ValidationResult.failure("Not a valid field name: " + line);
        }

        return ValidationResult.success("Successful Field Declaration");
    }
    private void rememberVariable(String name, String type, String value, boolean rememberVariable) {
        if(rememberVariable){
            variables.putIfAbsent(name, new VariableData(type, value));
        }
    }
    private boolean validateInteger(String value){
        char[] chars = value.toCharArray();
        if(chars[0] == '-'){
            chars =  Arrays.copyOfRange(chars, 1, chars.length);
        }
        for(Character c : chars){
            if(!Character.isDigit(c)){
                return false;
            }
        }
        return true;
    }
    private boolean validateDouble(String value){
        if(!value.contains(".")) return false;
        if(value.split("\\.").length != 2) return false;
        value = value.replace(".","");
        char[] chars = value.toCharArray();
        if(chars[0] == '-'){
            chars =  Arrays.copyOfRange(chars, 1, chars.length);
        }
        for(Character c : chars){
            if(!Character.isDigit(c)){
                return false;
            }
        }
        return true;
    }
    private boolean validateChar(String value){
        if(value.startsWith("'") && value.endsWith("'")){
            return value.length() == 3;
        }
        return false;
    }
    private boolean validateString(String value){
        return value.startsWith("\"") && value.endsWith("\"");
    }
    private boolean validateBoolean(String value){
        return value.equals("true") || value.equals("false");
    }
    public static void resetVariables() {
        getInstance();
        variables.clear();
    }
}
