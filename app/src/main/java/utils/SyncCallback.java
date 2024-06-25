package utils;

import entities.Categoria;

public interface SyncCallback {
    void onSyncComplete();

    void onSyncComplete(Categoria syncedCategoria);
}
