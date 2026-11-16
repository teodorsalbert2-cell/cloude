package pl.powodz;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

/** Strona klienta: woda w rzekach robi się brązowa (błotnista) podczas burzy. */
public class PowodzClient implements ClientModInitializer {
    private static final int BLOTO = 0x7A5A32;
    private static volatile int tryb = 0;       // 0 = przy burzy, 1 = zawsze, 2 = nigdy
    private static volatile boolean blotnista = false;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(BlotoPayload.ID, (payload, ctx) -> tryb = payload.tryb());
        ClientPlayConnectionEvents.DISCONNECT.register((h, c) -> { tryb = 0; blotnista = false; });

        FluidRenderHandlerRegistry.INSTANCE.register(Fluids.WATER, Fluids.FLOWING_WATER,
                new SimpleFluidRenderHandler(Identifier.ofVanilla("block/water_still"),
                        Identifier.ofVanilla("block/water_flow"), Identifier.ofVanilla("block/water_overlay")) {
                    @Override
                    public int getFluidColor(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
                        if (view == null || pos == null) return 0x3F76E4;
                        int kolor = BiomeColors.getWaterColor(view, pos);
                        if (blotnista && wRzece(view, pos)) kolor = mieszaj(kolor, BLOTO, 0.8f);
                        return kolor;
                    }
                });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) return;
            boolean teraz = switch (tryb) {
                case 1 -> true;
                case 2 -> false;
                default -> client.world.getThunderGradient(1f) > 0.5f && client.world.getRainGradient(1f) > 0.5f;
            };
            if (teraz != blotnista) {
                blotnista = teraz;
                client.worldRenderer.reload(); // przebuduj chunki z nowym kolorem wody
            }
        });
    }

    private static boolean wRzece(BlockRenderView view, BlockPos pos) {
        RegistryEntry<Biome> b = view.hasBiomes() ? view.getBiomeFabric(pos) : null;
        if (b == null) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world == null) return false;
            b = mc.world.getBiome(pos);
        }
        return b.isIn(BiomeTags.IS_RIVER);
    }

    private static int mieszaj(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return (r << 16) | (g << 8) | bl;
    }
}
