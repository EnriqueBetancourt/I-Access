package com.example.iacccess;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MonitorearVisitantes extends Fragment implements VisitaAdapter.OnVisitaSelectedListener {

    private RecyclerView recyclerView;
    private VisitaAdapter visitaAdapter;
    private List<Visita> visitaList;
    private FirebaseFirestore db;

    private Button btnMonitorear, btnRegistrarSalida;
    private String idDocumentoSeleccionado;  // Variable para almacenar el ID del documento seleccionado

    public MonitorearVisitantes() {
    }

    public static MonitorearVisitantes newInstance(String param1, String param2) {
        MonitorearVisitantes fragment = new MonitorearVisitantes();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monitorear_visitantes, container, false);
        return view;
    }

    private void cargarVisitas() {
        db.collection("visitas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    visitaList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String idResidente = document.getString("idResidente");
                        String idVisitante = document.getString("idVisitante");
                        String motivo = document.getString("motivo");
                        String fechaHoraEntrada = document.getString("fechaHoraEntrada");
                        String idDocumento = document.getId();
                        String idPortero = document.getString("idPortero");
                        double latitud = document.contains("latitud") ? document.getDouble("latitud") : 0.0;
                        double longitud = document.contains("longitud") ? document.getDouble("longitud") : 0.0;

                        // Crear objeto Visita
                        Visita visita = new Visita(idResidente, idVisitante, idPortero, motivo, fechaHoraEntrada, idDocumento, latitud, longitud);

                        // Obtener el nombre del visitante desde la colección de usuarios
                        db.collection("usuarios")
                                .document(idVisitante)
                                .get()
                                .addOnSuccessListener(visitanteSnapshot -> {
                                    if (visitanteSnapshot.exists()) {
                                        String nombre = visitanteSnapshot.getString("nombre");
                                        String apellido = visitanteSnapshot.getString("apellido");
                                        visita.setNombreVisitante(nombre + " " + apellido);  // Establecer nombre completo
                                    } else {
                                        visita.setNombreVisitante("Visitante desconocido");
                                    }

                                    visitaList.add(visita);

                                    // Solo notificar al adaptador cuando todas las visitas se han cargado
                                    if (visitaList.size() == queryDocumentSnapshots.size()) {
                                        visitaAdapter.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("MonitorearVisitantes", "Error al obtener nombre del visitante: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar visitas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnMonitorear = view.findViewById(R.id.btnMonitorear);
        btnRegistrarSalida = view.findViewById(R.id.btnRegistrarSalida);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        visitaList = new ArrayList<>();
        visitaAdapter = new VisitaAdapter(visitaList, this);  // Asegúrate de pasar this (el fragmento implementa la interfaz)
        recyclerView.setAdapter(visitaAdapter);

        db = FirebaseFirestore.getInstance();
        cargarVisitas();

        btnMonitorear.setOnClickListener(v -> {
            if (idDocumentoSeleccionado != null && !idDocumentoSeleccionado.isEmpty()) {
                Bundle args = new Bundle();
                args.putString("idVisita", idDocumentoSeleccionado);  // Pasar el ID del documento al siguiente fragmento
                Navigation.findNavController(v).navigate(R.id.monitoreoMapa, args);
            } else {
                Toast.makeText(getContext(), "Seleccione una visita", Toast.LENGTH_SHORT).show();
            }
        });

        btnRegistrarSalida.setOnClickListener(v -> {
            if (idDocumentoSeleccionado != null && !idDocumentoSeleccionado.isEmpty()) {
                db.collection("visitas")
                        .document(idDocumentoSeleccionado)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                // Registrar salida y mover al historial
                                String fechaHoraSalida = obtenerHoraActual();

                                HashMap<String, Object> historialData = new HashMap<>();
                                historialData.put("fechaHoraSalida", fechaHoraSalida);

                                db.collection("historialVisitas")
                                        .add(historialData)
                                        .addOnSuccessListener(aVoid -> {
                                            db.collection("visitas")
                                                    .document(idDocumentoSeleccionado)
                                                    .delete()
                                                    .addOnSuccessListener(aVoid1 -> {
                                                        Toast.makeText(getContext(), "Salida registrada.", Toast.LENGTH_SHORT).show();

                                                        // Actualizar el RecyclerView directamente
                                                        for (int i = 0; i < visitaList.size(); i++) {
                                                            if (visitaList.get(i).getIdDocumento().equals(idDocumentoSeleccionado)) {
                                                                visitaList.remove(i);
                                                                visitaAdapter.notifyItemRemoved(i);
                                                                break;
                                                            }
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(getContext(), "Error al eliminar la visita: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Error al guardar en el historial: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error al obtener los datos de la visita: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(getContext(), "Seleccione una visita.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onVisitaSelected(String idDocumentoVisita) {
        idDocumentoSeleccionado = idDocumentoVisita;  // Guardar el ID del documento seleccionado
        Toast.makeText(getContext(), "ID Seleccionado: " + idDocumentoVisita, Toast.LENGTH_SHORT).show();  // Mensaje para verificar
    }

    private String obtenerHoraActual() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

}
