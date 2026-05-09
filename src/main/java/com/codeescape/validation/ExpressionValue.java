package com.codeescape.validation;

record ExpressionValue(String type, String value) {
    boolean isNumeric() {
        return type.equals("int") || type.equals("double");
    }

    boolean isBoolean() {
        return type.equals("boolean");
    }

    double numberValue() {
        return Double.parseDouble(value);
    }
}
