package me.paulf.fairylights.util.fabric;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

public class TransferUtil {
    /**
     * @return Either an outer transaction or a nested one in the current open one
     */
    public static Transaction getTransaction() {
        if (Transaction.isOpen()) {
            //noinspection deprecation
            TransactionContext open = Transaction.getCurrentUnsafe();
            if (open != null) {
                return open.openNested();
            }
        }
        return Transaction.openOuter();
    }

    /** Quickly insert and commit the given variant with the given amount. */
    public static <T> long insert(Storage<T> storage, T variant, long amount) {
        if (!storage.supportsInsertion()) return 0;
        try (Transaction t = getTransaction()) {
            long inserted = storage.insert(variant, amount, t);
            t.commit();
            return inserted;
        }
    }

    /** Quickly insert and commit the given ItemStack. */
    public static long insertItem(Storage<ItemVariant> storage, ItemStack stack) {
        return insert(storage, ItemVariant.of(stack), stack.getCount());
    }
}
