package com.toys.common.data.constant

import com.google.gson.Gson
import com.toys.common.data.imp.IJsonTransferImp

object Constants {
    val GSON = Gson()
    val JSON_TRANS_IMP = IJsonTransferImp()

    const val ROUTER_SCHEME = "dm://"

    object RouterPath {
        const val MAIN = "main/main"
        const val LOGIN = "login/login"
        const val MEMBER = "member/member"

        const val LOGIN_TEST_PATH = "$ROUTER_SCHEME$LOGIN?age=25&username=zhangsan&user={\"username\":\"lisi\", \"age\":30}"
        const val MEMBER_TEST_PATH = "$ROUTER_SCHEME$MEMBER"
    }
}