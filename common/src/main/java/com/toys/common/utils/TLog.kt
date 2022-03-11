package com.toys.common.utils

import android.util.Log
import com.toys.common.BuildConfig

object TLog {
    val DEBUG_FLAG = BuildConfig.DEBUG

    fun v(tag: String, log: String){
        if(DEBUG_FLAG){
            Log.v(tag, log)
        }
    }

    fun d(tag: String, log: String){
        if(DEBUG_FLAG){
            Log.d(tag, log)
        }
    }

    fun i(tag: String, log: String){
        if(DEBUG_FLAG){
            Log.i(tag, log)
        }
    }

    fun w(tag: String, log: String){
        if(DEBUG_FLAG){
            Log.w(tag, log)
        }
    }
    
    fun e(tag: String, log: String){
        if(DEBUG_FLAG){
            Log.e(tag, log)
        }
    }
}