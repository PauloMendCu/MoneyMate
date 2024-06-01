package com.example.moneymate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import adapters.CuentaAdapter;
import entities.Cuenta;
import entities.Movimiento;
import entities.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import services.IFinanceService;

public class CuentasActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CuentaAdapter adapter;
    private List<Cuenta> cuentas;
    private TextView tvSaldoTotalMonto;
    private TextView tvSaldoTotalTexto;
    private TextView tvIngresosTotalesMonto;
    private TextView tvIngresosTotalesTexto;
    private TextView tvGastosTotalesMonto;
    private TextView tvGastosTotalesTexto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuentas);

        tvSaldoTotalMonto = findViewById(R.id.tv_saldo_total_monto);
        tvSaldoTotalTexto = findViewById(R.id.tv_saldo_total_texto);
        tvIngresosTotalesMonto = findViewById(R.id.tv_ingresos_totales_monto);
        tvIngresosTotalesTexto = findViewById(R.id.tv_ingresos_totales_texto);
        tvGastosTotalesMonto = findViewById(R.id.tv_gastos_totales_monto);
        tvGastosTotalesTexto = findViewById(R.id.tv_gastos_totales_texto);

        recyclerView = findViewById(R.id.recycler_view_cuentas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cuentas = new ArrayList<>();
        adapter = new CuentaAdapter(cuentas, cuenta -> {
            Intent intent = new Intent(this, MovimientosPorCuentaActivity.class);
            intent.putExtra("cuenta_id", cuenta.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);;

        // Botón para ver movimientos
        ImageButton btnVerMovimientos = findViewById(R.id.btn_ver_movimientos);
        btnVerMovimientos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CuentasActivity.this, MovimientosActivity.class);
                startActivity(intent);
            }
        });
        // Bton para crear nueva cuenta
        Button btnAgregarCuenta = findViewById(R.id.btnAgregarCuenta);
        btnAgregarCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CuentasActivity.this, NuevaCuentaActivity.class);
                startActivity(intent);
            }
        });
        // Botón para ver cuentas
        ImageButton btnVerCuentas = findViewById(R.id.btn_ver_cuentas);
        btnVerCuentas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CuentasActivity.this, CuentasActivity.class);
                startActivity(intent);
            }
        });



        // Botón para registrar nuevo movimiento
        ImageButton btnNuevoMovimiento = findViewById(R.id.btn_nuevo_movimiento);
        btnNuevoMovimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CuentasActivity.this, NuevoMovimientoActivity.class);
                startActivity(intent);
            }
        });

        fetchCuentas();
        fetchResumen();
    }
    // CuentasActivity.java
    @Override
    protected void onResume() {
        super.onResume();
        fetchCuentas();
        fetchResumen();

    }


    private void fetchResumen() {
        IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
        api.getMovimientos().enqueue(new Callback<List<Movimiento>>() {
            @Override
            public void onResponse(Call<List<Movimiento>> call, Response<List<Movimiento>> response) {
                if (response.isSuccessful()) {
                    List<Movimiento> movimientos = response.body();
                    double ingresosTotales = 0;
                    double gastosTotales = 0;

                    for (Movimiento movimiento : movimientos) {
                        if (movimiento.getTipo().equals("Ingreso") || movimiento.getTipo().equals("Transferencia")) {
                            ingresosTotales += movimiento.getMonto();
                        } else if (movimiento.getTipo().equals("Gasto")) {
                            gastosTotales += movimiento.getMonto();
                        }
                    }

                    tvIngresosTotalesMonto.setText(String.format("S/.%.2f", ingresosTotales));
                    tvGastosTotalesMonto.setText(String.format("S/.%.2f",gastosTotales));
                }
            }

            @Override
            public void onFailure(Call<List<Movimiento>> call, Throwable t) {
                Toast.makeText(CuentasActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchCuentas() {
        IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
        api.getCuentas().enqueue(new Callback<List<Cuenta>>() {
            @Override
            public void onResponse(Call<List<Cuenta>> call, Response<List<Cuenta>> response) {
                if (response.isSuccessful()) {
                    cuentas.clear();
                    cuentas.addAll(response.body());
                    adapter.notifyDataSetChanged(); // Actualizar el adaptador para mostrar las cuentas

                    // Calcular el saldo total de las cuentas
                    double saldoTotal = 0;
                    for (Cuenta cuenta : cuentas) {
                        saldoTotal += cuenta.getSaldo();
                    }

                    // Actualizar el TextView con el saldo total

                    tvSaldoTotalMonto.setText(String.format("S/.%.2f",saldoTotal));


                }
            }

            @Override
            public void onFailure(Call<List<Cuenta>> call, Throwable t) {
                Toast.makeText(CuentasActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
}