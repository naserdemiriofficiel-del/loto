package com.tiragechance.app

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import java.security.SecureRandom
import java.util.Locale

/**
 * Générateur de combinaisons aléatoires au format 5 numéros (1-50) + 2 étoiles (1-12).
 * Un gros bouton central lance un effet "machine à sous" : les boules tournent puis
 * s'arrêtent l'une après l'autre sur une nouvelle combinaison.
 */
class MainActivity : Activity() {

    private companion object {
        const val NUMBER_COUNT = 5
        const val NUMBER_MAX = 50
        const val STAR_COUNT = 2
        const val STAR_MAX = 12

        const val SPIN_TICK_MS = 55L
        const val FIRST_STOP_MS = 900L
        const val STOP_STEP_MS = 330L

        val COLOR_BG = Color.parseColor("#0E1319")
        val COLOR_PANEL = Color.parseColor("#161D26")
        val COLOR_STROKE = Color.parseColor("#2A3543")
        val COLOR_GOLD = Color.parseColor("#E0B04A")
        val COLOR_GOLD_LIGHT = Color.parseColor("#F3D58A")
        val COLOR_TEXT = Color.parseColor("#EEF2F6")
        val COLOR_MUTED = Color.parseColor("#8F9BAA")
        val COLOR_ON_GOLD = Color.parseColor("#2E2410")
        val COLOR_INK = Color.parseColor("#1F1809")
        val COLOR_STAR_BG = Color.parseColor("#1B1406")
    }

