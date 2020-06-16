package com.anchith.tictactoe

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BlendMode
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

lateinit var symbolSpinner: Spinner
lateinit var buttonList: List<Button>
lateinit var startButton: Button

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity()
{
	object Player
	{
		var isPlayerTurn: Boolean = true
		var turnEnded: Boolean = false
		var symbol: String = ""

		fun altSymbol(symbol: String = Player.symbol) = when (symbol)
		{
			"X" -> "O"
			"O" -> "X"
			else -> null
		}
	}

	private val scope = CoroutineScope(Dispatchers.IO)

	class MyMediaAdapter(private val context: Context,
						 private val content: List<String?>,
						 private val isImage: List<Boolean>) : BaseAdapter()
	{
		private val inflater = LayoutInflater.from(context)

		override fun getItem(p0: Int): Any? = content[p0]

		override fun getItemId(p0: Int): Long = 0

		override fun getCount(): Int = isImage.size

		@SuppressLint("InflateParams")
		override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View
		{
			val view: View

			if (isImage[p0])
			{
				view = inflater.inflate(R.layout.spinner_image, null) as ImageView
				view.setImageDrawable(androidx.appcompat.content.res.AppCompatResources.getDrawable(
					context,
					R.drawable.random_icon_24
																								   )
									 )
			}
			else
			{
				view = inflater.inflate(R.layout.spinner_text, null) as TextView
				view.text = content[p0]
			}

			return view
		}
	}

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		buttonList =
			listOf(findViewById(R.id.button_1_1),                            //add all buttons to global buttonList for easy access throughout the class
				findViewById(R.id.button_1_2),
				findViewById(R.id.button_1_3),
				findViewById(R.id.button_2_1),
				findViewById(R.id.button_2_2),
				findViewById(R.id.button_2_3),
				findViewById(R.id.button_3_1),
				findViewById(R.id.button_3_2),
				findViewById(R.id.button_3_3)
				  )

		for (button in buttonList)
		{
			button.text = "TicTacToe".elementAt(buttonList.indexOf(button)).toString()
		}

		symbolSpinner = findViewById(R.id.spinner)
		startButton = findViewById(R.id.start_button)

		val adapter = MyMediaAdapter(this, listOf("X", "O", null), listOf(false, false, true))
		symbolSpinner.adapter = adapter
		symbolSpinner.setSelection(2)
	}

	fun start(view: View)
	{
		for (button in buttonList)
		{
			button.apply {
				text = ""
				isEnabled = true
				backgroundTintList = ContextCompat.getColorStateList(applicationContext,
					R.color.playable_buttons_colour
																	)

				@RequiresApi(Build.VERSION_CODES.Q)
				backgroundTintBlendMode = BlendMode.SRC_ATOP

			}
		}

		view.visibility = View.GONE
		symbolSpinner.visibility = View.GONE

		scope.launch { play(playAs = symbolSpinner.selectedItem?.toString()) }
	}

	fun click(view: View)
	{
		for (button in buttonList)
		{
			button.isClickable = false
			button.postInvalidate()
		}

		val button = view as Button

		if (button.text == "")
		{
			button.text = when (Player.isPlayerTurn)
			{
				true -> Player.symbol
				false -> Player.altSymbol()
			}

			val shadowColor = when (button.text)
			{
				"X" -> getColor(R.color.color_X)
				else -> getColor(R.color.color_O)
			}

			button.setTextColor(shadowColor)
			button.apply { setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor) }
		}
		else Player.isPlayerTurn = !Player.isPlayerTurn

		Player.turnEnded = true
	}

	private suspend fun aiPlay()
	{
		while (true)
		{
			val num = (0..8).random()

			if (buttonList[num].text == "")
			{
				withContext(Dispatchers.Main.immediate)
				{
					buttonList[num].performClick()
					buttonList[num].invalidate()
				}

				break
			}
		}
	}

	@RequiresApi(Build.VERSION_CODES.Q)
	private suspend fun play(seed: Boolean = ((0..1).random() == 1), playAs: String?)
	{
		Player.symbol = playAs ?: listOf("X", "O").random()
		Player.isPlayerTurn = seed

		withContext(Dispatchers.Main.immediate) {
			findViewById<TextView>(R.id.result_text).text = "You are ${Player.symbol}"
		}

		while (!withContext(MainScope().coroutineContext) { isWin() })
		{
			if (Player.isPlayerTurn)
			{
				Player.turnEnded = false

				withContext(Dispatchers.Main.immediate)
				{
					for (view in buttonList)
					{
						view.isClickable = true
						view.invalidate()
					}
				}

				while (!Player.turnEnded) delay(1000)
			}
			else aiPlay()
			Player.isPlayerTurn = !Player.isPlayerTurn
		}

		withContext(Dispatchers.Main.immediate)
		{
			startButton.text = getString(R.string.restart_label)

			symbolSpinner.visibility = View.VISIBLE
			startButton.visibility = View.VISIBLE
		}
	}

	@RequiresApi(Build.VERSION_CODES.Q)
	private fun isWin(): Boolean
	{
		fun makeTrio(a: Int, b: Int, c: Int) = listOf(buttonList[a], buttonList[b], buttonList[c])
		fun changeBackground(trio: List<Button>, color: Int) = trio.forEach {
			it.backgroundTintList =
				ContextCompat.getColorStateList(this, color); it.backgroundTintBlendMode =
			BlendMode.SCREEN
		}

		fun buttonTextEqual(temp: List<Button>): CharSequence?
		{
			return if ((temp[0].text == temp[1].text) && (temp[1].text == temp[2].text) && (temp[1].text != ""))
				temp[1].text
			else null
		}

		fun onWin(trio: List<Button>, color: Int)
		{
			changeBackground(trio, color)
			findViewById<TextView>(R.id.result_text).text = "${trio[0].text} wins!"
		}

		val ranges =
			listOf(listOf(0, 3, 6, 1), listOf(0, 1, 2, 3), listOf(0, 4, 8, 0), listOf(2, 4, 6, 0))


		for (range in ranges)
		{
			var trio: List<Button>

			if (range[3] == 0)
			{
				trio = makeTrio(range[0], range[1], range[2])

				when (buttonTextEqual(trio))
				{
					"X" ->
					{
						onWin(trio, R.color.color_X); return true
					}
					"O" ->
					{
						onWin(trio, R.color.color_O); return true
					}
					else -> Unit
				}
			}
			else
			{
				for (i in range.subList(0, 3))
				{
					trio = makeTrio(i, i + range[3], i + (range[3] * 2))

					when (buttonTextEqual(trio))
					{
						"X" ->
						{
							onWin(trio, R.color.color_X); return true
						}
						"O" ->
						{
							onWin(trio, R.color.color_O); return true
						}
						else -> Unit
					}
				}
			}
		}

		if (buttonList.all { it.text != "" })
		{
			findViewById<TextView>(R.id.result_text).text = "Draw!"
			return true
		}

		return false
	}

	fun revealInfo(view: View)
	{
		val about = findViewById<TextView>(R.id.about)

		when (about.visibility)
		{
			View.VISIBLE -> about.animateVisibility(false)
			View.GONE -> about.animateVisibility(true)
			else -> Unit
		}
	}

	private fun View.animateVisibility(setVisible: Boolean)
	{
		if (setVisible)
		{
			this.scaleX = 0F
			this.scaleY = 0F

			animateView(this, true)
		}
		else animateView(this, false)
	}

	private fun animateView(v: View, expand: Boolean)
	{
		val valueAnimator = ValueAnimator.ofInt(0, 100)

		valueAnimator.addListener(object : Animator.AnimatorListener
		{

			override fun onAnimationStart(animation: Animator)
			{
				if (expand) v.visibility = View.VISIBLE
			}

			override fun onAnimationEnd(animation: Animator)
			{
				if (!expand) v.visibility = View.GONE
			}

			override fun onAnimationCancel(animation: Animator)
			{
			}

			override fun onAnimationRepeat(animation: Animator)
			{
			}
		})

		valueAnimator.addUpdateListener { animation ->
			v.scaleX = animation.animatedFraction
			v.scaleY = animation.animatedFraction
			(v as TextView).textScaleX = animation.animatedFraction
			v.alpha = animation.animatedFraction
			v.requestLayout()
		}

		valueAnimator.duration = 300
		valueAnimator.interpolator = DecelerateInterpolator()

		when (expand)
		{
			true -> valueAnimator.start()
			false -> valueAnimator.reverse()
		}
	}
}
