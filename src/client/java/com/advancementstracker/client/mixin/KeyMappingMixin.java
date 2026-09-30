package com.advancementstracker.client.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.advancementstracker.client.ModAdvancementScreen;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin {

    @Inject(method = "consumeClick", at = @At("HEAD"), cancellable = true)
    private void overrideKeyClick(CallbackInfoReturnable<Boolean> cir) {
        KeyMapping self = (KeyMapping) (Object) this;

        if ("key.advancements".equals(self.getName())) {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {

                if (client.options.keyAdvancements.isDown()) {
                    Minecraft.getInstance().gui.setScreen(
                            new ModAdvancementScreen(Component.empty())
                    );
                }

            }
            cir.setReturnValue(false);
        }
    }
}
