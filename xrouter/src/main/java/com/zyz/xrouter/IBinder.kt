package com.zyz.xrouter

/**
 * 用于绑定activity     Butterknife.bind(this)
 * @param <T>
</T> */
interface IBinder<T> {
    fun bind(target: T, iJsonTransfer: IJsonTransfer ?= null)
}