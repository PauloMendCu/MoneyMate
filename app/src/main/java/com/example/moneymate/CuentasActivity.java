package com.example.moneymate;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import adapters.CuentaAdapter;
import entities.AppDatabase;
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
        recyclerView.setAdapter(adapter);

        // Botón para ver movimientos
        ImageButton btnVerMovimientos = findViewById(R.id.btn_ver_movimientos);
        btnVerMovimientos.setOnClickListener(view -> {
            Intent intent = new Intent(CuentasActivity.this, MovimientosActivity.class);
            startActivity(intent);
        });

        // Botón para crear nueva cuenta
        Button btnAgregarCuenta = findViewById(R.id.btnAgregarCuenta);
        btnAgregarCuenta.setOnClickListener(v -> {
            Intent intent = new Intent(CuentasActivity.this, NuevaCuentaActivity.class);
            startActivity(intent);
        });

        // Botón para ver cuentas
        ImageButton btnVerCuentas = findViewById(R.id.btn_ver_cuentas);
        btnVerCuentas.setOnClickListener(view -> {
            Intent intent = new Intent(CuentasActivity.this, CuentasActivity.class);
            startActivity(intent);
        });

        // Botón para registrar nuevo movimiento
        ImageButton btnNuevoMovimiento = findViewById(R.id.btn_nuevo_movimiento);
        btnNuevoMovimiento.setOnClickListener(v -> {
            Intent intent = new Intent(CuentasActivity.this, NuevoMovimientoActivity.class);
            startActivity(intent);
        });

        cargarDatosLocales();
        fetchResumen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sincronizarCuentas();
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
                    tvGastosTotalesMonto.setText(String.format("S/.%.2f", gastosTotales));
                }
            }

            @Override
            public void onFailure(Call<List<Movimiento>> call, Throwable t) {
                Toast.makeText(CuentasActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sincronizarCuentas() {
        if (isNetworkAvailable()) {
            IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
            api.getCuentas().enqueue(new Callback<List<Cuenta>>() {
                @Override
                public void onResponse(Call<List<Cuenta>> call, Response<List<Cuenta>> response) {
                    if (response.isSuccessful()) {
                        List<Cuenta> cuentasAPI = response.body();

                        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
                            List<Cuenta> cuentasLocales = AppDatabase.getInstance(CuentasActivity.this).cuentaDao().getAllCuentas();
                            List<Cuenta> cuentasLocalesNoSincronizadas = AppDatabase.getInstance(CuentasActivity.this).cuentaDao().getCuentasNoSincronizadas();

                            // Sincronizar cuentas locales no sincronizadas con la API
                            for (Cuenta cuentaLocal : cuentasLocalesNoSincronizadas) {
                                guardarCuentaEnServidor(cuentaLocal);
                            }

                            // Identificar nuevas cuentas en la API que no están en local
                            List<Cuenta> nuevasCuentasAPI = new ArrayList<>();
                            for (Cuenta cuentaAPI : cuentasAPI) {
                                boolean existeEnLocal = false;
                                for (Cuenta cuentaLocal : cuentasLocales) {
                                    if (cuentaLocal.getId() == cuentaAPI.getId()) {
                                        existeEnLocal = true;
                                        break;
                                    }
                                }
                                if (!existeEnLocal) {
                                    nuevasCuentasAPI.add(cuentaAPI);
                                }
                            }

                            // Sincronizar nuevas cuentas de la API con la base de datos local
                            for (Cuenta cuentaAPI : nuevasCuentasAPI) {
                                cuentaAPI.setIsSynced(true); // Marcar como sincronizada
                                AppDatabase.getInstance(CuentasActivity.this).cuentaDao().insert(cuentaAPI);
                            }

                            // Marcar cuentas locales como sincronizadas
                            for (Cuenta cuentaLocal : cuentasLocalesNoSincronizadas) {
                                cuentaLocal.setIsSynced(true);
                                AppDatabase.getInstance(CuentasActivity.this).cuentaDao().update(cuentaLocal);
                            }

                            // Actualizar la lista de cuentas locales
                            List<Cuenta> cuentasActualizadas = AppDatabase.getInstance(CuentasActivity.this).cuentaDao().getAllCuentas();

                            // Actualizar la interfaz de usuario en el hilo principal
                            runOnUiThread(() -> {
                                cuentas.clear();
                                cuentas.addAll(cuentasActualizadas);
                                adapter.notifyDataSetChanged();
                                actualizarSaldoTotal();
                            });
                        });
                    }
                }

                @Override
                public void onFailure(Call<List<Cuenta>> call, Throwable t) {
                    Toast.makeText(CuentasActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            cargarDatosLocales();
        }
    }


    private void guardarCuentaEnServidor(Cuenta cuentaLocal) {
        IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
        api.crearCuenta(cuentaLocal).enqueue(new Callback<Cuenta>() {
            @Override
            public void onResponse(Call<Cuenta> call, Response<Cuenta> response) {
                if (response.isSuccessful()) {
                    Cuenta cuentaCreada = response.body();
                    cuentaCreada.setIsSynced(true); // Marcar la cuenta como sincronizada en la API
                    actualizarCuentaEnServidor(cuentaCreada); // Actualizar la cuenta en la API
                    AppDatabase.getDatabaseWriteExecutor().execute(() -> {
                        cuentaLocal.setId(cuentaCreada.getId());
                        cuentaLocal.setIsSynced(true); // Marca la cuenta como sincronizada
                        AppDatabase.getInstance(CuentasActivity.this).cuentaDao().update(cuentaLocal);
                    });
                } else {
                    Toast.makeText(CuentasActivity.this, "Error al sincronizar la cuenta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Cuenta> call, Throwable t) {
                Toast.makeText(CuentasActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarCuentaEnServidor(Cuenta cuenta) {
        IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
        api.actualizarCuenta(cuenta.getId(), cuenta).enqueue(new Callback<Cuenta>() {
            @Override
            public void onResponse(Call<Cuenta> call, Response<Cuenta> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(CuentasActivity.this, "Error al actualizar la cuenta en el servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Cuenta> call, Throwable t) {
                Toast.makeText(CuentasActivity.this, "No se pudo conectar al servidor para actualizar la cuenta", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarDatosLocales() {
        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            List<Cuenta> cuentasLocales = AppDatabase.getInstance(CuentasActivity.this).cuentaDao().getAllCuentas();
            runOnUiThread(() -> {
                cuentas.clear(); // Limpiar la lista antes de agregar las cuentas locales
                cuentas.addAll(cuentasLocales);
                adapter.notifyDataSetChanged();
                actualizarSaldoTotal();
            });
        });
    }

    private void actualizarSaldoTotal() {
        double saldoTotal = 0;
        for (Cuenta cuenta : cuentas) {
            saldoTotal += cuenta.getSaldo();
        }
        tvSaldoTotalMonto.setText(String.format("S/.%.2f", saldoTotal));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}


