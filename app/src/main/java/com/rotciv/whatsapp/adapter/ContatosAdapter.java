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

public class ContatosAdapter extends RecyclerView.Adapter<ContatosAdapter.MyViewHolder> {

    Context context;
    List<Usuario> usuarios;

    public ContatosAdapter(Context context, List<Usuario> usuarios) {
        this.context = context;
        this.usuarios = usuarios;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemLista = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_contatos, viewGroup, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        Usuario usuario = usuarios.get(i);
        boolean cabecalho = usuario.getEmail().isEmpty();

        myViewHolder.nome.setText(usuario.getNome());
        myViewHolder.email.setText(usuario.getEmail());
        if (usuario.getFoto() != null)
            Glide.with(context).load(usuario.getFoto()).into(myViewHolder.fotoPerfil);
        else {
            if (!cabecalho)
                myViewHolder.fotoPerfil.setImageResource(R.drawable.padrao);
            else {
                myViewHolder.fotoPerfil.setImageResource(R.drawable.icone_grupo);
                myViewHolder.email.setVisibility(View.GONE);
            }
        }
    }

    public List<Usuario> getUsuarios(){
        return this.usuarios;
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nome, email;
        CircleImageView fotoPerfil;

        public MyViewHolder(View itemView) {
            super(itemView);

            nome = itemView.findViewById(R.id.textNome);
            email = itemView.findViewById(R.id.textEmail);
            fotoPerfil = itemView.findViewById(R.id.fotoPerfil);
        }

    }
}
