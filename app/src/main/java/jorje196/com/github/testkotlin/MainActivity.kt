package jorje196.com.github.testkotlin

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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    private lateinit var tapMeButton: Button
    private val initHorizontalBias: Float = 0.5f
    private var horizontalBias = initHorizontalBias
    private val hEnumMin = 5
    //private val hEnumStep = 1
    private val hEnumMax = 95
    private val vEnumMin = 3
    //private val vEnumStep = 1
    private val vEnumMax = 97
    private val initVerticalBias: Float = 0.5f
    private var verticalBias = initVerticalBias
    private lateinit var gameScoreTextView: TextView
    private lateinit var timeLeftTextView: TextView
    private var score = 0
    private var gameStarted = false
    private lateinit var countDownTimer: CountDownTimer
    private val initialCountDown: Long = 30000
    private val countDownInterval: Long = 1000
    private val TAG = MainActivity::class.java.simpleName
    private var timeLeftOnTimer: Long = initialCountDown
    private var mConstraintSet = ConstraintSet()


    companion object {
        private const val SCORE_KEY = "SCORE_KEY"
        private const val TIME_LEFT_KEY = "TIME_LEFT_KEY"
        private const val HORIZONTAL_BIAS = "HORIZONTAL_BIAS"
        private const val VERTICAL_BIAS = "VERTICAL_BIAS"
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate called. Score is: $score")

        tapMeButton = findViewById<Button>(R.id.tap_me_button)  // as exp
        gameScoreTextView = findViewById(R.id.game_score_text_view)
        timeLeftTextView = findViewById(R.id.time_left_text_view)
        mConstraintSet.clone( findViewById<ConstraintLayout>(R.id.root))

        if (savedInstanceState != null) {
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

    private fun restoreGame() {
        gameScoreTextView.text = getString(R.string.game_score, score.toString())
        val restoredTime = timeLeftOnTimer /1000
        timeLeftTextView.text = getString(R.string.time_left, restoredTime.toString())

        countDownTimer = object : CountDownTimer(timeLeftOnTimer, countDownInterval) {
            override fun onTick(millisUnitFinished: Long) {
                timeLeftOnTimer = millisUnitFinished
                val timeLeft = millisUnitFinished / 1000
                timeLeftTextView.text = getString(R.string.time_left, timeLeft.toString())
            }
            override fun onFinish() {
                endGame()
            }
        }
        setTapMeLocation(horizontalBias, verticalBias)
        countDownTimer.start()
        gameStarted = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(SCORE_KEY, score)
        outState.putLong(TIME_LEFT_KEY, timeLeftOnTimer)
        outState.putFloat(HORIZONTAL_BIAS, horizontalBias)
        outState.putFloat(VERTICAL_BIAS, verticalBias)
        countDownTimer.cancel()
        Log.d(TAG, "onSaveInstanceState: Saving score = $score & Time Left = $timeLeftOnTimer")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called.")
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
            else -> {
                nothing()
                return false
            }

        }
        return true
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
        // TODO save result
        val toastMessage = getString(R.string.save_message, score.toString())
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
    }

    private fun nothing() {

    }

    private fun resetGame() {
        score = 0
        gameScoreTextView.text = getString(R.string.game_score, score.toString())
        val initialTimeLeft = initialCountDown / 1000
        timeLeftTextView.text = getString(R.string.time_left, initialTimeLeft.toString())

        countDownTimer = object: CountDownTimer(initialCountDown, countDownInterval) {
            override fun onTick(millisUnitFinished: Long) {
                timeLeftOnTimer = millisUnitFinished
                val timeLeft = millisUnitFinished / 1000
                timeLeftTextView.text = getString(R.string.time_left, timeLeft.toString())
            }

            override fun onFinish() {
                endGame()
            }
        }
        setTapMeLocation()
        gameStarted = false
    }

    private fun startGame() {
        countDownTimer.start()
        gameStarted = true
    }

    private fun endGame() {
        Toast.makeText(this, getString(R.string.game_over_massege, score.toString()), Toast.LENGTH_LONG).show()
        resetGame()
    }

    private fun incrementScore() {
        if (!gameStarted) startGame()
        score += 1
        val newScore = getString(R.string.game_score, score.toString())
        gameScoreTextView.text = newScore
        val blinkAnimation = AnimationUtils.loadAnimation(this, R.anim.blink)
        gameScoreTextView.startAnimation(blinkAnimation)
    }
}


