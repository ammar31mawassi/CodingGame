package com.codeescape.validation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class VariableDeclarationValidator implements CodeValidator {
    private static HashMap<String,String> variables;
    private static volatile VariableDeclarationValidator instance;

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
        return variables.getOrDefault(variableName, "Does not exist");
    }
    public ValidationResult validate(String line){
        System.out.println("Validating variables in line: " + line);
        if(!line.endsWith(";")){
            return ValidationResult.failure("Missing ; at the end of the line: " + line);
        }
        line = line.substring(0, line.length()-1);
        String[] words = line.split(" ");
        if(words.length != 4)
            return ValidationResult.failure("Not Enough words in the code OR extra words in line: " + line);
        if(!words[2].equals("=")){
            return ValidationResult.failure("Missing = in the line: " + line);
        }
        if(!validVarName(words[1])){ return ValidationResult.failure("Not valid Variable name in the line: " + line); }
        switch(words[0]){
            case "int":
                if(!validateInteger(words[3])){ return ValidationResult.failure("Not a valid Integer the line: " + line); }
                variables.putIfAbsent(words[1],"int");
                return ValidationResult.success("Successful Integer Declaration");
            case "double":
                if(!validateDouble(words[3])){ return ValidationResult.failure("Not a valid Integer the line: " + line); }
                variables.putIfAbsent(words[1],"double");
                return ValidationResult.success("Successful Double Declaration");
            case "char":
                if(!validateChar(words[3])){ return ValidationResult.failure("Not a valid Integer the line: " + line); }
                variables.putIfAbsent(words[1],"char");
                return ValidationResult.success("Successful Char Declaration");
            case "String":
                if(!validateString(words[3])){ return ValidationResult.failure("Not a valid Integer the line: " + line); }
                variables.putIfAbsent(words[1],"String");
                return ValidationResult.success("Successful String Declaration");
            case "boolean":
                if(!validateBoolean(words[3])){ return ValidationResult.failure("Not a valid Integer the line: " + line); }
                variables.putIfAbsent(words[1],"boolean");
                return ValidationResult.success("Successful Boolean Declaration");
            default:
                return ValidationResult.failure("Not a valid TYPE in line: " + line);
        }
    }
    private boolean validVarName(String name){
        if(variables.containsKey(name)){
            return  false;
        }
        if(name.contains("-") || Character.isDigit(name.charAt(0))){ //cant contain - or start with a number
            return  false;
        }
        String[] invalidWords = new String[]{"int", "double", "char", "String", "boolean","while"
                ,"if","for","class"};
        for(String invalidWord : invalidWords){// saved words in Java
            if(name.equals(invalidWord)){
                return  false;
            }
        }

        return true;

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
}
