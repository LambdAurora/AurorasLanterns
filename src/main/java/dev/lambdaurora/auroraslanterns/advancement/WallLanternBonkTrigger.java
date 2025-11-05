/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Represents the Wall Lantern player collision (aka bonk) advancement trigger.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.1
 */
public class WallLanternBonkTrigger extends SimpleCriterionTrigger<WallLanternBonkTrigger.TriggerInstance> {
	@Override
	public Codec<TriggerInstance> codec() {
		return TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer player, BlockState state) {
		this.trigger(player, triggerInstance -> triggerInstance.matches(state));
	}

	public record TriggerInstance(
			Optional<ContextAwarePredicate> player,
			Optional<Block> block
	) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
				BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("block").forGetter(TriggerInstance::block)
		).apply(instance, TriggerInstance::new));

		public static Criterion<TriggerInstance> bonk() {
			return AurorasLanternsRegistry.WALL_LANTERN_BONK_TRIGGER.createCriterion(
					new TriggerInstance(Optional.empty(), Optional.empty())
			);
		}

		public boolean matches(BlockState state) {
			return this.block.isEmpty() || state.is(this.block.get());
		}
	}
}