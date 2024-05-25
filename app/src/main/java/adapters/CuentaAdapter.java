package adapters;

import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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

        // Validar y mostrar el tipo de cuenta
        if (cuenta.getTipo() == 1) {
            holder.tipo.setText("Cuenta Dédito");
            holder.tipo.setTextColor(0xFFFFA500);
            holder.saldo.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
            holder.saldo.setText("Saldo: " + String.format("S/.%.2f", cuenta.getSaldo()));
            SpannableString spannableString = new SpannableString(holder.saldo.getText());
            int start = holder.saldo.getText().toString().indexOf("S/.");
            int end = holder.saldo.getText().length();
            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(holder.itemView.getContext(), R.color.verde)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.saldo.setText(spannableString);
        } else if (cuenta.getTipo() == 2) {
            holder.tipo.setText("Cuenta Credito");
            holder.tipo.setTextColor(0xFF0000FF);
            holder.saldo.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
            holder.saldo.setText("Límite: " + String.format("S/.%.2f", cuenta.getSaldo()));
            SpannableString spannableString = new SpannableString(holder.saldo.getText());
            int start = holder.saldo.getText().toString().indexOf("S/.") ;
            int end = holder.saldo.getText().length();
            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(holder.itemView.getContext(), R.color.verde)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.saldo.setText(spannableString);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), MovimientosPorCuentaActivity.class);
            intent.putExtra("cuenta_id", cuenta.getId());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cuentas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, saldo, tipo;

        public ViewHolder(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.tvNombreCuenta);
            saldo = itemView.findViewById(R.id.tvSaldo);
            tipo = itemView.findViewById(R.id.tvTipo);
        }
    }
}