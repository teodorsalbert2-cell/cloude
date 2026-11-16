package pl.powodz;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Skończona woda: każdy blok wody ma ilość 1..8 (8 = pełny blok / źródło, 1 = warstwa 1/8 bloku).
 * Woda nie tworzy się z niczego – tylko przemieszcza się w dół i wyrównuje na boki.
 */
public final class Woda {
    private static final Direction[] BOKI = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    private Woda() {}

    /** @return true, jeśli tick został obsłużony (waniliowy ma zostać pominięty). */
    public static boolean obsluz(World world, BlockPos pos) {
        if (world.isClient || !world.getGameRules().getBoolean(PowodzMod.FIZYKA)) return false;
        BlockState stan = world.getBlockState(pos);
        // wodę w blokach zalanych (np. schody, płoty) zostawiamy grze
        if (!stan.isOf(Blocks.WATER)) return false;

        int ile = ilosc(stan);
        if (ile <= 0) return true;

        // 1. spływanie w dół
        BlockPos dol = pos.down();
        BlockState stanDol = world.getBlockState(dol);
        if (stanDol.isOf(PowodzMod.ODPLYW)) {
            // odpływ połyka wodę stojącą na nim
            ustaw(world, pos, 0);
            return true;
        }
        if (pomiesci(stanDol)) {
            int ileDol = ilosc(stanDol);
            int ruch = Math.min(ile, 8 - ileDol);
            if (ruch > 0) {
                ustaw(world, dol, ileDol + ruch);
                ustaw(world, pos, ile - ruch);
                return true;
            }
        }

        // 2. wyrównywanie na boki (w losowej kolejności)
        Random rnd = world.getRandom();
        Direction[] boki = BOKI.clone();
        for (int i = boki.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            Direction t = boki[i]; boki[i] = boki[j]; boki[j] = t;
        }
        int start = ile;
        for (Direction d : boki) {
            if (ile <= 0) break;
            BlockPos obok = pos.offset(d);
            BlockState stanObok = world.getBlockState(obok);
            if (!pomiesci(stanObok)) continue;
            int ileObok = ilosc(stanObok);
            // przelewamy, gdy różnica >= 2, albo gdy obok jest krawędź (pusto pod spodem) – wtedy
            // nawet ostatnia warstwa spływa, np. do kanału
            boolean krawedz = ileObok == 0 && maMiejscePodSpodem(world, obok);
            if (ile - ileObok >= 2 || (krawedz && ile >= 1)) {
                ustaw(world, obok, ileObok + 1);
                ile--;
            }
        }
        // 3. nic się nie przelało – szukamy spadku (np. kanału) w zasięgu ZASIEG bloków
        if (ile == start && ile >= 1) {
            Direction najlepszy = null;
            int najblizej = Integer.MAX_VALUE;
            for (Direction d : boki) {
                int k = odlegloscDoSpadku(world, pos, d);
                if (k < najblizej) { najblizej = k; najlepszy = d; }
            }
            if (najlepszy != null) {
                BlockPos obok = pos.offset(najlepszy);
                int ileObok = ilosc(world.getBlockState(obok));
                if (ileObok < ile) {
                    ustaw(world, obok, ileObok + 1);
                    ile--;
                }
            }
        }
        if (ile != start) ustaw(world, pos, ile);
        return true;
    }

    private static final int ZASIEG = 6;

    /** Ile bloków w danym kierunku jest najbliższe miejsce, gdzie woda może spaść w dół (MAX_VALUE = brak). */
    private static int odlegloscDoSpadku(World world, BlockPos pos, Direction d) {
        BlockPos.Mutable p = pos.mutableCopy();
        for (int k = 1; k <= ZASIEG; k++) {
            p.move(d);
            if (!pomiesci(world.getBlockState(p))) return Integer.MAX_VALUE;
            if (maMiejscePodSpodem(world, p)) return k;
        }
        return Integer.MAX_VALUE;
    }

    private static boolean maMiejscePodSpodem(World world, BlockPos pos) {
        BlockState s = world.getBlockState(pos.down());
        if (s.isOf(PowodzMod.ODPLYW)) return true;
        return pomiesci(s) && ilosc(s) < 8;
    }

    /** Ilość wody w bloku (0..8). */
    public static int ilosc(BlockState stan) {
        if (!stan.isOf(Blocks.WATER)) return 0;
        FluidState f = stan.getFluidState();
        return f.isStill() ? 8 : Math.max(0, Math.min(8, f.getLevel()));
    }

    /** Czy woda może wejść do tego bloku (powietrze, trawa, kwiaty, inna woda). */
    public static boolean pomiesci(BlockState stan) {
        if (stan.isOf(Blocks.WATER)) return true;
        if (stan.isAir()) return true;
        return stan.isReplaceable() && stan.getFluidState().isEmpty() && !stan.isOf(Blocks.LAVA);
    }

    /** Ustawia blok wody o podanej ilości (0 = powietrze). */
    public static void ustaw(World world, BlockPos pos, int ile) {
        BlockState nowy;
        if (ile <= 0) nowy = Blocks.AIR.getDefaultState();
        else if (ile >= 8) nowy = Blocks.WATER.getDefaultState();
        else nowy = Fluids.FLOWING_WATER.getFlowing(ile, false).getBlockState();
        if (world.getBlockState(pos) != nowy) {
            world.setBlockState(pos, nowy, 3);
        }
    }
}
