package com.wx3.cardbattle.game.rules;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.gameevents.GameEvent;

/**
 * The RuleProcessor uses the Nashorn javascript engine to process
 * entity rules. The engine is created with a restrictive filter
 * to prevent rules from calling any Java objects/classes except
 * those supplied by the processor.
 *  
 * @author Kevin
 *
 */
public class RuleProcessor {
	
	final Logger logger = LoggerFactory.getLogger(RuleProcessor.class);

	private GameInstance game;
	private ScriptEngine engine;
	
	class RestrictiveFilter implements ClassFilter {

		@Override
		public boolean exposeToScripts(String s) {
			return false;
		}
		
	}
	
	public RuleProcessor(GameInstance game) {
		this.game = game;
		NashornScriptEngineFactory factory = new NashornScriptEngineFactory(); 
		
		this.engine = factory.getScriptEngine(new RestrictiveFilter());
		if(this.engine == null) {
			throw new RuntimeException("Unable to get script engine");
		}
	}
	
	public void processRule(GameEvent event, EntityRule rule) {
		try {
			if(rule.isTriggered(event)) {
				// Let the rule access the event, game and entity objects
				engine.put("event", event);
				engine.put("game", game);
				engine.put("entity", rule.getEntity());
				engine.eval(rule.getScript());
			}
		} catch (final ScriptException se) {
			logger.error("Exception processing rule: " + se.getMessage());
		}
	}
}
