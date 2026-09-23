package thomas.advancementstracker.client.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thomas.advancementstracker.client.ModAdvancementScreen;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin {
    @Shadow public abstract String getName();

    @Inject(method = "consumeClick", at = @At("HEAD"), cancellable = true)
    private void overrideKeyClick(CallbackInfoReturnable<Boolean> cir) {
        KeyMapping self = (KeyMapping) (Object) this;

        // Example: Target the inventory key mapping
        if ("key.advancements".equals(self.getName())) {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {

                while (client.options.keyAdvancements.isDown()) {
                    Minecraft.getInstance().gui.setScreen(
                            new ModAdvancementScreen(Component.empty())
                    );
                }

            }

            // Return false to tell the game the key wasn't "clicked" by vanilla standards
            cir.setReturnValue(false);
        }
    }
}
