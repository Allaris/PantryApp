package io.github.allaris.pantry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private PantryAdapter adapter;
    private List<PantryItem> pantryList;
    private boolean sortExpiry = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. ŁADOWANIE DANYCH (Najpierw dane, potem widoki)
        loadDataFromPhone();

        // 2. WIDOKI I TOOLBAR
        com.google.android.material.bottomappbar.BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);
        bottomAppBar.setTitle("");

        // 3. SORTOWANIE (ImageButton w XML)
        ImageButton btnSort = findViewById(R.id.btnSort);
        btnSort.setOnClickListener(v -> {
            androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(this, v);
            popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.sort_added) {
                    sortExpiry = false;
                } else if (item.getItemId() == R.id.sort_expiry) {
                    sortExpiry = true;
                }
                sortList(); // Odświeża i filtruje
                return true;
            });
            popupMenu.show();
        });

        // 4. WYSZUKIWARKA (Tylko jeden TextWatcher!)
        EditText searchField = findViewById(R.id.searchField);
        searchField.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }
        });

        // 5. KONFIGURACJA LISTY
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(pantryList);
        recyclerView.setAdapter(adapter);

        // Domyślne sortowanie na starcie
        sortList();

        // 6. PRZYCISKI (Add / AI)
        findViewById(R.id.fabAdd).setOnClickListener(v -> showAddItemDialog());

        findViewById(R.id.btnAskAi).setOnClickListener(v -> {
            StringBuilder selectedItems = new StringBuilder();
            for (PantryItem item : pantryList) {
                if (item.isSelected()) selectedItems.append(item.getName()).append(", ");
            }
            String prompt;
            if (selectedItems.length() == 0) {
                prompt = "Zaproponuj 3 szybkie dania z podstawowych produktów.";
            } else {
                prompt = "Zrób mi danie z: " + selectedItems + ". Podaj przepis i listę zakupów.";
            }

            Intent intent = new Intent(this, RecipeActivity.class);
            intent.putExtra("PROMPT", prompt);
            intent.putExtra("HIDE_RETRY", false); // Tutaj chcemy widzieć przycisk
            startActivity(intent);
        });

        // 7. USUWANIE (Swipe)
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder v, @NonNull RecyclerView.ViewHolder t) {return false;}
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                // Ważne: musimy usuwać z głównej listy, nie tylko z widoku!
                pantryList.remove(position);
                saveDataToPhone();
                sortList(); // Odśwież widok po usunięciu
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void filterList(String text) {
        if (pantryList == null) return;

        // Najpierw układamy główną listę
        if (sortExpiry) {
            pantryList.sort(Comparator.comparing(PantryItem::getExpiryDate));
        } else {
            pantryList.sort((i1, i2) -> i2.getAddedDate().compareTo(i1.getAddedDate()));
        }

        String query = text.toLowerCase().trim();
        if (query.isEmpty()) {
            adapter.updateList(new ArrayList<>(pantryList));
            return;
        }

        List<PantryItem> filteredList = new ArrayList<>();
        for (PantryItem item : pantryList) {
            if (item.getName().toLowerCase().contains(query)) {
                filteredList.add(item);
            }
        }
        adapter.updateList(filteredList);
    }

    private void sortList() {
        EditText searchField = findViewById(R.id.searchField);
        filterList(searchField.getText().toString());
    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dodaj nowy produkt");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // 1. Nazwa
        final EditText inputName = new EditText(this);
        inputName.setHint("Nazwa produktu");
        layout.addView(inputName);

        // 2. Ilość
        final EditText inputCount = new EditText(this);
        inputCount.setHint("Ilość");
        inputCount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(inputCount);

        // 3. Etykieta
        android.widget.TextView label = new android.widget.TextView(this);
        label.setText("\nData ważności:");
        layout.addView(label);

        // 4. DatePicker ze stylem Spinnera
        // Używamy ContextThemeWrapper, aby wymusić styl spinnera
        android.view.ContextThemeWrapper contextThemeWrapper = new android.view.ContextThemeWrapper(this, R.style.MyDatePickerSpinner);
        final android.widget.DatePicker datePicker = new android.widget.DatePicker(contextThemeWrapper);

        layout.addView(datePicker);

        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        scrollView.addView(layout);
        builder.setView(scrollView);

        builder.setPositiveButton("Dodaj", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            int count = 1;
            try {
                String c = inputCount.getText().toString();
                if (!c.isEmpty()) count = Integer.parseInt(c);
            } catch (Exception e) { count = 1; }

            if (!name.isEmpty()) {
                LocalDate expiry = LocalDate.of(datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());
                LocalDateTime now = LocalDateTime.now();

                for (int i = 0; i < count; i++) {
                    pantryList.add(new PantryItem(name, now, expiry));
                }
                sortList();
                saveDataToPhone();
            }
        });

        builder.setNegativeButton("Anuluj", null);
        builder.show();
    }

    private void saveDataToPhone() {
        SharedPreferences pref = getSharedPreferences("MojaSpiżarnia", MODE_PRIVATE);
        HashSet<String> set = new HashSet<>();
        for (PantryItem item : pantryList) {
            set.add(item.getName() + ";" + item.getAddedDate().toString() + ";" + item.getExpiryDate().toString());
        }
        pref.edit().remove("lista_produktow").apply();
        pref.edit().putStringSet("lista_produktow", set).apply();
    }

    private void loadDataFromPhone() {
        SharedPreferences pref = getSharedPreferences("MojaSpiżarnia", MODE_PRIVATE);
        Set<String> set = pref.getStringSet("lista_produktow", null);
        pantryList = new ArrayList<>();
        if (set != null) {
            for (String s : set) {
                String[] parts = s.split(";");
                if (parts.length == 3) pantryList.add(new PantryItem(parts[0], LocalDateTime.parse(parts[1]), LocalDate.parse(parts[2])));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sortList();
        SharedPreferences pref = getSharedPreferences("MojaSpiżarnia", MODE_PRIVATE);
        ImageButton btnShowLast = findViewById(R.id.btnShowLastRecipe);
        if (pref.getString("ostatni_przepis", null) != null) {
            btnShowLast.setVisibility(View.VISIBLE);
            btnShowLast.setOnClickListener(v -> {
                Intent intent = new Intent(this, RecipeActivity.class);
                intent.putExtra("PROMPT", "POKAZ_STARY");
                intent.putExtra("HIDE_RETRY", true); // UKRYWAMY przycisk w podglądzie
                startActivity(intent);
            });
        } else btnShowLast.setVisibility(View.GONE);
    }
}