package com.toys.utils

import com.toys.login.LoginActivity
import com.zyz.xrouter.IRouter
import com.zyz.xrouter.XRouter

/**
 * <pre>
 * author : ZYZ
 * e-mail : zyz163mail@163.com
 * time   : 2021/04/30
 * desc   :
 * version: 1.0
</pre> *
 */
class ActivityUtil : IRouter {
    override fun addActivity() {
        //大公司的 key 命名是有规范的
        XRouter.getInstance().addActivity("login/login", LoginActivity::class.java)
    }
}