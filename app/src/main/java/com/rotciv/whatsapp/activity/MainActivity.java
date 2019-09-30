package com.rotciv.whatsapp.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.rotciv.whatsapp.R;
import com.rotciv.whatsapp.helper.ConfiguracaoFirebase;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth autenticacao = ConfiguracaoFirebase.getAutenticacao();
    private EditText editEmail, editSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
    }

    public void irCadastroActivity(View view){
        startActivity(new Intent(this, CadastroActivity.class));
    }

    public void autenticarUsuario(View view){
        if (validaCampos()) {
            autenticacao.signInWithEmailAndPassword(editEmail.getText().toString(), editSenha.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Login realizado!", Toast.LENGTH_LONG).show();
                        checarAutenticacao();
                    } else {
                        Toast.makeText(MainActivity.this, "Falha na autenticação", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public boolean validaCampos(){
        if (editSenha.getText().toString().isEmpty() || editEmail.getText().toString().isEmpty()) {
            Toast.makeText(MainActivity.this, "Preencha todos os campos", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        checarAutenticacao();
    }

    public void checarAutenticacao() {
        if (autenticacao.getCurrentUser() != null) {
            startActivity(new Intent(this, PrincipalActivity.class));
            finish();
        }
    }
}
