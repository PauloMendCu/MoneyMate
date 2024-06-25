package entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cuentas")
public class Cuenta {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "nombre")
    private String nombre;
    @ColumnInfo(name = "saldo")
    private double saldo;
    @ColumnInfo(name = "tipo")
    private int tipo;
    public String userId;
    @ColumnInfo(name = "is_synced")
    private boolean isSynced;  // Nuevo campo
    public Cuenta(int id, String nombre, double saldo, int tipo, String userId, boolean isSynced) {
        this.id = id;
        this.nombre = nombre;
        this.saldo = saldo;
        this.tipo = tipo;
        this.userId = userId;
        this.isSynced = isSynced;
    }
    public Cuenta() {

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

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Getters y Setters para isSynced
    public boolean isSynced() {
        return isSynced;
    }

    public void setIsSynced(boolean isSynced) {
        this.isSynced = isSynced;
    }
    @Override
    public String toString() {
        return this.nombre; // Asegúrate de que `nombre` sea el atributo correcto que contiene el nombre de la cuenta
    }
}