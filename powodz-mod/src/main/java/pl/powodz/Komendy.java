package pl.powodz;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/** Komenda /powodz. */
public final class Komendy {
    private Komendy() {}

    public static void zarejestruj(CommandDispatcher<ServerCommandSource> d) {
        d.register(literal("powodz")
                .requires(s -> s.hasPermissionLevel(2))
                .executes(Komendy::pomoc)
                .then(literal("pomoc").executes(Komendy::pomoc))
                .then(literal("status").executes(Komendy::status))
                .then(literal("burza")
                        .executes(c -> pogoda(c, 600, true, true))
                        .then(argument("sekundy", IntegerArgumentType.integer(1, 100000))
                                .executes(c -> pogoda(c, IntegerArgumentType.getInteger(c, "sekundy"), true, true))))
                .then(literal("deszcz")
                        .executes(c -> pogoda(c, 600, true, false))
                        .then(argument("sekundy", IntegerArgumentType.integer(1, 100000))
                                .executes(c -> pogoda(c, IntegerArgumentType.getInteger(c, "sekundy"), true, false))))
                .then(literal("slonce").executes(c -> pogoda(c, 600, false, false)))
                .then(literal("sila")
                        .then(argument("krople", IntegerArgumentType.integer(0, 1000))
                                .executes(c -> ustawInt(c, PowodzMod.DESZCZ, IntegerArgumentType.getInteger(c, "krople"), "Siła deszczu"))))
                .then(literal("parowanie")
                        .then(argument("ile", IntegerArgumentType.integer(0, 1000))
                                .executes(c -> ustawInt(c, PowodzMod.PAROWANIE, IntegerArgumentType.getInteger(c, "ile"), "Parowanie"))))
                .then(literal("promien")
                        .then(argument("bloki", IntegerArgumentType.integer(1, 128))
                                .executes(c -> ustawInt(c, PowodzMod.PROMIEN, IntegerArgumentType.getInteger(c, "bloki"), "Promień"))))
                .then(literal("erozja")
                        .then(literal("wylacz").executes(c -> ustawInt(c, PowodzMod.EROZJA, 0, "Erozja")))
                        .then(argument("sila", IntegerArgumentType.integer(0, 1000))
                                .executes(c -> ustawInt(c, PowodzMod.EROZJA, IntegerArgumentType.getInteger(c, "sila"), "Erozja"))))
                .then(literal("bloto")
                        .then(literal("burza").executes(c -> ustawInt(c, PowodzMod.BLOTO, 0, "Błotnista woda (0=przy burzy)")))
                        .then(literal("zawsze").executes(c -> ustawInt(c, PowodzMod.BLOTO, 1, "Błotnista woda (1=zawsze)")))
                        .then(literal("nigdy").executes(c -> ustawInt(c, PowodzMod.BLOTO, 2, "Błotnista woda (2=nigdy)"))))
                .then(literal("fizyka")
                        .then(literal("wlacz").executes(c -> fizyka(c, true)))
                        .then(literal("wylacz").executes(c -> fizyka(c, false))))
                .then(literal("osusz")
                        .executes(c -> osusz(c, 32))
                        .then(argument("promien", IntegerArgumentType.integer(1, 64))
                                .executes(c -> osusz(c, IntegerArgumentType.getInteger(c, "promien")))))
        );
    }

    private static void info(ServerCommandSource s, String tekst) {
        s.sendFeedback(() -> Text.literal("[Powódź] ").formatted(Formatting.BLUE)
                .append(Text.literal(tekst).formatted(Formatting.GRAY)), true);
    }

