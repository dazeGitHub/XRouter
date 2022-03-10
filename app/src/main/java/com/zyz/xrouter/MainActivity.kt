package com.zyz.xrouter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.toys.common.Constants
import com.zyz.annotation.Route

@Route(Constants.RouterPath.MAIN)
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun jumpLoginActivity(view: View?) {
        XRouter.getInstance().jumpActivity(this,  url = Constants.RouterPath.LOGIN_TEST_PATH,scheme = null)
    }
}