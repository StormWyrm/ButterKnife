package com.github.stormwyrm.butterknife.compiler;

public class ClickBinding {
    private int value;
    private String methodName;
    private boolean hasParams;

    public ClickBinding(int value, String methodName, boolean hasParams) {
        this.value = value;
        this.methodName = methodName;
        this.hasParams = hasParams;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public boolean isHasParams() {
        return hasParams;
    }

    public void setHasParams(boolean hasParams) {
        this.hasParams = hasParams;
    }
}
