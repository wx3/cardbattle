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
package com.wx3.cardbattle.samplegame.events;

import javax.persistence.Transient;

import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.gameevents.GameEvent;

/**
 * Fired whenever a minion is brought into play.
 * 
 * @author Kevin
 *
 */
public final class SummonMinionEvent extends GameEvent {
	
	public int entityId;
	
	@Transient
	public GameEntity minion;
	
	public SummonMinionEvent(GameEntity minion) {
		this.minion = minion;
		this.entityId = minion.getId();
	}

}
