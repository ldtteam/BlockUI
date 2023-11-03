package com.ldtteam.blockui.hooks;

import com.ldtteam.blockui.mod.Log;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.IForgeRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Core class for managing and handling gui hooks
 * 
 * @param <T> instance of U
 * @param <U> forge-register type
 * @param <K> hashable thing to hash T, can be same as T
 */
public abstract class HookManager<T, U, K>
{
    /**
     * active ray trace scroll listener
     */
    private static HookScreen scrollListener;

    /**
     * list of registered hooks
     */
    private final List<HookEntry> registry = new ArrayList<>();
    /**
     * list of windows being rendered
     */
    private final Map<K, WindowEntry> activeWindows = new HashMap<>();

    private final IForgeRegistry<U> typeRegistryReference;

    protected HookManager(final IForgeRegistry<U> typeRegistryReference)
    {
        this.typeRegistryReference = typeRegistryReference;
    }

    /**
     * Creates a new entry in hook registry, making the system aware of new hook.
     *
     * @param targetThing    registry object of thing on which gui should be displayed on
     * @param guiLoc         location of gui xml
     * @param expirationTime how long should gui remain opened after the condition stops being satisfied [in millis]
     * @param trigger        trigger condition
     * @param shouldOpen     gets fired when gui is about to be opened, can deny opening
     * @param onOpen         gets fired when gui is opened
     * @param onClose        gets fired when gui is closed
     * @see IGuiHookable for gui callbacks
     */
    @SuppressWarnings("unchecked")
    protected void registerInternal(final U targetThing,
        final ResourceLocation guiLoc,
        final long expirationTime,
        final TriggerMechanism trigger,
        final BiPredicate<? extends T, TriggerMechanism> shouldOpen,
        final IGuiActionCallback<? extends T> onOpen,
        final IGuiActionCallback<? extends T> onClose)
    {
        Objects.requireNonNull(targetThing, "Target can't be null!");
        Objects.requireNonNull(guiLoc, "Gui location can't be null!");
        Objects.requireNonNull(trigger, "Trigger can't be null!");

        final BiPredicate<T, TriggerMechanism> shouldOpenTest = Objects.requireNonNullElse((BiPredicate<T, TriggerMechanism>) shouldOpen, (t, tt) -> true);
        final IGuiActionCallback<T> onOpenListener = Objects.requireNonNullElse((IGuiActionCallback<T>) onOpen, IGuiActionCallback.noAction());
        final IGuiActionCallback<T> onClosedListener = Objects.requireNonNullElse((IGuiActionCallback<T>) onClose, IGuiActionCallback.noAction());
        final ResourceLocation registryKey = typeRegistryReference.getKey(targetThing);

        final Optional<HookEntry> existing = registry.stream()
            .filter(hook -> hook.getTargetThingRegistryKey().equals(registryKey) && hook.trigger.getClass() == trigger.getClass())
            .findFirst();
        if (existing.isPresent())
        {
            Log.getLogger()
                .debug("Moving \"{}\" hook (with trigger \"{}\") from \"{}\" to \"{}\"",
                    registryKey,
                    existing.get().trigger.getName(),
                    existing.get().guiLoc,
                    guiLoc);
            registry.remove(existing.get());
        }

        registry.add(new HookEntry(targetThing, guiLoc, expirationTime, trigger, shouldOpenTest, onOpenListener, onClosedListener));
    }

    /**
     * Removes all hooks (regardless type) for given registry key.
     *
     * @param resLoc registry key to remove
     * @return true if anything got removed
     */
    public boolean unregister(final ResourceLocation resLoc)
    {
        return registry.removeIf(hook -> hook.getTargetThingRegistryKey().equals(resLoc));
    }

    /**
     * Removes all hooks (regardless type) for given registry thing.
     *
     * @param  thing registry thing to remove
     * @return       true if anything got removed
     */
    public boolean unregister(final U thing)
    {
        return unregister(typeRegistryReference.getKey(thing));
    }

    /**
     * Removes all hooks for given registry key and trigger type.
     *
     * @param  resLoc      registry key to remove
     * @param  triggerType trigger type
     * @return             true if anything got removed
     */
    public boolean unregister(final ResourceLocation resLoc, final TriggerMechanism triggerType)
    {
        return registry.removeIf(hook -> hook.getTargetThingRegistryKey().equals(resLoc) && hook.trigger.getClass() == triggerType.getClass());
    }

    /**
     * Removes all hooks for given registry thing and trigger type.
     *
     * @param  thing       registry thing to remove
     * @param  triggerType trigger type
     * @return             true if anything got removed
     */
    public boolean unregister(final U thing, final TriggerMechanism triggerType)
    {
        return unregister(typeRegistryReference.getKey(thing), triggerType);
    }

