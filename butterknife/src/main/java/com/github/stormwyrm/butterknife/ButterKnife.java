package com.github.stormwyrm.butterknife;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;

public class ButterKnife {
    private static final Map<Class<?>, Constructor<? extends Unbinder>> BINDINGS
            = new LinkedHashMap<>();

    public static Unbinder bind(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        new View.OnClickListener(){

            @Override
            public void onClick(View v) {

            }
        };
        return bindClass(activity, decorView);
    }

    private static  Unbinder bindClass(Activity target, View sourceView) {
        return crateBinding(target, sourceView);
    }

    private static Unbinder crateBinding(Activity target, View sourceView) {
        Class<?> targetClass = target.getClass();
        Constructor<? extends Unbinder> constructor = findBindingConstructor(targetClass);
        try {
            return constructor.newInstance(target, sourceView);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private static Constructor<? extends Unbinder> findBindingConstructor(Class<?> cls) {
        Constructor<? extends Unbinder> bindingCtor = BINDINGS.get(cls);

        if (bindingCtor != null)
            return bindingCtor;
        String clsName = cls.getName();

        try {
            Class<?> bindingClass = cls.getClassLoader().loadClass(clsName + "_ViewBinding");
            bindingCtor = (Constructor<? extends Unbinder>) bindingClass.getConstructor(cls, View.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        BINDINGS.put(cls, bindingCtor);
        return bindingCtor;
    }
}
