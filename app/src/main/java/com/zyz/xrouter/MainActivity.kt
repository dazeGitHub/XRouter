package com.zyz.xrouter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.toys.annotation.BindPath

@BindPath("main/main")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}