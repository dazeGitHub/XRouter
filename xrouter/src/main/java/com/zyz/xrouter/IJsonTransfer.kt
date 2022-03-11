package com.zyz.xrouter

interface IJsonTransfer {
    fun transJson2Obj(json: String?, clazz: Class<*>) : Any?
}