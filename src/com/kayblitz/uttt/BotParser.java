// Copyright 2016 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//  
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package com.kayblitz.uttt;

import java.util.Scanner;

import com.kayblitz.uttt.bot.Evaluation;
import com.kayblitz.uttt.bot.mcts.RAVETree;

/**
 * BotParser class
 * 
 * Main class that will keep reading output from the engine.
 * Will either update the bot state or get actions.
 * 
 * @author Jim van Eeden <jim@starapple.nl>
 */

public class BotParser {

	private final Scanner scanner;
	private final Bot bot;
	private Field field;

	public BotParser(Bot bot) {
		this.scanner = new Scanner(System.in);
		this.bot = bot;
	}

	public void run() {
		field = new Field();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.length() == 0)
				continue;
			
			String[] parts = line.split(" ");
			if(parts[0].equals("settings")) {
				if (parts[1].equals("your_botid")) {
					int botId = Integer.parseInt(parts[2]);
					bot.botId = botId;
					bot.opponentId = botId == 1 ? 2 : 1;
				}
			} else if (parts[0].equals("update") && parts[1].equals("heuristics")) {
				Evaluation.TWO_IN_A_ROW_OPTIMIZED = Double.valueOf(parts[2]);
				Evaluation.MIDDLE_OPTIMIZED = Double.valueOf(parts[3]);
				Evaluation.CORNER_OPTIMIZED = Double.valueOf(parts[4]);
				Evaluation.SIDE_OPTIMIZED = Double.valueOf(parts[5]);
				Evaluation.MINI_TWO_IN_A_ROW_OPTIMIZED = Double.valueOf(parts[6]);
				Evaluation.MINI_MIDDLE_OPTIMIZED = Double.valueOf(parts[7]);
				Evaluation.MINI_CORNER_OPTIMIZED = Double.valueOf(parts[8]);
				Evaluation.MINI_SIDE_OPTIMIZED = Double.valueOf(parts[9]);
			} else if (parts[0].equals("update") && parts[1].equals("rave")) {
				RAVETree.EXPLORATION_CONSTANT = Double.valueOf(parts[2]);
				RAVETree.RAVE_CONSTANT = Double.valueOf(parts[3]);
			} else if(parts[0].equals("update") && parts[1].equals("game")) { /* new game data */
			    field.parseGameData(parts[2], parts[3]);
			} else if(parts[0].equals("action")) {
				if (parts[1].equals("move")) { /* move requested */
					int timebank = Integer.parseInt(parts[2]);
					Move move = bot.makeMove(field, timebank, field.getMoveNum());
					System.out.println("place_move " + move.column + " " + move.row);
				}
			} else { 
				System.out.println("Unknown command: " + line);
			}
		}
	}
}
