package io.github.laptop59.concocti.common.detail;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public record DetailContext(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries, String id) {}
