package com.wx3.cardbattle.game.commands;

public class ChatCommand extends GameCommand {
	
	private String message;
	
	public ChatCommand() {}
	
	@Override
	public ValidationResult validate() {
		ValidationResult result = new ValidationResult();
		if(game == null) {
			result.addError("Game is null");
		}
		return result;
	}
	
	@Override
	public void execute() {
		game.getRuleEngine().chat(player, message);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
