package com.qingfeng.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

//负责保存注解的相关信息
public class ViewBinding {
    private final String name;
    private final TypeName type;
    private final int value;

    private ViewBinding(String name, TypeName type, int value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    static ViewBinding createViewBind(String name, TypeName type, int value) {
        return new ViewBinding(name, type, value);
    }

    public String getName() {
        return name;
    }

    public TypeName getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    ClassName getRawType() {
        if (type instanceof ParameterizedTypeName) {
            return ((ParameterizedTypeName) type).rawType;
        }
        return (ClassName) type;
    }
}
