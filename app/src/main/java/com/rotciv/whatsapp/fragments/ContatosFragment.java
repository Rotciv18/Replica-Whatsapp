package com.rotciv.whatsapp.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.rotciv.whatsapp.R;
import com.rotciv.whatsapp.activity.ChatActivity;
import com.rotciv.whatsapp.activity.GrupoActivity;
import com.rotciv.whatsapp.adapter.ContatosAdapter;
import com.rotciv.whatsapp.helper.ConfiguracaoFirebase;
import com.rotciv.whatsapp.helper.RecyclerItemClickListener;
import com.rotciv.whatsapp.helper.UsuarioFirebase;
import com.rotciv.whatsapp.model.Conversa;
import com.rotciv.whatsapp.model.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContatosFragment extends Fragment {

    private RecyclerView recyclerView;
    private ContatosAdapter adapter;
    private List<Usuario> usuarios = new ArrayList<>();
    private DatabaseReference usuariosRef = ConfiguracaoFirebase.getDatabase().child("usuarios");
    private ValueEventListener valueEventListener;
    private FirebaseUser usuarioAtual = UsuarioFirebase.getUsuarioAtual();

    public ContatosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_contatos, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);

        //configurar adapter
        adapter = new ContatosAdapter(view.getContext(), usuarios);

        //configurar recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager( layoutManager );
        recyclerView.setAdapter( adapter );

        //Configurar evento de click no RecyclerView
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(
                getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                List<Usuario> listaUsuariosAtualizada = adapter.getUsuarios();
                Usuario usuarioSelecionado = listaUsuariosAtualizada.get(position);
                boolean cabecalho = usuarioSelecionado.getEmail().isEmpty();

                if (cabecalho){
                    Intent i = new Intent(getActivity(), GrupoActivity.class);
                    startActivity(i);
                } else {
                    Intent i = new Intent(getActivity(), ChatActivity.class);
                    i.putExtra("chatContato", usuarioSelecionado);
                    startActivity(i);
                }
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }
        ));



        return view;
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

                Usuario itemGrupo = new Usuario();
                itemGrupo.setNome("Novo grupo");
                itemGrupo.setEmail("");
                usuarios.add(itemGrupo);

                for (DataSnapshot dados: dataSnapshot.getChildren()){
                    Usuario usuario = dados.getValue(Usuario.class);
                    if ( !(usuario.getEmail().equals(usuarioAtual.getEmail()) ) )
                        usuarios.add(usuario);

                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void recarregarContatos(){
        adapter = new ContatosAdapter(getActivity(), usuarios);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void pesquisarContatos (String texto){
        List<Usuario> listaUsuariosBusca = new ArrayList<>();

        for (Usuario usuario : usuarios){

            if (usuario.getNome() != null) {
                String nome = usuario.getNome().toLowerCase();
                if (nome.contains(texto))
                    listaUsuariosBusca.add(usuario);
            }
        }

        adapter = new ContatosAdapter(getActivity(), listaUsuariosBusca);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

}
