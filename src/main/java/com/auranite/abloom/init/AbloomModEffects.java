package com.auranite.abloom.init;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.effect.*;
import com.auranite.abloom.util.TauntTargetGoal;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class AbloomModEffects {
    public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(Registries.MOB_EFFECT, AbloomMod.MODID);
    public static final DeferredHolder<MobEffect, MobEffect> BURN = REGISTRY.register("burn", () -> new BurnEffect(0xFF5500));
    public static final DeferredHolder<MobEffect, MobEffect> WETNESS = REGISTRY.register("wetness", () -> new WetnessEffect(0x0080FF));
    public static final DeferredHolder<MobEffect, MobEffect> STUN = REGISTRY.register("stun", () -> new StunEffect(0x8B4513));
    public static final DeferredHolder<MobEffect, MobEffect> FREEZE = REGISTRY.register("freeze", () -> new FreezeEffect(0x00BFFF));
    public static final DeferredHolder<MobEffect, MobEffect> SHOCK = REGISTRY.register("shock", () -> new ShockEffect(0xFF19FF));
    public static final DeferredHolder<MobEffect, MobEffect> BREAK = REGISTRY.register("break", () -> new BreakEffect(0x9400D3));
    public static final DeferredHolder<MobEffect, MobEffect> RUPTURE = REGISTRY.register("rupture", () -> new RuptureEffect(0xC0C0C0));
    public static final DeferredHolder<MobEffect, MobEffect> BLOOM = REGISTRY.register("bloom", () -> new BloomEffect(0x32CD32));
    public static final DeferredHolder<MobEffect, MobEffect> OVERLOAD = REGISTRY.register("overload", () -> new OverloadEffect(0xFF00FF));
    public static final DeferredHolder<MobEffect, MobEffect> WINDSWEPT = REGISTRY.register("windswept", () -> new WindsweptEffect(0x00FFFF));
    public static final DeferredHolder<MobEffect, MobEffect> CORRUPTION = REGISTRY.register("corruption", () -> new CorruptionEffect(0x24B3A7));
    public static final DeferredHolder<MobEffect, MobEffect> TAUNT = REGISTRY.register("taunt", () -> new TauntEffect(0x9B2D30));
    public static final DeferredHolder<MobEffect, MobEffect> DISPERSION = REGISTRY.register("dispersion", () -> new DispersionEffect(0xFFFFE0));
    public static final DeferredHolder<MobEffect, MobEffect> ECLIPSE = REGISTRY.register("eclipse", () -> new EclipseEffect(0x4B0082));
    public static final DeferredHolder<MobEffect, MobEffect> CRITICAL_GAIN = REGISTRY.register("critical_gain", () -> new CriticalGainEffect(0xFFD700));
    public static final DeferredHolder<MobEffect, MobEffect> PRISM = REGISTRY.register("prism", () -> new PrismEffect(0xFFFFFF));

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance != null) {
            expireEffects(event.getEntity(), effectInstance);
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance != null) {
            expireEffects(event.getEntity(), effectInstance);
        }
    }

    private static void expireEffects(Entity entity, MobEffectInstance effectInstance) {
        if (effectInstance.is(AbloomModEffects.TAUNT)) {
            // Сброс агрессии мобов, когда эффект Taunt заканчивается
            if (entity.level() instanceof ServerLevel serverLevel) {
                List<Mob> mobs = serverLevel.getEntitiesOfClass(Mob.class, entity.getBoundingBox().inflate(16.0));
                for (Mob mob : mobs) {
                    // Если текущая цель моба имеет эффект Taunt, проверяем, нужен ли ему TauntTargetGoal
                    if (mob.getTarget() != null && mob.getTarget().hasEffect(AbloomModEffects.TAUNT)) {
                        // Проверяем, есть ли активный TauntTargetGoal
                        boolean hasActiveTauntGoal = false;
                        for (Goal goal : mob.goalSelector.getAvailableGoals()) {
                            if (goal instanceof TauntTargetGoal && goal.canUse()) {
                                hasActiveTauntGoal = true;
                                break;
                            }
                        }
                        // Если нет активного TauntGoal и у цели больше нет эффекта Taunt, сбрасываем цель
                        if (!hasActiveTauntGoal) {
                            mob.setTarget(null);
                        }
                    } else if (mob.getTarget() == entity) {
                        // Если цель моба - это тот, у кого закончился эффект Taunt
                        mob.setTarget(null);
                    }
                }
            }
        }
    }
}
