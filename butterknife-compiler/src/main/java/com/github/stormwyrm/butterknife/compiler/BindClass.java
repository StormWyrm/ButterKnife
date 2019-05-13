package com.github.stormwyrm.butterknife.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;


import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.text.View;


//BindClass用于保存需要生成的代码，里面封装了javapoet相关处理
public class BindClass {
    private final ClassName UTILS = ClassName.get("com.github.stormwyrm.butterknife", "ProcessorUtils");
    private final ClassName VIEW = ClassName.get("android.view", "View");
    private final ClassName UNBINDER = ClassName.get("com.github.stormwyrm.butterknife", "Unbinder");
    private final ClassName ONCLICKLISENTER = ClassName.get("android.view.View", "OnClickListener");
    private TypeName targetTypeName;
    private ClassName bindingClassName;
    private boolean isFinal;
    private List<ViewBinding> fields;
    private List<ClickBinding> methods;

    private BindClass(TypeElement enclosingElement) {
        //asType 表示注解所在字段是什么类型(eg. Button TextView)
        TypeName targetType = TypeName.get(enclosingElement.asType());
        if (targetType instanceof ParameterizedTypeName) {
            targetType = ((ParameterizedTypeName) targetType).rawType;
        }
        //注解所在类名(包括包名)
        String packageName = enclosingElement.getQualifiedName().toString();
        packageName = packageName.substring(0, packageName.lastIndexOf("."));
        String className = enclosingElement.getSimpleName().toString();
        //我们要生成的类的类名
        ClassName bindingClassName = ClassName.get(packageName, className + "_ViewBinding");
        boolean isFinal = enclosingElement.getModifiers().contains(Modifier.FINAL);
        //注解所在类，在生成的类中，用于调用findViewById
        this.targetTypeName = targetType;
        this.bindingClassName = bindingClassName;
        //生成的类是否是final
        this.isFinal = isFinal;
        //用于保存多个注解的信息
        fields = new ArrayList<>();
        methods = new ArrayList<>();
    }

    public static BindClass createBindClass(TypeElement enclosingElement) {
        return new BindClass(enclosingElement);
    }

    public void addAnnotationField(ViewBinding viewBinding) {
        fields.add(viewBinding);
    }

    public void addAnnotationMethod(ClickBinding clickBinding) {
        methods.add(clickBinding);
    }

    public JavaFile preJavaFile() {
        return JavaFile.builder(bindingClassName.packageName(), createTypeSpec())
                .addFileComment("Generated code from My Butter Knife. Do not modify!!!")
                .build();
    }

    private TypeSpec createTypeSpec() {
        TypeSpec.Builder result = TypeSpec.classBuilder(bindingClassName.simpleName())
                .addSuperinterface(UNBINDER)//父类接口
                .addField(targetTypeName, "target", Modifier.PRIVATE)
                .addField(VIEW, "source", Modifier.PRIVATE)
                .addMethod(createUnbinderMethod())
                .addModifiers(Modifier.PUBLIC);
        if (isFinal) {
            result.addModifiers(Modifier.FINAL);
        }
        result.addMethod(createConstructor(targetTypeName));
        return result.build();
    }

    private MethodSpec createConstructor(TypeName targetType) {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        //构造方法有两个参数，target和source，在本例子中，Target就是activity，source就是activity的DecorView
        constructor.addParameter(targetType, "target", Modifier.FINAL);
        constructor.addParameter(VIEW, "source");
        constructor.addStatement("this.$L = $L", "target", "target");
        constructor.addStatement("this.$L = $L", "source", "source");
        //可能有多个View需要初始化，也就是说activity中多个字段用到了注解
        for (ViewBinding bindings : fields) {
            //生成方法里的语句，也就是方法体
            addViewBinding(constructor, bindings);
        }

        for (ClickBinding clickBinding : methods) {
            addClickBinding(constructor, clickBinding);
        }
        return constructor.build();
    }

    private void addClickBinding(MethodSpec.Builder builder, ClickBinding clickBinding) {
        TypeSpec clickLisenter = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ONCLICKLISENTER)
                .addMethod(
                        MethodSpec.methodBuilder("onClick")
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(void.class)
                                .addParameter(VIEW,"v")
                                .addStatement("target.$L(v)", clickBinding.getMethodName())
                                .build())
                .build();
        builder.addStatement("source.findViewById($L).setOnClickListener($L)",clickBinding.getValue(),clickLisenter);
    }

    private MethodSpec createUnbinderMethod() {
        MethodSpec.Builder unbinder = MethodSpec.methodBuilder("unbind")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .beginControlFlow("if (target != null)")
                .addStatement("target = null")
                .endControlFlow();

        for (ViewBinding binding : fields) {
            unbinder.addStatement("target.$L = null", binding.getName());
        }
        for (ClickBinding clickBinding : methods) {
            unbinder.addStatement("source.findViewById($L).setOnClickListener(null)", clickBinding.getValue());
        }
        unbinder.addStatement("source = null");
        return unbinder.build();

    }

    private void addViewBinding(MethodSpec.Builder result, ViewBinding binding) {
        //通过CodeBlock生成语句，因为生成的语句比较复杂。
        CodeBlock.Builder builder = CodeBlock.builder()
                .add("target.$L = ", binding.getName());
        //判断是否需要强制类型转换，如果目标View本来就是View，那就不需要强转了
        boolean requiresCast = requiresCast(binding.getType());
        if (!requiresCast) {
            builder.add("source.findViewById($L)", binding.getValue());
        } else {
            //我们使用ProcessorUtils重点工具方法findViewByCast进行强转 $T就是一个占位符，UTILS就是ClassName包含了UTILS的包名和类名
            //用ProcessorUtils替换成$T CodeBlock还支持很多占位符，需要了解更多可以去看看文档.
            builder.add("$T.findViewByCast", UTILS);
            //ProcessorUtils.findViewByCast需要的参数source就是DecorView
            builder.add("(source, $L", binding.getValue());
            //ProcessorUtils.findViewByCast需要的参数$T.class，就是目标View需要强转的类型
            builder.add(", $T.class", binding.getRawType());
            builder.add(")");
        }
        result.addStatement("$L", builder.build());

    }

    private static boolean requiresCast(TypeName type) {
        return !"android.view.View".equals(type.toString());
    }
}
