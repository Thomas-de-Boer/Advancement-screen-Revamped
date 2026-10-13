package com.advancementstracker.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;

import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdvancementsTrackerClient implements ClientModInitializer {

    private static final Pattern SHARED_ADVANCEMENT = Pattern.compile(
            "\\[Advancement\\]\\s+(.+?)\\s+\\(([a-z0-9_.-]+:[a-z0-9_./-]+)\\)"
    );

	@Override
	public void onInitializeClient() {
        ClientReceiveMessageEvents.ALLOW_CHAT.register(
                (message, signedMessage, sender, boundChatType, timeStamp) -> {
                    Matcher matcher = SHARED_ADVANCEMENT.matcher(message.getString());
                    if (!matcher.find()) return true;

                    Minecraft minecraft = Minecraft.getInstance();
                    String playerName = sender == null ? "Someone" : sender.name();
                    String title = matcher.group(1);
                    String advancementId = matcher.group(2);

                    Component open = Component.literal("Click to open")
                            .withStyle(style -> style
                                    .withColor(ChatFormatting.AQUA)
                                    .withUnderlined(true)
                                    .withClickEvent(new ClickEvent.Custom(
                                            ModAdvancementScreen.shareClickId(),
                                            Optional.of(net.minecraft.nbt.StringTag.valueOf(advancementId)))));
                    Component decorated = Component.literal(playerName + " shared " + title + " - ")
                            .append(open);

                    if (minecraft.gui != null) {
                        minecraft.gui.hud.getChat().addPlayerMessage(
                                decorated,
                                signedMessage == null ? null : signedMessage.signature(),
                                GuiMessageTag.chatModified("advancements-tracker")
                        );
                        return false;
                    }
                    return true;
                }
        );
    }
}