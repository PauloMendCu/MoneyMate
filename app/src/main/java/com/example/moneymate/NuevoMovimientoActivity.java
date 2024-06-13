package com.example.moneymate;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import adapters.CategoriaAdapter;
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

    private LinearLayout layoutCategorias;
    private Spinner spinnerCuenta, spinnerCategoria;
    private EditText etDescripcion, etMonto;
    private RadioGroup rgTipo;
    private RadioButton rbIngreso, rbGasto, rbTransferencia;
    private Button btnGuardar;
    private Button btnCategoriaSeleccionada;
    private EditText etFecha;
    private Button btnSeleccionarFecha;
    private String fechaActual;
    private Spinner spinnerCuentaDestino;
    private AppDatabase db;
    private List<Categoria> categorias;
    private List<Cuenta> cuentas;
    private String fecha;
    private int categoriaSeleccionadaId = -1;

    private ArrayAdapter<Cuenta> cuentaAdapter;
    private ArrayAdapter<Cuenta> cuentaDestinoAdapter;
    private CategoriaAdapter categoriaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_movimiento);

        db = AppDatabase.getInstance(this);

        layoutCategorias = findViewById(R.id.layout_categorias);
        spinnerCuenta = findViewById(R.id.spinner_cuenta);
        etDescripcion = findViewById(R.id.et_descripcion);
        etMonto = findViewById(R.id.et_monto);
        rgTipo = findViewById(R.id.rg_tipo);
        rbIngreso = findViewById(R.id.rb_ingreso);
        rbGasto = findViewById(R.id.rb_gasto);
        rbTransferencia = findViewById(R.id.rb_transferencia);
        btnGuardar = findViewById(R.id.btn_guardar);
        etFecha = findViewById(R.id.et_fecha);
        btnSeleccionarFecha = findViewById(R.id.btn_seleccionar_fecha);
        spinnerCuentaDestino = findViewById(R.id.spinner_cuenta_destino);

        btnSeleccionarFecha.setOnClickListener(v -> mostrarSelectorFecha());

        cuentas = db.cuentaDao().getAll();
        categorias = db.categoriaDao().getAllCategorias();

        cuentaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cuentas);
        cuentaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCuenta.setAdapter(cuentaAdapter);

        cuentaDestinoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cuentas);
        cuentaDestinoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCuentaDestino.setAdapter(cuentaDestinoAdapter);

        categoriaAdapter = new CategoriaAdapter(this, categorias);
        spinnerCategoria.setAdapter(categoriaAdapter);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        fecha = sdf.format(new Date());

        // Botón para ver movimientos
        ImageButton btnVerMovimientos = findViewById(R.id.btn_ver_movimientos);
        btnVerMovimientos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NuevoMovimientoActivity.this, MovimientosActivity.class);
                startActivity(intent);
            }
        });

        // Botón para ver cuentas
        ImageButton btnVerCuentas = findViewById(R.id.btn_ver_cuentas);
        btnVerCuentas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NuevoMovimientoActivity.this, CuentasActivity.class);
                startActivity(intent);
            }
        });



        // Botón para registrar nuevo movimiento
        ImageButton btnNuevoMovimiento = findViewById(R.id.btn_nuevo_movimiento);
        btnNuevoMovimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NuevoMovimientoActivity.this, NuevoMovimientoActivity.class);
                startActivity(intent);
            }
        });

        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        fechaActual = sdf2.format(new Date());
        etFecha.setText(fechaActual);

        fetchCategorias();
        fetchCuentas();

        rgTipo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                List<String> cuentasNombres = new ArrayList<>();

                if (checkedId == R.id.rb_transferencia) {


                    cuentasNombres.add("Seleccionar cuenta origen"); // Agregar opción vacía

                    spinnerCuenta.setPrompt("Seleccionar cuenta origen");
                    spinnerCuentaDestino.setVisibility(View.VISIBLE);
                    configurarSpinnerCuentaDestino();
                } else {
                    cuentasNombres.add("Seleccionar cuenta"); // Agregar opción vacía
                    spinnerCuenta.setPrompt("Seleccionar cuenta");
                    spinnerCuentaDestino.setVisibility(View.GONE);
                }

                for (Cuenta cuenta : cuentas) {
                    cuentasNombres.add(cuenta.getNombre());
                }
                ArrayAdapter<String> adapterCuenta = new ArrayAdapter<>(NuevoMovimientoActivity.this, android.R.layout.simple_spinner_item, cuentasNombres);
                adapterCuenta.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCuenta.setAdapter(adapterCuenta);
            }
        });

        btnGuardar.setOnClickListener(v -> guardarMovimiento());
    }

    private void mostrarSelectorFecha() {
        if (etFecha.getText().toString().equals(fechaActual)) {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay);

                TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view1, selectedHour, selectedMinute) -> {
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                    selectedCalendar.set(Calendar.MINUTE, selectedMinute);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String fechaSeleccionada = sdf.format(selectedCalendar.getTime());
                    etFecha.setText(fechaSeleccionada);
                }, hour, minute, true);

                timePickerDialog.show();
            }, year, month, day);

            datePickerDialog.show();
        }
    }

    private void fetchCategorias() {
        if (isNetworkAvailable()){
            ICategoriaService categoriaApi = RetrofitClient.getInstanceCategorias().create(ICategoriaService.class);
            categoriaApi.getCategorias().enqueue(new Callback<List<Categoria>>() {
                @Override
                public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                    if (response.isSuccessful()) {
                        categorias = response.body();
                        configurarBotonesCategorias();
                    }
                }

                @Override
                public void onFailure(Call<List<Categoria>> call, Throwable t) {
                    Toast.makeText(NuevoMovimientoActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            cargarCategoriasLocales();
            configurarBotonesCategorias();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void configurarBotonesCategorias() {
        layoutCategorias.removeAllViews();

        for (Categoria categoria : categorias) {
            Button btnCategoria = new Button(this);
            btnCategoria.setText(categoria.getNombre());
            btnCategoria.setBackgroundResource(android.R.drawable.btn_default); // Fondo por defecto

            btnCategoria.setOnClickListener(v -> {
                categoriaSeleccionadaId = categoria.getId();

                // Restablecer el fondo del botón previamente seleccionado
                if (btnCategoriaSeleccionada != null) {
                    btnCategoriaSeleccionada.setBackgroundResource(android.R.drawable.btn_default);
                }

                // Cambiar el fondo del botón seleccionado
                btnCategoriaSeleccionada = (Button) v;
                btnCategoriaSeleccionada.setBackgroundResource(R.drawable.selector_button_bg);
            });

            layoutCategorias.addView(btnCategoria);
        }
    }

    private void cargarCuentasLocales() {
        cuentas.clear();
        cuentas.addAll(db.cuentaDao().getAll());
    }
    private void cargarCategoriasLocales() {
        categorias.clear();
        categorias.addAll(db.categoriaDao().getAllCategorias());
    }

    private void fetchCuentas() {
        if(isNetworkAvailable()){
            IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
            api.getCuentas().enqueue(new Callback<List<Cuenta>>() {
                @Override
                public void onResponse(Call<List<Cuenta>> call, Response<List<Cuenta>> response) {
                    if (response.isSuccessful()) {
                        cuentas = response.body();
                        configurarSpinnerCuenta();
                    }
                }

                @Override
                public void onFailure(Call<List<Cuenta>> call, Throwable t) {
                    Toast.makeText(NuevoMovimientoActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            cargarCuentasLocales();
            configurarSpinnerCuenta();
        }
    }

    private void configurarSpinnerCuenta() {
        List<String> cuentasNombres = new ArrayList<>();
        cuentasNombres.add("Seleccionar cuenta"); // Agregar opción vacía
        for (Cuenta cuenta : cuentas) {
            cuentasNombres.add(cuenta.getNombre());
        }
        ArrayAdapter<String> adapterCuenta = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cuentasNombres);
        adapterCuenta.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCuenta.setAdapter(adapterCuenta);
    }

    private void configurarSpinnerCuentaDestino() {
        List<String> cuentasNombres = new ArrayList<>();
        cuentasNombres.add("Seleccionar cuenta destino"); // Agregar opción vacía
        for (Cuenta cuenta : cuentas) {
            cuentasNombres.add(cuenta.getNombre());
        }
        ArrayAdapter<String> adapterCuentaDestino = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cuentasNombres);
        adapterCuentaDestino.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCuentaDestino.setAdapter(adapterCuentaDestino);
    }

    private void guardarMovimiento() {
        String descripcion = etDescripcion.getText().toString().trim();
        String montoString = etMonto.getText().toString().trim();
        int cuentaOrigenSeleccionada = spinnerCuenta.getSelectedItemPosition();
        int cuentaDestinoSeleccionada = spinnerCuentaDestino.getSelectedItemPosition();
        String tipo = obtenerTipoMovimiento();

        String fechaMovimiento;
        if (etFecha.getText().toString().equals(fechaActual)) {
            fechaMovimiento = fechaActual;
        } else {
            fechaMovimiento = etFecha.getText().toString().trim();
        }

        // Validaciones
        if (descripcion.isEmpty()) {
            Toast.makeText(this, "Debes ingresar una descripción", Toast.LENGTH_SHORT).show();
            return;
        }

        if (montoString.isEmpty()) {
            Toast.makeText(this, "Debes ingresar un monto", Toast.LENGTH_SHORT).show();
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El monto debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (categoriaSeleccionadaId == -1) {
            Toast.makeText(this, "Debes seleccionar una categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cuentaOrigenSeleccionada == 0) {
            Toast.makeText(this, "Debes seleccionar una cuenta", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tipo == null) {
            Toast.makeText(this, "Debes seleccionar un tipo de movimiento", Toast.LENGTH_SHORT).show();
            return;
        }


        int cuentaId = cuentas.get(cuentaOrigenSeleccionada - 1).getId(); // Restar 1 para compensar la opción vacía
        Cuenta cuentaOrigen = getCuentaById(cuentaId);
        if ((tipo.equals("Gasto") || tipo.equals("Transferencia")) && monto > cuentaOrigen.getSaldo()) {
            Toast.makeText(this, "El monto supera el saldo de la cuenta", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si todas las validaciones pasan, crear el objeto Movimiento y guardarlo
        Movimiento nuevoMovimiento = new Movimiento();
        nuevoMovimiento.setDescripcion(descripcion);
        nuevoMovimiento.setMonto(monto);
        nuevoMovimiento.setTipo(tipo);
        nuevoMovimiento.setCategoriaId(categoriaSeleccionadaId);
        nuevoMovimiento.setCuentaId(cuentaId);
        nuevoMovimiento.setFecha(fechaMovimiento);

        if (tipo.equals("Transferencia")) {
            if (cuentaDestinoSeleccionada == 0) {
                Toast.makeText(this, "Debes seleccionar una cuenta destino", Toast.LENGTH_SHORT).show();
                return;
            }
            int cuentaDestinoId = cuentas.get(cuentaDestinoSeleccionada - 1).getId();
            nuevoMovimiento.setCuentaDestId(cuentaDestinoId);
        } else {
            nuevoMovimiento.setCuentaDestId(0); // Valor por defecto para tipos diferentes a "Transferencia"
        }

        if (isNetworkAvailable()){
            IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
            api.agregarMovimiento(nuevoMovimiento).enqueue(new Callback<Movimiento>() {
                @Override
                public void onResponse(Call<Movimiento> call, Response<Movimiento> response) {
                    if (response.isSuccessful()) {
                        nuevoMovimiento.setIsSynced(true);
                        db.movimientoDao().insert(nuevoMovimiento);
                        actualizarSaldoCuenta(nuevoMovimiento); // Llamar al método para actualizar el saldo
                        Toast.makeText(NuevoMovimientoActivity.this, "Movimiento guardado correctamente", Toast.LENGTH_SHORT).show();
                        finish(); // Cierra la actividad actual
                    } else {
                        Toast.makeText(NuevoMovimientoActivity.this, "Error al guardar el movimiento", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Movimiento> call, Throwable t) {
                    Toast.makeText(NuevoMovimientoActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            nuevoMovimiento.setIsSynced(false);
            db.movimientoDao().insert(nuevoMovimiento);
            actualizarSaldoCuentaLocalmente(nuevoMovimiento);
            Toast.makeText(this, "Movimiento guardado localmente", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private Cuenta getCuentaById(int cuentaId) {
        for (Cuenta cuenta : cuentas) {
            if (cuenta.getId() == cuentaId) {
                return cuenta;
            }
        }
        return null;
    }

    private String obtenerTipoMovimiento() {
        if (rbIngreso.isChecked()) {
            return "Ingreso";
        } else if (rbGasto.isChecked()) {
            return "Gasto";
        } else if (rbTransferencia.isChecked()){
            return "Transferencia";
        } else {
            return null;
        }
    }

    private void actualizarSaldoCuenta(Movimiento movimiento) {
        IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
        int cuentaId = movimiento.getCuentaId();
        double montoMovimiento = movimiento.getMonto();
        String tipoMovimiento = movimiento.getTipo();

        api.getCuentaById(cuentaId).enqueue(new Callback<Cuenta>() {
            @Override
            public void onResponse(Call<Cuenta> call, Response<Cuenta> response) {
                if (response.isSuccessful()) {
                    Cuenta cuenta = response.body();
                    double nuevoSaldo;

                    if (tipoMovimiento.equals("Ingreso")) {
                        nuevoSaldo = cuenta.getSaldo() + montoMovimiento;
                    } else if (tipoMovimiento.equals("Gasto")) {
                        nuevoSaldo = cuenta.getSaldo() - montoMovimiento;
                    } else { // Transferencia
                        int cuentaDestinoId = movimiento.getCuentaDestId();
                        nuevoSaldo = cuenta.getSaldo() - montoMovimiento;
                        actualizarSaldoCuentaDestino(cuentaDestinoId, montoMovimiento);
                    }

                    cuenta.setSaldo(nuevoSaldo);
                    api.actualizarCuenta(cuenta.getId(), cuenta).enqueue(new Callback<Cuenta>() {
                        @Override
                        public void onResponse(Call<Cuenta> call, Response<Cuenta> response) {
                            if (response.isSuccessful()) {
                                // Saldo actualizado correctamente
                            } else {
                                Toast.makeText(NuevoMovimientoActivity.this, "Error al actualizar el saldo", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Cuenta> call, Throwable t) {
                            Toast.makeText(NuevoMovimientoActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(NuevoMovimientoActivity.this, "Error al obtener la cuenta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Cuenta> call, Throwable t) {
                Toast.makeText(NuevoMovimientoActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarSaldoCuentaDestino(int cuentaDestinoId, double monto) {
        IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
        api.getCuentaById(cuentaDestinoId).enqueue(new Callback<Cuenta>() {
            @Override
            public void onResponse(Call<Cuenta> call, Response<Cuenta> response) {
                if (response.isSuccessful()) {
                    Cuenta cuenta = response.body();
                    double nuevoSaldo = cuenta.getSaldo() + monto;
                    cuenta.setSaldo(nuevoSaldo);
                    api.actualizarCuenta(cuenta.getId(), cuenta).enqueue(new Callback<Cuenta>() {
                        @Override
                        public void onResponse(Call<Cuenta> call, Response<Cuenta> response) {
                            if (response.isSuccessful()) {
                                // Saldo de la cuenta destino actualizado correctamente
                            } else {
                                Toast.makeText(NuevoMovimientoActivity.this, "Error al actualizar el saldo de la cuenta destino", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Cuenta> call, Throwable t) {
                            Toast.makeText(NuevoMovimientoActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(NuevoMovimientoActivity.this, "Error al obtener la cuenta destino", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Cuenta> call, Throwable t) {
                Toast.makeText(NuevoMovimientoActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarSaldoCuentaLocalmente(Movimiento movimiento) {
        int cuentaId = movimiento.getCuentaId();
        double montoMovimiento = movimiento.getMonto();
        String tipoMovimiento = movimiento.getTipo();

        Cuenta cuenta = db.cuentaDao().getCuentaById(cuentaId);
        double nuevoSaldo;

        if (tipoMovimiento.equals("Ingreso")) {
            nuevoSaldo = cuenta.getSaldo() + montoMovimiento;
        } else if (tipoMovimiento.equals("Gasto")) {
            nuevoSaldo = cuenta.getSaldo() - montoMovimiento;
        } else { // Transferencia
            int cuentaDestinoId = movimiento.getCuentaDestId();
            nuevoSaldo = cuenta.getSaldo() - montoMovimiento;
            actualizarSaldoCuentaDestinoLocalmente(cuentaDestinoId, montoMovimiento);
        }

        cuenta.setSaldo(nuevoSaldo);
        db.cuentaDao().update(cuenta);
    }

    private void actualizarSaldoCuentaDestinoLocalmente(int cuentaDestinoId, double monto) {
        Cuenta cuentaDestino = db.cuentaDao().getCuentaById(cuentaDestinoId);
        double nuevoSaldo = cuentaDestino.getSaldo() + monto;
        cuentaDestino.setSaldo(nuevoSaldo);
        db.cuentaDao().update(cuentaDestino);
    }
}