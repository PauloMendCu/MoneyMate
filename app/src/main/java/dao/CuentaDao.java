package dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import entities.Cuenta;

@Dao
public interface CuentaDao {
    @Query("SELECT * FROM cuentas")
    List<Cuenta> getAll();

    @Query("SELECT * FROM cuentas WHERE is_synced = 0")
    List<Cuenta> getCuentasNoSincronizadas();


    @Query("SELECT * FROM cuentas WHERE id = :id")
    Cuenta getCuentaById(int id);

    @Delete
    void delete(Cuenta cuenta);
    @Insert
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
