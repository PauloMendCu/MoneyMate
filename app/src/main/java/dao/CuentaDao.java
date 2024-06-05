package dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import entities.Cuenta;

@Dao
public interface CuentaDao {
    @Query("SELECT * FROM cuentas")
    List<Cuenta> getAll();

    @Query("SELECT * FROM cuentas WHERE isSynced = 0")
    List<Cuenta> getCuentasNoSincronizadas();

    @Query("SELECT * FROM cuentas WHERE id = :id")
    Cuenta getCuentaById(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Cuenta cuenta);

    @Update
    void update(Cuenta cuenta);
    @Delete
    void delete(Cuenta cuenta);
}
