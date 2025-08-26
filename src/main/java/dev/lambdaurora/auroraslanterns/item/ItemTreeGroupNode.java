/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.item;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ItemTreeGroupNode implements ItemTreeNode {
	private final Identifier id;
	protected final List<ItemTreeNode> nodes = new ArrayList<>();
	private final Map<Identifier, ItemTreeGroupNode> groupNodes = new Object2ObjectOpenHashMap<>();
	private final CreativeModeTab.TabVisibility visibility = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;

	public ItemTreeGroupNode(Identifier id) {this.id = id;}

	public void add(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
		this.nodes.add(new ItemTreeItemNode(stack, visibility));
	}

	public void add(ItemStack stack) {
		this.add(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	}

	public void add(int index, ItemStack stack, CreativeModeTab.TabVisibility visibility) {
		this.nodes.add(index, new ItemTreeItemNode(stack, visibility));
	}

	public void add(int index, ItemStack stack) {
		this.add(index, stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	}

	public void add(ItemLike item) {
		this.add(new ItemStack(item));
	}

	public void add(ItemLike item, CreativeModeTab.TabVisibility visibility) {
		this.add(new ItemStack(item), visibility);
	}

	public void add(ItemTreeGroupNode groupNode) {
		this.nodes.add(groupNode);
		this.groupNodes.put(groupNode.id, groupNode);

		this.groupNodes.putAll(groupNode.groupNodes);
	}

	public void add(int index, ItemTreeGroupNode groupNode) {
		this.nodes.add(index, groupNode);
		this.groupNodes.put(groupNode.id, groupNode);

		this.groupNodes.putAll(groupNode.groupNodes);
	}

	private int addRelative(ItemStack toFind, ItemTreeNode node, int offset) {
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i) instanceof ItemTreeItemNode item) {
				if (ItemStack.isSameItemSameTags(item.stack(), toFind)) {
					this.nodes.add(i + offset, node);
					return i + offset;
				}
			}
		}

		return -1;
	}

	public int addAfter(ItemStack toFind, ItemTreeNode node) {
		return this.addRelative(toFind, node, 1);
	}

	public int addAfter(ItemStack toFind, ItemStack toAdd, CreativeModeTab.TabVisibility visibility) {
		return this.addAfter(toFind, new ItemTreeItemNode(toAdd, visibility));
	}

	public int addAfter(ItemStack toFind, ItemStack toAdd) {
		return this.addAfter(toFind, toAdd, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	}

	public void addAfter(ItemStack toFind, ItemLike toAdd) {
		this.addAfter(toFind, new ItemStack(toAdd));
	}

	public void addAfter(ItemLike toFind, ItemLike toAdd) {
		this.addAfter(new ItemStack(toFind), toAdd);
	}

	public @Nullable ItemTreeGroupNode collectItemsAsGroup(Identifier id, Predicate<ItemStack> collector) {
		int start = -1, end = -1;

		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i) instanceof ItemTreeItemNode item) {
				if (collector.test(item.stack())) {
					if (start == -1) {
						start = i;
					}
				} else if (start != -1) {
					end = i - 1;
					break;
				}
			}
		}

		if (start == -1 || end == -1) return null;
		if (end < start) return null;

		return this.replaceNodesWithGroup(id, start, end);
	}

	private ItemTreeGroupNode replaceNodesWithGroup(Identifier id, int start, int end) {
		var group = new ItemTreeGroupNode(id);
		group.nodes.addAll(this.nodes.subList(start, end + 1));
		group.detectGroups();

		this.nodes.removeAll(group.nodes);

		this.add(start, group);

		return group;
	}

	@Override
	public CreativeModeTab.TabVisibility getVisibility() {
		return this.visibility;
	}

	@Override
	public void build(Collection<ItemStack> stacks, FeatureFlagSet enabledFeatures, CreativeModeTab.TabVisibility visibility) {
		if (this.visibility == CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS || this.visibility == visibility) {
			for (var node : this.nodes) {
				node.build(stacks, enabledFeatures, visibility);
			}
		}
	}

	private void detectGroups() {
		for (var node : this.nodes) {
			if (node instanceof ItemTreeGroupNode groupNode) {
				this.groupNodes.putIfAbsent(groupNode.id, groupNode);
			}
		}
	}
}
