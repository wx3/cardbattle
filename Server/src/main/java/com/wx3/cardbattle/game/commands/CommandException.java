package com.wx3.cardbattle.game.commands;

public class CommandException extends Exception {
	
	private static final long serialVersionUID = 6001L;
	
	private GameCommand command;

	public CommandException(GameCommand command, String message) {
		super(message);
		this.command = command;
	}
	
	public CommandException(GameCommand command, String message, Throwable throwable) {
		super(message, throwable);
		this.command = command;
	}
	
	public GameCommand getCommand() {
		return command;
	}
	
}
