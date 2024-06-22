package dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import entities.Categoria;
import entities.Movimiento;

@Dao
public interface MovimientoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Movimiento movimiento);

    @Query("SELECT * FROM movimientos WHERE userId = :userId")
    List<Movimiento> getAllByUser(String userId);
    @Query("SELECT * FROM movimientos WHERE isSynced = 0")
    List<Movimiento> getMovimientosNoSincronizados();

    @Query("SELECT * FROM movimientos WHERE userId = :userId AND isSynced = 0")
    List<Movimiento> getUnsyncedMovimientos(String userId);
    @Update
    void update(Movimiento movimiento);
    @Query("SELECT * FROM movimientos WHERE userId =:userId AND cuentaId = :cuentaId OR (tipo = 'Transferencia' AND cuentaDestId = :cuentaId) ORDER BY fecha DESC")
    List<Movimiento> getMovimientosPorCuenta(String userId, int cuentaId);
    @Delete
    void delete(Movimiento movimiento);
    @Query("SELECT * FROM movimientos WHERE id = :id AND userId = :userId")
    Movimiento getMovimientoById(int id, String userId);

    @Transaction
    default void insertOrUpdate(Movimiento movimiento) {
        long id = insert(movimiento);
        if (id == -1) {
            update(movimiento);
        }
    }

    @Query("SELECT * FROM movimientos")
    List<Movimiento> getAllMovimientos();

}
