package com.otherworldinn.init;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.fatigue.FatigueData;
import com.otherworldinn.world.husbandry.HusbandryData;
import com.otherworldinn.world.picnic.PicnicBoxData;
import com.otherworldinn.world.teleport.DeathExploreAnchorData;
import com.otherworldinn.world.teleport.NetherPortalReturnData;
import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, OtherworldInn.MODID);

    public static final Supplier<AttachmentType<FatigueData>> PLAYER_FATIGUE =
            ATTACHMENT_TYPES.register(
                    "player_fatigue",
                    () -> AttachmentType.serializable(FatigueData::new)
                            .copyOnDeath()
                            .build());

    public static final Supplier<AttachmentType<PicnicBoxData>> PLAYER_PICNIC_BOX =
            ATTACHMENT_TYPES.register(
                    "player_picnic_box",
                    () -> AttachmentType.serializable(PicnicBoxData::new)
                            .copyOnDeath()
                            .build());

    public static final Supplier<AttachmentType<DeathExploreAnchorData>> PLAYER_DEATH_EXPLORE_ANCHOR =
            ATTACHMENT_TYPES.register(
                    "player_death_explore_anchor",
                    () -> AttachmentType.serializable(DeathExploreAnchorData::new)
                            .copyOnDeath()
                            .build());

    public static final Supplier<AttachmentType<NetherPortalReturnData>> PLAYER_NETHER_PORTAL_RETURN =
            ATTACHMENT_TYPES.register(
                    "player_nether_portal_return",
                    () -> AttachmentType.serializable(NetherPortalReturnData::new)
                            .copyOnDeath()
                            .build());

    /** 养殖动物的好感度（挂在 Animal 实体上，随实体存档）。 */
    public static final Supplier<AttachmentType<HusbandryData>> HUSBANDRY =
            ATTACHMENT_TYPES.register(
                    "husbandry",
                    () -> AttachmentType.serializable(HusbandryData::new).build());

    private ModAttachments() {}
}
