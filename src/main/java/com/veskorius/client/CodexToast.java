package com.veskorius.client;

import com.veskorius.codex.CodexEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Bulle « nouvelle entrée de Codex ».
 *
 * <p>Le Codex s'écrivait tout seul depuis le début — et <b>personne ne s'en apercevait</b>.
 * Les pages apparaissaient en silence pendant qu'on jouait, si bien que le livre restait
 * l'objet qu'on ouvre une fois au début et qu'on n'ouvre plus. C'est le problème que
 * l'idée d'un déblocage par craft cherchait à résoudre ; il ne manquait pas une serrure,
 * il manquait un <b>signal</b>.
 *
 * <p>Elle nomme l'entrée et montre son icône : on sait ce qu'on vient de gagner sans
 * ouvrir le livre, et on décide si ça vaut le détour maintenant ou plus tard.
 */
public class CodexToast implements Toast {

    private static final ResourceLocation BACKGROUND =
        ResourceLocation.withDefaultNamespace("toast/advancement");

    private static final long DURATION_MS = 5000L;

    private final CodexEntry entry;

    public CodexToast(CodexEntry entry) {
        this.entry = entry;
    }

    @Override
    public Visibility render(GuiGraphics graphics, ToastComponent component, long timeSinceLastVisible) {
        graphics.blitSprite(BACKGROUND, 0, 0, width(), height());
        var font = component.getMinecraft().font;
        graphics.drawString(font, Component.translatable("gui.veskorius.codex.toast"),
            30, 7, 0xFFB57CE0, false);
        graphics.drawString(font, Component.translatable(entry.titleKey()),
            30, 18, 0xFFFFFFFF, false);
        graphics.renderFakeItem(entry.icon(), 8, 8);
        return timeSinceLastVisible >= DURATION_MS * component.getNotificationDisplayTimeMultiplier()
            ? Visibility.HIDE : Visibility.SHOW;
    }

    /**
     * Deux entrées débloquées au même instant ne doivent pas se remplacer l'une l'autre :
     * le jeton est l'identité de l'entrée, pas la classe. Sans ça, gagner trois pages d'un
     * coup — ce qui arrive au passage d'un palier — n'en annoncerait qu'une.
     */
    @Override
    public Object getToken() {
        return entry.id();
    }
}
