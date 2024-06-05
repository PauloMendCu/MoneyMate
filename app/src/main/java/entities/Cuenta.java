package entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cuentas")
public class Cuenta {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String nombre;
    private double saldo;
    private int tipo;
    private boolean isSynced;  // Nuevo campo


    public Cuenta(int id, String nombre, double saldo, int tipo, boolean isSynced) {
        this.id = id;
        this.nombre = nombre;
        this.saldo = saldo;
        this.tipo = tipo;
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

    public boolean isSynced() {
        return isSynced;
    }

    public void setIsSynced(boolean isSynced) {
        this.isSynced = isSynced;
    }
}