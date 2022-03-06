package com.toys.arouter;

import android.app.Activity;
import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexFile;

/**
 * <pre>
 *     author : ZYZ
 *     e-mail : zyz163mail@163.com
 *     time   : 2021/04/30
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class ARouter {
    public static ARouter aRouter = new ARouter();

    //装载 Activity 的容器也叫路由表
    private Map<String, Class<? extends Activity>> map;
    private Context context;

    private ARouter() {
        map = new HashMap();
    }

    public static ARouter getInstance() {
        return aRouter;
    }

    /**
     * app 模块的 Application 调用该初始化方法
     *
     * @param context
     */
    public void init(Context context) {
        this.context = context;
        List<String> className = getClassName("com.toys.utils");
        for (String str : className) {
            try {
                Class<?> aClass = Class.forName(str);
                //进行第二步验证，这个类是否是 IRouter 接口的实现类
                if (IRouter.class.isAssignableFrom(aClass)) {
                    //通过 newInstance() 得到工具类的实例，并通过接口的引用指向子类的实例，否则还需要反射它的方法再执行，比较麻烦
                    IRouter iRouter = (IRouter) aClass.newInstance();
                    iRouter.addActivity();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据包名获取这个包下面所有类的类名
     *
     * @param packageName
     * @return
     */
    private List<String> getClassName(String packageName) {
        //创建一个 class 对象的集合
        List<String> classList = new ArrayList<>();
        try {
            DexFile df = new DexFile(context.getPackageCodePath());
            Enumeration<String> entries = df.entries();
            while (entries.hasMoreElements()) {
                String className = (String) entries.nextElement();
                if (className.contains(packageName)) {
                    classList.add(className);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return classList;
    }

    /**
     * 将 Activity 的类对象加入到 map 中
     *
     * @param key
     * @param clazz
     */
    public void addActivity(String key, Class<? extends Activity> clazz) {
        if (key != null && clazz != null && !map.containsKey(key)) {
            map.put(key, clazz);
        }
    }

    /**
     * 获取指定的 Activity 的 class
     *
     * @param key
     * @return
     */
    public Class getActivity(String key) {
        if (key != null && map.containsKey(key)) {
            return map.get(key);
        }
        return null;
    }
}
