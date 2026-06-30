package com.otherworldinn.client.renderer;

import com.otherworldinn.client.util.SponsorSkinTextureService;
import com.otherworldinn.entity.guest.SponsorGuestEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SponsorGuestRenderer extends GuestRenderer<SponsorGuestEntity> {
    public SponsorGuestRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(SponsorGuestEntity entity) {
        String sponsorName = entity.getSponsorName();
        ResourceLocation fallback = SponsorSkinTextureService.resolveFallbackTexture(sponsorName);
        String resolvedSkinUrl = entity.getResolvedSkinUrl();
        if (resolvedSkinUrl == null || resolvedSkinUrl.isBlank()) {
            return fallback;
        }
        return SponsorSkinTextureService.getResolvedTexture(sponsorName, resolvedSkinUrl, fallback);
    }
}
