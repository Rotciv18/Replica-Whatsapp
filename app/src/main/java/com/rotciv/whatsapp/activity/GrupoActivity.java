package com.rotciv.whatsapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.rotciv.whatsapp.R;
import com.rotciv.whatsapp.adapter.ContatosAdapter;
import com.rotciv.whatsapp.adapter.GrupoSelecionadoAdapter;
import com.rotciv.whatsapp.helper.ConfiguracaoFirebase;
import com.rotciv.whatsapp.helper.RecyclerItemClickListener;
import com.rotciv.whatsapp.helper.UsuarioFirebase;
import com.rotciv.whatsapp.model.Usuario;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GrupoActivity extends AppCompatActivity {

    private RecyclerView recyclerMembrosSelecionados;
    private RecyclerView recyclerMembros;
    private ContatosAdapter contatosAdapter;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private List<Usuario> usuarios = new ArrayList<>();
    private List<Usuario> usuariosSelecionados = new ArrayList<>();
    private DatabaseReference usuariosRef;
    private ValueEventListener valueEventListener;
    private FirebaseUser usuarioAtual = UsuarioFirebase.getUsuarioAtual();
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo grupo");
        setSupportActionBar(toolbar);


        FloatingActionButton fab = findViewById(R.id.fabAvancarCadastro);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(GrupoActivity.this, CadastroGrupoActivity.class);
                i.putExtra("membros", (Serializable) usuariosSelecionados);
                startActivity(i);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configurações iniciais
        recyclerMembros = findViewById(R.id.recyclerMembros);
        recyclerMembrosSelecionados = findViewById(R.id.recyclerMembrosSelecionados);
        usuariosRef = ConfiguracaoFirebase.getDatabase().child("usuarios");

        //Configurar adapter
        contatosAdapter = new ContatosAdapter(getApplicationContext(), usuarios);
        //Configurar recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMembros.setLayoutManager(layoutManager);
        recyclerMembros.setHasFixedSize(true);
        recyclerMembros.setAdapter(contatosAdapter);

        recyclerMembros.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), recyclerMembros, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Usuario usuarioSelecionado = usuarios.get(position);

                //Remover usuário selecionado
                usuarios.remove(position);
                contatosAdapter.notifyDataSetChanged();
                usuariosSelecionados.add(usuarioSelecionado);
                grupoSelecionadoAdapter.notifyDataSetChanged();

                atualizarMembrosToolbar();
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }));

        //Configura adapter para Membros Selecionados
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(getApplicationContext(), usuariosSelecionados);

        //Configura recyclerView para grupo de selecionados
        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(), LinearLayoutManager.HORIZONTAL, false
        );
        recyclerMembrosSelecionados.setLayoutManager(layoutManagerHorizontal);
        recyclerMembrosSelecionados.setHasFixedSize(true);
        recyclerMembrosSelecionados.setAdapter(grupoSelecionadoAdapter);

        //Evento de click nos membros selecionados
        recyclerMembrosSelecionados.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), recyclerMembrosSelecionados, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Usuario usuarioSelecionado = usuariosSelecionados.get(position);
                usuariosSelecionados.remove(position);
                usuarios.add(usuarioSelecionado);

                grupoSelecionadoAdapter.notifyDataSetChanged();
                contatosAdapter.notifyDataSetChanged();
                atualizarMembrosToolbar();
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }));
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuariosRef.removeEventListener(valueEventListener);

    }

    public void recuperarContatos(){

        valueEventListener = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usuarios.clear();

                for (DataSnapshot dados: dataSnapshot.getChildren()){
                    Usuario usuario = dados.getValue(Usuario.class);
                    if ( !(usuario.getEmail().equals(usuarioAtual.getEmail()) ) )
                        usuarios.add(usuario);

                }

                contatosAdapter.notifyDataSetChanged();
                atualizarMembrosToolbar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void atualizarMembrosToolbar(){
        int totalSelecionados = usuariosSelecionados.size();
        int total = usuarios.size() + totalSelecionados;

        toolbar.setSubtitle(totalSelecionados + " de " + total + " selecionados");
    }

}
