/*
 * This file is part of the wallhack project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025  daylight and contributors
 *
 * wallhack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * wallhack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with wallhack.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
//? if >=1.21.5 {
import net.minecraft.client.renderer.block.model.BlockModelPart;
//?}
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(SectionCompiler.class)
public class SectionBuilderMixin {
    @WrapOperation(method = "compile", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/chunk/VisGraph;setOpaque(Lnet/minecraft/core/BlockPos;)V"))
    public void onMarkClosed(
            VisGraph instance, BlockPos blockPos, Operation<Void> original) {
        if (SelectiveRenderingManager.shouldRenderBlock(blockPos)) {
            original.call(instance, blockPos);
        }
    }
    //? if >=1.21.5 {

    @WrapOperation(method = "compile", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLjava/util/List;)V"))
    public void onBuilBlock(
            BlockRenderDispatcher instance, BlockState blockState, BlockPos blockPos, BlockAndTintGetter blockAndTintGetter, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, List<BlockModelPart> list, Operation<Void> original) {
        if (SelectiveRenderingManager.shouldRenderBlock(blockState, blockPos)) {
            original.call(instance, blockState, blockPos, blockAndTintGetter, poseStack, vertexConsumer, bl, list);
        }
        //?} else {
    /*@WrapOperation(method = "compile", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;)V"))
    public void onBuilBlock(
            BlockRenderDispatcher instance, BlockState blockState, BlockPos blockPos, BlockAndTintGetter blockAndTintGetter, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, RandomSource randomSource, Operation<Void> original) {
     if (SelectiveRenderingManager.shouldRenderBlock(blockState, blockPos)) {
            original.call(instance, blockState, blockPos, blockAndTintGetter, poseStack, vertexConsumer, bl, randomSource);
        }
    *///?}

    }

    @WrapOperation(method = "compile", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderLiquid(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V"))
    public void onBuilFluid(
            BlockRenderDispatcher instance, BlockPos blockPos, BlockAndTintGetter blockAndTintGetter, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, Operation<Void> original) {
        if (SelectiveRenderingManager.shouldRenderBlock(blockState, blockPos)) {
            original.call(instance, blockPos, blockAndTintGetter, vertexConsumer, blockState, fluidState);
        }
    }
    @WrapOperation(method = "compile", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/chunk/SectionCompiler;handleBlockEntity(Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;Lnet/minecraft/world/level/block/entity/BlockEntity;)V"))
    public void onBuilBlockEntity(
            SectionCompiler instance, SectionCompiler.Results results, BlockEntity blockEntity, Operation<Void> original) {
        if (SelectiveRenderingManager.shouldRenderBlock(blockEntity.getBlockState(), blockEntity.getBlockPos())) {
            original.call(instance, results, blockEntity);
        }
    }
}