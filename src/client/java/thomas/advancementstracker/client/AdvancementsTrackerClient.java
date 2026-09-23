package thomas.advancementstracker.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import thomas.advancementstracker.AdvancementsTracker;

public class AdvancementsTrackerClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		AdvancementsTracker.LOGGER.info("Advancements Tracker: client started!");

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.player != null) {
                client.player.sendSystemMessage(
                        Component.literal("Advancements Tracker is active!")
                );
            }
        });
    }
}