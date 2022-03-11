package com.toys.base

import android.app.Application

/**
 * <pre>
 * author : ZYZ
 * e-mail : zyz163mail@163.com
 * time   : 2021/04/29
 * desc   :
 * version: 1.0
</pre> *
 */
open class BaseApplication : Application() {

    //得到了在 config.gradle 中的 is_application 变量
    companion object{
        var is_application = BuildConfig.is_application
    }

    override fun onCreate() {
        super.onCreate()
    }
}