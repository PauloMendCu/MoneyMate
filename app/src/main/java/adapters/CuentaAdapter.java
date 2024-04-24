package adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymate.MovimientosPorCuentaActivity;
import com.example.moneymate.R;

import java.util.List;

import entities.Cuenta;

public class CuentaAdapter extends RecyclerView.Adapter<CuentaAdapter.ViewHolder> {

    private List<Cuenta> cuentas;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Cuenta cuenta);
    }

    public CuentaAdapter(List<Cuenta> cuentas, OnItemClickListener listener) {
        this.cuentas = cuentas;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cuenta, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Cuenta cuenta = cuentas.get(position);
        holder.nombre.setText(cuenta.getNombre());
        holder.saldo.setText(String.valueOf(cuenta.getSaldo()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), MovimientosPorCuentaActivity.class);
            intent.putExtra("cuenta_id", cuenta.getId());  // Pasar el ID de la cuenta
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cuentas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, saldo;

        public ViewHolder(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.tvNombreCuenta);
            saldo = itemView.findViewById(R.id.tvSaldo);
        }
    }
}