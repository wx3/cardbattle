package com.wx3.cardbattle.game.commands;

import javax.persistence.Transient;

import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.messages.CommandResponseMessage;

/**
 * A GameCommand is how a player communicates actions to the game.
 * 
 * @author Kevin
 *
 */
public abstract class GameCommand {
	
	@Transient
	protected GameInstance game;
	@Transient
	protected GamePlayer player;
	
	protected int id;
	
	/**
	 * Parsing the command resolves id references. Eg., if the command refers
	 * to an entity, parsing will populate the entity property.
	 */
	public void parse() {
		
	}
	
	public void validate() throws CommandException {
		if(player == null) {
			throw new CommandException(this, "Player is null");
		}
		if(game == null) {
			throw new CommandException(this, "Game is null");
		}
		if(game.getRuleEngine().getCurrentPlayer() != player) {
			throw new CommandException(this, "Not your turn");
		}
	}
	
	public abstract CommandResponseMessage execute();
	
	public void setGameInstance(GameInstance game) {
		this.game = game;
	}
	
	public int getId() {
		return id;
	}
	
	public GamePlayer getPlayer() {
		return player;
	}
	
	public void setPlayer(GamePlayer player) {
		this.player = player;
	}
}
