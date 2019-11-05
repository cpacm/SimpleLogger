package com.cpacm.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.cpacm.annotations.*
import com.cpacm.sample.ui.main.MainFragment
import com.cpacm.logger.SimpleLogger
import com.cpacm.logger.SimpleLoggerConfig
import kotlinx.coroutines.runBlocking

@LifeLog(key = "lifecycle",level = LoggerLevel.VERBOSE)
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

    @LifeLogStart
    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    @LifeLogEnd
    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
