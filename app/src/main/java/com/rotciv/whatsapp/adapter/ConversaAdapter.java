package com.rotciv.whatsapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rotciv.whatsapp.R;
import com.rotciv.whatsapp.model.Conversa;
import com.rotciv.whatsapp.model.Grupo;
import com.rotciv.whatsapp.model.Usuario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversaAdapter extends RecyclerView.Adapter<ConversaAdapter.MyViewHolder> {

    Context context;
    List<Conversa> conversas;

    public ConversaAdapter(Context context, List<Conversa> conversas) {
        this.context = context;
        this.conversas = conversas;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemLista = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_conversas, viewGroup, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        Conversa conversa = conversas.get(i);
        Usuario usuario = conversa.getUsuarioExibicao();

        if (conversa.getIsGroup().equals("true")){
            Grupo grupo = conversa.getGrupo();
            myViewHolder.nomeDestinatario.setText(grupo.getNome());
            myViewHolder.mensagemDestinatario.setText(conversa.getUltimaMensagem());
            if (grupo.getFoto() != null) {
                Glide.with(context).load(grupo.getFoto()).into(myViewHolder.imagemDestinatario);
            } else {
                myViewHolder.imagemDestinatario.setImageResource(R.drawable.padrao);
            }

        } else {
            myViewHolder.nomeDestinatario.setText(usuario.getNome());
            myViewHolder.mensagemDestinatario.setText(conversa.getUltimaMensagem());
            if (usuario.getFoto() != null) {
                Glide.with(context).load(usuario.getFoto()).into(myViewHolder.imagemDestinatario);
            } else {
                myViewHolder.imagemDestinatario.setImageResource(R.drawable.padrao);
            }
        }
    }

    public List<Conversa> getConversas(){
        return this.conversas;
    }

    @Override
    public int getItemCount() {
        return conversas.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imagemDestinatario;
        TextView nomeDestinatario;
        TextView mensagemDestinatario;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imagemDestinatario = itemView.findViewById(R.id.fotoPerfil);
            nomeDestinatario = itemView.findViewById(R.id.nomeDestinatario);
            mensagemDestinatario = itemView.findViewById(R.id.mensagemDestinatario);
        }
    }
}
