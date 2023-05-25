package com.zyz.annotation_compiler

import com.google.auto.service.AutoService
import javax.tools.Diagnostic
import com.zyz.annotation.Route
import java.io.IOException
import java.io.Writer
import java.util.HashMap
import javax.annotation.processing.*
import javax.lang.model.element.TypeElement

/**
 * 目的 : 在这里生成 Kotlin 类 ActivityUtilXXX.java , 然后 XRouter 初始化时将 路由 key 和 该 ActivityUtilXXX.java 反射后得到的 class 放到路由表里
 * ActivityUtilXXX.java 路径为 : 项目根目录/login/build/generated/source/kapt/debug/com/zyz/utils/ActivityUtilXXX.java
 */
@AutoService(value = [Processor::class]) //@SupportedAnnotationTypes({"com.zyz.annotation.BindPath"})
//@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
class RouterProcessor : BaseProcessor() {

    /**
     * 初始化方法，执行顺序是在 process() 之前执行的
     *
     * @param processingEnvironment
     */
    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        messager?.printMessage(Diagnostic.Kind.WARNING, "RouterProcessor init finish \n") //Diagnostic : 诊断的
    }

    override fun getSupportedAnnotationTypes(annotationSet: MutableSet<String>) {
        annotationSet.add(Route::class.java.canonicalName)
    }

    /**
     * process() 方法的作用 : 去当前模块中搜索注解
     */
    override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean { //roundEnvironment 表示 搜索引擎
        //目的: 去搜索当前模块用到了 BindPath 注解的类 (注解处理器的作用域只有当前模块)
        messager?.printMessage(Diagnostic.Kind.WARNING, "RouterProcessor process pre \n")

        if (set.isNotEmpty()) {
            messager?.printMessage(Diagnostic.Kind.WARNING, "RouterProcessor process set.isNotEmpty() \n")

            //Element ：类 (TypeElement)、成员变量 (VariableElement)、方法 (ExecutableElement)，就看注解 BindPath 放到了什么上面
            val elementsSet = roundEnvironment.getElementsAnnotatedWith(Route::class.java)
            messager?.printMessage(Diagnostic.Kind.WARNING, "RouterProcessor process elementsSet.size = ${elementsSet.size} \n")

            //遍历所有的类，然后把这个类的类名和注解里面携带的 key 拿出来
            val activityRouterPathClassMap: MutableMap<String, String> = HashMap() // Map 的 key 就是注解里的 key，value 就是类名
            val fragmentRouterPathClassNameMap: MutableMap<String, String> = HashMap() //fragment 的 value 不传 全类名.class, 而是传 全类名

            for (element in elementsSet) {
                messager?.printMessage(Diagnostic.Kind.WARNING, "RouterProcessor process element = $element \n")
                if (element is TypeElement) {
                    //获取全类名
                    val eleQualifiedName = element.qualifiedName.toString()
                    messager?.printMessage(Diagnostic.Kind.WARNING, "RouterProcessor process activityName = $eleQualifiedName \n")
                    //RouterProcessor process activityName = com.smartcity.login.ui.activity.LoginActivity

                    //获取上面的注解的值 (路由路径)
                    val routerPath = element.getAnnotation(Route::class.java).key
                    messager?.printMessage(Diagnostic.Kind.WARNING, "RouterProcessor process Route key = $routerPath \n")
                    //RouterProcessor process Route key = login/login

                    if(eleQualifiedName.contains("Activity")){
                        activityRouterPathClassMap[routerPath] = "$eleQualifiedName.class"
                    }else if(eleQualifiedName.contains("Fragment")){
                        fragmentRouterPathClassNameMap[routerPath] = eleQualifiedName
                    }else{
                        messager?.printMessage(Diagnostic.Kind.WARNING,
                            "RouterProcessor 无效的被注解类, routerPath = $routerPath eleQualifiedName = $eleQualifiedName"
                        )
                    }
                }
            }

            messager?.printMessage(Diagnostic.Kind.WARNING, "RouterProcessor process activityVarEleMap.size = ${activityRouterPathClassMap.size} \n")
            messager?.printMessage(Diagnostic.Kind.WARNING, "RouterProcessor process fragmentRouterPathClassMap.size = ${fragmentRouterPathClassNameMap.size} \n")

            if (activityRouterPathClassMap.isNotEmpty()) { //如果有被注解的 Activity 类，才需要生成 ActivityUtil
                generateActivityUtilFile(activityRouterPathClassMap)
            }
            if (fragmentRouterPathClassNameMap.isNotEmpty()){
                generateFragmentUtilFile(fragmentRouterPathClassNameMap)
            }
        }
        messager?.printMessage(Diagnostic.Kind.WARNING, "RouterProcessor process finish \n")

        return false
    }

    private fun generateActivityUtilFile(activityRouterPathClassMap: Map<String, String>) {
        var writer: Writer? = null
        //生成文件
        try {
            val activityUtilName = "ActivityUtil" + System.currentTimeMillis() //这个 ActivityUtilxxx 的数量是和有多少依赖 annotation_compiler 的模块相关的，加上时间戳是为了防止类名重复
            val sourceFile = mJavaFiler!!.createSourceFile("com.zyz.utils.$activityUtilName") //注意不要写成 filer.createClassFile()
            writer = sourceFile.openWriter()

            val stringBuffer = StringBuffer()
            stringBuffer.append("package com.zyz.utils;\n")
            stringBuffer.append("import com.zyz.xrouter.IRouter;\n")
            stringBuffer.append("import com.zyz.xrouter.XRouter;\n")

            //如果 ActivityUtilxxx 要生成多个，则需要让它们实现 IRouter 接口，
            stringBuffer.append("public class $activityUtilName implements IRouter {\n")

            //添加 addActivity() 方法
            stringBuffer.append("@Override \n")
            stringBuffer.append("public void addActivity() {\n")
            val iterator = activityRouterPathClassMap.keys.iterator()
            while (iterator.hasNext()) {
                val key = iterator.next()
                val activityClassName = activityRouterPathClassMap[key]
                stringBuffer.append("XRouter.Companion.getInstance().addActivity(\"$key\", $activityClassName);\n")
            }
            stringBuffer.append("\n}\n")

            //添加 addFragment() 方法
            stringBuffer.append("@Override \n")
            stringBuffer.append("public void addFragment() {\n")
            stringBuffer.append("}\n}")

            writer.write(stringBuffer.toString())
        } catch (e: IOException) {
            e.printStackTrace()
            messager?.printMessage(Diagnostic.Kind.WARNING, "RouterProcessor generateActivityUtilFile exception = " + e.message)
        } finally {
            if (writer != null) {
                try {
                    writer.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun generateFragmentUtilFile(fragmentRouterPathClassNameMap: Map<String, String>){
        var writer: Writer? = null
        //生成文件
        try {
            val fragmentUtilName = "FragmentUtil" + System.currentTimeMillis()
            val sourceFile = mJavaFiler!!.createSourceFile("com.zyz.utils.$fragmentUtilName") //注意不要写成 filer.createClassFile()
            writer = sourceFile.openWriter()
            val stringBuffer = StringBuffer()
            stringBuffer.append("package com.zyz.utils;\n")
            stringBuffer.append("import com.zyz.xrouter.IRouter;\n")
            stringBuffer.append("import com.zyz.xrouter.XRouter;\n")

            //如果 ActivityUtilxxx 要生成多个，则需要让它们实现 IRouter 接口，
            stringBuffer.append("public class $fragmentUtilName implements IRouter {\n")

            //添加 addActivity() 方法
            stringBuffer.append("@Override \n")
            stringBuffer.append("public void addActivity() {\n")
            stringBuffer.append("\n}\n")

            //添加 addFragment() 方法
            stringBuffer.append("@Override \n")
            stringBuffer.append("public void addFragment() {\n")
            val iterator = fragmentRouterPathClassNameMap.keys.iterator()
            while (iterator.hasNext()) {
                val key = iterator.next()
                val fragmentClassName = fragmentRouterPathClassNameMap[key]
                stringBuffer.append("XRouter.Companion.getInstance().addFragment(\"$key\", \"$fragmentClassName\");\n")
            }
            stringBuffer.append("}\n}")

            writer.write(stringBuffer.toString())
        } catch (e: IOException) {
            e.printStackTrace()
            messager?.printMessage(Diagnostic.Kind.WARNING, "RouterProcessor generateFragmentUtilFile exception = " + e.message)
        } finally {
            if (writer != null) {
                try {
                    writer.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}