package com.veskorius.compat.curios;

import com.veskorius.Veskorius;
import java.lang.reflect.Method;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Pont <b>optionnel</b> vers Curios (10-Mod-Integrations.md) : un objet de lecture de
 * champ rangé dans un slot Curios active le HUD exactement comme s'il était dans
 * l'inventaire.
 *
 * <p><b>Règle du dossier</b> : Curios est un confort d'ergonomie (libérer une case),
 * jamais un prérequis — sans lui, tout fonctionne depuis l'inventaire. C'est pourquoi
 * on ne compile pas contre son API : le mod ne gagne aucune dépendance de build, et un
 * changement d'API chez lui ne peut pas casser Veskorius. Le prix est cette poignée
 * d'appels par réflexion, isolés ici et désactivés définitivement à la première erreur
 * (on ne re-tente pas à chaque tick, et le HUD retombe simplement sur l'inventaire).
 */
public final class CuriosCompat {

    private static final String MOD_ID = "curios";
    private static final String API_CLASS = "top.theillusivec4.curios.api.CuriosApi";

    /** Résolution paresseuse : la classe peut être chargée avant que Curios le soit. */
    private static boolean resolved;
    private static boolean available;
    private static Method getCuriosInventory;
    private static Method getEquippedCurios;

    private CuriosCompat() {
    }

    /** Vrai si {@code item} est équipé dans un slot Curios de ce joueur. */
    public static boolean isEquipped(LivingEntity entity, Item item) {
        if (!resolve()) {
            return false;
        }
        try {
            Object inventory = getCuriosInventory.invoke(null, entity);
            if (inventory instanceof Optional<?> optional) {
                if (optional.isEmpty()) {
                    return false;
                }
                inventory = optional.get();
            }
            if (inventory == null) {
                return false;
            }
            if (!(getEquippedCurios.invoke(inventory) instanceof IItemHandler slots)) {
                return false;
            }
            for (int slot = 0; slot < slots.getSlots(); slot++) {
                ItemStack stack = slots.getStackInSlot(slot);
                if (!stack.isEmpty() && stack.is(item)) {
                    return true;
                }
            }
            return false;
        } catch (ReflectiveOperationException | RuntimeException e) {
            disable("appel de l'API Curios impossible", e);
            return false;
        }
    }

    private static boolean resolve() {
        if (resolved) {
            return available;
        }
        resolved = true;
        if (!ModList.get().isLoaded(MOD_ID)) {
            return false;
        }
        try {
            Class<?> api = Class.forName(API_CLASS);
            getCuriosInventory = api.getMethod("getCuriosInventory", LivingEntity.class);
            // Le type de retour est une interface de Curios : on ne la nomme pas, on
            // appelle la méthode sur l'objet obtenu.
            getEquippedCurios = getCuriosInventory.getReturnType() == Optional.class
                ? findEquippedGetter(api)
                : getCuriosInventory.getReturnType().getMethod("getEquippedCurios");
            available = getEquippedCurios != null;
            if (available) {
                Veskorius.LOGGER.info("Curios détecté : le HUD de champ acceptera un slot Curios.");
            }
            return available;
        } catch (ReflectiveOperationException | RuntimeException e) {
            disable("API Curios introuvable", e);
            return false;
        }
    }

    /**
     * {@code getCuriosInventory} rend un {@code Optional<ICuriosItemHandler>} : le
     * paramètre générique est effacé, on retrouve donc l'interface par son nom.
     */
    private static Method findEquippedGetter(Class<?> api) throws ReflectiveOperationException {
        Class<?> handler = Class.forName(
            "top.theillusivec4.curios.api.type.capability.ICuriosItemHandler", true, api.getClassLoader());
        return handler.getMethod("getEquippedCurios");
    }

    private static void disable(String reason, Throwable cause) {
        available = false;
        // Une seule fois : ce chemin est appelé à intervalle régulier.
        Veskorius.LOGGER.warn("Intégration Curios désactivée ({}) — le HUD de champ "
            + "reste disponible via l'inventaire.", reason, cause);
    }
}
