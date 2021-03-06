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
package com.wx3.samplegame.events;

import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.gameevents.GameEvent;

/**
 * A DrawCardEvent is fired whenever a player draws a card.
 * 
 * @author Kevin
 *
 */
public final class DrawCardEvent extends GameEvent {

	@SuppressWarnings("unused")
	private String playerName;
	@SuppressWarnings("unused")
	private int entityId;

	public DrawCardEvent(String playerName, GameEntity entity, GameEntity cause) {
		this.playerName = playerName;
		this.entityId = entity.getId();
		if(cause != null) {
			this.cause = cause;
			this.causeId = cause.getId();
		}
	}

}
