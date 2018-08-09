package com.qingfeng.compiler;

import com.google.auto.service.AutoService;
import com.qingfeng.lib.annotation.BindView;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.qingfeng.lib.annotation.BindView", "com.qingfeng.lib.annotation.BindView"})  //告诉这个实现类,需要监听检索哪些注解
@SupportedSourceVersion(SourceVersion.RELEASE_7)//最低支持的源码版本,这里是java7
public class ButterKnifeProcessor extends AbstractProcessor {
    private ProcessingEnvironment processingEnv;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnv = processingEnv;
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement typeElement : annotations) {
            System.out.println("ButterKnife:"+ typeElement.getSimpleName().toString());
        }

        Map<TypeElement, BindClass> map = new LinkedHashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(BindView.class)) {
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();//注解所在的类

            //要创建的代码，
            BindClass bindClass = map.get(typeElement);
            if (bindClass == null) {
                bindClass = BindClass.createBindClass(typeElement);
                map.put(typeElement, bindClass);
            }


            //ViewBinding用于保存每个注解的相关信息（比如注解所在字段的名称、注解所在字段的类型、注解上的值）
            String name = element.getSimpleName().toString();//注解所在字段的名称
            TypeName type = TypeName.get(element.asType());//注解所在字段的类型
            int annotationValue = element.getAnnotation(BindView.class).value();//注解的值
            ViewBinding viewBinding = ViewBinding.createViewBind(name, type, annotationValue);


            //一个类上可能有多个注解，用一个类来保存
            bindClass.addAnnotationField(viewBinding);
        }

        //迭代分组后的信息，主义生成对应的类
        for (Map.Entry<TypeElement, BindClass> entry : map.entrySet()) {
            try {
                entry.getValue().preJavaFile().writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
