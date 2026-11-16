package pl.powodz;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

/** Deszcz dolewa po 1/8 bloku wody w losowych miejscach, słońce powoli ją odparowuje. */
public final class Deszcz {
    private Deszcz() {}

    public static void tick(ServerWorld world) {
        if (!world.getGameRules().getBoolean(PowodzMod.FIZYKA)) return;
        if (!world.getDimension().hasSkyLight()) return;

        int ile;
        boolean pada = world.isRaining();
        if (world.isThundering()) ile = world.getGameRules().getInt(PowodzMod.DESZCZ) * 3;
        else if (pada) ile = world.getGameRules().getInt(PowodzMod.DESZCZ);
        else ile = world.getGameRules().getInt(PowodzMod.PAROWANIE);
        if (ile <= 0) return;

        int r = Math.max(1, Math.min(128, world.getGameRules().getInt(PowodzMod.PROMIEN)));
        Random rnd = world.getRandom();
        for (ServerPlayerEntity gracz : world.getPlayers()) {
            if (gracz.isSpectator()) continue;
            BlockPos srodek = gracz.getBlockPos();
            for (int i = 0; i < ile; i++) {
                int x = srodek.getX() + rnd.nextInt(2 * r + 1) - r;
                int z = srodek.getZ() + rnd.nextInt(2 * r + 1) - r;
                if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;
                BlockPos gora = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
                if (pada) kropla(world, gora); else odparuj(world, gora);
            }
        }
    }

    private static void kropla(ServerWorld world, BlockPos gora) {
        if (!world.hasRain(gora)) return; // pustynie, zimne biomy (śnieg), dach nad głową
        BlockPos pod = gora.down();
        BlockState stanPod = world.getBlockState(pod);
        int ilePod = Woda.ilosc(stanPod);
        if (stanPod.isOf(Blocks.WATER) && ilePod < 8) {
            Woda.ustaw(world, pod, ilePod + 1);
            return;
        }
        BlockState stan = world.getBlockState(gora);
        if (!Woda.pomiesci(stan) || stan.isOf(Blocks.WATER)) return;
        // nowa warstwa 1/8 na pełnej wodzie albo na stałym podłożu
        if (stanPod.isOf(Blocks.WATER) || stanPod.isSideSolidFullSquare(world, pod, Direction.UP)) {
            Woda.ustaw(world, gora, 1);
        }
    }

    private static void odparuj(ServerWorld world, BlockPos gora) {
        if (!world.isSkyVisible(gora)) return;
        BlockPos pod = gora.down();
        BlockState stan = world.getBlockState(pod);
        // odparowują tylko niepełne warstwy (morza i jeziora zostają)
        int ile = Woda.ilosc(stan);
        if (ile > 0 && ile < 8) Woda.ustaw(world, pod, ile - 1);
    }
}
