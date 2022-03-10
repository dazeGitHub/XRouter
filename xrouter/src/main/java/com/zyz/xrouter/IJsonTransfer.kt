package com.zyz.xrouter

interface IJsonTransfer {
    fun <Type> transJson2Obj(json: String?, classStr: String) : Type? //如果使用 clazz: Class<Type>, 那么 xxActivityBinding 中无法导入该类型的包, 导致报错
}