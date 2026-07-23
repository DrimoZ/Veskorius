package com.veskorius.energy;

import com.veskorius.Veskorius;
import com.veskorius.block.entity.FieldEmitterBlockEntity;
import com.veskorius.block.entity.ModBlockEntities;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = Veskorius.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModCapabilities {

    /**
     * Capability exposée par les émetteurs de champ. {@code createVoid} (sans
     * contexte de face) : un champ de Résonance est radial, il n'a pas de côté
     * privilégié — contrairement à un ItemHandler qu'on branche par une face.
     */
    public static final BlockCapability<IResonanceField, Void> RESONANCE_FIELD =
        BlockCapability.createVoid(
            ResourceLocation.fromNamespaceAndPath(Veskorius.MOD_ID, "resonance_field"),
            IResonanceField.class);

    private ModCapabilities() {
    }

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(RESONANCE_FIELD, ModBlockEntities.FIELD_EMITTER.get(),
            (emitter, ctx) -> emitter);

        // Slot de carburant accessible aux hoppers : insérer des Stable Crystals
        // depuis un système d'automatisation, sans passer par le GUI.
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.FIELD_EMITTER.get(),
            (emitter, side) -> emitter.getFuelHandler());

        // Émetteur Accordable : même contrat, bande harmonique choisie.
        event.registerBlockEntity(RESONANCE_FIELD, ModBlockEntities.TUNABLE_FIELD_EMITTER.get(),
            (emitter, ctx) -> emitter);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.TUNABLE_FIELD_EMITTER.get(),
            (emitter, side) -> emitter.getFuelHandler());

        // Machines actives : vue sidée de l'inventaire (item I/O configurable par face,
        // 12-UX-and-Advancements.md). L'ÉNERGIE ne passe jamais par une capability —
        // elle reste sans tuyaux, par champ. Défaut « façon four » : sortie sous le bloc,
        // entrée par les autres faces (getItemHandler renvoie null pour une face désactivée).
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.RESONANCE_STABILIZER.get(),
            (machine, side) -> machine.getItemHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.COMPONENT_ASSEMBLER.get(),
            (machine, side) -> machine.getItemHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.RESONANCE_WHETSTONE.get(),
            (machine, side) -> machine.getItemHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.FLUX_PURIFIER.get(),
            (machine, side) -> machine.getItemHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.CRYSTAL_CRUSHER.get(),
            (machine, side) -> machine.getItemHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.CRYSTAL_ROOST.get(),
            (machine, side) -> machine.getItemHandler(side));
    }
}
