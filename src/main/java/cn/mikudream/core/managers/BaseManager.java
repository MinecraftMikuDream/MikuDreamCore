package cn.mikudream.core.managers;

import cn.mikudream.core.MikuDream;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class BaseManager {
    protected final MikuDream plugin;
    protected final Logger logger;
    protected final Path dataFolder;

    protected BaseManager(MikuDream plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dataFolder = plugin.getDataFolder().toPath();
    }

    protected YamlConfiguration loadOrCreateYaml(String fileName) throws IOException {
        Path filePath = dataFolder.resolve(fileName);

        if (!Files.exists(filePath)) {
            Files.createDirectories(dataFolder);
            Files.createFile(filePath);
            return new YamlConfiguration();
        }

        return YamlConfiguration.loadConfiguration(filePath.toFile());
    }

    protected void saveYaml(YamlConfiguration config, String fileName) throws IOException {
        Path filePath = dataFolder.resolve(fileName);
        config.save(filePath.toFile());
    }

    protected String formatMaterialName(Material material) {
        return Arrays.stream(material.name().toLowerCase().split("_"))
                .map(word -> !word.isEmpty()
                        ? Character.toUpperCase(word.charAt(0)) + word.substring(1)
                        : "")
                .collect(Collectors.joining(" "));
    }

    protected String getItemDisplayName(ItemStack item) {
        return Optional.ofNullable(item)
                .filter(ItemStack::hasItemMeta)
                .map(ItemStack::getItemMeta)
                .filter(ItemMeta::hasDisplayName)
                .map(ItemMeta::getDisplayName)
                .orElseGet(() -> {
                    assert item != null;
                    return formatMaterialName(item.getType());
                });
    }

    protected void handleIOException(String operation, IOException e) {
        logger.warning("Failed to %s: %s".formatted(operation, e.getMessage()));
    }
}