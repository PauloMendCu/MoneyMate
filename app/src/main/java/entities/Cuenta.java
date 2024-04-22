package entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Cuenta {
    private String nombre;
    private double saldo;
    private List<Movimiento> movimientos;

    public Cuenta(String nombre) {
        this.nombre = nombre;
        this.saldo = 0.0;
        this.movimientos = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public double getSaldo() {
        return saldo;
    }

    public List<Movimiento> getMovimientos() {
        return movimientos;
    }

    public void agregarMovimiento(Movimiento movimiento) {
        movimientos.add(movimiento);
        actualizarSaldo(movimiento);
    }

    private void actualizarSaldo(Movimiento movimiento) {
        if (movimiento.getTipo() == Movimiento.TipoMovimiento.INGRESO) {
            saldo += movimiento.getMonto();
        } else if (movimiento.getTipo() == Movimiento.TipoMovimiento.GASTO) {
            saldo -= movimiento.getMonto();
        } else if (movimiento.getTipo() == Movimiento.TipoMovimiento.TRANSFERENCIA) {
            saldo -= movimiento.getMonto();
        }
    }

    public void transferirEntreCuentas(double monto, Cuenta cuentaDestino, String descripcion) {
        Movimiento movimientoOrigen = new Movimiento(descripcion, monto, new Date(), Movimiento.TipoMovimiento.TRANSFERENCIA);
        Movimiento movimientoDestino = new Movimiento(descripcion, monto, new Date(), Movimiento.TipoMovimiento.TRANSFERENCIA);

        this.agregarMovimiento(movimientoOrigen);
        cuentaDestino.agregarMovimiento(movimientoDestino);
    }

}