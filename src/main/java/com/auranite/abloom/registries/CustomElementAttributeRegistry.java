package com.auranite.abloom.registries;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.datapack.CustomElementProvider;
import com.auranite.abloom.init.AbloomModAttributes;
import com.auranite.abloom.util.ElementType;
import com.auranite.abloom.util.IElementalType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages dynamic registration of NeoForge attributes for custom elemental types.
 * <p>
 * Built-in elements use {@link AbloomModAttributes} with static {@code DeferredHolder} fields.
 * Custom elements (loaded from datapacks) cannot use {@code DeferredRegister} because they
 * are loaded during the registry phase, which occurs after the registry phase where
 * {@code DeferredRegister} fires.
 * <p>
 * This class uses NeoForge's {@code RegisterEvent} to:
 * 1. Load custom element types from datapacks
 * 2. Register attributes for custom elements
 * <p>
 * Registration order:
 * <ol>
 *   <li>Mod constructor → {@code AbloomModAttributes.REGISTRY.register(modEventBus)}</li>
 *   <li>{@code RegisterEvent} → load datapacks → register attributes for custom elements</li>
 *   <li>{@code EntityAttributeModificationEvent} → add all attributes (including custom) to entities</li>
 * </ol>
 */
public final class CustomElementAttributeRegistry {

    private CustomElementAttributeRegistry() {
        // Utility class
    }

    /** Thread-safe map: element name -> ResourceLocation of damage bonus attribute */
    private static final Map<String, ResourceLocation> DMG_BONUS_IDS = new ConcurrentHashMap<>();
    /** Thread-safe map: element name -> ResourceLocation of resistance modifier attribute */
    private static final Map<String, ResourceLocation> RESIST_MOD_IDS = new ConcurrentHashMap<>();
    /** Thread-safe map: element name -> ResourceLocation of resistance shred attribute */
    private static final Map<String, ResourceLocation> RES_SHRED_IDS = new ConcurrentHashMap<>();

    /** Namespace prefix for custom element attributes */
    private static final String NAMESPACE = AbloomMod.MODID;

    /** Flag to ensure datapack loading and attribute registration happens only once */
    private static boolean initialized = false;

    /**
     * Registers this registry's event listener with the mod event bus.
     * Must be called during mod construction.
     */
    public static void register(IEventBus modEventBus) {
        modEventBus.register(CustomElementAttributeRegistry.class);
    }

    /**
     * Handles {@code RegisterEvent} to load datapacks and register attributes for custom elements.
     * This fires during the registry phase, BEFORE FMLCommonSetupEvent, ensuring custom
     * element types are available when we register their attributes.
     */
    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        if (event.getRegistryKey() != Registries.ATTRIBUTE) {
            return;
        }
        if (initialized) return;
        initialized = true;

        Registry<Attribute> registry = (Registry<Attribute>) (Registry<?>) event.getRegistry();

        // Load custom element types from datapacks FIRST
        AbloomMod.LOGGER.info("RegisterEvent<Attribute> — loading custom elemental types from datapacks...");
        CustomElementProvider.loadFromResources();
        AbloomMod.LOGGER.info("Custom elemental types loading complete. Total registered: {}",
                ElementType.getCustomTypeCount());

        // Now register attributes for all loaded custom elements
        AbloomMod.LOGGER.info("Registering attributes for {} custom element(s)...",
                ElementType.getCustomTypeCount());

        for (String elementName : ElementType.getCustomTypeNames()) {
            registerAttributesForElement(registry, elementName);
        }

