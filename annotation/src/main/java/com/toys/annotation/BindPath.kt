package com.toys.annotation

/**
 * <pre>
 * author : ZYZ
 * e-mail : zyz163mail@163.com
 * time   : 2021/04/30
 * desc   :
 * version: 1.0
</pre> *
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.BINARY)
annotation class BindPath(val key: String)