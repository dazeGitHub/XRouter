package com.toys.login

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.toys.bean.User
import com.toys.common.Constants
import com.zyz.annotation.Autowired
import com.zyz.annotation.Route
import com.zyz.xrouter.XRouterKnife

@Route(Constants.RouterPath.LOGIN)
class LoginActivity : AppCompatActivity() {
    private val TAG = LoginActivity::class.java.name

    @Autowired("age")
    var age: Int? = null
    @Autowired(value = "username")
    var userNameStr: String? = null
    @Autowired(value = "user")
    var userObj: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        XRouterKnife.bind(this)
        Log.e(TAG,"LoginActivity age = $age userNameStr = $userNameStr userObj = $userObj")
    }
}