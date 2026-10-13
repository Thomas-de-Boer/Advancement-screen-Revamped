package com.advancementstracker.client.mixin;

import com.advancementstracker.client.ModAdvancementScreen;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    @Inject(method = "handleComponentClicked", at = @At("HEAD"), cancellable = true)
    private void openSharedAdvancement(Style style, boolean insert, CallbackInfoReturnable<Boolean> cir) {
        if (insert || style.getClickEvent() == null || !(style.getClickEvent() instanceof ClickEvent.Custom custom)) {
            return;
        }
        if (!Identifier.fromNamespaceAndPath("advancement-screen-revamped", "open_advancement").equals(custom.id())) {
            return;
        }
        custom.payload()
                .flatMap(tag -> tag instanceof StringTag stringTag ? stringTag.asString() : java.util.Optional.empty())
                .ifPresent(ModAdvancementScreen::openSharedAdvancement);
        cir.setReturnValue(true);
    }
}
