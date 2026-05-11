<img width="286" height="630" alt="image" src="https://github.com/user-attachments/assets/7b19d9ee-bd79-441f-bb0d-c4bb746df6d3" />


📄 Dokumentacja Techniczna: 
Aplikacja służy do zarządzania produktami spożywczymi, monitorowania ich dat ważności oraz automatycznego powiadamiania użytkownika o kończącym się terminie spożycia poprzez kolory interfejsu.

1. Oboekty kefelki: PantryItem.java
 – klasa reprezentująca pojedynczy produkt.

getName(): Zwraca nazwę produktu (np. "Sok").

getAddedDate(): Pobiera datę dodania produktu do listy.

getExpiryDate(): Pobiera datę ważności (do logiki kolorów).



2. Logika Widoku: PantryAdapter.java
- Zamienia dane z listy Java na widoczne kafelki na ekranie telefonu.

onCreateViewHolder(): Tworzy nowy "szkielet" wiersza na liście (ładowanie pliku XML). (Tworzy pusty kafelek)

onBindViewHolder():
Daje do kafelka nazwe i date podaną przez uzytkownika
Oblicza różnicę dni między LocalDate.now() a datą ważności.
Ustawia tło karty (setCardBackgroundColor) na podstawie dni:
  Czerwony: Termin minął (dni < 0).
  Żółty: Termin blisko (np. dni <= 3).
  Biały/Szary: Produkt świeży.

getItemCount(): Mówi systemowi, ile przedmiotów ma narysować na ekranie.

3. Dane: MainActivity.java

onCreate(): Inicjalizuje interfejs, łączy się z bazą SharedPreferences i ładuje listę przy starcie.

onResume(): Odświeża listę (notifyDataSetChanged()) za każdym razem, gdy wraca sie do aplikacji, aby kolory zaktualizowały się względem nowej daty systemowej.

saveDataToPhone():
Pakuje listę produktów do formatu tekstowego (String).
Czyści stary zapis w SharedPreferences. (Usuwa stary plik i robi nowy)
Zapisuje nową wersję "na sztywno" do pamięci stałej telefonu.

loadDataFromPhone():
Wczytuje zapisany zestaw tekstów (Set).
Rozcina każdy napis (używając ;) z powrotem na nazwę i daty.
Odbudowuje listę obiektów PantryItem i wkłada je do RecyclerView.

showAddDialog(): Wyświetla okno wpisywania nowego produktu i kalendarz (DatePickerDialog).

sortList(): Układa produkty tak, aby te z najkrótszą datą ważności zawsze były na samej górze, albo data dodania (jak ustawi uzytkownik)

4. Interfejs (Layouts)
activity_main.xml: Zawiera główny kontener, listę RecyclerView oraz dolny pasek nawigacji (Toolbar/BottomAppBar).

item_pantry.xml: Definiuje wygląd pojedynczego kafelka.


Wszystkie funkcje modyfikujące dane (dodawanie, usuwanie, sortowanie) kończą się wywołaniem dwóch metod:
adapter.notifyDataSetChanged() – aby od razu widać zmianę na ekranie.
saveDataToPhone() – aby nie stracić danych po wyłączeniu telefonu.