    private static int pomoc(CommandContext<ServerCommandSource> c) {
        String[] linie = {
                "/powodz status – obecne ustawienia",
                "/powodz burza [sekundy] – wielka burza (woda rośnie 3x, błoto, erozja)",
                "/powodz deszcz [sekundy] – zwykły deszcz",
                "/powodz slonce – koniec opadów, woda paruje",
                "/powodz sila <krople> – siła deszczu (domyślnie 8)",
                "/powodz erozja <siła>|wylacz – porywanie bloków przy rzece (domyślnie 10)",
                "/powodz bloto burza|zawsze|nigdy – kiedy rzeka jest brązowa",
                "/powodz parowanie <ile> – parowanie przy słońcu (domyślnie 2)",
                "/powodz promien <bloki> – zasięg deszczu wokół gracza",
                "/powodz fizyka wlacz|wylacz – skończona fizyka wody",
                "/powodz osusz [promień] – usuń wodę powyżej poziomu morza wokół Ciebie"};
        c.getSource().sendFeedback(() -> Text.literal("=== Powódź – komendy ===").formatted(Formatting.BLUE, Formatting.BOLD), false);
        for (String l : linie) c.getSource().sendFeedback(() -> Text.literal(l).formatted(Formatting.AQUA), false);
        return 1;
    }

    private static int status(CommandContext<ServerCommandSource> c) {
        ServerWorld w = c.getSource().getWorld();
        GameRules g = w.getGameRules();
        String pogoda = w.isThundering() ? "BURZA" : w.isRaining() ? "deszcz" : "słońce";
        String[] bloto = {"przy burzy", "zawsze", "nigdy"};
        info(c.getSource(), "pogoda: " + pogoda + ", fizyka: " + (g.getBoolean(PowodzMod.FIZYKA) ? "wł." : "wył.")
                + ", deszcz: " + g.getInt(PowodzMod.DESZCZ) + ", erozja: " + g.getInt(PowodzMod.EROZJA)
                + ", parowanie: " + g.getInt(PowodzMod.PAROWANIE) + ", promień: " + g.getInt(PowodzMod.PROMIEN)
                + ", błoto: " + bloto[Math.max(0, Math.min(2, g.getInt(PowodzMod.BLOTO)))]
                + ", nurt: " + Erozja.nurt().asString());
        return 1;
    }

    private static int pogoda(CommandContext<ServerCommandSource> c, int sekundy, boolean deszcz, boolean burza) {
        ServerWorld w = c.getSource().getServer().getOverworld();
        int t = sekundy * 20;
        if (deszcz) w.setWeather(0, t, true, burza);
        else w.setWeather(t, 0, false, false);
        info(c.getSource(), burza ? "Nadciąga wielka burza na " + sekundy + " s!" : deszcz ? "Zaczyna padać na " + sekundy + " s." : "Wychodzi słońce.");
        return 1;
    }

    private static int ustawInt(CommandContext<ServerCommandSource> c, GameRules.Key<GameRules.IntRule> klucz, int wartosc, String nazwa) {
        c.getSource().getServer().getGameRules().get(klucz).set(wartosc, c.getSource().getServer());
        info(c.getSource(), nazwa + " = " + wartosc);
        return 1;
    }

    private static int fizyka(CommandContext<ServerCommandSource> c, boolean wl) {
        c.getSource().getServer().getGameRules().get(PowodzMod.FIZYKA).set(wl, c.getSource().getServer());
        info(c.getSource(), "Skończona fizyka wody: " + (wl ? "włączona" : "wyłączona"));
        return 1;
    }

    private static int osusz(CommandContext<ServerCommandSource> c, int r) {
        ServerWorld w = c.getSource().getWorld();
        BlockPos s = BlockPos.ofFloored(c.getSource().getPosition());
        int morze = w.getChunkManager().getChunkGenerator().getSeaLevel(); // usuwamy wodę od tego Y w górę
        int usunieto = 0;
        BlockPos.Mutable p = new BlockPos.Mutable();
        for (int x = s.getX() - r; x <= s.getX() + r; x++) {
            for (int z = s.getZ() - r; z <= s.getZ() + r; z++) {
                int gora = w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
                for (int y = gora; y >= morze; y--) {
                    p.set(x, y, z);
                    if (w.getBlockState(p).isOf(Blocks.WATER)) {
                        w.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
                        usunieto++;
                    }
                }
            }
        }
        info(c.getSource(), "Osuszono " + usunieto + " bloków wody (promień " + r + ").");
        return usunieto;
    }
}
