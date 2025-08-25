package com.example.countryfinder

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/**
 * Runner for tests, overwrited Android one because of HTTP usage :(
 */
class TestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application {
        // Force TestApp as Application, in tests
        return newApplication(TestApp::class.java, context)
    }
}
