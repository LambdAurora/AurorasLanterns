/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import dev.lambdaurora.auroraslanterns.block.chandelier.AbstractChandelierBlock;
import dev.lambdaurora.auroraslanterns.block.chandelier.AbstractChandelierBlock.AttachmentType;
import dev.lambdaurora.auroraslanterns.block.chandelier.CeilingChandelierBlock;
import dev.lambdaurora.auroraslanterns.block.chandelier.WallChandelierBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public record ChandelierBlocks(
		AttachmentEntry<CeilingChandelierBlock> ceiling,
		AttachmentEntry<WallChandelierBlock> wall
) {
	private static final List<ChandelierBlocks> ALL = new ArrayList<>();
	public static final Supplier<BiMap<AbstractChandelierBlock, AbstractChandelierBlock>> NEXT_BY_BLOCK
			= Suppliers.memoize(() -> {
		var builder = ImmutableBiMap.<AbstractChandelierBlock, AbstractChandelierBlock>builder();
		ALL.forEach(blocks -> {
			builder.putAll(blocks.ceiling.cycleMapping());
			builder.putAll(blocks.wall.cycleMapping());
		});
		return builder.build();
	});

	public static ChandelierBlocks create(
			String type
	) {
		var chandeliers = new ChandelierBlocks(
				AttachmentEntry.create(type, "ceiling", CeilingChandelierBlock::new),
				AttachmentEntry.create(type, "wall", WallChandelierBlock::new)
		);
		ALL.add(chandeliers);
		return chandeliers;
	}

	public static Stream<ChandelierBlocks> streamAll() {
		return ALL.stream();
	}

	public AttachmentEntry<? extends AbstractChandelierBlock> get(AttachmentType attachmentType) {
		return switch (attachmentType) {
			case CEILING -> this.ceiling;
			case WALL -> this.wall;
			case STANDING -> throw new UnsupportedOperationException();
		};
	}

	public void forEach(Consumer<? super AbstractChandelierBlock> consumer) {
		this.ceiling.forEach(consumer);
		this.wall.forEach(consumer);
	}

	public Stream<AbstractChandelierBlock> stream() {
		var builder = Stream.<AbstractChandelierBlock>builder();
		this.forEach(builder::add);
		return builder.build();
	}

	public record AttachmentEntry<C extends AbstractChandelierBlock>(
			C single,
			C duo,
			C trio,
			C quad
	) {
		static <C extends AbstractChandelierBlock> AttachmentEntry<C> create(
				String type, String attachment, AbstractChandelierBlock.Factory<C> factory
		) {
			var single = AurorasLanternsRegistry.registerBlock(
					AurorasLanterns.id("chandelier/%s/%s/single".formatted(type, attachment)),
					properties -> factory.create(1, properties),
					AbstractChandelierBlock.properties(1, SoundType.IRON, MapColor.METAL)
			);
			var duo = AurorasLanternsRegistry.registerBlock(
					AurorasLanterns.id("chandelier/%s/%s/duo".formatted(type, attachment)),
					properties -> factory.create(2, properties),
					AbstractChandelierBlock.properties(2, SoundType.IRON, MapColor.METAL)
			);
			var trio = AurorasLanternsRegistry.registerBlock(
					AurorasLanterns.id("chandelier/%s/%s/trio".formatted(type, attachment)),
					properties -> factory.create(3, properties),
					AbstractChandelierBlock.properties(3, SoundType.IRON, MapColor.METAL)
			);
			var quad = AurorasLanternsRegistry.registerBlock(
					AurorasLanterns.id("chandelier/%s/%s/quad".formatted(type, attachment)),
					properties -> factory.create(4, properties),
					AbstractChandelierBlock.properties(4, SoundType.IRON, MapColor.METAL)
			);

			return new AttachmentEntry<>(single, duo, trio, quad);
		}

		/**
		 * Gets the chandelier block from the holders count.
		 *
		 * @param holders the holders count
		 * @return the chandelier block
		 */
		public C get(@Range(from = 1, to = 4) int holders) {
			return switch (holders) {
				case 1 -> this.single;
				case 2 -> this.duo;
				case 3 -> this.trio;
				case 4 -> this.quad;
				default -> throw new IllegalArgumentException("Invalid holders " + holders + ", expected between 1 and 4.");
			};
		}

		public ImmutableBiMap<C, C> cycleMapping() {
			return ImmutableBiMap.of(this.single, this.duo, this.duo, this.trio, this.trio, this.quad);
		}

		public void forEach(Consumer<? super C> consumer) {
			consumer.accept(this.single);
			consumer.accept(this.duo);
			consumer.accept(this.trio);
			consumer.accept(this.quad);
		}
	}
}
