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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import adapters.CuentaAdapter;
import dao.CuentaDao;
import dao.MovimientoDao;
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
    private ExecutorService executorService;
    private AppDatabase db;
    private IFinanceService apiService;
    String userId;

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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = (currentUser != null) ? currentUser.getUid() : null;

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
            startActivityForResult(intent, 1); // Usa startActivityForResult
        });

        // Botón para ver cuentas
        ImageButton btnVerCuentas = findViewById(R.id.btn_ver_cuentas);
        btnVerCuentas.setOnClickListener(view -> {
            Intent intent = new Intent(CuentasActivity.this, CuentasActivity.class);
            startActivity(intent);
        });

        // Botón para ver categorias
        ImageButton bntVerCategorias = findViewById(R.id.btn_ver_categorias);
        bntVerCategorias.setOnClickListener(view -> {
            Intent intent = new Intent(CuentasActivity.this, CategoriaActivity.class);
            startActivity(intent);
        });


        // Botón para ver inicio
        ImageButton btnInicio = findViewById(R.id.btn_menu);
        btnInicio.setOnClickListener(view -> {
            Intent intent = new Intent(CuentasActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Botón para registrar nuevo movimiento
        ImageButton btnNuevoMovimiento = findViewById(R.id.btn_nuevo_movimiento);
        btnNuevoMovimiento.setOnClickListener(v -> {
            Intent intent = new Intent(CuentasActivity.this, NuevoMovimientoActivity.class);
            startActivity(intent);
        });

        executorService = Executors.newSingleThreadExecutor();
        db = AppDatabase.getInstance(this);
        apiService = RetrofitClient.getInstance().create(IFinanceService.class);

        cargarDatosLocales();
        fetchResumen();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Si una nueva cuenta fue agregada, recargar datos locales
            cargarDatosLocales();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sincronizarCuentas();
        fetchResumen();
    }

    private void fetchResumen() {
        executorService.execute(() -> {
            MovimientoDao movimientoDao = db.movimientoDao();
            List<Movimiento> movimientos = movimientoDao.getAllByUser(userId);
            double ingresosTotales = 0;
            double gastosTotales = 0;

            for (Movimiento movimiento : movimientos) {
                if (movimiento.getTipo().equals("Ingreso") || movimiento.getTipo().equals("Transferencia")) {
                    ingresosTotales += movimiento.getMonto();
                } else if (movimiento.getTipo().equals("Gasto")) {
                    gastosTotales += movimiento.getMonto();
                }
            }

            double finalIngresosTotales = ingresosTotales;
            double finalGastosTotales = gastosTotales;

            runOnUiThread(() -> {
                tvIngresosTotalesMonto.setText(String.format("S/.%.2f", finalIngresosTotales));
                tvGastosTotalesMonto.setText(String.format("S/.%.2f", finalGastosTotales));
            });
        });
    }

    private void sincronizarCuentas() {
        executorService.execute(() -> {
            CuentaDao cuentaDao = db.cuentaDao();
            List<Cuenta> cuentasNoSincronizadas = cuentaDao.getUnsyncedCuentas(userId);

            if (!cuentasNoSincronizadas.isEmpty()) {
                for (Cuenta cuenta : cuentasNoSincronizadas) {
                    apiService.crearCuenta(cuenta).enqueue(new Callback<Cuenta>() {
                        @Override
                        public void onResponse(Call<Cuenta> call, Response<Cuenta> response) {
                            if (response.isSuccessful()) {
                                Cuenta cuentaSincronizada = response.body();
                                if (cuentaSincronizada != null) {
                                    cuentaSincronizada.setIsSynced(true);
                                    executorService.execute(() -> {
                                        cuentaDao.insert(cuentaSincronizada);
                                    });
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Cuenta> call, Throwable t) {
                            // Manejar error
                        }
                    });
                }
            }

            apiService.getCuentas().enqueue(new Callback<List<Cuenta>>() {
                @Override
                public void onResponse(Call<List<Cuenta>> call, Response<List<Cuenta>> response) {
                    if (response.isSuccessful()) {
                        List<Cuenta> cuentasServidor = response.body();
                        if (cuentasServidor != null) {
                            executorService.execute(() -> {
                                for (Cuenta cuenta : cuentasServidor) {
                                    cuenta.setIsSynced(true);
                                    Cuenta cuentaExistente = cuentaDao.getCuentaById(cuenta.getId(), userId); // Ajustar la llamada
                                    if (cuentaExistente == null) {
                                        cuentaDao.insert(cuenta);
                                    } else {
                                        cuentaDao.update(cuenta);
                                    }
                                }
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<Cuenta>> call, Throwable t) {
                    // Manejar error
                }
            });
        });
    }

    private void guardarCuentaEnServidor(Cuenta cuentaLocal) {
        apiService.crearCuenta(cuentaLocal).enqueue(new Callback<Cuenta>() {
            @Override
            public void onResponse(Call<Cuenta> call, Response<Cuenta> response) {
                if (response.isSuccessful()) {
                    Cuenta cuentaCreada = response.body();
                    cuentaCreada.setIsSynced(true); // Marcar la cuenta como sincronizada en la API
                    actualizarCuentaEnServidor(cuentaCreada); // Actualizar la cuenta en la API
                    executorService.execute(() -> {
                        cuentaLocal.setId(cuentaCreada.getId());
                        cuentaLocal.setIsSynced(true); // Marca la cuenta como sincronizada
                        db.cuentaDao().update(cuentaLocal);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(CuentasActivity.this, "Error al sincronizar la cuenta", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(Call<Cuenta> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(CuentasActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void actualizarCuentaEnServidor(Cuenta cuenta) {
        apiService.actualizarCuenta(cuenta.getId(), cuenta).enqueue(new Callback<Cuenta>() {
            @Override
            public void onResponse(Call<Cuenta> call, Response<Cuenta> response) {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(CuentasActivity.this, "Error al actualizar la cuenta en el servidor", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(Call<Cuenta> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(CuentasActivity.this, "No se pudo conectar al servidor para actualizar la cuenta", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void cargarDatosLocales() {
        executorService.execute(() -> {
            List<Cuenta> cuentasLocales = db.cuentaDao().getAllByUser(userId);
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
            if(cuenta.getTipo()==1) {
                saldoTotal += cuenta.getSaldo();
            }
        }
        tvSaldoTotalMonto.setText(String.format("S/.%.2f", saldoTotal));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
