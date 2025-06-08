package com.badlyac.firstfabricmod.mixin;



import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(ResourcePackManager.class)
public interface ResourcePackManagerAccessor {
    @Accessor("enabled")
    List<ResourcePackProfile> getEnabledProfiles();

    @Accessor("profiles")
    Map<String, ResourcePackProfile> getProfiles();
}