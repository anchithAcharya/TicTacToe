package com.anchith.tictactoe

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.runBlocking

lateinit var buttonGrid: MutableList<Button>

class MainActivity : AppCompatActivity() {
	private var playerTurn: Boolean = true
	private var turnEnded: Boolean = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.button)

		buttonGrid = mutableListOf(findViewById<Button>(R.id.button_1_1))
		buttonGrid.add(findViewById<Button>(R.id.button_1_2))
		buttonGrid.add(findViewById<Button>(R.id.button_1_3))
		buttonGrid.add(findViewById<Button>(R.id.button_2_1))
		buttonGrid.add(findViewById<Button>(R.id.button_2_2))
		buttonGrid.add(findViewById<Button>(R.id.button_2_3))
		buttonGrid.add(findViewById<Button>(R.id.button_3_2))
		buttonGrid.add(findViewById<Button>(R.id.button_3_1))
		buttonGrid.add(findViewById<Button>(R.id.button_3_3))
	}

	fun start(view:View)
	{
		view.visibility = View.GONE
//		suspend { play((0..1).random() == 1) }
		play(false)
	}

	fun change(view:View)
	{
		val button = view as Button

		val mark = when(playerTurn)
		{
			true -> "X"
			false -> "O"
		}

		if(button.text == "")
		{
			button.text = mark
		}

		if(playerTurn) turnEnded = true
	}

	private fun aiPlay()
	{
		while(true)
		{
			val num = (0..8).random()

			if(buttonGrid[num].text == "")
			{
				buttonGrid[num].performClick()
				break
			}
		}
	}

	private fun play(seed: Boolean)
	{
		playerTurn = seed

		while(!win())
		{
			if(playerTurn)
			{
				turnEnded = false

				for(view in buttonGrid)
				{
					view.isClickable = true
					view.requestLayout()
				}


				runBlocking { while(!turnEnded); }

				for(view in buttonGrid)
					view.isClickable = false
			}

			else aiPlay()

			playerTurn = !playerTurn
		}

		val button = findViewById<Button>(R.id.le_button)
		button.text = "Restart"
		button.visibility = View.VISIBLE
	}

	private fun win():Boolean
	{
		for(i in 0..8 step 3)
		{
			if(buttonGrid[i].text == buttonGrid[i+1].text
				&& buttonGrid[i].text == buttonGrid[i+2].text)
				if(buttonGrid[i].text != "") return true
		}

		for(i in 0..2)
		{
			if(buttonGrid[i].text == buttonGrid[i+3].text
				&& buttonGrid[i].text == buttonGrid[i+6].text)
				if(buttonGrid[i].text != "") return true
		}

		if(buttonGrid[0].text == buttonGrid[4].text
			&& buttonGrid[4].text == buttonGrid[8].text)
			if(buttonGrid[4].text != "") return true

		if(buttonGrid[2].text == buttonGrid[4].text
			&& buttonGrid[4].text == buttonGrid[6].text)
			if(buttonGrid[4].text != "") return true

		return false
	}
}
