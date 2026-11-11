package me.paulf.fairylights.server.item.components;

import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public class ModifiableDataComponentMap implements DataComponentMap {
    private final Reference2ObjectMap<DataComponentType<?>, Object> map = new Reference2ObjectLinkedOpenHashMap<>();

    @Override
    public @Nullable <T> T get(DataComponentType<? extends T> component) {
        return (T) this.map.get(component);
    }

    @Override
    public Set<DataComponentType<?>> keySet() {
        return this.map.keySet();
    }

    public <T> void set(DataComponentType<T> type, T object) {
        if (object == null)
            map.remove(type);
        else
            map.put(type, object);
    }

    public void remove(DataComponentType<?> type) {
        map.remove(type);
    }

    public void merge(DataComponentMap map) {
        for (TypedDataComponent<?> component : map) {
            this.map.put(component.type(), component.value());
        }
    }
}
