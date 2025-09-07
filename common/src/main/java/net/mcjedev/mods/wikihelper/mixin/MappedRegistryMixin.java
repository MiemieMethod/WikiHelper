package net.mcjedev.mods.wikihelper.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.mcjedev.mods.wikihelper.util.DumpHelper;

@Mixin(MappedRegistry.class)
public class MappedRegistryMixin {
    @Unique
    private static final Logger wiki_Helper_1_21$LOGGER = LogManager.getLogger(MappedRegistryMixin.class);

    @Inject(
            method = "<init>(Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Lifecycle;Z)V",
            at = @At("RETURN")
    )
    private void onConstructed(ResourceKey<?> resourceKey, Lifecycle lifecycle, boolean bl, CallbackInfo ci) {
        String key = resourceKey.location().toString();
        if (DumpHelper.TRACKED_REGISTRIES.containsKey(key)) {
            wiki_Helper_1_21$LOGGER.info("Duplicated registry: {}", resourceKey.location());
            return;
        }
        DumpHelper.TRACKED_REGISTRIES.put(key, (MappedRegistry<?>) (Object) this);
        wiki_Helper_1_21$LOGGER.info("Tracked registry: {}", resourceKey.location());
    }
}