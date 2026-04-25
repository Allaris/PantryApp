package io.github.allaris.pantry;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        //  USTAWIANIE NAZWY
        holder.tvName.setText(item.getName());

        // FORMATOWANIE DAT
        DateTimeFormatter formatCzasu = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        DateTimeFormatter formatDaty = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        String dodanoLadne = item.getAddedDate().format(formatCzasu);
        String wazneLadne = item.getExpiryDate().format(formatDaty);

        // USTAWIANIE TEKSTU
        holder.tvDates.setText("Dodano: " + dodanoLadne + " | Ważne do: " + wazneLadne);

        // LOGIKA KOLORÓW
        long daysToExpiry = ChronoUnit.DAYS.between(LocalDate.now(), item.getExpiryDate());

        if (daysToExpiry < 0) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFCDD2")); // Czerwony
        } else if (daysToExpiry <= 7) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFF9C4")); // Żółty
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDates;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvDates = itemView.findViewById(R.id.tvDates);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}