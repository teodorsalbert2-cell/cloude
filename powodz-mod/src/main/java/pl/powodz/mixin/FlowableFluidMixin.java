package pl.powodz.mixin;

import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.powodz.Woda;

/** Zastępuje waniliowe płynięcie wody skończoną fizyką z {@link Woda}. */
@Mixin(FlowableFluid.class)
public abstract class FlowableFluidMixin {
    @Inject(method = "onScheduledTick", at = @At("HEAD"), cancellable = true)
    private void powodz$tick(World world, BlockPos pos, FluidState state, CallbackInfo ci) {
        if ((Object) this instanceof WaterFluid && Woda.obsluz(world, pos)) {
            ci.cancel();
        }
    }
}
