package com.flechazo.modernfurniture.block.entity;

import com.flechazo.modernfurniture.block.WallMountedAirConditioningBlock;
import com.flechazo.modernfurniture.block.manager.BlockEntityManager;
import com.flechazo.modernfurniture.util.room.RoomDetector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import software.bernie.geckolib.core.animation.RawAnimation;

public class WallMountedAirConditioningBlockEntity extends AbstractAirConditioningBlockEntity {
    private static final RawAnimation OPEN_ANIMATION = RawAnimation.begin().thenPlayAndHold("open");
    private static final RawAnimation OPENED_ANIMATION = RawAnimation.begin().thenPlayAndHold("opened");
    private static final RawAnimation CLOSE_ANIMATION = RawAnimation.begin().thenPlayAndHold("close");

    public WallMountedAirConditioningBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityManager.WALL_MOUNTED_AIR_CONDITIONING_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected RawAnimation getOpenAnimation() {
        return OPEN_ANIMATION;
    }

    @Override
    protected RawAnimation getOpenedAnimation() {
        return OPENED_ANIMATION;
    }

    @Override
    protected RawAnimation getCloseAnimation() {
        return CLOSE_ANIMATION;
    }

    @Override
    protected String getControllerName() {
        return "wall_ac_controller";
    }

    @Override
    protected BooleanProperty getOpenProperty() {
        return WallMountedAirConditioningBlock.OPEN;
    }

    @Override
    protected Direction getAirConditioningFacing() {
        BlockState state = this.level.getBlockState(this.worldPosition);
        return state.getValue(WallMountedAirConditioningBlock.FACING);
    }

    @Override
    protected BlockPos getRoomDetectionStartPos() {
        Direction facing = getAirConditioningFacing();
        BlockPos startPos = this.worldPosition.relative(facing);
        if (!RoomDetector.isPassable(this.level, startPos)) {
            startPos = this.worldPosition.above();
        }
        return startPos;
    }

    @Override
    protected BlockPos getParticleEffectPos() {
        Direction facing = getAirConditioningFacing();
        return this.worldPosition.relative(facing);
    }
}