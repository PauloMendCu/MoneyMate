package adapters;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymate.R;

import java.util.List;

import entities.TarjetaCredito;
import android.text.Spanned;

public class TarjetaCreditoAdapter extends RecyclerView.Adapter<TarjetaCreditoAdapter.ViewHolder> {

    private List<TarjetaCredito> tarjetasCredito;

    public TarjetaCreditoAdapter(List<TarjetaCredito> tarjetasCredito) {
        this.tarjetasCredito = tarjetasCredito;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarjeta_credito, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        TarjetaCredito tarjetaCredito = tarjetasCredito.get(position);
        holder.nombre.setText("Tarjeta de Credito: "+tarjetaCredito.getNombre());

        String lineaCreditoText = "Línea de Crédito: ";
        SpannableString spannableString = new SpannableString(lineaCreditoText + "-S/" + String.valueOf(tarjetaCredito.getLineaCredito()));

        // Obtener el índice del inicio del monto
        int montoIndex = lineaCreditoText.length(); // Longitud de "Línea de Crédito: -S/"

        // Crear un ForegroundColorSpan para el color rojo
        int colorRojo = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark);
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(colorRojo);

        // Aplicar el color rojo al monto
        spannableString.setSpan(foregroundColorSpan, montoIndex, montoIndex + String.valueOf(tarjetaCredito.getLineaCredito()).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        holder.lineaCredito.setText(spannableString);



        holder.saldoConsumido.setText("Saldo Consumido: "+String.valueOf(tarjetaCredito.getSaldoConsumido()));
        holder.saldoDisponible.setText("Saldo Disponible: "+String.valueOf(tarjetaCredito.getSaldoDisponible()));
        holder.proxPago.setText("Proximo Pago: "+String.valueOf(tarjetaCredito.getProxPago()));
        holder.pagoMinimo.setText("Pago Minimo: "+String.valueOf(tarjetaCredito.getPagoMinimo()));
        holder.fechaFacturacion.setText("Fecha de Facturacion: "+tarjetaCredito.getFechaFacturacion());
        holder.fechaVencimiento.setText("Fecha de Vencimiento: "+tarjetaCredito.getFechaVencimiento());
    }

    @Override
    public int getItemCount() {
        return tarjetasCredito.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, lineaCredito, saldoConsumido, saldoDisponible, proxPago, pagoMinimo, fechaFacturacion, fechaVencimiento;

        public ViewHolder(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.tv_nombre_tarjeta);
            lineaCredito = itemView.findViewById(R.id.tv_linea_credito);
            saldoConsumido = itemView.findViewById(R.id.tv_saldo_consumido);
            saldoDisponible = itemView.findViewById(R.id.tv_saldo_disponible);
            proxPago = itemView.findViewById(R.id.tv_proximo_pago);
            pagoMinimo = itemView.findViewById(R.id.tv_pago_minimo);
            fechaFacturacion = itemView.findViewById(R.id.tv_fecha_facturacion);
            fechaVencimiento = itemView.findViewById(R.id.tv_fecha_vencimiento);
        }
    }
}
