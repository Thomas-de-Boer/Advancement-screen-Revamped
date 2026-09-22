package thomas.advancementstracker.client;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents; // GUESS: 85% (exists in 1.21, may have moved in 26.x)
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.chat.Component; // SURE: LocalPlayer imports this exact class
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import thomas.advancementstracker.AdvancementsTracker;

public class AdvancementsTrackerClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		AdvancementsTracker.LOGGER.info("Advancements Tracker: client started!");

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (client.player != null) { // GUESS: 85% - public field "player" on Minecraft
				client.player.sendSystemMessage( // SURE: exists in LocalPlayer (you pasted it)
						Component.literal("Advancements Tracker is active!") // GUESS: 85% - Component.literal(String)
				);
			}

			ClientAdvancements advancements = handler.getAdvancements();
			advancements.setListener(new BacapAdvancementListener(advancements));
		});
	}
}