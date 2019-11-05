package com.cpacm.aspectjx

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.cpacm.annotations.CLog
import com.cpacm.annotations.MLog
import com.cpacm.aspectjx.ui.main.MainFragment
import com.cpacm.logger.SimpleLogger
import com.cpacm.logger.SimpleLoggerConfig
import kotlinx.coroutines.runBlocking
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }

        SimpleLogger.init(SimpleLoggerConfig(this.application, true))

        val test = Test()
        test.onLifeStart(9)

        runBlocking {
            test.getTime(0)
        }

        Test.fuck("cpacm")

        test.onLifeEnd()

        test.doSomeThing("cpacm", false, 1, 66.0, 'k', 4F, 6L, Test(), 10000)
    }

    @MLog(key = "test")
    override fun onResume() {
        super.onResume()
    }
}
