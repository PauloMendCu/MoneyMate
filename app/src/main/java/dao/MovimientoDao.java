package dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import entities.Movimiento;

@Dao
public interface MovimientoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Movimiento movimiento);
    @Query("SELECT * FROM movimientos WHERE isSynced = 0")
    List<Movimiento> getMovimientosNoSincronizados();
    @Update
    void update(Movimiento movimiento);

    @Delete
    void delete(Movimiento movimiento);
    @Query("SELECT * FROM movimientos WHERE id = :id LIMIT 1")
    Movimiento getMovimientoById(int id);

    @Query("SELECT * FROM movimientos")
    List<Movimiento> getAllMovimientos();

    @Query("SELECT * FROM movimientos WHERE cuentaId = :cuentaId")
    List<Movimiento> getMovimientosByCuentaId(int cuentaId);
}
