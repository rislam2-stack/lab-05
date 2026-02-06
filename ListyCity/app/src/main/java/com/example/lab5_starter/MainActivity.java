package com.example.lab5_starter;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private Button deleteCityButton;
    private EditText deleteCityEditText;
    private ListView cityListView;

    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addCityButton = findViewById(R.id.buttonAddCity);
        deleteCityButton = findViewById(R.id.buttonDeleteCity);
        deleteCityEditText = findViewById(R.id.editTextDeleteCity);
        cityListView = findViewById(R.id.listviewCities);

        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        citiesRef.addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;

            cityArrayList.clear();
            for (QueryDocumentSnapshot doc : value) {
                String name = doc.getString("name");
                String province = doc.getString("province");
                cityArrayList.add(new City(name, province));
            }
            cityArrayAdapter.notifyDataSetChanged();
        });

        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(), "Add City");
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            cityDialogFragment.show(getSupportFragmentManager(), "City Details");
        });

        deleteCityButton.setOnClickListener(v -> {
            String nameToDelete = deleteCityEditText.getText().toString().trim();
            if (!nameToDelete.isEmpty()) {
                citiesRef.document(nameToDelete).delete();
                deleteCityEditText.setText("");
            }
        });
    }

    @Override
    public void updateCity(City city, String title, String year) {
        city.setName(title);
        city.setProvince(year);
        cityArrayAdapter.notifyDataSetChanged();

        Map<String, Object> data = new HashMap<>();
        data.put("name", city.getName());
        data.put("province", city.getProvince());
        citiesRef.document(city.getName()).set(data);
    }

    @Override
    public void addCity(City city) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", city.getName());
        data.put("province", city.getProvince());
        citiesRef.document(city.getName()).set(data);
    }

    public void addDummyData() {
        City m1 = new City("Edmonton", "AB");
        City m2 = new City("Vancouver", "BC");
        cityArrayList.add(m1);
        cityArrayList.add(m2);
        cityArrayAdapter.notifyDataSetChanged();
    }
}