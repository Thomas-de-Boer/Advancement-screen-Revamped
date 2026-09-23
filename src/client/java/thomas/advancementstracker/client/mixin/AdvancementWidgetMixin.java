// File: src/client/java/thomas/advancementstracker/client/mixin/AdvancementWidgetMixin.java
package thomas.advancementstracker.client.mixin;

import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(AdvancementWidget.class)
public class AdvancementWidgetMixin {

    @Mutable
    @Shadow
    @Final
    private List<FormattedCharSequence> description;

    @Mutable
    @Shadow
    @Final
    private int width;

    @Shadow
    @Final
    private AdvancementNode advancementNode;

    @Shadow
    @Final
    private DisplayInfo display;

    @Inject(method = "setProgress", at = @At("TAIL"))
    private void onSetProgress(AdvancementProgress progress, CallbackInfo info) {
        Font font = Minecraft.getInstance().font;
        int wrapWidth = 200;
        int maxCriteriaShown = 6; // pas dit later aan naar wat het beste uitkomt

        List<FormattedCharSequence> newDescription = new ArrayList<>(font.split(this.display.description(), wrapWidth));

        AdvancementRequirements requirementsWrapper = this.advancementNode.advancement().requirements();
        boolean multipleCriteria = requirementsWrapper.names().size() > 1;

        if (!progress.isDone() && multipleCriteria && !hasOrGroup()) {
            newDescription.addAll(font.split(Component.literal("\nCriteria:"), wrapWidth));

            List<String> remaining = new ArrayList<>();
            progress.getRemainingCriteria().forEach(remaining::add);

            int shown = 0;
            for (String criterion : remaining) {
                if (shown >= maxCriteriaShown) {
                    int leftOver = remaining.size() - maxCriteriaShown;
                    newDescription.addAll(font.split(Component.literal("...and " + leftOver + " more"), wrapWidth));
                    break;
                }
                newDescription.addAll(font.split(Component.literal("- " + criterion), wrapWidth));
                shown++;
            }
        }

        this.description = newDescription;

        int maxLineWidth = 0;
        for (FormattedCharSequence line : this.description) {
            maxLineWidth = Math.max(maxLineWidth, font.width(line));
        }
        this.width = Math.max(this.width, maxLineWidth + 8);
    }

    @Unique
    private boolean hasOrGroup() {
        AdvancementRequirements requirementsWrapper = this.advancementNode.advancement().requirements();
        for (List<String> group : requirementsWrapper.requirements()) {
            if (group.size() > 1) {
                return true;
            }
        }
        return false;
    }
}