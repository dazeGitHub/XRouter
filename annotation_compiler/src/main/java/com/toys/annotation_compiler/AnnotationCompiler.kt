package com.toys.annotation_compiler

import com.google.auto.service.AutoService
import javax.tools.Diagnostic
import com.toys.annotation.BindPath
import java.io.IOException
import java.io.Writer
import java.util.HashMap
import java.util.HashSet
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * 目的 : 在这里生成 Kotlin 代码
 */
@AutoService(value = [Processor::class]) //@SupportedAnnotationTypes({"com.toys.annotation.BindPath"})
//@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
class AnnotationCompiler : AbstractProcessor() {
    //使用 filter 对象来生成 Kotlin 文件代码，filter 对象在 init() 方法中初始化，并可以直接从 processingEnv 得到
    var filer: Filer? = null
    var messager: Messager? = null

    /**
     * 初始化方法，执行顺序是在 process() 之前执行的
     *
     * @param processingEnvironment
     */
    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)

        //这个形参 processingEnvironment 和 processingEnv 是同一个对象，但是使用 processingEnv 的话不知道他是否为空，所以尽量使用 processingEnvironment 对象
        filer = processingEnvironment.filer

        //Log 是运行时才能打印出来的，但是注解处理器是在编译时进行的，所以这里输出日志不能用 Log，用 System.out.println() 又比较 low
        messager = processingEnvironment.messager
        messager?.printMessage(Diagnostic.Kind.WARNING, "AnnotationCompiler init finish \n") //Diagnostic : 诊断的
    }

    //步骤一: 声明当前注解处理器要处理的注解有哪些 (String 类型的参数传的就是注解的包名 + 类名，即全类名)
    override fun getSupportedAnnotationTypes(): Set<String> {
        val annotations: MutableSet<String> = HashSet()
        annotations.add(BindPath::class.java.canonicalName) //Canonical [kəˈnɒnɪkl] : 规范的
        //      annotations.add(Override.class.getCanonicalName());
        return annotations
    }

    //步骤二: 声明支持的 java 版本
    override fun getSupportedSourceVersion(): SourceVersion {
        return processingEnv.sourceVersion
    }

    /**
     * process() 方法的作用 : 去当前模块中搜索注解
     */
    override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean { //roundEnvironment 表示 搜索引擎
        //目的: 去搜索当前模块用到了 BindPath 注解的类 (注解处理器的作用域只有当前模块)
        messager!!.printMessage(Diagnostic.Kind.WARNING, "AnnotationCompiler process pre \n")

        //Element ：类 (TypeElement)、成员变量 (VariableElement)、方法 (ExecutableElement)，就看注解 BindPath 放到了什么上面
        val elements = roundEnvironment.getElementsAnnotatedWith(
            BindPath::class.java
        )

        //遍历所有的类，然后把这个类的类名和注解里面携带的 key 拿出来
        val map: MutableMap<String, String> = HashMap() // Map 的 key 就是注解里的 key，value 就是类名
        for (element in elements) {
            if (element is TypeElement) {
                val typeElement = element
                //获取全类名
                val activityName = typeElement.qualifiedName.toString()
                //获取上面的注解
                val key = typeElement.getAnnotation(BindPath::class.java).key
                map[key] = "$activityName.class"
            }
        }
        if (map.isNotEmpty()) { //如果有被注解的 Activity 类，才需要生成 ActivityUtil
            generateActivityUtilFile(map)
        }
        messager!!.printMessage(Diagnostic.Kind.WARNING, "AnnotationCompiler process finish \n")
        return false
    }

    private fun generateActivityUtilFile(annnKeyClassMap: Map<String, String>) {
        var writer: Writer? = null
        //生成文件
        try {
            val utilName = "ActivityUtil" + System.currentTimeMillis() //这个 ActivityUtilxxx 的数量是和有多少依赖 annotation_compiler 的模块相关的，加上时间戳是为了防止类名重复
            val sourceFile = filer!!.createSourceFile("com.toys.utils.$utilName") //注意不要写成 filer.createClassFile()
            writer = sourceFile.openWriter()
            val stringBuffer = StringBuffer()
            stringBuffer.append("package com.toys.utils;\n")
            stringBuffer.append("import com.toys.xrouter.IRouter;\n")
            stringBuffer.append("import com.toys.xrouter.XRouter;\n")
            //如果 ActivityUtilxxx 要生成多个，则需要让它们实现 IRouter 接口，
            stringBuffer.append("public class $utilName implements IRouter {\n")
            stringBuffer.append("@Override \n")
            stringBuffer.append("public void addActivity() {\n")
            val iterator = annnKeyClassMap.keys.iterator()
            while (iterator.hasNext()) {
                val key = iterator.next()
                val activityName = annnKeyClassMap[key]
                stringBuffer.append("XRouter.getInstance().addActivity(\"$key\", $activityName);\n")
            }
            stringBuffer.append("}\n}")
            writer.write(stringBuffer.toString())
        } catch (e: IOException) {
            e.printStackTrace()
            messager!!.printMessage(
                Diagnostic.Kind.WARNING,
                "AnnotationCompiler generateActivityUtilFile exception = " + e.message
            )
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