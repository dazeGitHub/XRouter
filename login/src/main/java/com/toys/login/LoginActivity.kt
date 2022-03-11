package com.toys.login

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.toys.base.BaseActivity
import com.toys.common.bean.User
import com.toys.common.data.constant.Constants
import com.toys.common.utils.TLog
import com.zyz.annotation.Autowired
import com.zyz.annotation.Route
import com.zyz.xrouter.IJsonTransfer
import com.zyz.xrouter.XRouterKnife

@Route(Constants.RouterPath.LOGIN)
class LoginActivity : BaseActivity() {

    @Autowired("age")
    var age: Int? = null
    @Autowired(value = "username")
    var userNameStr: String? = null
    @Autowired(value = "user")
    var userObj: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        XRouterKnife.bind(this, Constants.JSON_TRANS_IMP)

        "LoginActivity age = $age userNameStr = $userNameStr userObj = $userObj".let{
            TLog.e(TAG,it)
            findViewById<TextView>(R.id.tv_receive_msg).text = it
        }
    }
}