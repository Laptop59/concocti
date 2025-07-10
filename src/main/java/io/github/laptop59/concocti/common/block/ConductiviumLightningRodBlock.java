package io.github.laptop59.concocti.common.block;

import io.github.laptop59.concocti.common.machine.impl.ConcoctiElectronCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.NotNull;

public class ConductiviumLightningRodBlock extends LightningRodBlock  {
    public ConductiviumLightningRodBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (level.isThundering()
                && (long)level.random.nextInt(50) <= level.getGameTime() % 200L
                && pos.getY() == level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()) - 1) {
            ParticleUtils.spawnParticlesAlongAxis(
                    state.getValue(FACING).getAxis(), level, pos, 0.150, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(1, 2)
            );
        }
    }

    @Override
    public void onLightningStrike(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        super.onLightningStrike(state, level, pos);
        BlockPos machineBelowPos = pos.below(1);
        BlockEntity blockEntity = level.getBlockEntity(machineBelowPos);
        if (blockEntity instanceof ConcoctiElectronCollector.BlockEntity concoctiElectronCollectorBlockEntity) {
            concoctiElectronCollectorBlockEntity.markLightningState();
        }
    }
}
