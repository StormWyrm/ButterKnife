package com.github.stormwyrm.butterknife.compiler;

import com.github.stormwyrm.butterknife.annotation.OnClick;
import com.google.auto.service.AutoService;
import com.github.stormwyrm.butterknife.annotation.BindView;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@AutoService(Processor.class)
public class ButterKnifeProcessor extends AbstractProcessor {
    private Types typeUtils;
    private Filer filer;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnv = processingEnv;
        typeUtils = processingEnv.getTypeUtils();
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new HashSet<>();
        annotations.add(BindView.class.getCanonicalName());
        annotations.add(OnClick.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.size() == 0) {
            return false;
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

        for (Element element : roundEnv.getElementsAnnotatedWith(OnClick.class)) {
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();//注解所在的类

            //要创建的代码，
            BindClass bindClass = map.get(typeElement);
            if (bindClass == null) {
                bindClass = BindClass.createBindClass(typeElement);
                map.put(typeElement, bindClass);
            }


            if (!element.getKind().equals(ElementKind.METHOD) || !(element instanceof ExecutableElement)) {
                throw new UnsupportedOperationException("Onclick annotation only support method");
            }

            ExecutableElement executableElement = (ExecutableElement) element;
            TypeMirror returnType = executableElement.getReturnType();
            if (!returnType.equals(void.class)) {

            }

            List<? extends VariableElement> parameters = executableElement.getParameters();
            if (parameters.size() > 1) {
                throw new UnsupportedOperationException("Onclick annotation only max support a params");
            }

            OnClick annotation = element.getAnnotation(OnClick.class);
            int[] ids = annotation.value();
            for (int id : ids) {
               if(id > 0){
                   bindClass.addAnnotationMethod(new ClickBinding(id,element.getSimpleName().toString(),parameters.size() == 0));
               }
            }
        }

        //迭代分组后的信息，主义生成对应的类
        for (Map.Entry<TypeElement, BindClass> entry : map.entrySet()) {
            try {
                entry.getValue().preJavaFile().writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}
