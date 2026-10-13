package com.advancementstracker.client.mixin;

import com.advancementstracker.client.ModAdvancementScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin {

    @Shadow
    abstract Button openScreenButton(Component label, Supplier<Screen> screenSupplier);

    @Redirect(
            method = "createPauseMenu",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/PauseScreen;openScreenButton(Lnet/minecraft/network/chat/Component;Ljava/util/function/Supplier;)Lnet/minecraft/client/gui/components/Button;"
            )
    )
    private Button useModAdvancementScreen(PauseScreen screen, Component label, Supplier<Screen> screenSupplier) {
        if ("Advancements".equals(label.getString())) {
            screenSupplier = () -> new ModAdvancementScreen(Component.empty());
        }
        return this.openScreenButton(label, screenSupplier);
    }
}
