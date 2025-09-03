/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.advancement;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.lambdaurora.auroraslanterns.AurorasLanterns;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WallLanternBonkTrigger extends SimpleCriterionTrigger<WallLanternBonkTrigger.TriggerInstance> {
	public static final Identifier ID = AurorasLanterns.id("wall_lantern_bonk");

	@Override
	public @NotNull Identifier getId() {
		return ID;
	}

	public @NotNull TriggerInstance createInstance(
			JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		Block block = deserializeBlock(jsonObject);
		return new TriggerInstance(contextAwarePredicate, block);
	}

	private static @Nullable Block deserializeBlock(JsonObject json) {
		if (json.has("block")) {
			Identifier identifier = new Identifier(GsonHelper.getAsString(json, "block"));
			return BuiltInRegistries.BLOCK.getOptional(identifier)
					.orElseThrow(() -> new JsonSyntaxException("Unknown block type '" + identifier + "'"));
		} else {
			return null;
		}
	}

	public void trigger(ServerPlayer player, BlockState state) {
		this.trigger(player, triggerInstance -> triggerInstance.matches(state));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final @Nullable Block block;

		public TriggerInstance(ContextAwarePredicate player, @Nullable Block block) {
			super(WallLanternBonkTrigger.ID, player);
			this.block = block;
		}

		public static WallLanternBonkTrigger.TriggerInstance bonk(@Nullable Block block) {
			return new WallLanternBonkTrigger.TriggerInstance(ContextAwarePredicate.ANY, block);
		}

		public boolean matches(BlockState state) {
			return this.block == null || state.is(this.block);
		}

		@Override
		public @NotNull JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject json = super.serializeToJson(serializationContext);

			if (this.block != null) {
				json.addProperty("block", BuiltInRegistries.BLOCK.getId(this.block).toString());
			}

			return json;
		}
	}
}