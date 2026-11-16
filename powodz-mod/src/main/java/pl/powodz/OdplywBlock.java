package pl.powodz;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/** Odpływ: co pół sekundy zabiera wodę z bloków nad sobą i obok siebie. */
public class OdplywBlock extends Block {
    public static final MapCodec<OdplywBlock> CODEC = createCodec(OdplywBlock::new);
    private static final int CO_ILE_TICKOW = 10;

    public OdplywBlock(Settings settings) {
        super(settings);
    }

    @Override
    public MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        if (!world.isClient) world.scheduleBlockTick(pos, this, CO_ILE_TICKOW);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        for (Direction d : new Direction[]{Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos p = pos.offset(d);
            int ile = Woda.ilosc(world.getBlockState(p));
            if (ile > 0) Woda.ustaw(world, p, 0);
        }
        world.scheduleBlockTick(pos, this, CO_ILE_TICKOW);
    }
}
