package com.anchith.tictactoe

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.runBlocking

lateinit var buttonList: MutableList<Button>

class MainActivity : AppCompatActivity() {
	private var playerTurn: Boolean = true
	private var turnEnded: Boolean = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.button)

		buttonList = mutableListOf(findViewById<Button>(R.id.button_1_1))							//add all buttons to global buttonList for easy access throughout the class
		buttonList.add(findViewById<Button>(R.id.button_1_2))
		buttonList.add(findViewById<Button>(R.id.button_1_3))
		buttonList.add(findViewById<Button>(R.id.button_2_1))
		buttonList.add(findViewById<Button>(R.id.button_2_2))
		buttonList.add(findViewById<Button>(R.id.button_2_3))
		buttonList.add(findViewById<Button>(R.id.button_3_2))
		buttonList.add(findViewById<Button>(R.id.button_3_1))
		buttonList.add(findViewById<Button>(R.id.button_3_3))
	}

	fun start(view:View)																			//view refers to "Start" button
	{
		view.visibility = View.GONE                                                                 //view's visibility will be updated:
//		suspend { play((0..1).random() == 1) }                                                		//->immediately (parallel execution)
		play((0..1).random() == 1)															//->after control returns from play() and exits start()
	}

	fun click(view:View)
	{
		val button = view as Button

		if(button.text == "")																		//if button is empty:
		{
			button.text = when(playerTurn)															//	button =
			{
				true -> "X"																			//			 'X' if playerTurn is true (player clicked the button)
				false -> "O"																		//			 'O' if playerTurn is false (AI clicked the button)
			}																						//NOTE: 'X' and 'O' are fixed for player and AI respectively, for now.
		}

		if(playerTurn) turnEnded = true																//finished clicking button; turn is ended
	}

	private fun aiPlay()																			//click random button that is empty
	{
		while(true)
		{
			val num = (0..8).random()

			if(buttonList[num].text == "")
			{
				buttonList[num].performClick()
				break
			}
		}
	}

	private fun play(seed: Boolean)
	{
		playerTurn = seed																			//so that initial move is randomly selected between player and AI

		while(!win())																				//while there is no winner
		{
			if(playerTurn)																			//if it's player's turn
			{
				turnEnded = false																	//turn has started

				for(view in buttonList)
				{
					view.isClickable = true															//make all views clickable,
					view.invalidate()																//and refresh the views
				}

				runBlocking { while(!turnEnded); }													//while turn has not ended (this will be changed by click()) ???

				for(view in buttonList)
				{
					view.isClickable = false														//make all the views unclickable again,
					view.invalidate()																//and refresh them
				}

			}

			else aiPlay()																			//if not player's turn, let AI make its move

			playerTurn = !playerTurn																//change the next turn
		}

		val button = findViewById<Button>(R.id.le_button)
		button.text = "Restart"																		//if someone has won, display the "restart" button
		button.visibility = View.VISIBLE															//TODO: show win message (who has won)
	}

	private fun win():Boolean
	{
		for(i in 0..8 step 3)
		{
			if(buttonList[i].text == buttonList[i+1].text											//check if all buttons in row have same symbol
				&& buttonList[i].text == buttonList[i+2].text)
				if(buttonList[i].text != "") return true
		}

		for(i in 0..2)
		{
			if(buttonList[i].text == buttonList[i+3].text											//check if all buttons in column have same symbol
				&& buttonList[i].text == buttonList[i+6].text)
				if(buttonList[i].text != "") return true
		}

		if(buttonList[0].text == buttonList[4].text													//check if all buttons in left diagonal have same symbol
			&& buttonList[4].text == buttonList[8].text)
			if(buttonList[4].text != "") return true

		if(buttonList[2].text == buttonList[4].text													//check if all buttons in right diagonal have same symbol
			&& buttonList[4].text == buttonList[6].text)
			if(buttonList[4].text != "") return true

		return false																				//nobody has won yet
	}
}
