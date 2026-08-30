package com.otherworldinn.world.team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** 所有队伍数据的持久化（存于 Overworld SavedData）。 */
public class TeamSavedData extends SavedData {
    private static final String DATA_NAME = "otherworldinn_teams";

    private final Map<UUID, TeamData> teams = new HashMap<>();
    private final Map<UUID, UUID> playerToTeam = new HashMap<>();

    public static TeamSavedData get(ServerLevel level) {
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(
                        new SavedData.Factory<>(TeamSavedData::new, TeamSavedData::load, null),
                        DATA_NAME);
    }

    public TeamSavedData() {}

    public static TeamSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        TeamSavedData data = new TeamSavedData();

        if (tag.contains("Teams", Tag.TAG_LIST)) {
            ListTag teamsTag = tag.getList("Teams", Tag.TAG_COMPOUND);
            for (Tag t : teamsTag) {
                CompoundTag teamTag = (CompoundTag) t;
                UUID teamId = teamTag.getUUID("TeamId");
                TeamData team = new TeamData(teamId);
                team.load(teamTag);

                data.teams.put(teamId, team);

                // 重建 playerToTeam 映射
                for (UUID memberId : team.getMembers()) {
                    data.playerToTeam.put(memberId, teamId);
                }
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag teamsTag = new ListTag();
        for (TeamData team : teams.values()) {
            teamsTag.add(team.save(new CompoundTag()));
        }
        tag.put("Teams", teamsTag);

        return tag;
    }

    public Map<UUID, TeamData> getTeams() {
        return teams;
    }

    public Map<UUID, UUID> getPlayerToTeam() {
        return playerToTeam;
    }

    public void addTeam(TeamData team) {
        teams.put(team.getTeamId(), team);
        for (UUID memberId : team.getMembers()) {
            playerToTeam.put(memberId, team.getTeamId());
        }
        setDirty();
    }

    public void removeTeam(UUID teamId) {
        TeamData team = teams.remove(teamId);
        if (team != null) {
            for (UUID memberId : team.getMembers()) {
                playerToTeam.remove(memberId);
            }
            setDirty();
        }
    }

    public void addMember(UUID teamId, UUID playerId) {
        TeamData team = teams.get(teamId);
        if (team != null) {
            team.addMember(playerId);
            playerToTeam.put(playerId, teamId);
            setDirty();
        }
    }

    public void removeMember(UUID teamId, UUID playerId) {
        TeamData team = teams.get(teamId);
        if (team != null) {
            team.removeMember(playerId);
            playerToTeam.remove(playerId);
            setDirty();
        }
    }

    public void markDirty() {
        setDirty();
    }
}
