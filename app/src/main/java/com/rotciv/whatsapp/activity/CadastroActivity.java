package com.rotciv.whatsapp.activity;

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
import com.rotciv.whatsapp.helper.Base64Custom;
import com.rotciv.whatsapp.helper.ConfiguracaoFirebase;
import com.rotciv.whatsapp.helper.UsuarioFirebase;
import com.rotciv.whatsapp.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    private EditText editNome, editEmail, editSenha;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getAutenticacao();
    private Usuario usuario = new Usuario();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        getSupportActionBar().setTitle("Cadastro");

        editNome = findViewById(R.id.editNome);
        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
    }

    public boolean validaCampos(){
        if (editSenha.getText().toString().isEmpty() || editEmail.getText().toString().isEmpty() || editNome.getText().toString().isEmpty()) {
            Toast.makeText(CadastroActivity.this, "Preencha todos os campos", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public void CadastrarUsuario (View view){
        if (validaCampos()){
            usuario.setEmail(editEmail.getText().toString());
            usuario.setNome(editNome.getText().toString());
            usuario.setSenha(editSenha.getText().toString());

            autenticacao.createUserWithEmailAndPassword(usuario.getEmail(), usuario.getSenha()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        try {
                            String idUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                            usuario.setId(idUsuario);
                            usuario.salvar();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Toast.makeText(CadastroActivity.this, "Cadastro realizado!", Toast.LENGTH_LONG).show();
                        UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());
                        finish();

                    } else {
                        try {
                            throw (task.getException());
                        } catch (FirebaseAuthInvalidUserException e) {
                            Toast.makeText(CadastroActivity.this, "Email inválido!", Toast.LENGTH_LONG).show();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            Toast.makeText(CadastroActivity.this, "Senha inválida!", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {

                        }
                    }
                }
            });
        }
    }
}
