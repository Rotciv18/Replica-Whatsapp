package com.rotciv.whatsapp.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.rotciv.whatsapp.helper.ConfiguracaoFirebase;
import com.rotciv.whatsapp.helper.UsuarioFirebase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

//Serializable para poder passar como parâmetro em Intent.putExtra()
public class Usuario implements Serializable {

    private String nome, email, senha, id, foto;

    public Usuario() {
    }

    @Exclude
    public Map<String, Object> converterParaMap(){
        HashMap<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("email", getEmail());
        usuarioMap.put("nome", getNome());
        usuarioMap.put("foto", getFoto());

        return usuarioMap;
    }

    public void salvar(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getDatabase();
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(getId());
        usuarioRef.setValue(this);
    }

    public void atualizar(){
        String uID = UsuarioFirebase.getIdentificadorUsuario();
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getDatabase();
        DatabaseReference usuariosRef = firebaseRef.child("usuarios").child(uID);

        Map<String, Object> valoresUsuario = converterParaMap();

        usuariosRef.updateChildren(valoresUsuario);
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getNome() {
        return nome;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