        AbloomMod.LOGGER.info("Custom element attribute registration complete. Registered {} elements.",
                DMG_BONUS_IDS.size());
    }

    /**
     * Registers attributes for a single custom element by name.
     */
    private static void registerAttributesForElement(
            Registry<Attribute> registry, String elementName) {
        if (elementName == null || elementName.isEmpty()) {
            return;
        }

        String normalizedName = elementName.toUpperCase(java.util.Locale.ROOT);

        // Skip if already registered
        if (DMG_BONUS_IDS.containsKey(normalizedName)) {
            return;
        }

        String lowerName = normalizedName.toLowerCase(java.util.Locale.ROOT);

        // Register damage bonus attribute
        ResourceLocation dmgBonusId = ResourceLocation.fromNamespaceAndPath(NAMESPACE, lowerName + "_dmg_bonus");
        Attribute dmgBonus = new RangedAttribute("attribute.power." + dmgBonusId.getPath(), 0, -1, 1).setSyncable(true);
        Registry.register(registry, dmgBonusId, dmgBonus);
        DMG_BONUS_IDS.put(normalizedName, dmgBonusId);

        // Register resistance modifier attribute
        ResourceLocation resistModId = ResourceLocation.fromNamespaceAndPath(NAMESPACE, lowerName + "_resist_mod");
        Attribute resistMod = new RangedAttribute("attribute.power." + resistModId.getPath(), 0, -1, 1).setSyncable(true);
        Registry.register(registry, resistModId, resistMod);
        RESIST_MOD_IDS.put(normalizedName, resistModId);

        // Register resistance shred attribute
        ResourceLocation resShredId = ResourceLocation.fromNamespaceAndPath(NAMESPACE, lowerName + "_res_shred");
        Attribute resShred = new RangedAttribute("attribute.power." + resShredId.getPath(), 0, 0, 1).setSyncable(true);
        Registry.register(registry, resShredId, resShred);
        RES_SHRED_IDS.put(normalizedName, resShredId);

        AbloomMod.LOGGER.info("Registered attributes for custom element '{}': dmg_bonus, resist_mod, res_shred",
                normalizedName);
    }

    /**
     * Adds all registered custom element attributes to an entity type.
     * <p>
     * This method is called from {@link AbloomModAttributes#addAttributes} during
     * {@code EntityAttributeModificationEvent} to add custom element attributes
     * to all entity types.
     *
     * @param event the entity attribute modification event
     */
    public static void addCustomElementAttributes(
            net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent event) {
        var attributeRegistry = BuiltInRegistries.ATTRIBUTE;

        event.getTypes().forEach(entity -> {
            for (ResourceLocation id : DMG_BONUS_IDS.values()) {
                attributeRegistry.getHolder(id).ifPresent(holder -> event.add(entity, holder));
            }
            for (ResourceLocation id : RESIST_MOD_IDS.values()) {
                attributeRegistry.getHolder(id).ifPresent(holder -> event.add(entity, holder));
            }
            for (ResourceLocation id : RES_SHRED_IDS.values()) {
                attributeRegistry.getHolder(id).ifPresent(holder -> event.add(entity, holder));
            }
        });
    }

    /**
     * Gets the ResourceLocation of the damage bonus attribute for a custom element.
     */
    public static ResourceLocation getDmgBonusId(IElementalType type) {
        if (type == null || !type.isCustom()) {
            return null;
        }
        return DMG_BONUS_IDS.get(type.name());
    }

    /**
     * Gets the ResourceLocation of the resistance modifier attribute for a custom element.
     */
    public static ResourceLocation getResistModId(IElementalType type) {
        if (type == null || !type.isCustom()) {
            return null;
        }
        return RESIST_MOD_IDS.get(type.name());
    }

    /**
     * Gets the ResourceLocation of the resistance shred attribute for a custom element.
     */
    public static ResourceLocation getResShredId(IElementalType type) {
        if (type == null || !type.isCustom()) {
            return null;
        }
        return RES_SHRED_IDS.get(type.name());
    }

    /**
     * Returns the number of custom elements with registered damage bonus attributes.
     */
    public static int getDmgBonusAttributeCount() {
        return DMG_BONUS_IDS.size();
    }

    /**
     * Returns the number of custom elements with registered resistance modifier attributes.
     */
    public static int getResistModAttributeCount() {
        return RESIST_MOD_IDS.size();
    }

    /**
     * Returns the number of custom elements with registered resistance shred attributes.
     */
    public static int getResShredAttributeCount() {
        return RES_SHRED_IDS.size();
    }

    /**
     * Clears all registered custom element attributes.
     * Intended for testing or config reload scenarios.
     */
    public static void clearAll() {
        DMG_BONUS_IDS.clear();
        RESIST_MOD_IDS.clear();
        RES_SHRED_IDS.clear();
        initialized = false;
        AbloomMod.LOGGER.info("Cleared all custom element attributes");
    }
}
