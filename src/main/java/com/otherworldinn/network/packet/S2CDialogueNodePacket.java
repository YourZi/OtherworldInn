package com.otherworldinn.network.packet;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.util.ClientServices;
import com.otherworldinn.world.dialogue.DialogueNodeView;
import com.otherworldinn.world.dialogue.DialogueOptionType;
import com.otherworldinn.world.dialogue.DialogueOptionView;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record S2CDialogueNodePacket(DialogueNodeView view) implements CustomPacketPayload {
    public static final Type<S2CDialogueNodePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "dialogue_node"));

    private static final StreamCodec<RegistryFriendlyByteBuf, DialogueOptionView> OPTION_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    DialogueOptionView::id,
                    ByteBufCodecs.STRING_UTF8,
                    DialogueOptionView::labelKey,
                    ByteBufCodecs.INT,
                    option -> option.type().ordinal(),
                    (id, labelKey, typeOrdinal) ->
                            new DialogueOptionView(
                                    id,
                                    labelKey,
                                    DialogueOptionType.values()[
                                            Math.max(
                                                    0,
                                                    Math.min(
                                                            DialogueOptionType.values().length - 1,
                                                            typeOrdinal))]));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CDialogueNodePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    packet -> packet.view().entityId(),
                    ByteBufCodecs.STRING_UTF8,
                    packet -> packet.view().dialogueId(),
                    ByteBufCodecs.STRING_UTF8,
                    packet -> packet.view().nodeId(),
                    ByteBufCodecs.STRING_UTF8,
                    packet -> packet.view().textKey(),
                    ByteBufCodecs.collection(ArrayList::new, OPTION_CODEC),
                    packet -> new ArrayList<>(packet.view().options()),
                    (entityId, dialogueId, nodeId, textKey, options) ->
                            new S2CDialogueNodePacket(
                                    new DialogueNodeView(entityId, dialogueId, nodeId, textKey, options)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CDialogueNodePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientServices.handleDialogueNode(packet.view()));
    }
}
