package com.zyz.annotation_compiler

import com.google.auto.service.AutoService
import com.zyz.annotation.Autowired
import java.io.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import javax.tools.JavaFileObject


/**
 * 注解处理器，用来生成代码的
 * 使用前需要注册
 */
@AutoService(value = [Processor::class])
class AutoWiredProcessor() : BaseProcessor() {

    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        messager?.printMessage(Diagnostic.Kind.WARNING, "AutoWiredProcessor init finish \n")
    }

    override fun getSupportedAnnotationTypes(annotationSet: MutableSet<String>){
        annotationSet.add(Autowired::class.java.canonicalName)
    }

    override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        messager?.printMessage(Diagnostic.Kind.WARNING, "AutoWiredProcessor process pre \n")

        if (set.isNotEmpty()) {
            messager?.printMessage(Diagnostic.Kind.WARNING, "AutoWiredProcessor process set.isNotEmpty() \n")

            //获取APP中所有用到了 Autowired 注解的对象
            val elementsSet: Set<Element> = roundEnvironment.getElementsAnnotatedWith(Autowired::class.java)
            messager?.printMessage(Diagnostic.Kind.WARNING, "AutoWiredProcessor process elementsSet.size = ${elementsSet.size} \n")

            val activityEleMap: MutableMap<String, MutableList<Element>> = HashMap() // Map 的 key 就是 activityName，value 是该 Activity 中注解标注的所有全局变量
            for (element: Element in elementsSet) {
                messager?.printMessage(Diagnostic.Kind.WARNING, "AutoWiredProcessor process element = $element element.type = ${element.asType()} \n")
                //AutoWiredProcessor process element = age$annotations() element.type = ()void

//                if (element is VariableElement) {
//                    messager?.printMessage(Diagnostic.Kind.WARNING, "AutoWiredProcessor process element is VariableElement");

                    val activityName = element.enclosingElement.simpleName.toString()
                    messager?.printMessage(Diagnostic.Kind.WARNING, "AutoWiredProcessor process activityName = $activityName \n")

                    var variableElementsList = activityEleMap[activityName]
                    if (variableElementsList == null) {
                        variableElementsList = ArrayList()
                        activityEleMap[activityName] = variableElementsList
                    }
                    variableElementsList.add(element)
//                }else{
//                    messager?.printMessage(Diagnostic.Kind.WARNING, "AutoWiredProcessor process element is not VariableElement");
//                }
            }

            if (activityEleMap.isNotEmpty()) {
                generateActivityViewBinding(activityEleMap)
            }
        }
        messager?.printMessage(Diagnostic.Kind.WARNING, "AutoWiredProcessor process finish \n")

        return false
    }

    private fun generateActivityViewBinding(activityEleMap: MutableMap<String, MutableList<Element>>){
        messager?.printMessage(Diagnostic.Kind.WARNING, "AutoWiredProcessor generateActivityViewBinding activityVarEleMap.size = ${activityEleMap.size} \n")

        var javaFileWriter: Writer? = null
        var ktFileRandomAccessFile: RandomAccessFile? = null
        val iterator: Iterator<String> = activityEleMap.keys.iterator()

        while (iterator.hasNext()) {
            val activityName = iterator.next()
            val elementsList: List<Element>? = activityEleMap[activityName]
            //得到包名
            val enclosingElement = elementsList!![0].enclosingElement as TypeElement
            val packageName = processingEnv.elementUtils.getPackageOf(enclosingElement).toString()
            try {
                //这里新建个 java 文件并写入并没有实际意义, 只是为了获取该 java 文件的绝对路径, 从而能写入一个新的 kt 文件
                val sourceFile : JavaFileObject = mJavaFiler!!.createSourceFile(packageName + "." + activityName + "_VB") //_ViewBinding
                javaFileWriter = sourceFile.openWriter()
                javaFileWriter.write("package $packageName;\n")
                javaFileWriter.close()

                messager?.printMessage(Diagnostic.Kind.WARNING, "AutoWiredProcessor generateActivityViewBinding sourceFile.name = ${sourceFile.name} \n")
                val resultJavaFile = File(sourceFile.name.replace("_VB.java","_ViewBinding.kt"))
                ktFileRandomAccessFile = RandomAccessFile(resultJavaFile, "rw")

                val importPackageStr = "package $packageName\n"

                ktFileRandomAccessFile.write(importPackageStr.toByteArray())
                ktFileRandomAccessFile.write("import android.os.Bundle\n".toByteArray())
                ktFileRandomAccessFile.write("import com.zyz.xrouter.IBinder\n".toByteArray())
                ktFileRandomAccessFile.write("import com.zyz.xrouter.IJsonTransfer\n\n".toByteArray())

                ktFileRandomAccessFile.write(("class " + activityName + "_ViewBinding : IBinder<" + packageName + "." + activityName + ">{\n").toByteArray())
                ktFileRandomAccessFile.write(("    override fun bind(target: $packageName.$activityName, iJsonTransfer: IJsonTransfer?) { \n").toByteArray())
                ktFileRandomAccessFile.write(("        val bundle = target.intent.getExtras() \n").toByteArray())
                //target.tvText=(android.widget.TextView)target.findViewById(2131165325);
                for (element: Element in elementsList) {
//                  printElementInfo(element)

                    //得到名字
                    val variableName = element.simpleName.toString()
                    //得到注解的 name 的值
                    val annoNameValue: String = element.getAnnotation(Autowired::class.java).value
                    //得到类型
                    val typeMirror = element.asType()

                    messager?.printMessage(Diagnostic.Kind.WARNING, "AutoWiredProcessor element.javaClass.isPrimitive = ${element.javaClass.isPrimitive} typeMirror = ${typeMirror}\n")
                    //警告: AutoWiredProcessor element.javaClass.isPrimitive = false typeMirror = java.lang.Integer 或 com.toys.bean.User
                    messager?.printMessage(Diagnostic.Kind.WARNING, "AutoWiredProcessor element.javaClass = ${element.javaClass} keyName = ${annoNameValue}\n")
                    //警告: AutoWiredProcessor element.javaClass = class com.sun.tools.javac.code.Symbol$VarSymbol keyName = username
                    messager?.printMessage(Diagnostic.Kind.WARNING, "typeMirror.javaClass = ${typeMirror.javaClass}\n")
                    //typeMirror.javaClass = class com.sun.tools.javac.code.Type$ClassType

                    val convertStr = getFieldInjectTypeConvertStr(typeMirror)
                    if(convertStr.isNotEmpty() || ("java.lang.String" == typeMirror.toString())){ //如果是基本类型 或 字符串, 直接强转
                        ktFileRandomAccessFile.write("        target.$variableName = bundle?.getString(\"${annoNameValue}\")${convertStr}".toByteArray())
                    } else{
                        ktFileRandomAccessFile.write("        target.$variableName = iJsonTransfer?.transJson2Obj(bundle?.getString(\"${annoNameValue}\"), ${typeMirror}::class.java) as ${typeMirror}?".toByteArray()) // ${typeMirror}.class)
                    }
                    ktFileRandomAccessFile.write("\n".toByteArray())
                }
                ktFileRandomAccessFile.write("\n}}".toByteArray())


                //重命名 XXActivityBinding.java 为 kotlin 文件, 防止为字段设置值时还需要加 @JvmField 注解, 但是直接重命名会报错, 所以只能采用单独新建个 kt 文件的方式写入文件
                messager?.printMessage(Diagnostic.Kind.WARNING, "AutoWiredProcessor sourceFile.toUri() = ${ sourceFile.toUri()} sourceFile.name = ${sourceFile.name}\n")
//              sourceFile.toUri() = file:///G:/Project/AsProject/xxx/xxx/LoginActivity_ViewBinding.java
//              sourceFile.name = G:\Project\AsProject\xxx\LoginActivity_ViewBinding.java


            } catch (e: Exception) {
                e.printStackTrace()
                messager?.printMessage(Diagnostic.Kind.WARNING, "AutoWiredProcessor generateActivityViewBinding() Exception errMsg = ${e.message} \n")
            } finally {
                try {
                    javaFileWriter?.close()
                    ktFileRandomAccessFile?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            //直接重命名会报错
//            tempSourceFile?.let{
//                val sourceOriginJavaFile = File(tempSourceFile.name)
//                val targetResultKtFile = File(tempSourceFile.name.replace(".java",".kt"))
//                if(sourceOriginJavaFile.exists()){
//                    val isSuccess = sourceOriginJavaFile.renameTo(targetResultKtFile)
//                    messager?.printMessage(Diagnostic.Kind.WARNING, "AutoWiredProcessor rename success = $isSuccess \n")
//                }else{
//                    messager?.printMessage(Diagnostic.Kind.WARNING, "AutoWiredProcessor rename ${tempSourceFile.name} failed ! \n")
//                }
//            }

        }
        messager?.printMessage(Diagnostic.Kind.WARNING, "AutoWiredProcessor generateActivityViewBinding() execute finish ! \n")
    }

    private fun getFieldInjectTypeConvertStr(typeMirror: TypeMirror): String{
        return when(typeMirror.toString()){
            "java.lang.Byte" -> {
                "?.toByte()"
            }
            "java.lang.Short" -> {
                "?.toShort()"
            }
            "java.lang.Integer" -> {
                "?.toInt()"
            }
            "java.lang.Long" -> {
                "?.toLong()"
            }
            "java.lang.Float" -> {
                "?.toFloat()"
            }
            "java.lang.Double" -> {
                "?.toDouble()"
            }
            "java.lang.Character" -> {
                "?.toChar()"
            }
            "java.lang.Boolean" -> {
                "?.toBoolean()"
            }
            else -> {
                ""
            }
        }
    }

    /**
     * java.lang.String.class 变为 String::class.java
     * com.toys.bean.User.class 变为 User::class.java
     */
    private fun getFieldKtClass(typeMirror: TypeMirror): String{
        val noClassTypeMirrorStr = typeMirror.toString().replace(".class", "")
        return noClassTypeMirrorStr.substring(noClassTypeMirrorStr.lastIndexOf(".") + 1, noClassTypeMirrorStr.length) + "::class.java"
    }

    //打印 Element 相关信息
    private fun printElementInfo(element: Element){
        messager?.printMessage(Diagnostic.Kind.WARNING, "AutoWiredProcessor printElementInfo begin --------------------- \n")
        when (element) {
            is VariableElement -> {
                messager?.printMessage(Diagnostic.Kind.WARNING,"AutoWiredProcessor element is VariableElement")
            }
            is PackageElement -> {
                messager?.printMessage(Diagnostic.Kind.WARNING,"AutoWiredProcessor element is PackageElement")
            }
            is TypeElement -> {
                messager?.printMessage(Diagnostic.Kind.WARNING,"AutoWiredProcessor element is TypeElement")
            }
            is TypeParameterElement -> {
                messager?.printMessage(Diagnostic.Kind.WARNING,"AutoWiredProcessor element is TypeParameterElement")
            }
            is ExecutableElement -> {
                messager?.printMessage(Diagnostic.Kind.WARNING,"AutoWiredProcessor element is ExecutableElement")
            }
            else -> {
                messager?.printMessage(Diagnostic.Kind.WARNING,"AutoWiredProcessor element is OtherElement")
            }
        }

        messager?.printMessage(Diagnostic.Kind.WARNING, "AutoWiredProcessor printElementInfo end --------------------- \n")
    }
}