package io.github.allaris.pantry;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    private List<PantryItem> itemList;

    public PantryAdapter(List<PantryItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PantryItem item = itemList.get(position);

        holder.tvName.setText(item.getName());

        DateTimeFormatter formatCzasu = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        DateTimeFormatter formatDaty = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        String dodanoLadne = item.getAddedDate().format(formatCzasu);
        String wazneLadne = item.getExpiryDate().format(formatDaty);

        holder.tvDates.setText("Dodano: " + dodanoLadne + " | Ważne do: " + wazneLadne);

        long daysToExpiry = ChronoUnit.DAYS.between(LocalDate.now(), item.getExpiryDate());

        if (daysToExpiry < 0) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFCDD2"));
        } else if (daysToExpiry <= 7) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFF9C4"));
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }

        // TUTAJ JUŻ NIE BĘDZIE BŁĘDU:
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(item.isSelected());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setSelected(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDates;
        CardView cardView;
        CheckBox checkBox; // 2. DODAJ TO TUTAJ

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvDates = itemView.findViewById(R.id.tvDates);
            cardView = itemView.findViewById(R.id.cardView);
            // 3. I POWIĄŻ Z ID Z XML (upewnij się, że w XML masz android:id="@+id/checkboxSelect")
            checkBox = itemView.findViewById(R.id.checkboxSelect);
        }
    }

    public void updateList(List<PantryItem> newList) {
        this.itemList = newList;
        notifyDataSetChanged();
    }
}