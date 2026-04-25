package io.github.allaris.pantry;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private PantryAdapter adapter;
    private List<PantryItem> pantryList;
    private boolean sortExpiry = false; // Zapamiętuje aktualny wybór sortowania

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Znajduje DOLNY pasek
        com.google.android.material.bottomappbar.BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);
        setSupportActionBar(bottomAppBar); // 3 kropki na dole lewo
        // W MainActivity.java pod setSupportActionBar(bottomAppBar);
        bottomAppBar.setTitle(""); // Usuwa domyślny tytuł, aby TextView na środku miał miejsce
        bottomAppBar.setOverflowIcon(androidx.core.content.ContextCompat.getDrawable(this, android.R.drawable.ic_menu_more));


        // WCZYTYWANIE DANYCH
        loadDataFromPhone();
        sortList(); // Sortuj domyślnie po wczytaniu

        // KONFIGURACJA LISTY
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(pantryList);
        recyclerView.setAdapter(adapter);

        // PRZYCISK DODAWANIA
        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> showAddItemDialog());

        // USUWANIE
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                pantryList.remove(position);
                adapter.notifyItemRemoved(position);
                saveDataToPhone(); // Zapisujemy po usunięciu
            }
        }).attachToRecyclerView(recyclerView);
    }

    // MENU (TRZY KROPKI)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.sort_added) {
            sortExpiry = false;
            sortList();
            return true;
        } else if (item.getItemId() == R.id.sort_expiry) {
            sortExpiry = true;
            sortList();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // LOGIKA DODAWANIA
    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nazwa produktu");
        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Dalej", (dialog, which) -> {
            String name = input.getText().toString();
            if (!name.isEmpty()) openDatePicker(name);
        });
        builder.show();
    }

    private void openDatePicker(String productName) {
        LocalDate now = LocalDate.now();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // Data ważności (wybrana z kalendarza)
            LocalDate selectedExpiry = LocalDate.of(year, month + 1, dayOfMonth);

            // Data dodania (AUTOMATYCZNA z godziną)
            LocalDateTime currentDateTime = LocalDateTime.now();

            // Dodanie do listy
            pantryList.add(new PantryItem(productName, currentDateTime, selectedExpiry));

            sortList();
            saveDataToPhone();
        }, now.getYear(), now.getMonthValue() - 1, now.getDayOfMonth()).show();
    }

    // SORTOWANIE
    private void sortList() {
        if (pantryList == null) return;

        if (sortExpiry) {
            // Sortuj wg daty ważności (najbliższa na górze)
            pantryList.sort((i1, i2) -> i1.getExpiryDate().compareTo(i2.getExpiryDate()));
        } else {
            // Sortuj wg daty dodania (najnowsze na górze)
            pantryList.sort((i1, i2) -> i2.getAddedDate().compareTo(i1.getAddedDate()));
        }

        if (adapter != null) adapter.notifyDataSetChanged();
    }

    // ZAPIS I ODCZYT
    private void saveDataToPhone() {
        SharedPreferences pref = getSharedPreferences("MojaSpiżarnia", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        HashSet<String> set = new HashSet<>();
        for (PantryItem item : pantryList) {
            String row = item.getName() + ";" + item.getAddedDate().toString() + ";" + item.getExpiryDate().toString();
            set.add(row);
        }
        editor.remove("lista_produktow");
        editor.apply();
        editor.putStringSet("lista_produktow", set);
        editor.apply();
    }

    private void loadDataFromPhone() {
        SharedPreferences pref = getSharedPreferences("MojaSpiżarnia", MODE_PRIVATE);
        Set<String> set = pref.getStringSet("lista_produktow", null);
        pantryList = new ArrayList<>();
        if (set != null) {
            for (String s : set) {
                String[] parts = s.split(";");
                if (parts.length == 3) {
                    String name = parts[0];
                    LocalDateTime added = LocalDateTime.parse(parts[1]); // Zmienione na LocalDateTime
                    LocalDate expiry = LocalDate.parse(parts[2]);
                    pantryList.add(new PantryItem(name, added, expiry));
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume(); //  funkcja wznowienia

        // Odświeżamy listę, aby kolory przeliczyły się względem nowej daty
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}