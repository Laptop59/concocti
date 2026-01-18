package io.github.laptop59.concocti.integration.emi;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiStack;
import io.github.laptop59.concocti.client.gui.components.SolarBar;
import io.github.laptop59.concocti.common.Concocti;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SolarEmiStack extends EmiStack {
    public static final ResourceLocation ID_KEY = ResourceLocation.fromNamespaceAndPath(Concocti.MODID, "solar");

    public SolarEmiStack(long amount) {
        this.amount = amount;
    }

    @Override
    public EmiStack copy() {
        return new SolarEmiStack(amount);
    }

    @Override
    public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
        PoseStack poseStack = draw.pose();
        if ((flags & RENDER_ICON) != 0) {
            RenderSystem.enableDepthTest();
            ResourceLocation tex = ResourceLocation.fromNamespaceAndPath(Concocti.MODID, "textures/gui/recipe_viewer/solar.png");
            poseStack.pushPose();
            poseStack.translate(0, 0, 200);
            draw.blit(tex, x, y, 0, 0, 16, 16, 16,16, 16);
            poseStack.popPose();
        }
    }

    @Override
    public boolean isEmpty() {
        return amount == 0;
    }

    @Override
    public DataComponentPatch getComponentChanges() {
        return DataComponentPatch.EMPTY;
    }

    @Override
    public Object getKey() {
        return ID_KEY;
    }

    @Override
    public ResourceLocation getId() {
        return ID_KEY;
    }

    @Override
    public List<Component> getTooltipText() {
        return List.of(
                getName()
        );
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        List<ClientTooltipComponent> list = new ArrayList<>();
        list.add(ClientTooltipComponent.create(getName().getVisualOrderText()));
        if (amount > 1) {
            list.add(ClientTooltipComponent.create(Component.literal(SolarBar.formatSolar(amount)).getVisualOrderText()));
        }
        EmiTooltipComponents.appendModName(list, Concocti.MODID);
        return list;
    }

    @Override
    public Component getName() {
        return Component.translatable("resource.concocti.solar").withStyle(ChatFormatting.YELLOW);
    }
}
