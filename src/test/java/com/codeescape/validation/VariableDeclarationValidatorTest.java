package com.codeescape.validation;

import javax.swing.*;
import java.util.LinkedList;

class VariableDeclarationValidatorTest {
    private LinkedList<String> variables = new LinkedList<>();
    public VariableDeclarationValidatorTest() {}
    public boolean validate(String line){
        String[] words = line.split(" ");
        if(words.length != 4)
            return false;
        if(!words[2].equals("=")){
            return false;
        }
        switch(words[0]){
            case "int":
                if(!validVarName(words[1])){ return false; }
                if(!validateInteger(words[3])){ return false; }
                variables.add(words[1]);
                return true;
            case "double":
                return validateDouble(words[3]);
            case "char":
                return validateChar(words[3]);
            case "String":
                return validateString(words[3]);
            case "boolean":
                return validateBoolean(words[3]);
            default:
                return false;
        }
    }
    private boolean validVarName(String name){
        String[] invalidWords = new String[]{"int", "double", "char", "String", "boolean","while"
                ,"if","for","class","1","2","3","4","5","6","7","8","9","0"};
        for(String invalidWord : invalidWords){
            if(name.equals(invalidWord)){
                return  false;
            }
        }
        for(String varName : variables){
            if(name.equals(varName)){
                return  false;
            }
        }
        return true;

    }
    private boolean validateInteger(String value){
        char[] chars = value.toCharArray();
        if(value.toCharArray()[value.length()-1] != ';'){
            return false;
        }
        for(Character c : value.toCharArray()){
            if(!Character.isDigit(c)){
                return false;
            }
        }
        return true;
    }
    private boolean validateDouble(String value){
        return false;
    }
    private boolean validateChar(String value){
        return false;
    }
    private boolean validateString(String value){
        return false;
    }
    private boolean validateBoolean(String value){
        return false;
    }
}
