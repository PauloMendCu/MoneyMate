package entities;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dao.CategoriaDao;
import dao.CuentaDao;
import dao.MovimientoDao;

@Database(entities = {Cuenta.class, Movimiento.class, Categoria.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static final int NUMBER_OF_THREADS = 4;
    private static volatile AppDatabase INSTANCE;
    public abstract CuentaDao cuentaDao();
    public abstract MovimientoDao movimientoDao();
    public abstract CategoriaDao categoriaDao();

    private static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private static final RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onCreate(SupportSQLiteDatabase db) {
                    super.onCreate(db);
                    // Si es necesario, puedes agregar lógica para inicializar la base de datos con datos predeterminados
                }
            };

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "database-name")
                            .addCallback(sRoomDatabaseCallback)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static ExecutorService getDatabaseWriteExecutor() {
        return databaseWriteExecutor;
    }

    public int generateUniqueId() {
        int maxId = 0;
        List<Integer> ids = cuentaDao().getAllIds();
        if (!ids.isEmpty()) {
            maxId = Collections.max(ids);
        }
        return maxId + 1;
    }
}

