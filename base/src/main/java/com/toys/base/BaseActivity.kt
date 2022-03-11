package com.toys.base

import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    val TAG: String = this::class.java.name
}