package pl.powodz;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Serwer → klient: tryb błotnistej wody (0 = przy burzy, 1 = zawsze, 2 = nigdy). */
public record BlotoPayload(int tryb) implements CustomPayload {
    public static final Id<BlotoPayload> ID = new Id<>(Identifier.of(PowodzMod.ID, "bloto"));
    public static final PacketCodec<RegistryByteBuf, BlotoPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.INTEGER, BlotoPayload::tryb, BlotoPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
