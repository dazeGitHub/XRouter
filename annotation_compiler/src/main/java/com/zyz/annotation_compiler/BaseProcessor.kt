package com.zyz.annotation_compiler

import java.util.HashSet
import javax.annotation.processing.*
import javax.lang.model.SourceVersion

abstract class BaseProcessor : AbstractProcessor(){

    //使用 filter 对象来生成 Kotlin 文件代码，filter 对象在 init() 方法中初始化，并可以直接从 processingEnv 得到
    var mJavaFiler: Filer? = null
    var messager: Messager? = null

    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)

        //这个形参 processingEnvironment 和 processingEnv 是同一个对象，但是使用 processingEnv 的话不知道他是否为空，所以尽量使用 processingEnvironment 对象
        mJavaFiler = processingEnvironment.filer

        //Log 是运行时才能打印出来的，但是注解处理器是在编译时进行的，所以这里输出日志不能用 Log，用 System.out.println() 又比较 low
        messager = processingEnvironment.messager
    }

    //步骤一: 声明当前注解处理器要处理的注解有哪些 (String 类型的参数传的就是注解的包名 + 类名，即全类名)
    override fun getSupportedAnnotationTypes(): Set<String> {
        //      annotations.add(Route::class.java.canonicalName) //Canonical [kəˈnɒnɪkl] : 规范的
        //      annotations.add(Override.class.getCanonicalName());
        return HashSet<String>().also {
            getSupportedAnnotationTypes(it)
        }
    }

    //步骤二: 声明支持的 java 版本
    override fun getSupportedSourceVersion(): SourceVersion {
        return processingEnv.sourceVersion
    }

    abstract fun getSupportedAnnotationTypes(annotationSet: MutableSet<String>)
}