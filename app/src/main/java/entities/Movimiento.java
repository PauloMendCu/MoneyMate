package entities;

import java.util.Date;

public class Movimiento {
    private String descripcion;
    private double monto;
    private Date fecha;
    private TipoMovimiento tipo;

    public Movimiento(String descripcion, double monto, Date fecha, TipoMovimiento tipo) {
        this.descripcion = descripcion;
        this.monto = monto;
        this.fecha = fecha;
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getMonto() {
        return monto;
    }

    public Date getFecha() {
        return fecha;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public enum TipoMovimiento {
        INGRESO,
        GASTO,
        TRANSFERENCIA
    }
}