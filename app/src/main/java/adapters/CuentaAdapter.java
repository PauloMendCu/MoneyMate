package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymate.R;

import java.util.List;

import entities.Cuenta;

public class CuentaAdapter extends RecyclerView.Adapter<CuentaAdapter.ViewHolder> {

    private List<Cuenta> cuentas;

    public CuentaAdapter(List<Cuenta> cuentas) {
        this.cuentas = cuentas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cuenta, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cuenta cuenta = cuentas.get(position);
        holder.tvNombreCuenta.setText(cuenta.getNombre());
        holder.tvSaldo.setText(String.valueOf(cuenta.getSaldo()));
    }

    @Override
    public int getItemCount() {
        return cuentas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreCuenta, tvSaldo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreCuenta = itemView.findViewById(R.id.tvNombreCuenta);
            tvSaldo = itemView.findViewById(R.id.tvSaldo);
        }
    }
}