    private val random = SecureRandom()
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var balls: List<TextView>
    private lateinit var drawButton: TextView
    private var spinning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildUi())
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    // ---------------------------------------------------------------- UI

    private fun buildUi(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = GradientDrawable().apply {
                setGradientType(GradientDrawable.RADIAL_GRADIENT)
                setGradientRadius(resources.displayMetrics.heightPixels * 0.65f)
                setGradientCenter(0.5f, 0.42f)
                setColors(intArrayOf(Color.parseColor("#1B2634"), COLOR_BG))
            }
        }
        val basePad = dp(24)
        root.setPadding(basePad, basePad, basePad, basePad)
        @Suppress("DEPRECATION")
        root.setOnApplyWindowInsetsListener { v, insets ->
            v.setPadding(
                basePad + insets.systemWindowInsetLeft,
                basePad + insets.systemWindowInsetTop,
                basePad + insets.systemWindowInsetRight,
                basePad + insets.systemWindowInsetBottom
            )
            insets
        }

        // Titre
        root.addView(label("ÉTOILES D'OR", 20f, COLOR_GOLD_LIGHT, bold = true, spacing = 0.2f))
        root.addView(label("Générateur de combinaisons aléatoires", 13f, COLOR_MUTED).apply {
            (layoutParams as LinearLayout.LayoutParams).topMargin = dp(4)
        })

        root.addView(spacer(1f))

        // Panneau "machine"
        root.addView(buildMachinePanel(), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        root.addView(spacer(1f))

        // Gros bouton central
        drawButton = TextView(this).apply {
            text = "TIRER"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
            setTextColor(Color.parseColor("#2A1E06"))
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            letterSpacing = 0.1f
            gravity = Gravity.CENTER
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.parseColor("#FFF0B8"),
                    Color.parseColor("#F3D58A"),
                    Color.parseColor("#D9A93E"),
                    Color.parseColor("#F0CF7C"),
                    Color.parseColor("#B8892B")
                )
            ).apply { shape = GradientDrawable.OVAL }
            elevation = dp(10).toFloat()
            isClickable = true
            isFocusable = true
            setOnClickListener { startSpin() }
            setOnTouchListener { v, event ->
                when (event.actionMasked) {
                    android.view.MotionEvent.ACTION_DOWN ->
                        v.animate().scaleX(0.94f).scaleY(0.94f).setDuration(80).start()
                    android.view.MotionEvent.ACTION_UP,
                    android.view.MotionEvent.ACTION_CANCEL ->
                        v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }
                false
            }
        }
        val buttonSize = dp(160)
        root.addView(drawButton, LinearLayout.LayoutParams(buttonSize, buttonSize))

        root.addView(spacer(1f))

        // Mention légale
        root.addView(
            label(
                "Divertissement uniquement : cette application génère des numéros au hasard, " +
                    "ne prédit aucun résultat et n'augmente pas vos chances de gagner. " +
                    "Elle n'est affiliée ni à l'Euromillions ni à aucun opérateur de jeux. " +
                    "Jouer comporte des risques : endettement, isolement, dépendance.",
                11f, COLOR_MUTED
            ).apply { gravity = Gravity.CENTER }
        )

        return root
    }

    private fun buildMachinePanel(): View {
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.parseColor("#D9BE82"),
                    Color.parseColor("#B89A5E"),
                    Color.parseColor("#E6D09C"),
                    Color.parseColor("#F6E6AE"),
                    Color.parseColor("#D4B876"),
                    Color.parseColor("#B8995C")
                )
            ).apply {
                setStroke(dp(1), Color.argb(140, 255, 244, 205))
                cornerRadius = dp(22).toFloat()
            }
            setPadding(dp(16), dp(20), dp(16), dp(24))
            elevation = dp(4).toFloat()
        }

        // Liseré doré en haut de la machine
        panel.addView(View(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(Color.TRANSPARENT, Color.parseColor("#B36B5116"), Color.TRANSPARENT)
            )
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(2)).apply {
            bottomMargin = dp(18)
        })

        val available = resources.displayMetrics.widthPixels - dp(48) - dp(32)
        val ballSize = minOf(dp(60), available / NUMBER_COUNT - dp(8))

        val created = mutableListOf<TextView>()

        panel.addView(label("NUMÉROS", 11f, COLOR_ON_GOLD, spacing = 0.2f))
        val numbersRow = row()
        repeat(NUMBER_COUNT) {
            val ball = createBall(ballSize, isStar = false)
            created.add(ball)
            numbersRow.addView(ball)
        }
        panel.addView(numbersRow)

        panel.addView(label("ÉTOILES", 11f, COLOR_ON_GOLD, spacing = 0.2f).apply {
            (layoutParams as LinearLayout.LayoutParams).topMargin = dp(20)
        })
        val starsRow = row()
        repeat(STAR_COUNT) {
            val ball = createBall(ballSize, isStar = true)
            created.add(ball)
            starsRow.addView(ball)
        }
        panel.addView(starsRow)

        balls = created
        return panel
    }

    private fun createBall(size: Int, isStar: Boolean): TextView = TextView(this).apply {
        text = "?"
        setTextSize(TypedValue.COMPLEX_UNIT_PX, size * 0.38f)
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        gravity = Gravity.CENTER
        if (isStar) {
            setTextColor(COLOR_GOLD_LIGHT)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(COLOR_STAR_BG)
            }
        } else {
            setTextColor(COLOR_INK)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.argb(46, 255, 255, 255))
                setStroke(dp(2), COLOR_ON_GOLD)
            }
        }
        layoutParams = LinearLayout.LayoutParams(size, size).apply {
            setMargins(dp(4), dp(10), dp(4), 0)
        }
    }

    private fun row() = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
    }

    private fun label(
        text: String,
        sizeSp: Float,
        color: Int,
        bold: Boolean = false,
        spacing: Float = 0f
    ) = TextView(this).apply {
        this.text = text
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
        setTextColor(color)
        letterSpacing = spacing
        gravity = Gravity.CENTER
        if (bold) typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun spacer(weight: Float) = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(0, 0, weight).apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics
        ).toInt()

    // ------------------------------------------------------------ Tirage

    private fun startSpin() {
        if (spinning) return
        spinning = true
        drawButton.isEnabled = false
        drawButton.animate().alpha(0.55f).setDuration(150).start()
        drawButton.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

        val result = pickDistinct(NUMBER_COUNT, NUMBER_MAX) + pickDistinct(STAR_COUNT, STAR_MAX)
        var stopped = 0
        val start = SystemClock.uptimeMillis()

        balls.forEachIndexed { index, ball ->
            val stopAt = FIRST_STOP_MS + index * STOP_STEP_MS
            val max = if (index < NUMBER_COUNT) NUMBER_MAX else STAR_MAX

            val tick = object : Runnable {
                override fun run() {
                    if (SystemClock.uptimeMillis() - start >= stopAt) {
                        ball.text = format(result[index])
                        ball.scaleY = 1f
                        ball.animate().scaleX(1.18f).scaleY(1.18f).setDuration(110).withEndAction {
                            ball.animate().scaleX(1f).scaleY(1f).setDuration(140).start()
                        }.start()
                        ball.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        stopped++
                        if (stopped == balls.size) finishSpin()
                    } else {
                        ball.text = format(random.nextInt(max) + 1)
                        ball.scaleY = if (ball.scaleY < 1f) 1f else 0.9f
                        handler.postDelayed(this, SPIN_TICK_MS)
                    }
                }
            }
            handler.post(tick)
        }
    }

    private fun finishSpin() {
        spinning = false
        drawButton.isEnabled = true
        drawButton.animate().alpha(1f).setDuration(150).start()
    }

    /** Tire [count] valeurs distinctes entre 1 et [max], triées par ordre croissant. */
    private fun pickDistinct(count: Int, max: Int): List<Int> {
        val picked = linkedSetOf<Int>()
        while (picked.size < count) picked.add(random.nextInt(max) + 1)
        return picked.sorted()
    }

    private fun format(n: Int): String = String.format(Locale.ROOT, "%02d", n)
}
