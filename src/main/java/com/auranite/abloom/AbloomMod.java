package com.auranite.abloom;

import com.auranite.abloom.config.AbloomConfig;
import com.auranite.abloom.datapack.ElementalWeaponProvider;
import com.auranite.abloom.datapack.ArmorResistanceProvider;
import com.auranite.abloom.util.TauntTargetGoal;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.IEventBus;

import net.minecraft.util.Tuple;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;

import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;

@Mod("abloom")
public class AbloomMod {
    public static final Logger LOGGER = LogManager.getLogger(AbloomMod.class);
    public static final String MODID = "abloom";

    public AbloomMod(IEventBus modEventBus, ModContainer modContainer) {

        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::registerNetworking);

        modContainer.registerConfig(ModConfig.Type.CLIENT, AbloomConfig.CLIENT_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, AbloomConfig.SERVER_SPEC);

        modEventBus.addListener(AbloomConfig::onConfigLoad);
        modEventBus.addListener(AbloomConfig::onConfigReload);

        AbloomModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        AbloomModEffects.REGISTRY.register(modEventBus);
        AbloomModItems.REGISTRY.register(modEventBus);
        AbloomModTabs.REGISTRY.register(modEventBus);
        AbloomAttributes.registerAttributes(modEventBus);
        
        ElementDamageDisplayManager displayManager = new ElementDamageDisplayManager();
        ElementDamageHandler.setDisplayManager(displayManager);
        ElementDamageHandler.initDamageColors();
        ElementalProjectileRegistry.register(modEventBus);
        modEventBus.addListener(AbloomModElementalProjectiles::onCommonSetup);
        // Register datapack for elemental weapons
        modEventBus.addListener(this::setup);
    }
    
    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            AbloomMod.LOGGER.info("Loading elemental weapons datapack...");
            ElementalWeaponProvider.loadFromResources();
            AbloomMod.LOGGER.info("Elemental weapons datapack loaded");
            
            AbloomMod.LOGGER.info("Loading armor resistances datapack...");
            ArmorResistanceProvider.loadFromResources();
            AbloomMod.LOGGER.info("Armor resistances datapack loaded");
            
            AbloomMod.LOGGER.info("Datapack loading complete");
        });
    }
    @SubscribeEvent
    public void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {

            serverLevel.getServer().execute(() -> {
                try {
                    ElementDamageDisplayManager.cleanupOrphanedDisplaysOnWorldLoad(serverLevel);
                } catch (Exception e) {
                    LOGGER.error("Failed to cleanup orphaned displays", e);
                }
            });
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Pre event) {
        MinecraftServer server = event.getServer();
        if (server.isDedicatedServer() || server.isSingleplayer()) {
            for (ServerLevel level : server.getAllLevels()) {
                try {
                    ElementDamageDisplayManager.tickSelfDestructDisplays(level);
                } catch (Exception e) {
                    LOGGER.warn("Error in self-destruct tick for level {}", level.dimension(), e);
                }
            }
        }
    }

    private static boolean networkingRegistered = false;
    private static final Map<CustomPacketPayload.Type<?>, NetworkMessage<?>> MESSAGES = new HashMap<>();

    private record NetworkMessage<T extends CustomPacketPayload>(StreamCodec<? extends FriendlyByteBuf, T> reader,
                                                                 IPayloadHandler<T> handler) {
    }

    public static <T extends CustomPacketPayload> void addNetworkMessage(CustomPacketPayload.Type<T> id, StreamCodec<? extends FriendlyByteBuf, T> reader, IPayloadHandler<T> handler) {
        if (networkingRegistered)
            throw new IllegalStateException("Cannot register new network messages after networking has been registered");
        MESSAGES.put(id, new NetworkMessage<>(reader, handler));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID);
        MESSAGES.forEach((id, networkMessage) -> registrar.playBidirectional(id, ((NetworkMessage) networkMessage).reader(), ((NetworkMessage) networkMessage).handler()));
        networkingRegistered = true;
    }

    private static final Collection<Tuple<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

    public static void queueServerWork(int tick, Runnable action) {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
            workQueue.add(new Tuple<>(action, tick));
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Mob mob && !mob.level().isClientSide()) {
            // Фильтр: только враждебные (MONSTER) и нейтральные мобы
            boolean isHostileOrNeutral = mob.getType().getCategory() == MobCategory.MONSTER ||
                    (!(mob instanceof Animal) &&
                            !(mob instanceof WaterAnimal));

            if (isHostileOrNeutral) {
                mob.goalSelector.addGoal(1, new TauntTargetGoal(mob));
            }
        }
    }

    @SubscribeEvent
    public void tick(ServerTickEvent.Post event) {
        List<Tuple<Runnable, Integer>> actions = new ArrayList<>();
        workQueue.forEach(work -> {
            work.setB(work.getB() - 1);
            if (work.getB() == 0)
                actions.add(work);
        });
        actions.forEach(e -> e.getA().run());
        workQueue.removeAll(actions);
    }

    @SubscribeEvent
    public void onWorldLoad(net.neoforged.neoforge.event.level.LevelEvent.Load event) {
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            net.minecraft.core.HolderLookup.Provider lookupProvider = serverLevel.registryAccess();
            ElementResistanceRegistry.init(lookupProvider);
            ElementResistanceManager.debugPrintRegistry();
        }
    }
}
