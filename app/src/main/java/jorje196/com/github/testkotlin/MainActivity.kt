package jorje196.com.github.testkotlin

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast

/**
 * Тестирование обработки прохождения приложения через различные состояния
 * (нажатия кнопок и повороты) + незатейливая дебилушка.
 * Принципы чистоты из-за простоты и для простоты применены ограничено.
 */
fun debugLog (tag: String, msg: String) {
    if (BuildConfig.DEBUG) {
        Log.d(tag, msg)
    }
}

/* TODO  Глянуть поведение на паузе.                                it's done
   TODO  Можно добавить звук и нарастание скорости отсчета
   TODO  Можно добавить сохранение результатов.                     it's done
   TODO  Можно добавить настройки (языки по рандому, период и пр.)
 */
    private const val defMaxScore = 0

class MainActivity : AppCompatActivity() {

    private lateinit var tapMeButton: ImageButton
    private val initHorizontalBias: Float = 0.5f
    private var horizontalBias = initHorizontalBias
    private val hEnumMin = 5
    private val hEnumMax = 95
    private val vEnumMin = 3
    private val vEnumMax = 97
    private val initVerticalBias: Float = 0.5f
    private var verticalBias = initVerticalBias
    private lateinit var gameScoreTextView: TextView
    private lateinit var timeLeftTextView: TextView
    private var score = 0
    private var maxScore = defMaxScore
    private var gameStarted = false
    private lateinit var countDownTimer: CountDownTimer
    private val initialCountDown: Long = 30000
    private val countDownInterval: Long = 1000
    private val _tag = MainActivity::class.java.simpleName
    private var timeLeftOnTimer: Long = initialCountDown
    private var mConstraintSet = ConstraintSet()
    private lateinit var appSettings: SharedPreferences




