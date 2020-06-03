package com.anchith.tictactoe

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

lateinit var buttonList: List<Button>
lateinit var startButton: Button
lateinit var spinner: Spinner

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

		startButton = findViewById(R.id.le_button)
		spinner = findViewById(R.id.spinner)

		val spinner = findViewById<Spinner>(R.id.spinner)
		val adapter = MyMediaAdapter(this, listOf("X", "O", null), listOf(false, false, true))
		spinner.adapter = adapter
		spinner.setSelection(2)
	}

	fun start(view: View)
	{
		for (button in buttonList)
		{
			button.text = ""
			button.visibility = View.VISIBLE
		}

		view.visibility = View.GONE
		spinner.visibility = View.GONE

		scope.launch { play(playAs = spinner.selectedItem?.toString()) }
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

	private suspend fun play(seed: Boolean = ((0..1).random() == 1), playAs: String?)
	{
		Player.symbol = playAs ?: listOf("X", "O").random()
		Player.isPlayerTurn = seed

		while (!withContext(MainScope().coroutineContext) { win() })
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

			spinner.visibility = View.VISIBLE
			startButton.visibility = View.VISIBLE
		}
	}

	private fun win(): Boolean
	{
		val toast = Toast.makeText(applicationContext, "", Toast.LENGTH_LONG)

		for (i in 0..8 step 3)
		{
			if (buttonList[i].text == buttonList[i + 1].text
				&& buttonList[i].text == buttonList[i + 2].text)
				if (buttonList[i].text != "")
				{
					toast.setText("${buttonList[i].text} wins!")
					toast.show()
					return true
				}
		}

		for (i in 0..2)
		{
			if (buttonList[i].text == buttonList[i + 3].text
				&& buttonList[i].text == buttonList[i + 6].text)
				if (buttonList[i].text != "")
				{
					toast.setText("${buttonList[i].text} wins!")
					toast.show()
					return true
				}
		}

		if (buttonList[0].text == buttonList[4].text
			&& buttonList[4].text == buttonList[8].text)
			if (buttonList[4].text != "")
			{
				toast.setText("${buttonList[4].text} wins!")
				toast.show()
				return true
			}

		if (buttonList[2].text == buttonList[4].text
			&& buttonList[4].text == buttonList[6].text)
			if (buttonList[4].text != "")
			{
				toast.setText("${buttonList[4].text} wins!")
				toast.show()
				return true
			}

		if (buttonList.all { it.text != "" })
		{
			toast.setText("Draw!")
			toast.show()
			return true
		}

		return false
	}
}
