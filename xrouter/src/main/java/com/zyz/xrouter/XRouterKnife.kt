package com.zyz.xrouter

import android.app.Activity
import java.lang.Exception

/**
 * 仿 ButterKnife 为 Activity 的字段注入值
 */
object XRouterKnife {
    fun bind(activity: Activity, iJsonTransfer: IJsonTransfer ?= null) {
        val name = activity.javaClass.name + "_ViewBinding"
        try {
            val aClass: Class<*> = Class.forName(name)
            val iBinder: IBinder<Activity> = aClass.newInstance() as IBinder<Activity>
            iBinder.bind(activity, iJsonTransfer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}