    /**
     * @param thingType registered type
     * @param trigger   hook trigger
     * @return all things of thingType being triggered by trigger of given hook
     */
    protected abstract List<T> findTriggered(final U thingType, final TriggerMechanism trigger);

    /**
     * @param thing instance of registered type
     * @return hashable key unique to every thing
     */
    protected abstract K keyMapper(final T thing);

    /**
     * Translates matrixstack from 0,0,0 to bottom_middle of gui.
     *
     * @param ms           matrixstack of world rendering
     * @param thing        instance of registered type
     * @param partialTicks partialTicks, see world rendering
     */
    protected abstract void translateToGuiBottomCenter(final PoseStack ms, final T thing, final float partialTicks);

    protected void tick(final long ticks)
    {
        final long now = System.currentTimeMillis();

        // find new things (aka trigger tick)
        registry.forEach(hook -> {
            if (hook.trigger.canTick(ticks))
            {
                findTriggered(hook.targetThing, hook.trigger).forEach(thing -> {
                    final K key = keyMapper(thing);
                    final WindowEntry entry = activeWindows.get(key);

                    // new entry or override
                    if ((entry == null || entry.hook.trigger.isLowerPriority(hook.trigger)) && hook.shouldOpen.test(thing, hook.trigger))
                    {
                        if (entry != null)
                        {
                            entry.screen.removed();
                        }

                        final WindowEntry window = new WindowEntry(now, thing, hook, HookWindow::new);
                        activeWindows.put(key, window);
                        window.screen.init(Minecraft.getInstance(), window.screen.getWindow().getWidth(), window.screen.getWindow().getHeight());
                    }
                    // already existing entry
                    else if (entry != null)
                    {
                        entry.lastTimeAccessed = now;
                    }
                });
            }
        });

        // check for expired windows or tick them
        // values().remove() uses naive non-hash removing, but removeIf uses HashIterator
        activeWindows.values().removeIf(entry -> {
            if (entry.hook.trigger.canTick(ticks) && now - entry.lastTimeAccessed > entry.hook.expirationTime) // expired
            {
                entry.screen.removed();
                return true;
            }
            else // tickable
            {
                entry.screen.tick();
                return false;
            }
        });
    }

    protected void render(final PoseStack ms, final float partialTicks)
    {
        activeWindows.values().forEach(entry -> {
            ms.pushPose();
            translateToGuiBottomCenter(ms, entry.thing, partialTicks);
            ms.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
            ms.scale(-0.01F, -0.01F, 0.01F);
            entry.screen.render(ms);
            ms.popPose();
        });
    }

    // scroll hook management

    /**
     * @param horizontalWheel x-axis scrolling, minus for down, plus for up.
     * @param verticalWheel   y-axis scrolling, minus for down, plus for up.
     */
    public static boolean onScroll(final double horizontalWheel, final double verticalWheel)
    {
        if (scrollListener != null)
        {
            return scrollListener.mouseScrolled(horizontalWheel, verticalWheel);
        }
        return false;
    }

    public static HookScreen getScrollListener()
    {
        return scrollListener;
    }

    public static void setScrollListener(final HookScreen scrollListener)
    {
        HookManager.scrollListener = scrollListener;
    }

    /**
     * Represents registered hook.
     */
    protected class HookEntry
    {
        protected final U targetThing;
        protected final ResourceLocation guiLoc;
        protected final long expirationTime;
        protected final TriggerMechanism trigger;
        protected final BiPredicate<T, TriggerMechanism> shouldOpen;
        protected final IGuiActionCallback<T> onOpen;
        protected final IGuiActionCallback<T> onClose;

        private HookEntry(final U targetThing,
            final ResourceLocation guiLoc,
            final long expirationTime,
            final TriggerMechanism trigger,
            final BiPredicate<T, TriggerMechanism> shouldOpen,
            final IGuiActionCallback<T> onOpen,
            final IGuiActionCallback<T> onClose)
        {
            this.targetThing = targetThing;
            this.guiLoc = guiLoc;
            this.expirationTime = expirationTime;
            this.trigger = trigger;
            this.shouldOpen = shouldOpen;
            this.onOpen = onOpen;
            this.onClose = onClose;
        }

        public ResourceLocation getTargetThingRegistryKey()
        {
            return typeRegistryReference.getKey(targetThing);
        }
    }

    /**
     * Represents active (being rendered) window.
     */
    protected class WindowEntry
    {
        private long lastTimeAccessed = 0;
        protected final T thing;
        protected final HookEntry hook;
        protected final HookScreen screen;

        public WindowEntry(final long lastTimeAccessed,
            final T thing,
            final HookManager<T, U, K>.HookEntry hook,
            final Function<WindowEntry, HookWindow<T, U>> windowFactory)
        {
            this.lastTimeAccessed = lastTimeAccessed;
            this.thing = thing;
            this.hook = hook;
            this.screen = windowFactory.apply(this).getScreen();
        }
    }
}
