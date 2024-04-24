package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymate.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import entities.Cuenta;
import entities.Movimiento;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MovimientoAdapter extends RecyclerView.Adapter<MovimientoAdapter.MovimientoViewHolder> {

    // Clase interna para el ViewHolder
    static class MovimientoViewHolder extends RecyclerView.ViewHolder {
        TextView descripcion;
        TextView nombreCuenta;
        TextView fecha;
        TextView monto;

        public MovimientoViewHolder(@NonNull View itemView) {
            super(itemView);
            descripcion = itemView.findViewById(R.id.texto_descripcion);
            nombreCuenta = itemView.findViewById(R.id.texto_nombre_cuenta);
            fecha = itemView.findViewById(R.id.texto_fecha);
            monto = itemView.findViewById(R.id.texto_monto);
        }
    }

    private List<Movimiento> movimientos;

    private List<Cuenta> cuentas;


    public MovimientoAdapter(List<Movimiento> movimientos, List<Cuenta> cuentas) {
        this.movimientos = movimientos;
        this.cuentas = cuentas;
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

        // Asignar valores a los elementos del ViewHolder
        holder.descripcion.setText(movimiento.getDescripcion());
        holder.fecha.setText(movimiento.getFecha());

        // Buscar el nombre de la cuenta basado en el campo "cuentaId"
        String nombreCuenta = "Desconocido"; // Valor por defecto si no se encuentra
        for (Cuenta cuenta : cuentas) {
            if (cuenta.getId() == movimiento.getCuentaId()) {
                nombreCuenta = cuenta.getNombre();
                break;
            }
        }

        holder.nombreCuenta.setText(nombreCuenta);

        // Ajustar el monto y su color según el tipo de movimiento
        String montoTexto = "";
        int montoColor = 0;

        switch (movimiento.getTipo()) {
            case "Ingreso":
                montoTexto = "+" + movimiento.getMonto();
                montoColor = 0xFF00FF00; // Verde
                break;
            case "Gasto":
                montoTexto = "-" + movimiento.getMonto();
                montoColor = 0xFFFF0000; // Rojo
                break;
            case "Transferencia":
                montoTexto = "±" + movimiento.getMonto();
                montoColor = 0xFF0000FF; // Azul
                break;
        }

        holder.monto.setText(montoTexto);
        holder.monto.setTextColor(montoColor);
    }

    @Override
    public int getItemCount() {
        return movimientos.size();
    }
}
