/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Kevin Lin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *******************************************************************************/
package com.wx3.cardbattle.game.commands;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Creates commands from a command name and JSON
 * 
 * @author Kevin
 *
 */
public class JsonCommandFactory {

	public GameCommand createCommand(String commandName, JsonElement jsonElement) {
		if(jsonElement == null) {
			throw new RuntimeException("Suppied Json cannot be null");
		}
		GameCommand command = null;
		Gson gson = new Gson();
		switch(commandName) {
			case "Chat" : command = gson.fromJson(jsonElement, ChatCommand.class);
				break;
			case "EndTurn" : command = gson.fromJson(jsonElement, EndTurnCommand.class);
				break;
			case "PlayCard" : command = gson.fromJson(jsonElement, PlayCardCommand.class);
				break;
			case "Attack" : command = gson.fromJson(jsonElement, AttackCommand.class);
				break;
			default : throw new RuntimeException("Invalid command '" + commandName + "'");
		}
		return command;
	}
}
