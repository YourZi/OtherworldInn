package com.otherworldinn.world.husbandry;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 单只养殖动物的好感度数据，挂在实体附件 {@code ModAttachments.HUSBANDRY} 上。
 *
 * <p>所有时间戳都是"世界日"（{@link com.otherworldinn.util.WorldDayUtils}），
 * 用 {@link #NEVER} 表示"从未发生"，这样"今天还没做"的判断天然成立。
 * 参考实现的字段语义见 {@code docs/superpowers/specs/2026-10-22-husbandry-animals-affection-design.md}。
 */
public class HusbandryData implements INBTSerializable<CompoundTag> {
    /** 从未发生过的时间戳：任何 {@code day > lastX} 判断都会成立。 */
    public static final long NEVER = Long.MIN_VALUE;

    /** 好感度上限（500 = 5 心）。 */
    public static final int MAX_AFFECTION = 500;

    /** 每一颗心对应的好感度。 */
    public static final int AFFECTION_PER_HEART = 100;

    /** 好感度显示与判定的最大心数。 */
    public static final int MAX_HEARTS = 5;

    private int affection;

    private long lastPetDay = NEVER;
    private long lastFedDay = NEVER;
    private long lastMilkedDay = NEVER;
    private long lastSpecialDropDay = NEVER;

    /** 最后一次被照顾（喂食/抚摸/命名）的世界日；{@link #NEVER} 表示还没开始计时。 */
    private long lastCareDay = NEVER;

    /** 已经结算过的衰减累计值，用于只补扣增量（见 HusbandryService#pendingDecay）。 */
    private int appliedDecay;

    /** 上次被魔法剪刀采集的世界日。 */
    private long lastMagicDay = NEVER;


    public int getAffection() {
        return affection;
    }

    public void setAffection(int affection) {
        this.affection = Mth.clamp(affection, 0, MAX_AFFECTION);
    }

    /** 增减好感度，结果自动夹在 {@code [0, MAX_AFFECTION]}。 */
    public void addAffection(int delta) {
        setAffection(affection + delta);
    }

    /** 当前心数（0–5），由好感度换算。 */
    public int getHearts() {
        return Mth.clamp(affection / AFFECTION_PER_HEART, 0, MAX_HEARTS);
    }

    // ── 抚摸 ──

    public long getLastPetDay() {
        return lastPetDay;
    }

    public void markPet(long day) {
        lastPetDay = day;
    }

    public boolean canPetOn(long day) {
        return day > lastPetDay;
    }

    // ── 喂食 ──

    public long getLastFedDay() {
        return lastFedDay;
    }

    public void markFed(long day) {
        lastFedDay = day;
    }

    public boolean canFeedOn(long day) {
        return day > lastFedDay;
    }

    /** 当天还没被喂过（参考实现的判定：{@code day > ageLastFed}）。心情系统的"饥饿"扣分用它。 */
    public boolean isHungry(long day) {
        return day > lastFedDay;
    }

    // ── 照顾与衰减 ──

    public long getLastCareDay() {
        return lastCareDay;
    }

    public void setLastCareDay(long day) {
        lastCareDay = day;
    }

    public int getAppliedDecay() {
        return appliedDecay;
    }

    public void setAppliedDecay(int appliedDecay) {
        this.appliedDecay = Math.max(0, appliedDecay);
    }

    /** 记录一次"被照顾"，同时把衰减进度归零。 */
    public void markCare(long day) {
        lastCareDay = day;
        appliedDecay = 0;
    }

    // ── 奇迹药水 ──

    // ── 魔法剪刀 ──

    public long getLastMagicDay() {
        return lastMagicDay;
    }

    public void markMagicHarvest(long day) {
        lastMagicDay = day;
    }

    public boolean canMagicHarvestOn(long day) {
        return day > lastMagicDay;
    }

    // ── 采集 ──

    public long getLastMilkedDay() {
        return lastMilkedDay;
    }

    public void markMilked(long day) {
        lastMilkedDay = day;
    }

    public boolean canMilkOn(long day, int cooldownDays) {
        return day > lastMilkedDay + Math.max(1, cooldownDays) - 1L;
    }

    public long getLastSpecialDropDay() {
        return lastSpecialDropDay;
    }

    public void markSpecialDrop(long day) {
        lastSpecialDropDay = day;
    }

    public boolean canSpecialDropOn(long day) {
        return day > lastSpecialDropDay;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Affection", affection);
        tag.putLong("LastPetDay", lastPetDay);
        tag.putLong("LastFedDay", lastFedDay);
        tag.putLong("LastMilkedDay", lastMilkedDay);
        tag.putLong("LastSpecialDropDay", lastSpecialDropDay);
        tag.putLong("LastCareDay", lastCareDay);
        tag.putLong("LastMagicDay", lastMagicDay);
        tag.putInt("AppliedDecay", appliedDecay);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        setAffection(tag.contains("Affection") ? tag.getInt("Affection") : 0);
        lastPetDay = tag.contains("LastPetDay") ? tag.getLong("LastPetDay") : NEVER;
        lastFedDay = tag.contains("LastFedDay") ? tag.getLong("LastFedDay") : NEVER;
        lastMilkedDay = tag.contains("LastMilkedDay") ? tag.getLong("LastMilkedDay") : NEVER;
        lastSpecialDropDay =
                tag.contains("LastSpecialDropDay") ? tag.getLong("LastSpecialDropDay") : NEVER;
        lastCareDay = tag.contains("LastCareDay") ? tag.getLong("LastCareDay") : NEVER;
        lastMagicDay = tag.contains("LastMagicDay") ? tag.getLong("LastMagicDay") : NEVER;
        appliedDecay = tag.contains("AppliedDecay") ? Math.max(0, tag.getInt("AppliedDecay")) : 0;
    }
}
