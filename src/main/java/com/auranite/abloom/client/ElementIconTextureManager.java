package com.auranite.abloom.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.util.IElementalType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

/**
 * Resolves element icon textures by naming convention: an element named {@code FIRE} maps to
 * {@code abloom:textures/gui/element_icons/fire.png}, and a custom element registered by
 * another mod (with namespaced damage type id, e.g. {@code yourmod:plasma_dmg}) maps to
 * {@code yourmod:textures/gui/element_icons/plasma.png}.
 * <p>
 * Textures are loaded from any active resource pack (mod jar or user resource pack) on first
 * use and cached per ResourceLocation. Missing files simply yield null so callers can fall
 * back to effect sprites or colored squares.
 */
public class ElementIconTextureManager {

    /** Cache keyed by texture location; the MISSING sentinel marks "checked, file not found". */
    private static final Map<ResourceLocation, ElementIconSprite> CACHE = new ConcurrentHashMap<>();

    /** Sentinel used only for negative caching (never returned to callers). */
    private static final ElementIconSprite MISSING = new ElementIconSprite(
            ResourceLocation.fromNamespaceAndPath("abloom", "gui/element_icons/_missing_"));

    /**
     * Returns the icon sprite for the given elemental type, or null if no texture exists.
     * The lookup path is derived from {@link IElementalType#getElementIconTexture()}:
     * "{namespace}:gui/element_icons/{name_lowercase}" ->
     * "{namespace}/textures/gui/element_icons/{name_lowercase}.png".
     * <p>
     * Note: the returned handle's {@code textureLocation()} points at the raw PNG itself
     * (not a stitched atlas), so it must be rendered via {@code drawCustomSprite} in the
     * render utilities, which binds it with {@code RenderSystem.setShaderTexture}.
     */
    public static ElementIconSprite getIcon(IElementalType type) {
        if (type == null) return null;
        return getIcon(type.getElementIconTexture());
    }

    /** Convenience overload: resolves an icon for any explicit texture location. */
    public static ElementIconSprite getIcon(ResourceLocation textureLocation) {
        if (textureLocation == null) return null;
        ElementIconSprite cached = CACHE.get(textureLocation);
        if (cached != null) return cached;
        if (CACHE.containsKey(textureLocation)) return null; // negative cache hit
        ElementIconSprite sprite = load(textureLocation);
        if (sprite != null) {
            CACHE.put(textureLocation, sprite);
        } else {
            CACHE.put(textureLocation, MISSING);
        }
        return sprite;
    }

    private static ElementIconSprite load(ResourceLocation loc) {
        try {
            var resources = Minecraft.getInstance().getResourceManager();
            // loc is already a texture-path ResourceLocation, e.g. abloom:gui/element_icons/fire
            // -> check abloom:textures/gui/element_icons/fire.png exists in ANY active pack.
            if (resources.getResource(loc.withPrefix("textures/").withSuffix(".png")).isEmpty()) {
                return null;
            }
            // No GPU upload needed: rendering binds this location directly as a plain texture,
            // and vanilla's TextureManager loads the PNG on demand.
            return new ElementIconSprite(loc);
        } catch (Exception e) {
            AbloomMod.LOGGER.debug("Failed to resolve element icon texture '{}': {}", loc, e.getMessage());
            return null;
        }
    }

    /** Clears the cache (e.g. on resource reload). */
    public static void clearCache() {
        CACHE.clear();
    }

    /**
     * Detects a resource pack (re)load by comparing the identity of the active
     * {@link net.minecraft.server.packs.resources.ResourceManager} instance and drops the sprite
     * cache so textures from newly applied resource packs are picked up. Safe to call per frame.
     */
    public static void clearCacheIfReloaded() {
        try {
            var manager = Minecraft.getInstance().getResourceManager();
            if (manager != lastManager) {
                lastManager = manager;
                CACHE.clear();
            }
        } catch (Exception ignored) {
        }
    }

    private static net.minecraft.server.packs.resources.ResourceManager lastManager;
}
