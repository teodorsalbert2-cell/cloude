package pl.powodz;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

/** Podczas burzy woda przy rzekach wypłukuje brzegi, porywa bloki i kawałki domów oraz niesie przedmioty. */
public final class Erozja {
    public static final TagKey<net.minecraft.block.Block> WYMYWALNE =
            TagKey.of(RegistryKeys.BLOCK, Identifier.of(PowodzMod.ID, "wymywalne"));
    private static final Direction[] KIERUNKI = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN};

    /** Kierunek nurtu – losowany na początku każdej burzy. */
    private static Direction nurt = Direction.EAST;
    private static boolean bylaBurza = false;

    private Erozja() {}

    public static boolean przyRzece(ServerWorld world, BlockPos pos) {
        if (world.getBiome(pos).isIn(BiomeTags.IS_RIVER)) return true;
        for (int odl : new int[]{8, 16}) {
            for (Direction d : Direction.Type.HORIZONTAL) {
                if (world.getBiome(pos.offset(d, odl)).isIn(BiomeTags.IS_RIVER)) return true;
            }
        }
        return false;
    }

    public static void tick(ServerWorld world, int r) {
        boolean burza = world.isThundering();
        if (burza && !bylaBurza) nurt = Direction.Type.HORIZONTAL.random(world.getRandom());
        bylaBurza = burza;
        if (!burza) return;

        int ile = world.getGameRules().getInt(PowodzMod.EROZJA);
        Random rnd = world.getRandom();
        for (ServerPlayerEntity gracz : world.getPlayers()) {
            BlockPos srodek = gracz.getBlockPos();
            for (int i = 0; i < ile; i++) {
                int x = srodek.getX() + rnd.nextInt(2 * r + 1) - r;
                int z = srodek.getZ() + rnd.nextInt(2 * r + 1) - r;
                if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;
                wyplucz(world, x, z, rnd);
            }
            niesPrzedmioty(world, gracz, Math.min(r, 48));
        }
    }

    private static void wyplucz(ServerWorld world, int x, int z, Random rnd) {
        BlockPos powierzchnia = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z)).down();
        if (!world.getBlockState(powierzchnia).isOf(Blocks.WATER)) return;
        if (!przyRzece(world, powierzchnia)) return;
        // losowa głębokość w słupie wody (0..3) i losowy sąsiad
        BlockPos woda = powierzchnia.down(rnd.nextInt(4));
        if (!world.getBlockState(woda).isOf(Blocks.WATER)) woda = powierzchnia;
        Direction d = KIERUNKI[rnd.nextInt(KIERUNKI.length)];
        BlockPos cel = woda.offset(d);
        BlockState stan = world.getBlockState(cel);
        if (!stan.isIn(WYMYWALNE)) return;
        // dno kopie się wolniej niż brzegi i ściany
        float szansa = d == Direction.DOWN ? 0.05f : 0.3f;
        if (rnd.nextFloat() < szansa) {
            world.breakBlock(cel, true);
        }
    }

    private static void niesPrzedmioty(ServerWorld world, ServerPlayerEntity gracz, int r) {
        Box obszar = gracz.getBoundingBox().expand(r, 16, r);
        for (ItemEntity item : world.getEntitiesByType(EntityType.ITEM, obszar, e -> e.isTouchingWater())) {
            if (!przyRzece(world, item.getBlockPos())) continue;
            Vec3d v = item.getVelocity();
            double vx = v.x + nurt.getOffsetX() * 0.02;
            double vz = v.z + nurt.getOffsetZ() * 0.02;
            double max = 0.35;
            vx = Math.max(-max, Math.min(max, vx));
            vz = Math.max(-max, Math.min(max, vz));
            item.setVelocity(vx, Math.max(v.y, 0.01), vz);
            item.velocityModified = true;
        }
    }

    public static Direction nurt() {
        return nurt;
    }
}
