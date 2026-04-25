package io.github.allaris.pantry;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PantryItem {
    private String name;
    private LocalDateTime addedDate; // LocalDateTime  data i godzina
    private LocalDate expiryDate;    // LocalDate data tylko


    public PantryItem(String name, LocalDateTime addedDate, LocalDate expiryDate) {
        this.name = name;
        this.addedDate = addedDate;
        this.expiryDate = expiryDate;
    }

    // GETTERY
    public String getName() {
        return name;
    }

    public LocalDateTime getAddedDate() {
        return addedDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    // SETTERY (edytowanie przedmiotow)
    public void setName(String name) {
        this.name = name;
    }

    public void setAddedDate(LocalDateTime addedDate) {
        this.addedDate = addedDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
}