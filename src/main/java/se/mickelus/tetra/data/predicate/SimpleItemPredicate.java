package se.mickelus.tetra.data.predicate;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.util.RegistryHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@ParametersAreNonnullByDefault
public class SimpleItemPredicate implements TetraItemPredicate {
    private final Set<Item> items;
    /**
     * Whether the predicate declared an item constraint at all. Item ids that don't resolve (e.g. they belong to
     * a mod that isn't installed) get dropped while building {@link #items}, so an empty set is ambiguous: it
     * means either "no items were declared" or "items were declared but none of them exist". Only the former may
     * fall through to the tag check - treating the latter as unconstrained would make the predicate match every
     * item in the game. On 1.20 this case was handled by ItemPredicate.fromJson throwing on unknown ids, which
     * left the predicate null and disabled whatever referenced it.
     */
    private final boolean hasItemConstraint;
    @Nullable
    private final TagKey<Item> tag;

    public SimpleItemPredicate(Collection<ResourceLocation> itemIds, @Nullable ResourceLocation tagId) {
        this.items = new HashSet<>();
        this.hasItemConstraint = !itemIds.isEmpty();
        itemIds.stream()
                .map(itemId -> RegistryHelper.get(BuiltInRegistries.ITEM, itemId))
                .filter(item -> item != null)
                .forEach(items::add);
        this.tag = tagId != null ? ItemTags.create(tagId) : null;
    }

    @Override
    public boolean matches(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }

        if (hasItemConstraint && !items.contains(itemStack.getItem())) {
            return false;
        }

        return tag == null || itemStack.is(tag);
    }
}
