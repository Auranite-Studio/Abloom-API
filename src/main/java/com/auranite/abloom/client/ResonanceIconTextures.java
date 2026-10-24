package com.auranite.abloom.client;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.datapack.CustomElementData;
import com.auranite.abloom.registries.CustomElementRegistry;
import com.auranite.abloom.util.IElementalType;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationSectionsMetadata;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.inventory.InventoryMenu;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads and caches resonance effect icons from {@code assets/<namespace>/textures/resonance_effects/*.png}.
 *
 * <p><b>Why this exists:</b> previously the renderer requested sprites for these textures from the
 * {@code minecraft:blocks} atlas via {@link Minecraft#getTextureAtlas(ResourceLocation)}. The custom
 * atlas definition in {@code data/abloom/atlases/resonance_effects.json} only registers a dynamic
 * texture surveyor (it affects resource reloading), it does NOT add sprites to any atlas that is
 * actually uploaded at runtime. As a result every lookup returned the missing-texture sprite, which
 * produced "missing texture" errors on icon load.
 *
 * <p>This utility loads each PNG directly as an individual OpenGL texture and wraps it into a
 * {@link TextureAtlasSprite} bound to {@link InventoryMenu#BLOC_PARTICLES_ATLAS_LOCATION}, so the
 * icons can be rendered reliably without depending on atlas stitching. Sprites are cached per
 * location; failed loads are also cached (as {@code null}) to avoid repeated IO and log spam.
 */
public final class ResonanceIconTextures {

    /** Namespace of this mod. */
    public static final String NAMESPACE = "abloom";

    /** Texture folder path (without the leading {@code textures/} and the trailing {@code .png}). */
    public static final String TEXTURE_FOLDER = "resonance_effects";

    /** Atlas the dynamically created sprites are bound to for rendering. */
    private static final ResourceLocation RENDER_ATLAS = InventoryMenu.BLOC_PARTICLES_ATLAS_LOCATION;

    /** Cache: sprite location -> loaded sprite (null value means "load already attempted and failed"). */
    private static final Map<ResourceLocation, Optional<TextureAtlasSprite>> CACHE = new ConcurrentHashMap<>();

    private ResonanceIconTextures() {
        // Utility class
    }

    /**
     * Builds the texture location for an elemental type, following the datapack contract:
     * built-in elements resolve to {@code abloom:textures/resonance_effects/<name>.png},
     * custom elements resolve to {@code <element_id_namespace>:textures/resonance_effects/<element_id_path>.png}.
     *
     * @param type the elemental type
     * @return the texture resource location
     */
    public static ResourceLocation getTextureLocation(IElementalType type) {
        if (!type.isCustom()) {
            return ResourceLocation.fromNamespaceAndPath(
                    NAMESPACE, TEXTURE_FOLDER + "/" + type.name().toLowerCase(Locale.ROOT) + ".png");
        }
        // Custom elements: namespace and path come from elementId (e.g. "yourmod:custom_element"
        // -> assets/yourmod/textures/resonance_effects/custom_element.png)
        String namespace = NAMESPACE;
        String path = type.name().toLowerCase(Locale.ROOT);
        Optional<CustomElementData> dataOpt = CustomElementRegistry.getData(type);
        if (dataOpt.isPresent()) {
            ResourceLocation elementId = dataOpt.get().elementId();
            namespace = elementId.getNamespace();
            path = elementId.getPath();
        }
        return ResourceLocation.fromNamespaceAndPath(namespace, TEXTURE_FOLDER + "/" + path + ".png");
    }

    /**
     * Returns a renderable sprite for the given elemental type, or {@code null} if the texture
     * cannot be loaded (callers should fall back to a colored square in that case).
     */
    public static TextureAtlasSprite getSprite(Minecraft minecraft, IElementalType type) {
        return getSprite(minecraft, getTextureLocation(type));
    }

    /**
     * Returns a renderable sprite for the given texture location, or {@code null} on failure.
     * Loads the PNG directly (outside of any atlas) and caches the result.
     */
    public static TextureAtlasSprite getSprite(Minecraft minecraft, ResourceLocation textureLocation) {
        if (minecraft == null || textureLocation == null) {
            return null;
        }
        Optional<TextureAtlasSprite> cached = CACHE.computeIfAbsent(textureLocation, loc -> {
            try {
                return Optional.ofNullable(loadSprite(minecraft, loc));
            } catch (Exception e) {
                AbloomMod.LOGGER.warn("Failed to load resonance effect icon {}: {}", loc, e.toString());
                return Optional.empty();
            }
        });
        return cached.orElse(null);
    }

    /**
     * Clears the sprite cache and releases the underlying GPU textures. Should be called when
     * resources are reloaded ({@code F3+T}) or on client disconnect so that updated PNGs take effect.
     */
    public static void clearCache() {
        CACHE.values().forEach(opt -> opt.ifPresent(sprite -> {
            SpriteContents contents = sprite.contents;
            if (contents != null) {
                try {
                    contents.close();
                } catch (Exception ignored) {
                    // Already released
                }
            }
        }));
        CACHE.clear();
    }

    private static TextureAtlasSprite loadSprite(Minecraft minecraft, ResourceLocation textureLocation) {
        ResourceProvider resourceProvider = minecraft.getResourceManager();
        ResourceLocation imageLocation = textureLocation;

        try (InputStream inputStream = resourceProvider.open(imageLocation)) {
            NativeImage image = NativeImage.read(NativeImage.Format.RGBA, inputStream);
            int width = image.getWidth();
            int height = image.getHeight();

            SpriteContents contents = new SpriteContents(
                    imageLocation,
                    new FrameSize(width, height),
                    image,
                    // No animation metadata: resonance icons are static textures.
                    new AnimationSectionsMetadata(Optional.empty()));

            TextureAtlasSprite sprite = contents.newSprite(
                    RENDER_ATLAS,
                    TextureAtlas.MAX_MIP_LEVEL,
                    false /* blur */,
                    false /* clamp */);

            // Upload the texture to the GPU outside of the normal atlas-stitching lifecycle.
            contents.loadTexture(TextureAtlas.MAX_MIP_LEVEL, false);
            return sprite;
        } catch (Exception e) {
            AbloomMod.LOGGER.debug("Resonance effect icon not available: {}", textureLocation, e);
            return null;
        }
    }
}
