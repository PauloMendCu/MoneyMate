package com.example.moneymate;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import entities.RetrofitClient;
import entities.TarjetaCredito;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import services.ICategoriaService;

public class RegistrarTarjetaCreditoActivity extends AppCompatActivity {

    private EditText etNombre, etLineaCredito, etSaldoConsumido, etSaldoDisponible, etProxPago, etPagoMinimo, etFechaFacturacion, etFechaVencimiento;
    private Button btnRegistrar, btnFechaFacturacion, btnFechaVencimiento;
    private DatePickerDialog.OnDateSetListener fechaFacturacionListener, fechaVencimientoListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_tarjeta_credito);

        etNombre = findViewById(R.id.et_nombre);
        etLineaCredito = findViewById(R.id.et_linea_credito);
        etSaldoConsumido = findViewById(R.id.et_saldo_consumido);
        etSaldoDisponible = findViewById(R.id.et_saldo_disponible);
        etProxPago = findViewById(R.id.et_prox_pago);
        etPagoMinimo = findViewById(R.id.et_pago_minimo);
        etFechaFacturacion = findViewById(R.id.et_fecha_facturacion);
        etFechaVencimiento = findViewById(R.id.et_fecha_vencimiento);

        btnRegistrar = findViewById(R.id.btn_registrar);
        btnFechaFacturacion = findViewById(R.id.btn_fecha_facturacion);
        btnFechaVencimiento = findViewById(R.id.btn_seleccionar_fecha);

        fechaFacturacionListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String fecha = year + "-" + (month + 1) + "-" + dayOfMonth;
                etFechaFacturacion.setText(fecha);
            }
        };

        fechaVencimientoListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String fecha = year + "-" + (month + 1) + "-" + dayOfMonth;
                etFechaVencimiento.setText(fecha);
            }
        };

        btnFechaFacturacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(fechaFacturacionListener);
            }
        });

        btnFechaVencimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(fechaVencimientoListener);
            }
        });

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarCampos()) {
                    registrarTarjetaCredito();
                } else {
                    Toast.makeText(RegistrarTarjetaCreditoActivity.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Botón para ver movimientos
        ImageButton btnVerMovimientos = findViewById(R.id.btn_ver_movimientos);
        btnVerMovimientos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegistrarTarjetaCreditoActivity.this, MovimientosActivity.class);
                startActivity(intent);
            }
        });

        // Botón para ver cuentas
        ImageButton btnVerCuentas = findViewById(R.id.btn_ver_cuentas);
        btnVerCuentas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegistrarTarjetaCreditoActivity.this, CuentasActivity.class);
                startActivity(intent);
            }
        });

        ImageButton btnVerTarjetas = findViewById(R.id.btnTarjetas);
        btnVerTarjetas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegistrarTarjetaCreditoActivity.this, TarjetasCreditoActivity.class);
                startActivity(intent);
            }
        });

        // Botón para registrar nuevo movimiento
        ImageButton btnNuevoMovimiento = findViewById(R.id.btn_nuevo_movimiento);
        btnNuevoMovimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrarTarjetaCreditoActivity.this, NuevoMovimientoActivity.class);
                startActivity(intent);
            }
        });

    }

    private void showDatePickerDialog(DatePickerDialog.OnDateSetListener listener) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, listener, year, month, dayOfMonth);
        datePickerDialog.show();
    }

    private boolean validarCampos() {
        if (TextUtils.isEmpty(etNombre.getText().toString().trim())
                || TextUtils.isEmpty(etLineaCredito.getText().toString().trim())
                || TextUtils.isEmpty(etSaldoConsumido.getText().toString().trim())
                || TextUtils.isEmpty(etSaldoDisponible.getText().toString().trim())
                || TextUtils.isEmpty(etProxPago.getText().toString().trim())
                || TextUtils.isEmpty(etPagoMinimo.getText().toString().trim())
                || TextUtils.isEmpty(etFechaFacturacion.getText().toString().trim())
                || TextUtils.isEmpty(etFechaVencimiento.getText().toString().trim())) {
            return false;
        }

        try {
            Double.parseDouble(etLineaCredito.getText().toString().trim());
            Double.parseDouble(etSaldoConsumido.getText().toString().trim());
            Double.parseDouble(etSaldoDisponible.getText().toString().trim());
            Double.parseDouble(etProxPago.getText().toString().trim());
            Double.parseDouble(etPagoMinimo.getText().toString().trim());
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private void registrarTarjetaCredito() {
        String nombre = etNombre.getText().toString().trim();
        double lineaCredito = Double.parseDouble(etLineaCredito.getText().toString().trim());
        double saldoConsumido = Double.parseDouble(etSaldoConsumido.getText().toString().trim());
        double saldoDisponible = Double.parseDouble(etSaldoDisponible.getText().toString().trim());
        double proxPago = Double.parseDouble(etProxPago.getText().toString().trim());
        double pagoMinimo = Double.parseDouble(etPagoMinimo.getText().toString().trim());
        String fechaFacturacion = etFechaFacturacion.getText().toString().trim();
        String fechaVencimiento = etFechaVencimiento.getText().toString().trim();

        TarjetaCredito nuevaTarjetaCredito = new TarjetaCredito(
                0, lineaCredito, saldoConsumido, saldoDisponible, proxPago, pagoMinimo, nombre, fechaFacturacion, fechaVencimiento
        );

        ICategoriaService api = RetrofitClient.getInstanceCategorias().create(ICategoriaService.class);
        api.registrarTarjetaCredito(nuevaTarjetaCredito).enqueue(new Callback<TarjetaCredito>() {
            @Override
            public void onResponse(Call<TarjetaCredito> call, Response<TarjetaCredito> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegistrarTarjetaCreditoActivity.this, "Tarjeta de crédito registrada exitosamente", Toast.LENGTH_SHORT).show();
                    // Puedes redirigir al usuario a la actividad TarjetasCreditoActivity si lo deseas
                } else {
                    Toast.makeText(RegistrarTarjetaCreditoActivity.this, "Error al registrar la tarjeta de crédito", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TarjetaCredito> call, Throwable t) {
                Toast.makeText(RegistrarTarjetaCreditoActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
