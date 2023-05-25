package com.zyz.xrouter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.toys.base.BaseActivity
import com.toys.common.bean.User
import com.toys.common.data.constant.Constants
import com.zyz.annotation.Route

@Route(Constants.RouterPath.MAIN)
class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun jumpLoginActivity(view: View?) {
        XRouter.getInstance().jumpPage(this,  url = Constants.RouterPath.LOGIN_TEST_PATH,)
    }
}