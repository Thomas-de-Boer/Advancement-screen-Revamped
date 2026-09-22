package thomas.advancementstracker.client;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.chat.Component; // GUESS: 60% — package en klasse
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class BacapAdvancementListener implements ClientAdvancements.Listener {

    private static final Identifier TEST_ADVANCEMENT_ID = Identifier.parse("minecraft:story/mine_stone");

    private final ClientAdvancements advancements;

    public BacapAdvancementListener(ClientAdvancements advancements) {
        this.advancements = advancements;
    }

    @Override
    public void onAdvancementsUpdated() {
        System.out.println("[BACAP] onAdvancementsUpdated called");

        AdvancementHolder holder = advancements.get(TEST_ADVANCEMENT_ID);
        System.out.println("[BACAP] holder = " + holder);
        if (holder == null) return;

        Map<AdvancementHolder, AdvancementProgress> progressMap = advancements.progress();
        AdvancementProgress progress = progressMap.get(holder);
        System.out.println("[BACAP] progress = " + progress);
        if (progress == null) return;

        var player = Minecraft.getInstance().player;
        System.out.println("[BACAP] player = " + player);
        if (player == null) return;

        player.sendSystemMessage( // GUESS: 55% — naam en of tweede argument (boolean) klopt
                Component.literal("[BACAP] " + TEST_ADVANCEMENT_ID + " done=" + progress.isDone())
        );

//        player.sendSystemMessage(Component.literal(advancements.tree().toString()));

        var tree = advancements.tree();

        int count = 0;
        for (var node : tree.nodes()) { // GUESS: 50% op de naam "nodes()" — check dit via autocomplete
            count++;
        }
        player.sendSystemMessage(Component.literal("[BACAP] Total advancements in tree: " + count));

        for (String criterion : progress.getRemainingCriteria()) {
            player.sendSystemMessage(Component.literal("  missing: " + criterion));
        }
    }

    @Override
    public void onAdvancementsCleared() {}

    @Override
    public void onSelectedTabChanged(@Nullable AdvancementHolder selectedTab) {}
}