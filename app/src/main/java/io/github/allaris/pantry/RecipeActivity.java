package io.github.allaris.pantry;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.Markwon;



public class RecipeActivity extends AppCompatActivity {
    private TextView textResponse;
    private ProgressBar progressBar;
    private Markwon markwon;
    private List<Content> conversationHistory = new ArrayList<>(); //Chat session odmawia współpracy
    private GenerativeModelFutures modelFutures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        // Inicjalizacja widoków
        textResponse = findViewById(R.id.textResponse);
        progressBar = findViewById(R.id.progressBar);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnTryAgain = findViewById(R.id.btnTryAgain);

        // Inicjalizacja formatowania tekstu (Markdown)
        markwon = Markwon.create(this);

        // Konfiguracja modelu Gemini  gemini-2.5-flash-lite ,gemini-2.5-flash ,gemini-2.0-flash
        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash-lite", "AIzaSyCvXTBWiuYDHsJWRtT0oJ7YOG4VyWUQaEk"); //kluzc z https://aistudio.google.com/api-keys
        modelFutures = GenerativeModelFutures.from(gm);

        String prompt = getIntent().getStringExtra("PROMPT");
        boolean hideRetry = getIntent().getBooleanExtra("HIDE_RETRY", false);

        // Jeśli podgląd -> ukryj przycisk
        if (hideRetry) {
            btnTryAgain.setVisibility(View.GONE);
        }

        if ("POKAZ_STARY".equals(prompt)) {
            loadLastRecipe();
        } else {
            sendToAi(prompt);
        }

        // Obsługa przycisku "Podaj inny"
        btnTryAgain.setOnClickListener(v -> {
            sendToAi("To mi nie odpowiada, podaj inny przepis z tych samych składników.");
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void sendToAi(String message) {
        progressBar.setVisibility(View.VISIBLE);
        textResponse.setAlpha(0.5f);

        // NAPRAWA BŁĘDU VOID: Tworzymy wiadomość użytkownika krok po kroku
        Content.Builder userBuilder = new Content.Builder();
        userBuilder.setRole("user");
        userBuilder.addText(message);
        Content userMessage = userBuilder.build();

        conversationHistory.add(userMessage);

        // Wysyłamy historię jako tablicę
        ListenableFuture<GenerateContentResponse> response =
                modelFutures.generateContent(conversationHistory.toArray(new Content[0]));

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    textResponse.setAlpha(1.0f);

                    String responseText = result.getText();

                    // NAPRAWA BŁĘDU VOID: Tworzymy odpowiedź AI krok po kroku
                    Content.Builder aiBuilder = new Content.Builder();
                    aiBuilder.setRole("model");
                    aiBuilder.addText(responseText);
                    Content aiMessage = aiBuilder.build();

                    conversationHistory.add(aiMessage);

                    markwon.setMarkdown(textResponse, responseText);
                    saveRecipeToPrefs(responseText);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    textResponse.setAlpha(1.0f);
                    Log.e("GEMINI_ERROR", "Błąd:", t);
                    textResponse.setText("Błąd AI: " + t.getLocalizedMessage());
                });
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void saveRecipeToPrefs(String recipe) {
        SharedPreferences pref = getSharedPreferences("MojaSpiżarnia", MODE_PRIVATE);
        pref.edit().putString("ostatni_przepis", recipe).apply();
    }

    private void loadLastRecipe() {
        SharedPreferences pref = getSharedPreferences("MojaSpiżarnia", MODE_PRIVATE);
        String lastRecipe = pref.getString("ostatni_przepis", "Nie znaleziono zapisanego przepisu.");
        markwon.setMarkdown(textResponse, lastRecipe);
        progressBar.setVisibility(View.GONE);
    }
}