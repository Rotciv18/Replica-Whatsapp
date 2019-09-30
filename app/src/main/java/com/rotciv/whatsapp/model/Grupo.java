package com.rotciv.whatsapp.model;

import com.google.firebase.database.DatabaseReference;
import com.rotciv.whatsapp.helper.Base64Custom;
import com.rotciv.whatsapp.helper.ConfiguracaoFirebase;

import java.io.Serializable;
import java.util.List;

public class Grupo implements Serializable {

    private String id, nome, foto;
    private List<Usuario> usuarios;

    public Grupo() {
        DatabaseReference databaseReference = ConfiguracaoFirebase.getDatabase();
        DatabaseReference grupoRef = databaseReference.child("grupos");

        String idFirebase = grupoRef.push().getKey();
        setId(idFirebase);
    }

    public void salvar(){
        DatabaseReference databaseReference = ConfiguracaoFirebase.getDatabase();
        DatabaseReference grupoRef = databaseReference.child("grupos");

        grupoRef.child(getId()).setValue(this);

        //Salvar conversa no Database
        for (Usuario usuario : getUsuarios()){
            String idRemetente = Base64Custom.codificarBase64(usuario.getEmail());
            String idDestinatario = getId();

            Conversa conversa = new Conversa();
            conversa.setIdRemetente(idRemetente);
            conversa.setIdDestinatario(idDestinatario);
            conversa.setUltimaMensagem("");
            conversa.setIsGroup("true");
            conversa.setGrupo(this);

            conversa.salvar();

        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
}
