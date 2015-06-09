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
 * Rule scripts should not be modified during the course of play.
 * 
 * @author Kevin
 *
 */
@Entity
@Table(name="rules")
public class EntityRule {
	
	@Id
	private String id;
	
	private String description;
	
	/**
	 * The simple name of the GameEvent that triggers this rule
	 */
	private String eventTrigger;
	
	/**
	 * The script code that is eval'd when the trigger is fired.
	 */
	private String triggeredScript;
	
	private boolean permanent;
	
	public EntityRule() {}
	
	public EntityRule(Class<? extends GameEvent> trigger, String script, String id, String description) {
		this.eventTrigger = trigger.getSimpleName();
		this.triggeredScript = script;
		this.id = id;
		this.description = description;
	}
	
	public EntityRule(EntityRule rule) {
		this.eventTrigger = rule.eventTrigger;
		this.triggeredScript = rule.triggeredScript;
		this.id = rule.id;
		this.description = rule.description;
		this.permanent = rule.permanent;
	}
	
	public String getId() {
		return id;
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
	
	public boolean isPermanent() {
		return permanent;
	}

	public String getScript() {
		return triggeredScript;
	}
	
	@Override
	public String toString() {
		return "EntityRule." + description;
	}

}
