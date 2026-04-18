package com.auranite.abloom;

import com.auranite.abloom.config.AbloomConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.level.LevelEvent;
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
        ElementResistanceRegistry.init();
        ElementResistanceManager.debugPrintRegistry();
        ElementDamageDisplayManager displayManager = new ElementDamageDisplayManager();
        ElementDamageHandler.setDisplayManager(displayManager);
        ElementDamageHandler.initDamageColors();
        ElementalProjectileRegistry.register(modEventBus);
        modEventBus.addListener(AbloomModElementalProjectiles::onCommonSetup);
        modEventBus.addListener(AbloomModElementalWeapons::onCommonSetup);

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
                    LOGGER.warn("Error in self-destruct tick for level {}", level.dimension().location(), e);
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
}