package com.rotciv.whatsapp.helper;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.rotciv.whatsapp.model.Usuario;

public class UsuarioFirebase {
    public static String getIdentificadorUsuario(){
        FirebaseAuth usuario = ConfiguracaoFirebase.getAutenticacao();
        String uID = Base64Custom.codificarBase64(usuario.getCurrentUser().getEmail());
        return uID;
    }

    public static FirebaseUser getUsuarioAtual(){
        FirebaseAuth usuario = ConfiguracaoFirebase.getAutenticacao();
        return usuario.getCurrentUser();

    }

    public static boolean atualizarNomeUsuario (String nome){
        try {
            FirebaseUser user = getUsuarioAtual();
            UserProfileChangeRequest profileChange = new UserProfileChangeRequest.Builder().setDisplayName(nome).build();

            user.updateProfile(profileChange).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()){
                        Log.d("Perfil", "Erro ao atualizar nome de perfil");
                    }
                }
            });

            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean atualizarFotoUsuario (Uri url){
        try {
            FirebaseUser user = getUsuarioAtual();
            UserProfileChangeRequest profileChange = new UserProfileChangeRequest.Builder().setPhotoUri(url).build();

            user.updateProfile(profileChange).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()){
                        Log.d("Perfil", "Erro ao atualizar foto de perfil");
                    }
                }
            });

            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static Usuario getDadosUsuarioLogado(){
        FirebaseUser usuarioAtual = getUsuarioAtual();
        Usuario usuario = new Usuario();

        usuario.setEmail(usuarioAtual.getEmail());
        usuario.setNome(usuarioAtual.getDisplayName());
        usuario.setId(Base64Custom.codificarBase64(usuarioAtual.getEmail()));
        if (usuarioAtual.getPhotoUrl() == null){
            usuario.setFoto("");
        } else {
            usuario.setFoto(usuarioAtual.getPhotoUrl().toString());
        }

        return usuario;
    }

}
