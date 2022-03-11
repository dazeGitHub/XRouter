package com.toys.common.data.imp

import com.toys.common.bean.User
import com.toys.common.data.constant.Constants
import com.zyz.xrouter.IJsonTransfer

class IJsonTransferImp : IJsonTransfer {
    override fun transJson2Obj(json: String?, clazz: Class<*>): Any? {
        return when(clazz){
            User::class.java -> {
                Constants.GSON.fromJson<User>(json, clazz)
            }
            else -> {
                null
            }
        }
    }
}