package io.github.laptop59.concocti.network;

import io.github.laptop59.concocti.common.machine.FluidTankHolder;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class FluidBarInteractionPayloadC2SHandler {
    public static void handleData(final FluidBarInteractionPayloadC2S data, final IPayloadContext context) {
        // Try to get the cursor item of the player.
        context.enqueueWork(() -> {
                    Player player = context.player();
                    if (player instanceof ServerPlayer target) {
                        AbstractContainerMenu menu = target.containerMenu;
                        if (menu instanceof AbstractConcoctiMachineMenu<?> machineMenu) {
                            if (machineMenu.containerId != data.containerId()) return; // just in case
                            // We get the carried item.
                            ItemStack held = machineMenu.getCarried();
                            // Get the player's possible item capability.
                            IFluidHandlerItem capability = held.getCapability(Capabilities.FluidHandler.ITEM);
                            if (capability == null) return;
                            // Try to fill it.
                            if (machineMenu.getContainer() instanceof FluidTankHolder fluidTankHolder) {
                                // IFluidHandler handler = fluidTankHolder.getFluidTank();
                                IFluidHandler handler = fluidTankHolder.getIndexedFluidHandlers().get(data.tankId());
                                if (data.buttonNum() == 0) {
                                    // LEFT CLICK: fill item
                                    FluidStack drained = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                                    // Try filling the stack.
                                    int filled = capability.fill(drained, IFluidHandler.FluidAction.SIMULATE);
                                    if (filled > 0 && !(held.is(Items.BUCKET) && filled < 1000)) {
                                        // Success! fill the item.
                                        drained = handler.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                                        if (held.is(Items.BUCKET)) {
                                            // Buckets: return the filled fluid item.
                                            Fluid fluid = drained.getFluid();
                                            machineMenu.setCarried(new ItemStack(fluid.getBucket()));
                                        } else {
                                            capability.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                                        }
                                        // Play a sound.
                                        PacketDistributor.sendToPlayer(target, new FluidBarSoundPayloadS2C(true));
                                    }
                                } else if (data.buttonNum() == 1) {
                                    // RIGHT CLICK: empty item
                                    FluidStack drained = capability.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                                    // Try filling the stack.
                                    int filled = handler.fill(drained, IFluidHandler.FluidAction.SIMULATE);
                                    if (filled > 0 && !(held.getItem() instanceof BucketItem && filled < 1000)) {
                                        // Success! fill the tank.
                                        if (held.getItem() instanceof BucketItem) {
                                            // Buckets: fill the bucket fluid.
                                            machineMenu.setCarried(new ItemStack(Items.BUCKET));
                                            drained.setAmount(filled);
                                        } else {
                                            drained = capability.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                                        }
                                        handler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                                        // Play a sound.
                                        player.playSound(SoundEvents.BUCKET_EMPTY, 1.0F, 1.0F);
                                        PacketDistributor.sendToPlayer(target, new FluidBarSoundPayloadS2C(false));
                                    }
                                }
                            }
                        }
                    }
                })
                .exceptionally(e -> {
                    // Who cares anyway?
                    return null;
                });
    }
}
