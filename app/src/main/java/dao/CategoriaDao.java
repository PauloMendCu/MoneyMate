package dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import entities.Categoria;
import entities.Cuenta;
import entities.Movimiento;

@Dao
public interface CategoriaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Categoria categoria);

    @Query("SELECT * FROM categorias")
    List<Categoria> getAllCategorias();

    @Query("SELECT * FROM categorias WHERE id = :id LIMIT 1")
    Categoria getCategoriaById(int id);

    @Query("SELECT * FROM categorias WHERE isSynced = 0")
    List<Categoria> getCategoriasNoSincronizadas();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Categoria> categorias);

    @Update
    void update(Categoria categoria);
    @Query("DELETE FROM categorias")
    void deleteAllCategorias();

    @Delete
    void delete(Categoria categoria);

}
