package com.zyz.xrouter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
    private val TAG: String = this.javaClass.name

    private var mAppContext: Context? = null

    private val mActivityClassMap: MutableMap<String?, Class<out Activity?>?>   //装载 Activity 的容器也叫路由表, key 是 path, value 是 Class
    private val mFragmentClassMap: MutableMap<String?, String> //Class<out Fragment?>?

    companion object {
        private var xRouter = XRouter()

        //mActivityClassMap 必须放一个 ContainerActivity 的路由, 否则跳转到 Fragment 时没有 Activity 来装载 Fragment
        const val ROUTER_FRAG_CONTAINER = "container/container";
        const val JUMP_KEY_CONTAINER_FRAG_CLASS_NAME = "JUMP_KEY_CONTAINER_FRAG_CLASS_NAME";  //ContainerActivity 根据这个接收 Fragment 的全类名

        fun getInstance(): XRouter {
            return xRouter
        }
    }

    init {
        mActivityClassMap = HashMap()
        mFragmentClassMap = HashMap()
    }

    /**
     * app 模块的 Application 调用该初始化方法
     *
     * @param appContext
     */
    fun init(appContext: Context?) {
        this.mAppContext = appContext
        val className = getClassNameList("com.zyz.utils")
        for (str in className) {
            try {
                val aClass = Class.forName(str)
                //进行第二步验证，这个类是否是 IRouter 接口的实现类
                if (IRouter::class.java.isAssignableFrom(aClass)) {
                    //通过 newInstance() 得到工具类的实例，并通过接口的引用指向子类的实例，否则还需要反射它的方法再执行，比较麻烦
                    val iRouter = aClass.newInstance() as IRouter
                    iRouter.addActivity()
                    iRouter.addFragment()
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
    private fun getClassNameList(packageName: String): List<String> {
        //创建一个 class 对象的集合
        val classList: MutableList<String> = ArrayList()
        try {
            val df = DexFile(mAppContext!!.packageCodePath)
            val entries = df.entries()
            while (entries.hasMoreElements()) {
                val className = entries.nextElement() as String
                if (className.contains(packageName)) {
//                  Log.d(TAG,"getClassNameList className.contains(packageName) className = $className packageName = $packageName")
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
        if (key != null && clazz != null && !mActivityClassMap.containsKey(key)) {
            mActivityClassMap[key] = clazz
        }
    }

    /**
     * 将 Fragment 的类对象加入到 map 中
     */
    fun addFragment(key: String?, fragmentClassName : String){ //clazz: Class<out Fragment?>?
        if (key != null && !mFragmentClassMap.containsKey(key)) {
            mFragmentClassMap[key] = fragmentClassName
        }
    }

    /**
     * 获取指定的 Activity 的 class
     *
     * @param key
     * @return
     */
    fun getActivity(key: String?): Class<*>? {
        return if (key != null && mActivityClassMap.containsKey(key)) {
            mActivityClassMap[key]
        } else null
    }

    /**
     * 获取指定的 Fragment 的 class
     */
    fun getFragment(key: String): Class<*>? {
        return if (mFragmentClassMap.containsKey(key)) {
            Class.forName(mFragmentClassMap[key])
        } else null
    }

    fun getFragmentName(key: String): String?{
        return if(mFragmentClassMap.containsKey(key)){
            mFragmentClassMap.get(key)
        } else null
    }

    /**
     * 使用 path 和 bundle 跳转界面
     * @param path
     * @param bundle
     */
    private fun jumpByBundle(context: Context, path: String?, bundle: Bundle?, requestCode : Int? = null) {
        val activityClass = mActivityClassMap[path]
        if (activityClass == null) {
            Log.e(TAG, "XRouter jumpActivity mActivityClassMap.size = ${mActivityClassMap.size} aclass == null return")
            Log.e(TAG, "路由表中找不到该 Activity 页面, path = $path")
            val fragmentClassName = mFragmentClassMap[path]
            if(fragmentClassName == null){
                Toast.makeText(context, "路由表中找不到该 Fragment 页面, path = $path", Toast.LENGTH_SHORT).show()
            }else{
                jumpFragment(context, fragmentClassName, bundle, requestCode)
            }
        }else{
            jumpActivity(context, activityClass, bundle, requestCode)
        }
    }

    private fun jumpActivity(context: Context, activityClass : Class<out Activity?>, bundle: Bundle?, requestCode : Int? = null){
        val intent = Intent(context, activityClass)
        if (bundle != null) {
            intent.putExtras(bundle)
        }
        if(requestCode != null){
            (context as Activity?)?.startActivityForResult(intent, requestCode)
        }else{
            context.startActivity(intent)
        }
    }

    private fun jumpFragment(context: Context, fragmentClassName : String, bundle: Bundle?, requestCode : Int? = null){
        Log.e(TAG, "jumpFragment, fragmentClass = $fragmentClassName")
        if(!mActivityClassMap.containsKey(ROUTER_FRAG_CONTAINER)){
            Toast.makeText(context, "Activity 路由表中没有 ContainerActivity, 导致要跳转的 Fragment 没有容器来装载 !", Toast.LENGTH_SHORT).show()
            return
        }
       mActivityClassMap[ROUTER_FRAG_CONTAINER]?.let{containerActivityClass ->
           val intent = Intent(context, containerActivityClass)
           if (bundle != null) {
               bundle.putString(JUMP_KEY_CONTAINER_FRAG_CLASS_NAME, fragmentClassName)
               intent.putExtras(bundle)
           }
           //同一个 ContainerActivity 的 Fragment 跳另一个 Fragment, 那么让 Activity 的启动模式为 singleTop,
           //然后触发 ContainerActivity 的 onNewIntent() 从而传递参数
           intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
           if(requestCode != null){
               (context as Activity?)?.startActivityForResult(intent, requestCode)
           }else{
               context.startActivity(intent)
           }
       }
    }

    /**
     * 使用路由跳转界面 (前面的 com.smartcity.login 是域名, 后边的 com.smartcity.login 是路径)
     * url         :   dm://com.smartcity.login/com.smartcity.login?username=user1&pwd=pwd1
     * scheme      :   协议, 例如 dm
     * jsonValue   :   如果不想使用 url, 那么就直接传一个 json 字符串过来, 并通过 JUMP_JSON_KEY 获取该 json 字符串
     */
    fun jumpPage(context: Context?, url: String?, bundle: Bundle? = null, requestCode : Int? = null) {
        context?.let{
            url?.let {
                //将 url 拆分为 path 和 参数, 参数放到 bundle 里
                val uri: Uri = Uri.parse(it)
                val hostPath = (uri.host ?: "") + uri.path
                val tmpBundle = bundle ?: Bundle()

                //将 tmpBundle 的非 string 类型的 value 值转换为 string
                //否则 XXActivity_ViewBinding 通过 bundle?.getString("age")?.toInt() 获取不到 String 类型的 value 值
                val keySet : Set<String>  = tmpBundle.keySet()
                keySet.forEach { key ->
                    if (tmpBundle.get(key) !is String) {
                        tmpBundle.putString(key, tmpBundle.get(key).toString())
                    }
                }

                uri.queryParameterNames.let { queryParameterNames ->
                    if (queryParameterNames.isNotEmpty()) {
                        for (key in queryParameterNames) {
                            val paramValue: String? = uri.getQueryParameter(key)
                            tmpBundle.putString(key, paramValue)
                        }
                    }
                    //                  Log.d(TAG, "XRouter jumpActivity() hostPath = $hostPath ")
                    jumpByBundle(context, path = hostPath, bundle = tmpBundle, requestCode)
                }
            } ?: let {
                Log.e(TAG, "XRouter jumpActivity() url == null, cannot jump !")
            }
        }
    }
}