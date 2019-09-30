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
import com.rotciv.whatsapp.model.Usuario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GrupoSelecionadoAdapter extends RecyclerView.Adapter<GrupoSelecionadoAdapter.MyViewHolder> {

    Context context;
    List<Usuario> usuarios;

    public GrupoSelecionadoAdapter(Context context, List<Usuario> usuarios) {
        this.context = context;
        this.usuarios = usuarios;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemLista = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_grupo_selecionado, viewGroup, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        Usuario usuario = usuarios.get(i);

        myViewHolder.nome.setText(usuario.getNome());
        if (usuario.getFoto() != null)
            Glide.with(context).load(usuario.getFoto()).into(myViewHolder.foto);
        else {
            myViewHolder.foto.setImageResource(R.drawable.padrao);
            }
        }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView foto;
        TextView nome;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            foto = itemView.findViewById(R.id.fotoPerfil);
            nome = itemView.findViewById(R.id.textNomeMembroSelecionado);
        }
    }
}
