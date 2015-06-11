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
package com.wx3.cardbattle.game.gameevents;

import javax.persistence.Transient;

import com.wx3.cardbattle.game.GameEntity;

/**
 * Event fired when an entity attacks another entity.
 * 
 * @author Kevin
 *
 */
public final class AttackEvent extends GameEvent {

	@SuppressWarnings("unused")
	private int attackerId;
	@SuppressWarnings("unused")
	private int targetId;
	
	@Transient
	public GameEntity attacker;
	@Transient
	public GameEntity target;
	
	public AttackEvent(GameEntity attacker, GameEntity target) {
		this.attacker = attacker;
		this.target = target;
		this.attackerId = attacker.getId();
		this.targetId = attacker.getId();
	}
}
