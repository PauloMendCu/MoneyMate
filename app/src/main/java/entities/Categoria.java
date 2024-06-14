package entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categorias")
public class Categoria {
    @PrimaryKey
    private int id;

    @ColumnInfo(name = "nombre")
    private String nombre;
    private boolean isSynced;

    public Categoria() {
    }

    public Categoria(int id, String nombre, boolean isSynced) {
        this.id = id;
        this.nombre = nombre;
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

    @Override
    public String toString() {
        return this.nombre; // Asegúrate de que `nombre` sea el atributo correcto que contiene el nombre de la categoría
    }
}
