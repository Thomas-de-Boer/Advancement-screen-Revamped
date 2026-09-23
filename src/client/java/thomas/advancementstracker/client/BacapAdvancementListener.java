package thomas.advancementstracker.client;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.chat.Component; // GUESS: 60% — package en klasse
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.List;
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

        Advancement advancement = holder.value(); // GUESS: 70% — methode op AdvancementHolder
//        var requirements = advancement.requirements(); // GUESS: 40% — naam en of dit uberhaupt public is
//
//// De structuur is normaal een lijst van lijsten (OR-groepen binnen AND).
//// Als een van de groepen meer dan 1 criterium heeft, is dat een OR-keuze.
//        boolean hasOrGroup = requirements.stream().anyMatch(group -> group.size() > 1); // GUESS: 40% op de exacte vorm van 'requirements'

        AdvancementRequirements requirementsWrapper = advancement.requirements(); // bevestigd: dit type bestaat (zie screenshot)
        List<List<String>> groups = requirementsWrapper.requirements(); // GUESS: 85% — de naam "requirements()" nogmaals, nu op het wrapper-object

        boolean hasOrGroup = false;
        for (List<String> group : groups) {
            if (group.size() > 1) {
                hasOrGroup = true;
                break;
            }
        }

    }

    @Override
    public void onAdvancementsCleared() {}

    @Override
    public void onSelectedTabChanged(@Nullable AdvancementHolder selectedTab) {}
}