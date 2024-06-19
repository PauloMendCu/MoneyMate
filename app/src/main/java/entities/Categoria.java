package entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categorias")
public class Categoria {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "nombre")
    public String nombre;

    public String userId;  // Nuevo campo para el ID del usuario
    public boolean isSynced;

    public Categoria() {
    }

    public Categoria(int id, String nombre, String userId, boolean isSynced) {
        this.id = id;
        this.nombre = nombre;
        this.userId = userId;
        this.isSynced = isSynced;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setIsSynced(boolean isSynced) {
        this.isSynced = isSynced;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return this.nombre; // Asegúrate de que `nombre` sea el atributo correcto que contiene el nombre de la categoría
    }
}
