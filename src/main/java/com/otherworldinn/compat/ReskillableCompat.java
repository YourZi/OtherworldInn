package com.otherworldinn.compat;

import com.otherworldinn.OtherworldInn;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

public final class ReskillableCompat {
    private static final String MOD_ID = "reskillable";

    private ReskillableCompat() {}

    public static void init() {
        if (isLoaded()) {
            OtherworldInn.LOGGER.info("Reskillable Reimagined API compatibility enabled");
        }
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static int getSkillLevel(ServerPlayer player, String skillId) {
        return getSkillLevel((Player) player, skillId);
    }

    public static int getSkillLevel(Player player, String skillId) {
        if (!isLoaded()) {
            return 0;
        }
        return Api.getSkillLevel(player, skillId);
    }

    public static int getMaxLevel() {
        if (!isLoaded()) {
            return 1;
        }
        return Api.getMaxLevel();
    }

    public static void addSkillExperience(ServerPlayer player, String skillId, int experience) {
        if (!isLoaded() || player == null || player.level().isClientSide || experience <= 0) {
            return;
        }
        int beforeLevel = Api.getSkillLevel(player, skillId);
        Api.addExperience(player, skillId, experience);
        int afterLevel = Api.getSkillLevel(player, skillId);
        if (afterLevel > beforeLevel) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.otherworldinn.reskillable.auto_level_up",
                            Component.translatable("skill." + skillId),
                            afterLevel),
                    false);
        }
        Api.sync(player);
    }

    private static final class Api {
        private Api() {}

        private static net.bandit.reskillable.common.capabilities.SkillModel getModel(Player player) {
            return net.bandit.reskillable.common.capabilities.SkillModel.get(player);
        }

        private static int getSkillLevel(Player player, String skillId) {
            return getModel(player).getSkillLevel(skillId);
        }

        private static int getMaxLevel() {
            return net.bandit.reskillable.Configuration.getMaxLevel();
        }

        private static void addExperience(ServerPlayer player, String skillId, int experience) {
            getModel(player).addExperience(skillId, experience);
        }

        private static void sync(ServerPlayer player) {
            getModel(player).syncSkills(player);
        }
    }
}
