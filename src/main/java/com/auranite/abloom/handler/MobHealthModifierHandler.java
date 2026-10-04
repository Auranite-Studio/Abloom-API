package com.auranite.abloom.handler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public class MobHealthModifierHandler {

    private static final ResourceLocation MOB_DOUBLE_HP_ID = ResourceLocation.parse("abloom:mob_double_hp");
    private static final ResourceLocation BOSS_HALF_HP_ID = ResourceLocation.parse("abloom:boss_half_hp");

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        if (event.getEntity() instanceof LivingEntity living) {
            if (living instanceof Player) {
                return;
            }

            AttributeInstance maxHealthAttr = living.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealthAttr == null) {
                return;
            }

            boolean isBoss = isBossEntity(living);
            ResourceLocation modifierId = isBoss ? BOSS_HALF_HP_ID : MOB_DOUBLE_HP_ID;
            double multiplierAmount = isBoss ? 0.5 : 1.0;

            // Проверяем, был ли модификатор уже применен
            boolean alreadyApplied = maxHealthAttr.hasModifier(modifierId);

            if (!alreadyApplied) {
                // Сохраняем ТЕКУЩЕЕ здоровье ДО применения модификатора
                float currentHealth = living.getHealth();
                float oldMaxHealth = living.getMaxHealth();

                // Применяем модификатор
                AttributeModifier modifier = new AttributeModifier(
                        modifierId,
                        multiplierAmount,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                maxHealthAttr.addPermanentModifier(modifier);

                // Масштабируем текущее здоровье пропорционально
                // Если моб был ранен на 50%, он останется ранен на 50%
                float healthRatio = currentHealth / oldMaxHealth;
                float newHealth = living.getMaxHealth() * healthRatio;
                living.setHealth(newHealth);
            }
        }
    }

    private boolean isBossEntity(LivingEntity entity) {
        if (entity instanceof WitherBoss || entity instanceof EnderDragon) {
            return true;
        }

        String entityName = entity.getClass().getSimpleName().toLowerCase();
        if (entityName.contains("boss") || entityName.contains("dragon") || entityName.contains("wither")) {
            return true;
        }

        return false;
    }
}