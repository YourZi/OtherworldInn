package com.otherworldinn.world.dialogue;

import java.util.List;
import org.jetbrains.annotations.Nullable;

public record DialogueOptionDef(
        String id,
        LocalizedText label,
        DialogueOptionType type,
        @Nullable String nextNodeId,
        @Nullable String functionId,
        List<DialogueRequirementDef> requirements,
        List<DialogueEffectDef> effects) {
    public DialogueOptionDef(
            String id,
            LocalizedText label,
            DialogueOptionType type,
            @Nullable String nextNodeId,
            @Nullable String functionId) {
        this(id, label, type, nextNodeId, functionId, List.of(), List.of());
    }

    public DialogueOptionDef withRequirements(DialogueRequirementDef... requirements) {
        return new DialogueOptionDef(
                this.id,
                this.label,
                this.type,
                this.nextNodeId,
                this.functionId,
                List.of(requirements),
                this.effects);
    }

    public DialogueOptionDef withEffects(DialogueEffectDef... effects) {
        return new DialogueOptionDef(
                this.id,
                this.label,
                this.type,
                this.nextNodeId,
                this.functionId,
                this.requirements,
                List.of(effects));
    }
}
