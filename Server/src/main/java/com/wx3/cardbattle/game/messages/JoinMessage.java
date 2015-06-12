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
package com.wx3.cardbattle.game.messages;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.wx3.cardbattle.game.Card;

/**
 * Message sent to the client on join, containing initial game
 * data like all the card prototypes.
 * 
 * @author Kevin
 *
 */
public final class JoinMessage extends GameMessage {

	private Map<Integer, Card> cards;
	
	public static JoinMessage createMessage(Collection<Card> cardList) {
		JoinMessage message = new JoinMessage();
		message.cards = new HashMap<Integer, Card>();
		for(Card card : cardList) {
			message.cards.put(card.getId(), card);
		}
		message.messageClass = JoinMessage.class.getSimpleName();
		return message;
	}
	
}
