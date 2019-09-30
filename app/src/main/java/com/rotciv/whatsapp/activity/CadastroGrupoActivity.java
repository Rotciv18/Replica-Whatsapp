package com.rotciv.whatsapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rotciv.whatsapp.R;
import com.rotciv.whatsapp.adapter.GrupoSelecionadoAdapter;
import com.rotciv.whatsapp.helper.ConfiguracaoFirebase;
import com.rotciv.whatsapp.helper.UsuarioFirebase;
import com.rotciv.whatsapp.model.Grupo;
import com.rotciv.whatsapp.model.Usuario;

import java.io.ByteArrayOutputStream;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CadastroGrupoActivity extends AppCompatActivity {

    private List<Usuario> usuarios;
    private TextView textTotalParticipantes;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private RecyclerView recyclerMembrosSelecionados;
    private CircleImageView imageGrupo;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageReference;
    private StorageReference imagemRef;
    private Grupo grupo;
    private EditText editNomeGrupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_grupo);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo grupo");
        toolbar.setSubtitle("Defina o nome");
        setSupportActionBar(toolbar);

        //Configurações iniciais
        textTotalParticipantes = findViewById(R.id.textTotalParticipantes);
        recyclerMembrosSelecionados = findViewById(R.id.recyclerMembrosGrupo);
        imageGrupo = findViewById(R.id.imageGrupo);
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        grupo = new Grupo();
        editNomeGrupo = findViewById(R.id.editNomeGrupo);

        FloatingActionButton fab = findViewById(R.id.fabAvancarCadastro);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nomeGrupo = editNomeGrupo.getText().toString();

                //Adiciona na lista de membros o usuário que já está logado
                usuarios.add(UsuarioFirebase.getDadosUsuarioLogado());

                grupo.setUsuarios(usuarios);
                grupo.setNome(nomeGrupo);
                grupo.salvar();

                Intent i = new Intent(CadastroGrupoActivity.this, ChatActivity.class);
                i.putExtra("chatGrupo", grupo);
                startActivity(i);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Recupera lista de membros passada
        if (getIntent().getExtras()!=null){
            usuarios = (List<Usuario>) getIntent().getExtras().getSerializable("membros");
            textTotalParticipantes.setText("Participantes " + usuarios.size());
        }

        //Configurar Adapter
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(getApplicationContext(), usuarios);

        //Configurar RecyclerView
        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(), LinearLayoutManager.HORIZONTAL, false
        );
        recyclerMembrosSelecionados.setLayoutManager(layoutManagerHorizontal);
        recyclerMembrosSelecionados.setHasFixedSize(true);
        recyclerMembrosSelecionados.setAdapter(grupoSelecionadoAdapter);
    }

    public void selecionarImagem(View view){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        if (i.resolveActivity(getPackageManager()) != null){
            startActivityForResult(i, SELECAO_GALERIA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            Bitmap imagem = null;

            try {
                Uri localImagemSelecionada = data.getData();
                imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);

                if (imagem != null) {
                    imageGrupo.setImageBitmap(imagem);

                    //Recuperar dados da imagem para o Firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosDaImagem = baos.toByteArray();

                    //Salvar imagem no FireBase
                    imagemRef = storageReference.child("imagens").child("grupos").child(grupo.getId() + "jpg");
                    UploadTask uploadTask = imagemRef.putBytes(dadosDaImagem);
                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful())
                                throw task.getException();
                            return imagemRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                String url = task.getResult().toString();
                                grupo.setFoto(url);

                                Toast.makeText(CadastroGrupoActivity.this, "Sucesso ao fazer upload da imagem", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(CadastroGrupoActivity.this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
