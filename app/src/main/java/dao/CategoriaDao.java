package dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import entities.Categoria;

@Dao
public interface CategoriaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Categoria categoria);

    @Query("SELECT * FROM categorias")
    List<Categoria> getAllCategorias();

    @Query("SELECT * FROM categorias WHERE userId = :userId")
    List<Categoria> getAllByUser(String userId);

    @Query("SELECT * FROM categorias WHERE id = :id AND userId = :userId LIMIT 1")
    Categoria getCategoriaById(int id, String userId);

    @Query("SELECT * FROM categorias WHERE isSynced = 0")
    List<Categoria> getCategoriasNoSincronizadas();

    @Query("SELECT * FROM categorias WHERE userId = :userId AND isSynced = 0")
    List<Categoria> getUnsyncedCategories(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Categoria> categorias);
    @Query("DELETE FROM categorias WHERE userId = :userId")
    void deleteAllCategoriasByUser(String userId);
    @Query("DELETE FROM categorias")
    void deleteAllCategorias();

    @Update
    void update(Categoria categoria);

    @Delete
    void delete(Categoria categoria);
}
