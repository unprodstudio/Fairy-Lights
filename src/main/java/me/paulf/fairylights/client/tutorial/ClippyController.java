package me.paulf.fairylights.client.tutorial;

import com.google.common.collect.ImmutableMap;
import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.client.FLClientConfig;
import me.paulf.fairylights.server.item.FLItems;
import me.paulf.fairylights.server.item.crafting.FLCraftingRecipes;
import me.paulf.fairylights.util.LazyItemStack;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ClippyController {
    private final ImmutableMap<String, Supplier<State>> states = Stream.<Supplier<State>>of(
            NoProgressState::new,
            CraftHangingLightsState::new,
            CompleteState::new
        ).collect(ImmutableMap.toImmutableMap(s -> s.get().name(), Function.identity()));

    private State state = new NoProgressState();

    public void init() {
        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, world) -> {
            this.reload();
        });
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (!mc.isPaused() && mc.player != null) {
                this.state.tick(mc.player, this);
            }
        });
        ModConfigEvents.loading(FairyLights.ID).register(config -> {
            if (config.getSpec() == FLClientConfig.SPEC && Minecraft.getInstance().player != null) {
                this.reload();
            }
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, mc) -> {
            this.reload();
            this.state.tick(mc.player, this);
        });
    }

    private void reload() {
        this.setState(this.states.getOrDefault(FLClientConfig.TUTORIAL.progress.get(), NoProgressState::new).get());
    }

    private void setState(final State state) {
        this.state.stop();
        this.state = state;
        this.state.start();
        FLClientConfig.TUTORIAL.progress.set(this.state.name());
        FLClientConfig.TUTORIAL.progress.save();
    }

    interface State {
        String name();

        default void start() {}

        default void tick(final LocalPlayer player, final ClippyController controller) {}

        default void stop() {}
    }

    static class NoProgressState implements State {
        @Override
        public String name() {
            return "none";
        }

        @Override
        public void tick(final LocalPlayer player, final ClippyController controller) {
            if (player.getInventory().contains(FLCraftingRecipes.LIGHTS)) {
                controller.setState(new CraftHangingLightsState());
            }
        }
    }

    static class CraftHangingLightsState implements State {
        final Balloon balloon;

        CraftHangingLightsState() {
            this.balloon = new Balloon(new LazyItemStack(FLItems.HANGING_LIGHTS, Item::getDefaultInstance),
                Component.translatable("tutorial.fairylights.craft_hanging_lights.title"),
                Component.translatable("tutorial.fairylights.craft_hanging_lights.description")
            );
        }

        @Override
        public String name() {
            return "hanging_lights";
        }

        @Override
        public void start() {
            Minecraft.getInstance().gui.toastManager().addToast(this.balloon);
        }

        @Override
        public void tick(final LocalPlayer player, final ClippyController controller) {
            if (!player.getInventory().contains(FLCraftingRecipes.LIGHTS) &&
                    !player.getInventory().getSelectedItem().is(FLCraftingRecipes.LIGHTS)) {
                controller.setState(new NoProgressState());
            } else if (FLItems.HANGING_LIGHTS.toOptional().filter(i ->
                    player.getInventory().getSelectedItem().getItem() == i ||
                    player.getInventory().contains(new ItemStack(i)) ||
                    player.getStats().getValue(Stats.ITEM_CRAFTED.get(i)) > 0).isPresent()) {
                controller.setState(new CompleteState());
            }
        }

        @Override
        public void stop() {
            this.balloon.hide();
        }
    }

    static class CompleteState implements State {
        @Override
        public String name() {
            return "complete";
        }
    }

    static class Balloon implements Toast {
        static final Identifier TEXTURE = Identifier.withDefaultNamespace("toast/tutorial");

        final LazyItemStack stack;
        final Component title;
        @Nullable
        final Component subtitle;
        Toast.Visibility visibility;

        Balloon(final LazyItemStack stack, final Component title, @Nullable final Component subtitle) {
            this.stack = stack;
            this.title = title;
            this.subtitle = subtitle;
            this.visibility = Visibility.SHOW;
        }

        void hide() {
            this.visibility = Toast.Visibility.HIDE;
        }

        @Override
        public Visibility getWantedVisibility() {
            return this.visibility;
        }

        @Override
        public void update(ToastManager manager, long fullyVisibleForMs) {
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor stack, Font font, long fullyVisibleForMs) {
            stack.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 0, 160, 32);
            stack.fakeItem(this.stack.get(), 6 + 2, 6 + 2);
            if (this.subtitle == null) {
                stack.text(font, this.title, 30, 12, 0xFF500050, false);
            } else {
                stack.text(font, this.title, 30, 7, 0xFF500050, false);
                stack.text(font, this.subtitle, 30, 18, 0xFF000000, false);
            }
        }
    }
}
