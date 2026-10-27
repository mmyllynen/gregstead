package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import com.lowdragmc.lowdraglib.transfer.persistence.INBTSerializable;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MedicalConditionTracker implements IMedicalConditionTracker, INBTSerializable<CompoundTag>, net.neoforged.neoforge.common.util.ValueIOSerializable {

    @Getter
    private final Object2FloatMap<MedicalCondition> medicalConditions = new Object2FloatOpenHashMap<>();
    private final Set<MedicalCondition> permanentConditions = new HashSet<>();
    private final Object2IntMap<Symptom.ConfiguredSymptom> activeSymptoms = new Object2IntOpenHashMap<>();
    private final Object2IntMap<Holder<MobEffect>> activeMobEffects = new Object2IntOpenHashMap<>();

    private final Set<MedicalCondition> flaggedForRemoval = new HashSet<>();

    @Setter
    private int maxAirSupply = -1;

    @Getter
    private final Player player;

    public MedicalConditionTracker(Player player) {
        this.player = player;
    }

    @Override
    public void tick() {
        if (!flaggedForRemoval.isEmpty()) updateActiveSymptoms();
        if (player.isCreative()) return;

        for (var entry : activeMobEffects.object2IntEntrySet()) {
            player.addEffect(new MobEffectInstance(entry.getKey(), 100, entry.getIntValue()));
        }

        if (player.level().getGameTime() % 20 == 0) { // apply idle progression every second
            for (MedicalCondition condition : medicalConditions.keySet()) {
                if (condition.idleProgressionType == MedicalCondition.IdleProgressionType.NONE) {
                    continue;
                }
                if (permanentConditions.contains(condition) &&
                        condition.idleProgressionType == MedicalCondition.IdleProgressionType.HEAL) {
                    // can't automatically heal permanent conditions.
                    continue;
                }
                int multiplier = (condition.idleProgressionType == MedicalCondition.IdleProgressionType.HEAL) ? -1 : 1;
                medicalConditions.replace(condition,
                        medicalConditions.getFloat(condition) + condition.idleProgressionRate * multiplier);
                evaluateMedicalCondition(condition);
            }
            if (!medicalConditions.isEmpty()) {
                updateActiveSymptoms();
            }
        }
    }

    public void progressCondition(@NotNull MedicalCondition condition, float strength) {
        if (player.isCreative()) return;

        medicalConditions.put(condition, medicalConditions.getOrDefault(condition, 0) + strength);

        updateActiveSymptoms();
    }

    private record SymptomState(MedicalCondition condition, Symptom.ConfiguredSymptom configured, int stage) {}

    private void updateActiveSymptoms() {
        for (var condition : flaggedForRemoval) {
            medicalConditions.removeFloat(condition);
            permanentConditions.remove(condition);
        }
        flaggedForRemoval.clear();

        var desired = new LinkedHashMap<Symptom, SymptomState>();
        for (var entry : medicalConditions.object2FloatEntrySet()) {
            var condition = entry.getKey();
            if (entry.getFloatValue() >= condition.maxProgression * 2) permanentConditions.add(condition);
            for (var configured : condition.symptoms) {
                int stage = calculateStage(condition, configured);
                if (stage <= 0) continue;
                var previous = desired.get(configured.symptom);
                if (previous == null || configured.relativeHarshness * stage >
                        previous.configured().relativeHarshness * previous.stage()) {
                    desired.put(configured.symptom, new SymptomState(condition, configured, stage));
                }
            }
        }

        for (var entry : activeSymptoms.object2IntEntrySet()) {
            var configured = entry.getKey();
            var next = desired.get(configured.symptom);
            if (next == null || next.configured() != configured || next.stage() != entry.getIntValue()) {
                var owner = MedicalCondition.CONDITIONS.values().stream()
                        .filter(condition -> condition.symptoms.contains(configured)).findFirst().orElse(null);
                configured.symptom.applyProgression(this, owner, configured, 0);
            }
        }
        for (var next : desired.values()) {
            if (activeSymptoms.getOrDefault(next.configured(), 0) != next.stage()) {
                next.configured().symptom.applyProgression(this, next.condition(), next.configured(), next.stage());
            }
            next.configured().symptom.tick(this, next.condition(), next.configured(), next.stage());
        }
        activeSymptoms.clear();
        desired.values().forEach(next -> activeSymptoms.put(next.configured(), next.stage()));
        if (!desired.containsKey(Symptom.AIR_SUPPLY_DEBUFF)) maxAirSupply = -1;
    }

    @Override
    public void removeMedicalCondition(MedicalCondition condition) {
        flaggedForRemoval.add(condition);
        permanentConditions.remove(condition);
    }

    private int calculateStage(MedicalCondition condition, Symptom.ConfiguredSymptom symptom) {
        return (int) Math.floor(Math.min(medicalConditions.getFloat(condition), condition.maxProgression) /
                (symptom.progressionThreshold * condition.maxProgression * symptom.stages));
    }

    // removes MedicalConditions without progression
    private void evaluateMedicalCondition(MedicalCondition condition) {
        if (permanentConditions.contains(condition)) {
            return;
        }
        if (medicalConditions.getFloat(condition) <= 0) {
            removeMedicalCondition(condition);
        }
    }

    /**
     * called on antidote/cure consumption
     *
     * @param condition   MedicalCondition to heal
     * @param progression amount of progression to decrease
     */
    @Override
    public void heal(MedicalCondition condition, int progression) {
        if (progression >= medicalConditions.getOrDefault(condition, 0)) {
            medicalConditions.removeFloat(condition);
            permanentConditions.remove(condition);
            updateActiveSymptoms();
            return;
        }
        medicalConditions.replace(condition, medicalConditions.getOrDefault(condition, 0) - progression);
        updateActiveSymptoms();
    }

    @Override
    public int getMaxAirSupply() {
        return maxAirSupply;
    }

    @Override
    public void setMobEffect(Holder<MobEffect> effect, int amplifier) {
        if (amplifier <= 0) {
            int previous = activeMobEffects.removeInt(effect);
            var current = player.getEffect(effect);
            if (current != null && current.getAmplifier() == previous) player.removeEffect(effect);
        } else if (amplifier >= activeMobEffects.getOrDefault(effect, -1)) {
            activeMobEffects.put(effect, amplifier);
        }
    }

    @Override
    public void serialize(net.minecraft.world.level.storage.ValueOutput output) {
        var entries = new ArrayList<CompoundTag>();
        for (var entry : medicalConditions.object2FloatEntrySet()) {
            var tag = new CompoundTag();
            tag.putString("condition", entry.getKey().name);
            tag.putFloat("progression", entry.getFloatValue());
            entries.add(tag);
        }
        output.store("medical_conditions", CompoundTag.CODEC.listOf(), entries);
        output.store("permanent_conditions", com.mojang.serialization.Codec.STRING.listOf(),
                permanentConditions.stream().map(condition -> condition.name).toList());
    }

    @Override
    public void deserialize(net.minecraft.world.level.storage.ValueInput input) {
        medicalConditions.clear();
        permanentConditions.clear();
        for (var tag : input.read("medical_conditions", CompoundTag.CODEC.listOf()).orElse(List.of())) {
            var condition = MedicalCondition.CONDITIONS.get(tag.getStringOr("condition", ""));
            if (condition != null) medicalConditions.put(condition, tag.getFloatOr("progression", 0));
        }
        for (var name : input.read("permanent_conditions", com.mojang.serialization.Codec.STRING.listOf()).orElse(List.of())) {
            var condition = MedicalCondition.CONDITIONS.get(name);
            if (condition != null) permanentConditions.add(condition);
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        ListTag effectsTag = new ListTag();
        for (var entry : medicalConditions.object2FloatEntrySet()) {
            CompoundTag medicalConditionTag = new CompoundTag();
            medicalConditionTag.putString("condition", entry.getKey().name);
            medicalConditionTag.putFloat("progression", entry.getFloatValue());
            effectsTag.add(medicalConditionTag);
        }
        tag.put("medical_conditions", effectsTag);

        ListTag permanentsTag = new ListTag();
        for (MedicalCondition condition : permanentConditions) {
            permanentsTag.add(StringTag.valueOf(condition.name));
        }
        tag.put("permanent_conditions", permanentsTag);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag arg) {
        medicalConditions.clear();
        permanentConditions.clear();
        ListTag medicalConditionsTag = com.lowdragmc.lowdraglib.transfer.persistence.NbtCompatibility.getList(arg, "medical_conditions", Tag.TAG_COMPOUND);
        for (int i = 0; i < medicalConditionsTag.size(); ++i) {
            CompoundTag compoundTag = medicalConditionsTag.getCompoundOrEmpty(i);
            MedicalCondition condition = MedicalCondition.CONDITIONS.get(compoundTag.getStringOr("condition", ""));
            float progression = compoundTag.getFloatOr("progression", 0.0F);

            if (condition != null) medicalConditions.put(condition, progression);
        }

        ListTag permanentConditionsTag = com.lowdragmc.lowdraglib.transfer.persistence.NbtCompatibility.getList(arg, "permanent_conditions", Tag.TAG_STRING);
        for (int i = 0; i < permanentConditionsTag.size(); ++i) {
            var condition = MedicalCondition.CONDITIONS.get(permanentConditionsTag.getStringOr(i, ""));
            if (condition != null) permanentConditions.add(condition);
        }
    }
}
