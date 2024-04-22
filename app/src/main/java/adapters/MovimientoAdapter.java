package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymate.R;

import java.util.List;

import entities.Movimiento;

public class MovimientoAdapter extends RecyclerView.Adapter<MovimientoAdapter.ViewHolder> {

    private List<Movimiento> movimientos;

    public MovimientoAdapter(List<Movimiento> movimientos) {
        this.movimientos = movimientos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movimiento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movimiento movimiento = movimientos.get(position);
        holder.tvDescripcion.setText(movimiento.getDescripcion());
        holder.tvMonto.setText(String.valueOf(movimiento.getMonto()));
        holder.tvFecha.setText(movimiento.getFecha().toString());
        holder.tvTipo.setText(movimiento.getTipo().toString());
    }

    @Override
    public int getItemCount() {
        return movimientos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescripcion, tvMonto, tvFecha, tvTipo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvMonto = itemView.findViewById(R.id.tvMonto);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvTipo = itemView.findViewById(R.id.tvTipo);
        }
    }
}
