package utils;

import android.content.Context;

import androidx.room.Room;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import entities.AppDatabase;
import entities.Categoria;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import services.ICategoriaService;
import retrofit2.converter.gson.GsonConverterFactory;

public class SyncService {
    private AppDatabase db;
    private ICategoriaService categoriaService;
    private String userId;

    public SyncService(Context context) {
        db = Room.databaseBuilder(context, AppDatabase.class, "database-name")
                .allowMainThreadQueries()
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://662da416a7dda1fa378afbe0.mockapi.io/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        categoriaService = retrofit.create(ICategoriaService.class);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = (currentUser != null) ? currentUser.getUid() : null;
    }
    public void syncCategories() {
        if (userId == null) return;
        List<Categoria> unsyncedCategories = db.categoriaDao().getUnsyncedCategories(userId);
        for (Categoria categoria : unsyncedCategories) {
            Categoria categoriaToSync = new Categoria(categoria.id, categoria.nombre, categoria.userId, true);

            categoriaService.agregarCategoria(categoriaToSync).enqueue(new Callback<Categoria>() {
                @Override
                public void onResponse(Call<Categoria> call, Response<Categoria> response) {
                    if (response.isSuccessful()) {
                        Categoria syncedCategoria = response.body();
                        if (syncedCategoria != null) {
                            categoria.setIsSynced(true);
                            db.categoriaDao().update(categoria);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Categoria> call, Throwable t) {
                }
            });
        }
    }
    public void agregarCategoriaEnApi(Categoria categoria, SyncCallback callback) {
        categoriaService.agregarCategoria(categoria).enqueue(new Callback<Categoria>() {
            @Override
            public void onResponse(Call<Categoria> call, Response<Categoria> response) {
                if (response.isSuccessful()) {
                    Categoria syncedCategoria = response.body();
                    if (callback != null) {
                        callback.onSyncComplete(syncedCategoria);
                    }
                }
            }

            @Override
            public void onFailure(Call<Categoria> call, Throwable t) {
            }
        });
    }

    public void fetchCategoriesFromApi() {
        if (userId == null) return;
        categoriaService.getCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful()) {
                    List<Categoria> apiCategories = response.body();
                    for (Categoria apiCategoria : apiCategories) {
                        if (apiCategoria.userId.equals(userId)) {
                            Categoria existingCategoria = db.categoriaDao().getCategoriaById(apiCategoria.id, userId);
                            if (existingCategoria == null) {
                                apiCategoria.setIsSynced(true);
                                db.categoriaDao().insert(apiCategoria);
                            } else if (!existingCategoria.isSynced()) {
                                existingCategoria.setIsSynced(true);
                                existingCategoria.nombre = apiCategoria.nombre; // Actualiza otros campos si es necesario
                                db.categoriaDao().update(existingCategoria);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
            }
        });
    }


    public void deleteCategoria(Categoria categoria) {
        if (!categoria.userId.equals(userId)) {
            return; // No se permite eliminar categorías que no pertenecen al usuario actual
        }

        categoriaService.deleteCategoria(categoria.id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    db.categoriaDao().delete(categoria);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Handle failure
            }
        });
    }
    public void updateCategoria(Categoria categoria) {
        if (!categoria.userId.equals(userId)) {
            return; // No se permite actualizar categorías que no pertenecen al usuario actual
        }

        categoriaService.updateCategoria(categoria.id, categoria).enqueue(new Callback<Categoria>() {
            @Override
            public void onResponse(Call<Categoria> call, Response<Categoria> response) {
                if (response.isSuccessful()) {
                    categoria.setIsSynced(true); // Aseguramos que isSynced se establece en true
                    db.categoriaDao().update(categoria);
                }
            }

            @Override
            public void onFailure(Call<Categoria> call, Throwable t) {
                // Handle failure
            }
        });
    }

}