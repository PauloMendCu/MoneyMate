package entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Comparator;
@Entity(tableName = "movimientos")
public class Movimiento {
    @PrimaryKey
    private int id;

    @ColumnInfo(name = "descripcion")
    private String descripcion;

    @ColumnInfo(name = "monto")
    private double monto;

    @ColumnInfo(name = "fecha")
    private String fecha;

    @ColumnInfo(name = "tipo")
    private String tipo;

    @ColumnInfo(name = "cuentaId")
    private int cuentaId;

    @ColumnInfo(name = "categoriaId")
    private int categoriaId;

    @ColumnInfo(name = "cuentaDestId")
    private int cuentaDestId;
    private boolean isSynced;  // Nuevo campo


    public Movimiento(int id, String descripcion, double monto, String fecha, String tipo, int cuentaId, int categoriaId, int cuentaDestId, boolean isSynced) {
        this.id = id;
        this.descripcion = descripcion;
        this.monto = monto;
        this.fecha = fecha;
        this.tipo = tipo;
        this.cuentaId = cuentaId;
        this.categoriaId = categoriaId;
        this.cuentaDestId = cuentaDestId;
        this.isSynced = isSynced;
    }

    public Movimiento() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(int cuentaId) {
        this.cuentaId = cuentaId;
    }

    public int getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }

    public int getCuentaDestId() {
        return cuentaDestId;
    }

    public void setCuentaDestId(int cuentaDestId) {
        this.cuentaDestId = cuentaDestId;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setIsSynced(boolean isSynced) {
        this.isSynced = isSynced;
    }

    public static Comparator<Movimiento> ordenarPorFechaDescendente = new Comparator<Movimiento>() {
        @Override
        public int compare(Movimiento m1, Movimiento m2) {
            return m2.getFecha().compareTo(m1.getFecha());
        }
    };

}
