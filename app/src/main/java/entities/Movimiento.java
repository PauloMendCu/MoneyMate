package entities;

import java.util.Comparator;

public class Movimiento {
    private int id;
    private String descripcion;
    private double monto;
    private String fecha;
    private String tipo;
    private int cuentaId;
    private int categoriaId;

    public Movimiento(int id, String descripcion, double monto, String fecha, String tipo, int cuentaId, int categoriaId) {
        this.id = id;
        this.descripcion = descripcion;
        this.monto = monto;
        this.fecha = fecha;
        this.tipo = tipo;
        this.cuentaId = cuentaId;
        this.categoriaId = categoriaId;
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


    public static Comparator<Movimiento> ordenarPorFechaDescendente = new Comparator<Movimiento>() {
        @Override
        public int compare(Movimiento m1, Movimiento m2) {
            return m2.getFecha().compareTo(m1.getFecha());
        }
    };

}
