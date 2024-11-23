package com.example.iacccess;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btnPantallaRegistrarse;
    private Button btnPantallaLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPantallaLogin = findViewById(R.id.btnPantallaLogin);
        btnPantallaRegistrarse = findViewById(R.id.btnPantallaRegistrarse);
    }

    public void pantallaRegistro(View v){
        Intent intent = new Intent(this, Registrarse.class);
        startActivity(intent);
    }

    public void pantallaInicioSesion(View v){
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }
}