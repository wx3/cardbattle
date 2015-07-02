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

import com.wx3.cardbattle.game.commands.GameCommand;
import com.wx3.cardbattle.game.commands.ValidationResult;
import com.wx3.cardbattle.server.OutboundMessage;

/**
 * Tells the player whether the command was accepted. If not,
 * includes an error. If the client supplied an optional commandId with
 * the command, this is included in the response so the client can
 * determine which command it is a response to.
 * 
 * @author Kevin
 *
 */
public class CommandResponseMessage extends OutboundMessage {

	private int commandId;
	private boolean isSuccess;
	private String errorMsg;
	private ValidationResult result;
	
	public CommandResponseMessage(GameCommand command, ValidationResult result) {
		this.messageClass = this.getClass().getSimpleName();
		this.commandId = command.getId();
		this.isSuccess = result.isValid();
		if(!isSuccess) {
			this.errorMsg = "There were 1 or more validation errors with the command.";
		}
		this.result = result;
	}
	
	public CommandResponseMessage(GameCommand command, boolean isSuccess, String errorMsg) {
		this.messageClass = this.getClass().getSimpleName();
		this.isSuccess = isSuccess;
		this.errorMsg = errorMsg;
		this.commandId = command.getId();
	}
	
	public boolean isSuccess() {
		return isSuccess;
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}

}
