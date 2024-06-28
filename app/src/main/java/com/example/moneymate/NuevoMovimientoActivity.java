package com.example.moneymate;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import entities.AppDatabase;
import entities.Categoria;
import entities.Cuenta;
import entities.Movimiento;
import entities.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import services.ICategoriaService;
import services.IFinanceService;

public class NuevoMovimientoActivity extends AppCompatActivity {
    private EditText etDescripcion, etMonto, etFecha;
    private Spinner spinnerCuenta, spinnerCuentaDestino, spinnerCategoria;
    private RadioButton rbIngreso, rbGasto, rbTransferencia;
    private Button btnGuardar, btnSeleccionarFecha;
    private AppDatabase db;
    private static final int NO_CUENTA_SELECCIONADA = -1;
    private Calendar selectedDate = Calendar.getInstance();
    private IFinanceService financeService;
    private ICategoriaService categoriaService;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_movimiento);

        etDescripcion = findViewById(R.id.et_descripcion);
        etMonto = findViewById(R.id.et_monto);
        etFecha = findViewById(R.id.et_fecha);
        spinnerCuenta = findViewById(R.id.spinner_cuenta);
        spinnerCuentaDestino = findViewById(R.id.spinner_cuenta_destino);
        spinnerCategoria = findViewById(R.id.spinner_categorias);
        rbIngreso = findViewById(R.id.rb_ingreso);
        rbGasto = findViewById(R.id.rb_gasto);
        rbTransferencia = findViewById(R.id.rb_transferencia);
        btnGuardar = findViewById(R.id.btn_guardar);
        btnSeleccionarFecha = findViewById(R.id.btn_seleccionar_fecha);

        db = AppDatabase.getInstance(this);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = (currentUser != null) ? currentUser.getUid() : null;

        financeService = RetrofitClient.getFinanceService();
        categoriaService = RetrofitClient.getCategoriaService();

        // Sincronizar categorías solo si hay conexión
        if (isNetworkAvailable()) {
            sincronizarCategorias();
        } else {
            cargarCategoriasDesdeLocal();
        }

        rbTransferencia.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                spinnerCuentaDestino.setVisibility(View.VISIBLE);
            } else {
                spinnerCuentaDestino.setVisibility(View.GONE);
            }
        });

        ImageButton btnVerMovimientos = findViewById(R.id.btn_ver_movimientos);
        btnVerMovimientos.setOnClickListener(view -> {
            Intent intent = new Intent(NuevoMovimientoActivity.this, MovimientosActivity.class);
            startActivity(intent);
        });

        // Botón para ver cuentas
        ImageButton btnVerCuentas = findViewById(R.id.btn_ver_cuentas);
        btnVerCuentas.setOnClickListener(view -> {
            Intent intent = new Intent(NuevoMovimientoActivity.this, CuentasActivity.class);
            startActivity(intent);
        });

        // Botón para ver inicio
        ImageButton btnInicio = findViewById(R.id.btn_menu);
        btnInicio.setOnClickListener(view -> {
            Intent intent = new Intent(NuevoMovimientoActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Botón para ver categorias
        ImageButton bntVerCategorias = findViewById(R.id.btn_ver_categorias);
        bntVerCategorias.setOnClickListener(view -> {
            Intent intent = new Intent(NuevoMovimientoActivity.this, CategoriaActivity.class);
            startActivity(intent);
        });

        // Botón para registrar nuevo movimiento
        ImageButton btnNuevoMovimiento = findViewById(R.id.btn_nuevo_movimiento);
        btnNuevoMovimiento.setOnClickListener(v -> {
            Intent intent = new Intent(NuevoMovimientoActivity.this, NuevoMovimientoActivity.class);
            startActivity(intent);
        });

        etMonto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().matches("\\d*\\.?\\d*")) {
                    etMonto.setError("Solo se permiten números.");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        etFecha.setText(sdf.format(selectedDate.getTime()));

        btnSeleccionarFecha.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    NuevoMovimientoActivity.this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, monthOfYear);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                NuevoMovimientoActivity.this,
                                (view1, hourOfDay, minute) -> {
                                    selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    selectedDate.set(Calendar.MINUTE, minute);
                                    etFecha.setText(sdf.format(selectedDate.getTime()));
                                },
                                selectedDate.get(Calendar.HOUR_OF_DAY),
                                selectedDate.get(Calendar.MINUTE),
                                true
                        );
                        timePickerDialog.show();
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        btnGuardar.setOnClickListener(v -> {
            if (validarCampos()) {
                Movimiento nuevoMovimiento = new Movimiento();
                nuevoMovimiento.setDescripcion(etDescripcion.getText().toString());
                nuevoMovimiento.setMonto(Double.parseDouble(etMonto.getText().toString()));

                // Convertir la fecha seleccionada a un formato legible
                SimpleDateFormat sdfTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                nuevoMovimiento.setFecha(sdfTimestamp.format(selectedDate.getTime()));

                nuevoMovimiento.setCuentaId(((Cuenta) spinnerCuenta.getSelectedItem()).getId());
                nuevoMovimiento.setCategoriaId(((Categoria) spinnerCategoria.getSelectedItem()).getId());
                nuevoMovimiento.setUserId(userId);  // Asegurarse de establecer el userId
                if (rbIngreso.isChecked()) {
                    nuevoMovimiento.setTipo("Ingreso");
                } else if (rbGasto.isChecked()) {
                    nuevoMovimiento.setTipo("Gasto");
                } else if (rbTransferencia.isChecked()) {
                    nuevoMovimiento.setTipo("Transferencia");
                    nuevoMovimiento.setCuentaDestId(((Cuenta) spinnerCuentaDestino.getSelectedItem()).getId());
                }
                if (isNetworkAvailable()) {
                    nuevoMovimiento.setIsSynced(true);
                    registrarMovimientoEnApi(nuevoMovimiento);
                } else {
                    nuevoMovimiento.setIsSynced(false);
                    registrarMovimientoLocalmente(nuevoMovimiento);
                }
            }
        });

        new LoadDataAsyncTask().execute();
    }

    private void sincronizarCategorias() {
        categoriaService.getCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Categoria> categorias = response.body();
                    new InsertCategoriasAsyncTask(db, userId).execute(categorias.toArray(new Categoria[0]));
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                // Manejar error
                Log.e("NuevoMovimientoActivity", "Error al sincronizar categorías", t);
                // En caso de error, cargar desde la base de datos local
                cargarCategoriasDesdeLocal();
            }
        });
    }

    private void cargarCategoriasDesdeLocal() {
        new LoadCategoriasDesdeLocalAsyncTask().execute();
    }

    private class InsertCategoriasAsyncTask extends AsyncTask<Categoria, Void, Void> {
        private AppDatabase db;
        private String userId;

        InsertCategoriasAsyncTask(AppDatabase db, String userId) {
            this.db = db;
            this.userId = userId;
        }

        @Override
        protected Void doInBackground(Categoria... categorias) {
            db.categoriaDao().deleteAllCategoriasByUser(userId);
            for (Categoria categoria : categorias) {
                categoria.setUserId(userId);  // Asegúrate de que la categoría tenga el userId correcto
                db.categoriaDao().insert(categoria);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new LoadDataAsyncTask().execute();
        }
    }

    private class LoadCategoriasDesdeLocalAsyncTask extends AsyncTask<Void, Void, List<Categoria>> {
        @Override
        protected List<Categoria> doInBackground(Void... voids) {
            return db.categoriaDao().getAllByUser(userId);
        }

        @Override
        protected void onPostExecute(List<Categoria> categorias) {
            super.onPostExecute(categorias);
            cargarCategoriasEnSpinner(categorias);
        }
    }

    private class LoadDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private List<Cuenta> cuentas;
        private List<Categoria> categorias;

        @Override
        protected Void doInBackground(Void... voids) {
            cuentas = db.cuentaDao().getAllByUser(userId);
            categorias = db.categoriaDao().getAllByUser(userId);

            // Eliminar duplicados si los hay
            Set<Categoria> categoriasSet = new LinkedHashSet<>(categorias);
            categorias = new ArrayList<>(categoriasSet);

            // Agregar elementos predeterminados
            Cuenta defaultCuenta = new Cuenta();
            defaultCuenta.setId(NO_CUENTA_SELECCIONADA);
            defaultCuenta.setNombre("Seleccionar cuenta");
            cuentas.add(0, defaultCuenta);

            Categoria defaultCategoria = new Categoria();
            defaultCategoria.setId(NO_CUENTA_SELECCIONADA);
            defaultCategoria.setNombre("Seleccionar categoría");
            categorias.add(0, defaultCategoria);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Adapter para cuentas
            ArrayAdapter<Cuenta> adapterCuentas = new ArrayAdapter<>(NuevoMovimientoActivity.this, android.R.layout.simple_spinner_item, cuentas);
            adapterCuentas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCuenta.setAdapter(adapterCuentas);
            spinnerCuentaDestino.setAdapter(adapterCuentas);

            // Cargar categorías en el spinner
            cargarCategoriasEnSpinner(categorias);
        }
    }

    private void cargarCategoriasEnSpinner(List<Categoria> categorias) {
        // Adapter para categorias
        ArrayAdapter<Categoria> adapterCategorias = new ArrayAdapter<>(NuevoMovimientoActivity.this, android.R.layout.simple_spinner_item, categorias);
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategorias);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private static class InsertMovimientoAsyncTask extends AsyncTask<Movimiento, Void, Void> {
        private AppDatabase db;

        InsertMovimientoAsyncTask(AppDatabase db) {
            this.db = db;
        }

        @Override
        protected Void doInBackground(Movimiento... movimientos) {
            db.movimientoDao().insert(movimientos[0]);
            return null;
        }
    }

    private void registrarMovimientoEnApi(Movimiento movimiento) {
        new Thread(() -> {
            // Log del movimiento antes de registrar
            Log.d("MovimientoRegistro", "Registrando en API: " + movimiento.toString());

            // Llamada a la API para registrar el movimiento
            financeService.agregarMovimiento(movimiento).enqueue(new Callback<Movimiento>() {
                @Override
                public void onResponse(Call<Movimiento> call, Response<Movimiento> response) {
                    if (response.isSuccessful()) {
                        Movimiento movimientoResponse = response.body();
                        if (movimientoResponse != null) {
                            movimiento.setId(movimientoResponse.getId());
                            actualizarSaldos(movimiento); // Actualizar saldos de las cuentas
                            new InsertMovimientoAsyncTask(db).execute(movimiento);

                            runOnUiThread(() -> {
                                Toast.makeText(NuevoMovimientoActivity.this, "Movimiento registrado", Toast.LENGTH_SHORT).show();
                                Log.d("MovimientoRegistro", "Movimiento registrado en API y localmente: " + movimiento.toString());
                                // Redirigir a la vista deseada
                                Intent intent = new Intent(NuevoMovimientoActivity.this, CuentasActivity.class);
                                startActivity(intent);
                            });
                        }
                        finish();
                    } else {
                        Toast.makeText(NuevoMovimientoActivity.this, "Error al registrar en la API", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Movimiento> call, Throwable t) {
                    movimiento.setIsSynced(false);
                    new InsertMovimientoAsyncTask(db).execute(movimiento);
                    runOnUiThread(() -> Toast.makeText(NuevoMovimientoActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show());
                }
            });
        }).start();
    }


    private void registrarMovimientoLocalmente(Movimiento movimiento) {
        new Thread(() -> {
            // Log del movimiento antes de registrar
            Log.d("MovimientoRegistro", "Registrando localmente: " + movimiento.toString());

            actualizarSaldos(movimiento); // Actualizar saldos de las cuentas
            new InsertMovimientoAsyncTask(db).execute(movimiento);

            runOnUiThread(() -> {
                Toast.makeText(NuevoMovimientoActivity.this, "Movimiento registrado localmente", Toast.LENGTH_SHORT).show();
                Log.d("MovimientoRegistro", "Movimiento registrado localmente: " + movimiento.toString());
                // Redirigir a la vista deseada
                Intent intent = new Intent(NuevoMovimientoActivity.this, CuentasActivity.class);
                startActivity(intent);
                finish();
            });
        }).start();
    }


    private void actualizarSaldos(Movimiento movimiento) {
        new Thread(() -> {
            try {
                Cuenta cuentaOrigen = db.cuentaDao().getCuentaById(movimiento.getCuentaId(), userId);
                if (movimiento.getTipo().equals("Ingreso")) {
                    cuentaOrigen.setSaldo(cuentaOrigen.getSaldo() + movimiento.getMonto());
                } else if (movimiento.getTipo().equals("Gasto")) {
                    cuentaOrigen.setSaldo(cuentaOrigen.getSaldo() - movimiento.getMonto());
                } else if (movimiento.getTipo().equals("Transferencia")) {
                    Cuenta cuentaDestino = db.cuentaDao().getCuentaById(movimiento.getCuentaDestId(), userId);
                    cuentaOrigen.setSaldo(cuentaOrigen.getSaldo() - movimiento.getMonto());
                    cuentaDestino.setSaldo(cuentaDestino.getSaldo() + movimiento.getMonto());
                    db.cuentaDao().update(cuentaDestino);

                    // Llamada a la API para actualizar saldo de cuenta destino
                    financeService.actualizarCuenta(cuentaDestino.getId(), cuentaDestino).enqueue(new Callback<Cuenta>() {
                        @Override
                        public void onResponse(Call<Cuenta> call, Response<Cuenta> response) {
                            if (!response.isSuccessful()) {
                                Log.d("ActualizarSaldo", "Error al sincronizar saldo de cuenta destino en la API: " + cuentaDestino.toString());
                            }
                        }

                        @Override
                        public void onFailure(Call<Cuenta> call, Throwable t) {
                            Log.d("ActualizarSaldo", "Fallo al sincronizar saldo de cuenta destino en la API: " + t.getMessage());
                        }
                    });
                }
                db.cuentaDao().update(cuentaOrigen);

                // Llamada a la API para actualizar saldo de cuenta origen
                financeService.actualizarCuenta(cuentaOrigen.getId(), cuentaOrigen).enqueue(new Callback<Cuenta>() {
                    @Override
                    public void onResponse(Call<Cuenta> call, Response<Cuenta> response) {
                        if (!response.isSuccessful()) {
                            Log.d("ActualizarSaldo", "Error al sincronizar saldo de cuenta origen en la API: " + cuentaOrigen.toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<Cuenta> call, Throwable t) {
                        Log.d("ActualizarSaldo", "Fallo al sincronizar saldo de cuenta origen en la API: " + t.getMessage());
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean validarCampos() {
        String descripcion = etDescripcion.getText().toString();
        String montoStr = etMonto.getText().toString();
        Cuenta cuentaOrigen = (Cuenta) spinnerCuenta.getSelectedItem();
        Categoria categoria = (Categoria) spinnerCategoria.getSelectedItem();

        if (TextUtils.isEmpty(descripcion) || TextUtils.isEmpty(montoStr)) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (cuentaOrigen == null || cuentaOrigen.getId() == NO_CUENTA_SELECCIONADA) {
            Toast.makeText(this, "Debe seleccionar una cuenta", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (categoria == null || categoria.getId() == NO_CUENTA_SELECCIONADA) {
            Toast.makeText(this, "Debe seleccionar una categoría", Toast.LENGTH_SHORT).show();
            return false;
        }

        double monto = Double.parseDouble(montoStr);

        if (rbGasto.isChecked() || rbTransferencia.isChecked()) {
            if (monto > cuentaOrigen.getSaldo()) {
                Toast.makeText(this, "El monto no puede superar el saldo de la cuenta origen", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (rbTransferencia.isChecked()) {
            Cuenta cuentaDestino = (Cuenta) spinnerCuentaDestino.getSelectedItem();
            if (cuentaDestino == null || cuentaDestino.getId() == NO_CUENTA_SELECCIONADA) {
                Toast.makeText(this, "Debe seleccionar una cuenta destino", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }
}
