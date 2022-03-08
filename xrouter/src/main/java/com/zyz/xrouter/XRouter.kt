package com.zyz.xrouter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import dalvik.system.DexFile
import java.io.IOException

/**
 * <pre>
 * author : ZYZ
 * e-mail : zyz163mail@163.com
 * time   : 2021/04/30
 * desc   :
 * version: 1.0
</pre> *
 */
class XRouter private constructor() {
    //装载 Activity 的容器也叫路由表
    private val map: MutableMap<String?, Class<out Activity?>?>
    private var appContext: Context? = null

    companion object {
        private var xRouter = XRouter()

        fun getInstance(): XRouter {
            return xRouter
        }
    }

    init {
        map = HashMap()
    }

    /**
     * app 模块的 Application 调用该初始化方法
     *
     * @param appContext
     */
    fun init(appContext: Context?) {
        this.appContext = appContext
        val className = getClassName("com.zyz.utils")
        for (str in className) {
            try {
                val aClass = Class.forName(str)
                //进行第二步验证，这个类是否是 IRouter 接口的实现类
                if (IRouter::class.java.isAssignableFrom(aClass)) {
                    //通过 newInstance() 得到工具类的实例，并通过接口的引用指向子类的实例，否则还需要反射它的方法再执行，比较麻烦
                    val iRouter = aClass.newInstance() as IRouter
                    iRouter.addActivity()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 根据包名获取这个包下面所有类的类名
     *
     * @param packageName
     * @return
     */
    private fun getClassName(packageName: String): List<String> {
        //创建一个 class 对象的集合
        val classList: MutableList<String> = ArrayList()
        try {
            val df = DexFile(appContext!!.packageCodePath)
            val entries = df.entries()
            while (entries.hasMoreElements()) {
                val className = entries.nextElement() as String
                if (className.contains(packageName)) {
                    classList.add(className)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return classList
    }

    /**
     * 将 Activity 的类对象加入到 map 中
     *
     * @param key
     * @param clazz
     */
    fun addActivity(key: String?, clazz: Class<out Activity?>?) {
        if (key != null && clazz != null && !map.containsKey(key)) {
            map[key] = clazz
        }
    }

    /**
     * 获取指定的 Activity 的 class
     *
     * @param key
     * @return
     */
    fun getActivity(key: String?): Class<*>? {
        return if (key != null && map.containsKey(key)) {
            map[key]
        } else null
    }

    /**
     * 跳转窗体的方法
     * @param key
     * @param bundle
     */
    fun jumpActivity(context: Context, key: String?, bundle: Bundle?) {
        val aClass = map[key] ?: return
        val intent = Intent(context, aClass)
        if (bundle != null) {
            intent.putExtras(bundle)
        }
        context.startActivity(intent)
    }
}