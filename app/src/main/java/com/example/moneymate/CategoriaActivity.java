    package com.example.moneymate;

    import android.content.Context;
    import android.content.Intent;
    import android.net.ConnectivityManager;
    import android.net.NetworkInfo;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ImageButton;
    import android.widget.Toast;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;
    import androidx.room.Room;

    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;

    import java.util.List;

    import adapters.CategoriaAdapter;
    import entities.AppDatabase;
    import entities.Categoria;
    import utils.SyncCallback;
    import utils.SyncService;

    public class CategoriaActivity extends AppCompatActivity {

        private AppDatabase db;
        private CategoriaAdapter adapter;
        private List<Categoria> categoriaList;
        private RecyclerView recyclerView;
        private EditText nombreCategoriaEditText;
        private Button addCategoriaButton;
        private String userId;
        private SyncService syncService;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_categoria);

            db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database-name")
                    .allowMainThreadQueries()
                    .build();
            syncService = new SyncService(this);

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            userId = (currentUser != null) ? currentUser.getUid() : null;

            recyclerView = findViewById(R.id.recyclerViewCategorias);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            addCategoriaButton = findViewById(R.id.addCategoriaButton);
            nombreCategoriaEditText = findViewById(R.id.nombreCategoriaEditText);

            // Botón para ver movimientos
            ImageButton btnVerMovimientos = findViewById(R.id.btn_ver_movimientos);
            btnVerMovimientos.setOnClickListener(view -> {
                Intent intent = new Intent(CategoriaActivity.this, MovimientosActivity.class);
                startActivity(intent);
            });


            // Botón para ver cuentas
            ImageButton btnVerCuentas = findViewById(R.id.btn_ver_cuentas);
            btnVerCuentas.setOnClickListener(view -> {
                Intent intent = new Intent(CategoriaActivity.this, CuentasActivity.class);
                startActivity(intent);
            });

            // Botón para ver categorias
            ImageButton bntVerCategorias = findViewById(R.id.btn_ver_categorias);
            bntVerCategorias.setOnClickListener(view -> {
                Intent intent = new Intent(CategoriaActivity.this, CategoriaActivity.class);
                startActivity(intent);
            });

            // Botón para ver inicio
            ImageButton btnInicio = findViewById(R.id.btn_menu);
            btnInicio.setOnClickListener(view -> {
                Intent intent = new Intent(CategoriaActivity.this, MainActivity.class);
                startActivity(intent);
            });

            // Botón para registrar nuevo movimiento
            ImageButton btnNuevoMovimiento = findViewById(R.id.btn_nuevo_movimiento);
            btnNuevoMovimiento.setOnClickListener(v -> {
                Intent intent = new Intent(CategoriaActivity.this, NuevoMovimientoActivity.class);
                startActivity(intent);
            });

            addCategoriaButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String nombre = nombreCategoriaEditText.getText().toString();
                    if (!nombre.isEmpty()) {
                        Categoria categoria = new Categoria();
                        categoria.nombre = nombre;
                        categoria.userId = userId;

                        if (isNetworkAvailable()) {
                            // Registrar la categoría en la API y luego en la base de datos local
                            categoria.setIsSynced(true);
                            syncService.agregarCategoriaEnApi(categoria, new SyncCallback() {
                                @Override
                                public void onSyncComplete() {

                                }

                                @Override
                                public void onSyncComplete(Categoria syncedCategoria) {
                                    syncedCategoria.setIsSynced(true);
                                    db.categoriaDao().insert(syncedCategoria);
                                    loadCategories();
                                }
                            });
                        } else {
                            // Registrar la categoría solo localmente con isSynced = false
                            categoria.setIsSynced(false);
                            db.categoriaDao().insert(categoria);
                            Toast.makeText(CategoriaActivity.this, "Categoria agregada offline", Toast.LENGTH_SHORT).show();
                            loadCategories();
                        }
                    }
                    nombreCategoriaEditText.setText("");
                }
            });
        }

        @Override
        protected void onResume() {
            super.onResume();
            if (isNetworkAvailable()) {
                syncService.syncCategories();
                syncService.fetchCategoriesFromApi();
            }
            loadCategories();
        }


        private void loadCategories() {
            categoriaList = db.categoriaDao().getAllByUser(userId);
            if (adapter == null) {
                adapter = new CategoriaAdapter(categoriaList, this);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateCategories(categoriaList);
            }
        }
        private boolean isNetworkAvailable() {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }
