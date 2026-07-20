package com.veskorius.menu;

import com.veskorius.Veskorius;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(BuiltInRegistries.MENU, Veskorius.MOD_ID);

    /**
     * {@code IMenuTypeExtension.create} (et non {@code new MenuType<>(...)}) :
     * c'est la variante NeoForge qui donne acces aux donnees supplementaires
     * envoyees a l'ouverture — ici la BlockPos de la machine.
     */
    public static final DeferredHolder<MenuType<?>, MenuType<ResonanceStabilizerMenu>>
        RESONANCE_STABILIZER = MENUS.register("resonance_stabilizer",
            () -> IMenuTypeExtension.create(ResonanceStabilizerMenu::new));
}
