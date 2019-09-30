package com.rotciv.whatsapp.helper;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ConfiguracaoFirebase {

    private static FirebaseAuth autenticacao;
    private static DatabaseReference database;
    private static StorageReference storage;

    public static DatabaseReference getDatabase(){
        if (database == null)
            database = FirebaseDatabase.getInstance().getReference();
        return database;
    }

    public static FirebaseAuth getAutenticacao(){
        if (autenticacao == null)
            autenticacao = FirebaseAuth.getInstance();
        return autenticacao;
    }

    public static StorageReference getFirebaseStorage(){
        if (storage == null)
            storage = FirebaseStorage.getInstance().getReference();
        return storage;
    }
}