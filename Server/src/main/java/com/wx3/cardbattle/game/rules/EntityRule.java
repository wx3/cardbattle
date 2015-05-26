package com.wx3.cardbattle.game.rules;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.google.common.base.Strings;
import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.gameevents.GameEvent;

/**
 * An entity rule is a script that is fired in response to a particular
 * GameEvent. The eventTrigger is the (simple) name of the GameEvent
 * class this rule can respond to. E.g., DrawCardEvent.
 * 
 * @author Kevin
 *
 */
@Entity
@Table(name="rules")
public class EntityRule {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	public String name;
	public String description;
	
	/**
	 * The simple name of the GameEvent that triggers this rule
	 */
	private String eventTrigger;
	
	/**
	 * The script code that is eval'd when the trigger is fired.
	 */
	private String triggeredScript;
	
	/**
	 * Permanent rules can't be removed during gameplay.
	 */
	private boolean permanent;
	
	@Transient
	private GameEntity entity;
	
	public EntityRule() {
		
	}
	
	public EntityRule(String trigger, String script) {
		this.eventTrigger = trigger;
		this.setScript(script);
	}
	
	public String getEventTrigger() {
		return eventTrigger;
	}
	
	public boolean isTriggered(String trigger) {
		if(trigger.equals(eventTrigger)) {
			return true;
		} else {
			return false;	
		}
	}
	
	public boolean isTriggered(GameEvent event) {
		// If event trigger is null (or empty) then we'll fire on a null event:
		if(event == null) {
			if(Strings.isNullOrEmpty(eventTrigger)) return true;
			return false;
		}
		// Otherwise we fire if the event class's simple name matches our trigger string: 
		String className = event.getClass().getSimpleName();
		return isTriggered(className);
	}

	public String getScript() {
		return triggeredScript;
	}

	public void setScript(String script) {
		this.triggeredScript = script;
	}

	public GameEntity getEntity() {
		return entity;
	}

	public void setEntity(GameEntity entity) {
		this.entity = entity;
	}

}
