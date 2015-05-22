package com.wx3.cardbattle.game.messages;

import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.commands.GameCommand;

/**
 * Tells the player whether the command was accepted. If not,
 * includes an error. If the client supplied an optional commandId with
 * the command, this is included in the response so the client can
 * determine which command it is a response to.
 * 
 * @author Kevin
 *
 */
public class CommandResponseMessage extends GameMessage {

	private int commandId;
	private boolean isSuccess;
	private String errorMsg;
	
	public CommandResponseMessage(GameCommand command, boolean isSuccess) {
		this.messageClass = this.getClass().getSimpleName();
		this.isSuccess = isSuccess;
		this.commandId = command.getId();
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
