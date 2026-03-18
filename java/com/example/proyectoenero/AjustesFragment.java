package com.example.proyectoenero;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.proyectoenero.databinding.FragmentAjustesBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

import auth.AuthActivity;
import sharedPreferences.MainViewModel;
import viewmodel.TmdbViewModel;

public class AjustesFragment extends Fragment {

    private FragmentAjustesBinding binding;
    private MainViewModel viewModel;

    private final List<String> idiomasNombres = Arrays.asList("Español (España)", "Inglés (USA)", "Francés", "Alemán");
    private final List<String> idiomasCodigos = Arrays.asList("es-ES", "en-US", "fr-FR", "de-DE");

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAjustesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setupSpinner();
        cargarPreferencias();

        binding.btnGuardar.setOnClickListener(v -> guardarPreferencias());
        binding.btnReset.setOnClickListener(v -> {
            viewModel.resetear();
            cargarPreferencias();
            aplicarTema("claro");
            Toast.makeText(getContext(), "Preferencias reseteadas", Toast.LENGTH_SHORT).show();
        });


        binding.btnLogout.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();


            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
            GoogleSignInClient googleClient = GoogleSignIn.getClient(requireActivity(), gso);
            googleClient.signOut();

            Intent intent = new Intent(requireContext(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, idiomasNombres);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerIdioma.setAdapter(adapter);
    }

    private void cargarPreferencias() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("displayName")) {
                        binding.etNombreUsuario.setText(doc.getString("displayName"));
                    }
                });
        binding.etNombreUsuario.setText(viewModel.getNombre());

        String idiomaActual = viewModel.getIdioma();
        int index = idiomasCodigos.indexOf(idiomaActual);
        if (index >= 0) binding.spinnerIdioma.setSelection(index);

        binding.cbSoloWifi.setChecked(viewModel.isSoloWifi());

        String tema = viewModel.getTema();
        if ("oscuro".equals(tema)) {
            binding.rbTemaOscuro.setChecked(true);
        } else {
            binding.rbTemaClaro.setChecked(true);
        }
    }

    private void guardarPreferencias() {
        String nuevoNombre = binding.etNombreUsuario.getText().toString();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users").document(uid)
                .update("displayName", nuevoNombre)
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error actualizando", Toast.LENGTH_SHORT).show());

        Toast.makeText(getContext(), "Cambios guardados", Toast.LENGTH_SHORT).show();
        viewModel.guardarNombre(binding.etNombreUsuario.getText().toString());

        int selectedLangIndex = binding.spinnerIdioma.getSelectedItemPosition();
        viewModel.guardarIdioma(idiomasCodigos.get(selectedLangIndex));

        viewModel.guardarSoloWifi(binding.cbSoloWifi.isChecked());

        String temaSeleccionado = binding.rbTemaOscuro.isChecked() ? "oscuro" : "claro";
        viewModel.guardarTema(temaSeleccionado);

        aplicarTema(temaSeleccionado);

        TmdbViewModel tmdbViewModel = new ViewModelProvider(requireActivity()).get(TmdbViewModel.class);
        tmdbViewModel.limpiarDatos();

        Toast.makeText(getContext(), "Cambios guardados", Toast.LENGTH_SHORT).show();
    }

    private void aplicarTema(String tema) {
        if ("oscuro".equals(tema)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}