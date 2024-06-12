package adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymate.R;

import java.util.ArrayList;
import java.util.List;

import entities.Categoria;
import entities.Cuenta;
import entities.Movimiento;

public class MovimientoAdapter extends RecyclerView.Adapter<MovimientoAdapter.MovimientoViewHolder> {

    // Clase interna para el ViewHolder
    static class MovimientoViewHolder extends RecyclerView.ViewHolder {
        TextView descripcion;
        TextView nombreCuenta;
        TextView cuentaDestino;
        TextView fecha;
        TextView monto;
        TextView categoria;

        public MovimientoViewHolder(@NonNull View itemView) {
            super(itemView);
            descripcion = itemView.findViewById(R.id.texto_descripcion);
            nombreCuenta = itemView.findViewById(R.id.texto_nombre_cuenta);
            cuentaDestino = itemView.findViewById(R.id.texto_cuenta_destino);
            fecha = itemView.findViewById(R.id.texto_fecha);
            monto = itemView.findViewById(R.id.texto_monto);
            categoria = itemView.findViewById(R.id.texto_categoria);
        }
    }

    private List<Movimiento> movimientos;
    private List<Cuenta> cuentas;
    private List<Categoria> categorias;


    public MovimientoAdapter(List<Movimiento> movimientos, List<Cuenta> cuentas, List<Categoria> categorias) {
        this.movimientos = movimientos;
        this.cuentas = cuentas;
        this.categorias = categorias;
    }

    @NonNull
    @Override
    public MovimientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_movimiento, parent, false);
        return new MovimientoViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MovimientoViewHolder holder, int position) {
        Movimiento movimiento = movimientos.get(position);

        holder.descripcion.setText(movimiento.getDescripcion());
        holder.fecha.setText(movimiento.getFecha());

        String nombreCuenta = "Desconocido";
        String nombreCuentaDestino = "Desconocido";

        for (Cuenta cuenta : cuentas) {
            if (cuenta.getId() == movimiento.getCuentaId()) {
                nombreCuenta = cuenta.getNombre();
            }
            if (cuenta.getId() == movimiento.getCuentaDestId()) {
                nombreCuentaDestino = cuenta.getNombre();
            }
        }

        if (movimiento.getTipo().equals("Transferencia")) {
            holder.nombreCuenta.setText("Cuenta origen: " + nombreCuenta);
            holder.cuentaDestino.setVisibility(View.VISIBLE);
            holder.cuentaDestino.setText("Cuenta destino: " + nombreCuentaDestino);
        } else {
            holder.nombreCuenta.setText("Cuenta: " + nombreCuenta);
            holder.cuentaDestino.setVisibility(View.GONE);
        }

        String nombreCategoria = "Desconocido";
        for (Categoria categoria : categorias) {
            if (categoria.getId() == movimiento.getCategoriaId()) {
                nombreCategoria = categoria.getNombre();
                break;
            }
        }

        holder.categoria.setText(nombreCategoria);

        String montoTexto = "";
        int montoColor = 0;

        switch (movimiento.getTipo()) {
            case "Ingreso":
                montoTexto = "+" + movimiento.getMonto() + " (Ingreso)";
                montoColor = 0xFF00FF00;
                break;
            case "Gasto":
                montoTexto = "-" + movimiento.getMonto() + " (Gasto)";
                montoColor = 0xFFFF0000;
                break;
            case "Transferencia":
                montoTexto = "±" + movimiento.getMonto() + " (Transferencia)";
                montoColor = 0xFF0000FF;
                break;
        }

        holder.monto.setText(montoTexto);
        holder.monto.setTextColor(montoColor);
    }

    public void updateMovimientos(List<Movimiento> movimientosNuevos) {
        movimientos = new ArrayList<>(movimientosNuevos);
        Log.d("MovimientoAdapter", "Nuevos movimientos: " + movimientos.size());

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return movimientos.size();
    }

}
