package com.rotciv.whatsapp.fragments;


import android.content.Context;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.rotciv.whatsapp.R;
import com.rotciv.whatsapp.activity.ChatActivity;
import com.rotciv.whatsapp.adapter.ConversaAdapter;
import com.rotciv.whatsapp.helper.Base64Custom;
import com.rotciv.whatsapp.helper.ConfiguracaoFirebase;
import com.rotciv.whatsapp.helper.RecyclerItemClickListener;
import com.rotciv.whatsapp.model.Conversa;
import com.rotciv.whatsapp.model.Usuario;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversasFragment extends Fragment {

    private List<Conversa> conversas = new ArrayList<>();
    private RecyclerView recyclerViewConversas;
    private DatabaseReference dbRef = ConfiguracaoFirebase.getDatabase();
    DatabaseReference conversasRef;
    private FirebaseAuth auth = ConfiguracaoFirebase.getAutenticacao();
    private ValueEventListener valueEventListener;
    ConversaAdapter adapter;

    public ConversasFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversas, container, false);

        recyclerViewConversas = view.findViewById(R.id.recyclerViewConversas);

        configuraRecyclerView(view.getContext());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperaConversas();
    }

    @Override
    public void onStop() {
        super.onStop();

        conversasRef.removeEventListener(valueEventListener);
    }

    public void recarregarConversas(){
        adapter = new ConversaAdapter(getActivity(), conversas);
        recyclerViewConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void pesquisarConversas (String texto){
        List<Conversa> listaConversasBusca = new ArrayList<>();

        for (Conversa conversa : conversas){

            if (conversa.getUsuarioExibicao() != null) {
                String nome = conversa.getUsuarioExibicao().getNome().toLowerCase();
                String ultimaMensagem = conversa.getUltimaMensagem().toLowerCase();
                if (nome.contains(texto) || ultimaMensagem.contains(texto))
                    listaConversasBusca.add(conversa);
            }
            else {
                String nome = conversa.getGrupo().getNome().toLowerCase();
                String ultimaMensagem = conversa.getUltimaMensagem().toLowerCase();
                if (nome.contains(texto) || ultimaMensagem.contains(texto))
                    listaConversasBusca.add(conversa);
            }
        }

        adapter = new ConversaAdapter(getActivity(), listaConversasBusca);
        recyclerViewConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void recuperaConversas(){

        String idRemetente = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());

        conversasRef = dbRef.child("conversas").child(idRemetente);

        valueEventListener = conversasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                conversas.clear();

                for (DataSnapshot dados : dataSnapshot.getChildren()){
                    Conversa conversa = dados.getValue(Conversa.class);
                    conversas.add(conversa);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void configuraRecyclerView(Context context){
        adapter = new ConversaAdapter(context, conversas);

        //configura recycler view
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerViewConversas.setHasFixedSize(true);
        recyclerViewConversas.setLayoutManager( layoutManager );
        recyclerViewConversas.setAdapter( adapter );

        //adiciona evento de clique

        recyclerViewConversas.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerViewConversas, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                List<Conversa> conversasAtualizadas = adapter.getConversas();
                Conversa conversaSelecionada = conversasAtualizadas.get(position);

                if (conversaSelecionada.getIsGroup().equals("true")){
                    Intent i = new Intent(getActivity(), ChatActivity.class);
                    i.putExtra("chatGrupo", conversaSelecionada.getGrupo());
                    startActivity(i);
                } else {
                    Intent i = new Intent(getActivity(), ChatActivity.class);
                    i.putExtra("chatContato", conversaSelecionada.getUsuarioExibicao());
                    startActivity(i);
                }
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }));
    }

}
