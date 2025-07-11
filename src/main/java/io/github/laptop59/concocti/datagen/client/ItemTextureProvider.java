package io.github.laptop59.concocti.datagen.client;

import com.google.common.base.Preconditions;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.item.MoldItem;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link DataProvider} that allows for the datagenning of item textures.
 */
public abstract class ItemTextureProvider implements DataProvider {

    protected final HashMap<Item, ItemTexture> textureMap = new HashMap<>();
    protected final EnumMap<MoldItem.Type, List<XY>> moldCutoutMap = new EnumMap<>(MoldItem.Type.class);
    protected final PackOutput output;
    protected final String modid;
    @VisibleForTesting
    public final ExistingFileHelper existingFileHelper;

    public ItemTextureProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        Preconditions.checkNotNull(output);
        this.output = output;
        Preconditions.checkNotNull(modid);
        this.modid = modid;
        Preconditions.checkNotNull(existingFileHelper);
        this.existingFileHelper = existingFileHelper;
    }

    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        textureMap.clear();
        moldCutoutMap.clear();
        // Make mold cutout images.
        generateMoldCutouts();
        addTextures();
        return generateAll();
    }

    private void generateMoldCutouts() {
        for (MoldItem.Type type : MoldItem.Type.values()) {
            try {
                // Get the cutout file.
                File file = this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                        .resolve("../../../main/resources/assets")
                        .resolve(modid)
                        .resolve("textures/item/mold_cutout")
                        .resolve(type.id + ".png").toFile();
                BufferedImage image = ImageIO.read(file);
                // Find any non-transparent pixels.
                List<XY> clear = new ArrayList<>();
                for (int x = 0; x < image.getWidth(); x++)
                    for (int y = 0; y < image.getHeight(); y++) {
                        // Color is something like: A(RGB)
                        int color = image.getRGB(x, y);
                        if (((color >> 24) & 0xff) > 0) {
                            // The boolean checks whether the colo(u)r of the pixel is black.
                            clear.add(new XY(x, y, (color & 0x00ffffff) == 0));
                        }
                    }
                moldCutoutMap.put(type, clear);
            } catch (Exception e) {
                LOGGER.warn("Could not get mold cutout of mold type {}: {}", type, e);
            }
        }
    }

    protected abstract void addTextures();

    protected CompletableFuture<?> generateAll() {
        CompletableFuture<?>[] futures = new CompletableFuture<?>[this.textureMap.size()];
        int i = 0;

        Path dir = this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(modid)
                .resolve("textures");
        dir.toFile().mkdir();
        dir = dir.resolve("item");
        dir.toFile().mkdir();

        for (ItemTexture image : this.textureMap.values()) {
            // Tell the helper that this file should exist.
            existingFileHelper.trackGenerated(image.path().withPrefix("item/"), PackType.CLIENT_RESOURCES,
                    ".png", "textures");
            Path target = getPath(image);
            futures[i++] = saveStable(image, target);
        }

        return CompletableFuture.allOf(futures);
    }

    @Override
    public @NotNull String getName() {
        return "Item Textures";
    }

    protected Path getPath(ItemTexture image) {
        ResourceLocation loc = image.path;
        return this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(loc.getNamespace())
                .resolve("textures/item").resolve(loc.getPath() + ".png");
    }

    static CompletableFuture<?> saveStable(ItemTexture texture, Path path) {
        return CompletableFuture.runAsync(() -> {
            try {
                File file = path.getParent()
                        .resolve("../../../../../../main/resources/assets/concocti/textures/item")
                        .resolve(path.getFileName())
                        .toFile();
                ImageIO.write(texture.image(), "png", file);
            } catch (IOException ioexception) {
                LOGGER.error("Failed to save file to {}", path, ioexception);
            }
        }, Util.backgroundExecutor());
    }

    /**
     * Adds a single texture.
     */
    protected void addTexture(Item item, BufferedImage image) {
        ResourceLocation loc = BuiltInRegistries.ITEM.getKey(item);
        textureMap.put(item, new ItemTexture(image, loc));
    }

    /**
     * Adds a mold texture with a hole in it from a mold item material and type.
     */
    protected void addShapedMoldTexture(MoldItem.Material material, MoldItem.Type type) {
        Item moldItem = ConcoctiItems.MOLDS.get(material).get(type).get();
        addShapedMoldTexture(moldItem, ConcoctiItems.MOLD_BASES.get(material).get(), type);
    }

    private void addShapedMoldTexture(Item item, Item moldSource, MoldItem.Type type) {
        ResourceLocation sourceLoc = BuiltInRegistries.ITEM.getKey(item);
        try {
            File file = getFileFromItem(moldSource, true);
            BufferedImage image = ImageIO.read(file);
            // Modify the image.
            double componentDivisor = 1.6;
            for (XY xy : moldCutoutMap.get(type)) {
                int color = 0;
                if (xy.outlinePixel) {
                    int initialColor = image.getRGB(xy.x, xy.y);
                    // Dividing each colo(u)r component will darken the pixel.
                    int r = (int) (((initialColor >> 16) & 0xff) / componentDivisor);
                    int g = (int) (((initialColor >> 8) & 0xff) / componentDivisor);
                    int b = (int) ((initialColor & 0xff) / componentDivisor);
                    // Set the colo(u)r back.
                    // Keep the alpha component.
                    color = (initialColor & 0xff000000) + (r << 16) + (g << 8) + b;
                }
                image.setRGB(xy.x, xy.y, color);
            }
            addTexture(item, image);
        } catch (Exception e) {
            LOGGER.warn("Error occurred while trying to generate texture for item {}: {}", sourceLoc, e);
        }
    }

    /**
     * Gets a {@link File} location from an {@link Item}.
     */
    private File getFileFromItem(Item item, boolean mainFolder) {
        ResourceLocation sourceLoc = getLocFromItem(item);
        return this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve((mainFolder ? "../../../main/resources/assets/" : "") + sourceLoc.getNamespace())
                .resolve("textures/item").resolve(sourceLoc.getPath() + ".png").toFile();
    }

    /**
     * Gets the <i>item registry location</i> from an {@link Item}.
     */
    private ResourceLocation getLocFromItem(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    public record ItemTexture(BufferedImage image, ResourceLocation path) {
    }

    public record XY(int x, int y, boolean outlinePixel) {
    }
}
