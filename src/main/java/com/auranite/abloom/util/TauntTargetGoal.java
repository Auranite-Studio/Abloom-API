package com.auranite.abloom.util;

import com.auranite.abloom.init.AbloomModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class TauntTargetGoal extends Goal {
    private final Mob mob;
    private LivingEntity tauntedTarget;
    private int scanTickCooldown = 0;

    public TauntTargetGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (this.mob.level().isClientSide()) return false;

        if (this.scanTickCooldown > 0) {
            this.scanTickCooldown--;
            return false;
        }

        this.scanTickCooldown = 20; // Проверка раз в секунду (оптимизация)
        this.tauntedTarget = findClosestTauntedEntity();
        return this.tauntedTarget != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.tauntedTarget != null
                && this.tauntedTarget.isAlive()
                && this.tauntedTarget.hasEffect(AbloomModEffects.TAUNT);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.tauntedTarget);
    }

    @Override
    public void stop() {
        // Сбрасываем цель, если эффект пропал или цель умерла
        if (this.mob.getTarget() == this.tauntedTarget) {
            this.mob.setTarget(null);
        }
    }

    private LivingEntity findClosestTauntedEntity() {
        AABB searchBox = this.mob.getBoundingBox().inflate(16.0, 4.0, 16.0);
        List<LivingEntity> candidates = this.mob.level().getEntitiesOfClass(
                LivingEntity.class, searchBox,
                e -> e.isAlive() && e.hasEffect(AbloomModEffects.TAUNT) && !e.isSpectator()
        );

        return candidates.stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(this.mob)))
                .orElse(null);
    }
}