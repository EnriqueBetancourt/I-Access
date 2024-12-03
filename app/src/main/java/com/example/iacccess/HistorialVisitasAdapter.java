package com.example.iacccess;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class HistorialVisitasAdapter extends RecyclerView.Adapter<HistorialVisitasAdapter.HistorialViewHolder> {

    private List<HistorialVisitas> historialVisitasList;
    private String userUID;

    public HistorialVisitasAdapter(List<HistorialVisitas> historialVisitasList, String userUID) {
        this.historialVisitasList = historialVisitasList;
        this.userUID = userUID;
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.historial_visita, parent, false);
        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        HistorialVisitas visita = historialVisitasList.get(position);

        if (visita != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Verificar si es visitante o residente
            if (userUID.equals(visita.getIdVisitante())) {
                // Consultar el nombre del residente
                db.collection("usuarios")
                        .document(visita.getIdResidente())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String nombreResidente = documentSnapshot.getString("nombre");
                                String apellidoResidente = documentSnapshot.getString("apellido");
                                holder.invitedBy.setText("Invitado por: " + nombreResidente + " " + apellidoResidente);
                            } else {
                                holder.invitedBy.setText("Invitado por: Usuario desconocido");
                            }
                        })
                        .addOnFailureListener(e -> holder.invitedBy.setText("Error al cargar el residente"));
            } else if (userUID.equals(visita.getIdResidente())) {
                // Consultar el nombre del visitante
                db.collection("usuarios")
                        .document(visita.getIdVisitante())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String nombreVisitante = documentSnapshot.getString("nombre");
                                String apellidoVisitante = documentSnapshot.getString("apellido");
                                holder.invitedBy.setText("Visitante: " + nombreVisitante + " " + apellidoVisitante);
                            } else {
                                holder.invitedBy.setText("Visitante: Usuario desconocido");
                            }
                        })
                        .addOnFailureListener(e -> holder.invitedBy.setText("Error al cargar el visitante"));
            }

            // Configurar hora de entrada y salida
            holder.checkInTime.setText("Hora de entrada: " + visita.getFechaHoraEntrada());
            holder.checkOutTime.setText("Hora de salida: " + visita.getFechaHoraSalida());
        } else {
            holder.checkInTime.setText("Datos no disponibles");
            holder.checkOutTime.setText("Datos no disponibles");
            holder.invitedBy.setText("Datos no disponibles");
        }
    }


    @Override
    public int getItemCount() {
        return historialVisitasList != null ? historialVisitasList.size() : 0;
    }

    public static class HistorialViewHolder extends RecyclerView.ViewHolder {

        public TextView checkInTime;
        public TextView checkOutTime;
        public TextView invitedBy;

        public HistorialViewHolder(View itemView) {
            super(itemView);
            checkInTime = itemView.findViewById(R.id.checkInTime);
            checkOutTime = itemView.findViewById(R.id.checkOutTime);
            invitedBy = itemView.findViewById(R.id.invitedBy);
        }
    }
}