    companion object {
            // Ключи сохраняемых при изменении конфигурации параметров
        private const val SCORE_KEY = "SCORE_KEY"
        private const val TIME_LEFT_KEY = "TIME_LEFT_KEY"
        private const val HORIZONTAL_BIAS = "HORIZONTAL_BIAS"
        private const val VERTICAL_BIAS = "VERTICAL_BIAS"
        private const val GAME_STARTED = "GAME_STARTED"
            // save settings for
        private const val APP_PROPERTIES = "app_settings"
        private const val MAX_SCORE = "MAX_SCORE"

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Отличное имя активности и приложения
        this.title = resources.getString(R.string.act_name)

        appSettings = getSharedPreferences(APP_PROPERTIES, Context.MODE_PRIVATE)
        maxScore = appSettings.getInt(MAX_SCORE, defMaxScore)

        debugLog(_tag, "onCreate called. Score is: $score")

        tapMeButton = findViewById(R.id.tap_me_button)
                        // full form: findViewById<Button>(R.id.tap_me_button)
        gameScoreTextView = findViewById(R.id.game_score_text_view)
        timeLeftTextView = findViewById(R.id.time_left_text_view)
        mConstraintSet.clone( findViewById<ConstraintLayout>(R.id.root))

        if (savedInstanceState != null && savedInstanceState.getBoolean(GAME_STARTED)) {
            gameStarted = savedInstanceState.getBoolean(GAME_STARTED)
            score = savedInstanceState.getInt(SCORE_KEY)
            timeLeftOnTimer = savedInstanceState.getLong(TIME_LEFT_KEY)
            horizontalBias = savedInstanceState.getFloat(HORIZONTAL_BIAS)
            verticalBias = savedInstanceState.getFloat(VERTICAL_BIAS)
            restoreGame()
        } else {
            resetGame()
        }

        tapMeButton.setOnClickListener { view ->
            val bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce)
            view.startAnimation(bounceAnimation)
            incrementScore()
            changeTapMeLocation()
        }
    }

    private fun incrementScore() {
        if (!gameStarted) startGame()
        score += 1
        val newScore = getString(R.string.game_score, score.toString())
        gameScoreTextView.text = newScore
        val blinkAnimation = AnimationUtils.loadAnimation(this, R.anim.blink)
        gameScoreTextView.startAnimation(blinkAnimation)
    }

    private fun startGame() {
        countDownTimer.start()
        gameStarted = true
    }

    private fun changeTapMeLocation() {
        horizontalBias = (hEnumMin..hEnumMax).random().toFloat() / 100
        verticalBias = ( vEnumMin..vEnumMax).random().toFloat() / 100
        setTapMeLocation(horizontalBias, verticalBias)
    }

    private fun setTapMeLocation(xBias: Float = initHorizontalBias, yBias: Float = initVerticalBias) {
        mConstraintSet.setHorizontalBias(R.id.tap_me_button, xBias)
        mConstraintSet.setVerticalBias(R.id.tap_me_button, yBias)
        mConstraintSet.applyTo( findViewById(R.id.root))
    }

    private fun restoreGame() {
        gameScoreTextView.text = getString(R.string.game_score, score.toString())
        val restoredTime = timeLeftOnTimer /1000
        timeLeftTextView.text = getString(R.string.time_left, restoredTime.toString())

        countDownTimer = timer(timeLeftOnTimer, countDownInterval)

        setTapMeLocation(horizontalBias, verticalBias)
        countDownTimer.start()
    }

    private fun resetGame() {
        score = 0
        gameScoreTextView.text = getString(R.string.game_score, score.toString())
        val initialTimeLeft = initialCountDown / 1000
        timeLeftTextView.text = getString(R.string.time_left, initialTimeLeft.toString())
        countDownTimer = timer(initialCountDown, countDownInterval)
        setTapMeLocation()
        gameStarted = false
    }

    private fun timer(time: Long, interval: Long) : CountDownTimer {
        return object: CountDownTimer(time, interval) {
            override fun onTick(millisUnitFinished: Long) {
                timeLeftOnTimer = millisUnitFinished
                val timeLeft = millisUnitFinished / 1000
                timeLeftTextView.text = getString(R.string.time_left, timeLeft.toString())
            }
            override fun onFinish() {
                endGame()
            }
        }
    }

    private fun endGame() {
        Toast.makeText(this, getString(R.string.game_over_massage,
            score.toString(), maxScore.toString()), Toast.LENGTH_LONG).show()
        if (score > maxScore) {
            maxScore = score
            val appSettingEditor = appSettings.edit()
            appSettingEditor.putInt(MAX_SCORE, maxScore).apply()
        }
        resetGame()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(GAME_STARTED, gameStarted)
        outState.putInt(SCORE_KEY, score)
        outState.putLong(TIME_LEFT_KEY, timeLeftOnTimer)
        outState.putFloat(HORIZONTAL_BIAS, horizontalBias)
        outState.putFloat(VERTICAL_BIAS, verticalBias)
        countDownTimer.cancel()
        debugLog(_tag, "onSaveInstanceState: Saving score = $score & Time Left = $timeLeftOnTimer")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item!!.itemId) {
            R.id.action_about ->  showInfo()
            R.id.save_result -> saveResult()
            R.id.reset_result -> resetResult()
            else -> {
                nothing()
                return false
            }

        }
        return true
    }

    private fun resetResult() {
        maxScore = defMaxScore
        val appSettingEditor = appSettings.edit()
        appSettingEditor.putInt(MAX_SCORE, maxScore).apply()
        val toastMessage = getString(R.string.reset_message, maxScore.toString())
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
    }

    private fun showInfo() {
        val dialogTitle = getString(R.string.about_title, BuildConfig.VERSION_NAME)
        val dialogMessage = getString(R.string.about_message)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(dialogTitle)
        builder.setMessage(dialogMessage)
        builder.create().show()
    }

    private fun saveResult() {
        val toastMessage = getString(R.string.save_message, score.toString())
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
    }

    private fun nothing() {

    }

    override fun onStart() {
        super.onStart()
        debugLog(_tag, "onStart: Saving score = $score & Time Left = $timeLeftOnTimer")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        debugLog(_tag, "onRestoreInstanceState: Saving score = $score & Time Left = $timeLeftOnTimer")
    }

    override fun onResume() {
        super.onResume()
        debugLog(_tag, "onResume: Saving score = $score & Time Left = $timeLeftOnTimer")
    }

    override fun onPause() {
        super.onPause()
        debugLog(_tag, "onPause: Saving score = $score & Time Left = $timeLeftOnTimer")
    }

    override fun onStop() {
        super.onStop()
        debugLog(_tag, "onStop: Saving score = $score & Time Left = $timeLeftOnTimer")

    }

    override fun onRestart() {
        super.onRestart()
        if (gameStarted) restoreGame() else resetGame()
        debugLog(_tag, "onRestart called.")
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
 /*       if (propetiesChanged) {
            appSettingEditor.commit()
        } */
        debugLog(_tag, "onDestroy called.")
    }

}
