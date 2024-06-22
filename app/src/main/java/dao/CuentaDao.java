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

@Dao
public interface CuentaDao {
    @Query("SELECT * FROM cuentas")
    List<Cuenta> getAll();

    @Query("SELECT * FROM cuentas WHERE is_synced = 0")
    List<Cuenta> getCuentasNoSincronizadas();

    @Query("SELECT * FROM cuentas WHERE userId = :userId")
    List<Cuenta> getAllByUser(String userId);

    @Query("SELECT * FROM cuentas WHERE userId = :userId AND is_synced = 0")
    List<Cuenta> getUnsyncedCuentas(String userId);

    @Query("SELECT * FROM cuentas WHERE id = :id AND userId = :userId")
    Cuenta getCuentaById(int id, String userId);

    @Delete
    void delete(Cuenta cuenta);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Cuenta cuenta);

    @Update
    void update(Cuenta cuenta);

    @Query("SELECT * FROM cuentas")
    List<Cuenta> getAllCuentas();

    @Query("DELETE FROM cuentas")
    void deleteAllCuentas();

    @Query("SELECT COALESCE(MAX(id), 0) + 1 FROM cuentas")
    int generateUniqueId();

    @Query("SELECT id FROM cuentas")
    List<Integer> getAllIds();
}
