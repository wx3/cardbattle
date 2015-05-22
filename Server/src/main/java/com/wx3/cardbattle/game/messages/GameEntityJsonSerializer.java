package com.wx3.cardbattle.game.messages;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.wx3.cardbattle.game.GameEntity;

/**
 * JsonSerializer for GameEntities being sent to clients
 * 
 * @author Kevin
 *
 */
public class GameEntityJsonSerializer implements JsonSerializer<GameEntity> {

	@Override
	public JsonElement serialize(GameEntity entity, Type typeOfSrc,
			JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		json.addProperty("id", entity.getId());
		json.addProperty("name", entity.name);
		json.add("tags", context.serialize(entity.getTags()));

		return json;
	}

}
