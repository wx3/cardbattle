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
	
	public ValidationResult validate()  {
		ValidationResult result = new ValidationResult();
		if(player == null) {
			result.addError("Player is null");
		}
		if(game == null) {
			result.addError("Game is null");
		}
		if(game.getRuleEngine().getCurrentPlayer() != player) {
			result.addError("Not your turn");
		}
		return result;
	}
	
	public abstract void execute(); 
	
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
