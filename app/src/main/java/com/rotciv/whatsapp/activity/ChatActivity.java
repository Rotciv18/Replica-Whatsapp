package com.rotciv.whatsapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rotciv.whatsapp.R;
import com.rotciv.whatsapp.adapter.MensagensAdapter;
import com.rotciv.whatsapp.helper.Base64Custom;
import com.rotciv.whatsapp.helper.ConfiguracaoFirebase;
import com.rotciv.whatsapp.helper.UsuarioFirebase;
import com.rotciv.whatsapp.model.Conversa;
import com.rotciv.whatsapp.model.Grupo;
import com.rotciv.whatsapp.model.Mensagem;
import com.rotciv.whatsapp.model.Usuario;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView textNome;
    private CircleImageView circleFoto;
    private Usuario usuarioDestinatario;
    private Usuario usuarioRemetente;
    private EditText editMensagem;
    private RecyclerView recyclerMensagens;
    private MensagensAdapter adapter;
    private List<Mensagem> mensagens = new ArrayList<>();
    private DatabaseReference mensagensRef;
    private DatabaseReference database;
    private StorageReference storage;
    private ChildEventListener childEventListenerMensagens;
    private ImageView imageCamera;
    private static final int SELECAO_CAMERA = 100;
    private Grupo grupo;

    //identificadores de usuarios remetente e destinatário
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        //Configurações iniciais
        textNome = findViewById(R.id.textContato);
        circleFoto = findViewById(R.id.circleImageFoto);
        editMensagem = findViewById(R.id.editMensagem);
        recyclerMensagens = findViewById(R.id.recyclerMensagens);
        imageCamera = findViewById(R.id.imageCamera);
        usuarioRemetente = UsuarioFirebase.getDadosUsuarioLogado();

        //Recuperar dados do usuário
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            if (bundle.containsKey("chatGrupo")){
                /********Bloco para conversas de grupo********/
                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                textNome.setText(grupo.getNome());
                idUsuarioDestinatario = grupo.getId();

                String foto = grupo.getFoto();
                if (foto != null){
                    Glide.with(ChatActivity.this)
                            .load(foto)
                            .into(circleFoto);
                } else {
                    circleFoto.setImageResource(R.drawable.padrao);
                }
                /********Bloco para conversas de grupo********/

            } else {

                /********Bloco para conversas privadas********/
                usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
                textNome.setText(usuarioDestinatario.getNome());

                String foto = usuarioDestinatario.getFoto();
                if (foto != null) {
                    Glide.with(ChatActivity.this)
                            .load(foto)
                            .into(circleFoto);
                } else {
                    circleFoto.setImageResource(R.drawable.padrao);
                }
                //Recuperar dados de usuario destinatário
                idUsuarioDestinatario = Base64Custom.codificarBase64(usuarioDestinatario.getEmail());
            }
            /********Bloco para conversas privadas********/
        }

        //Recupera dados de usuario remetente
        idUsuarioRemetente = usuarioRemetente.getId();

        //Recupera referencia Firebase Storage
        storage = ConfiguracaoFirebase.getFirebaseStorage();

        //Recuperar mensagens do chat
        database = ConfiguracaoFirebase.getDatabase();
        mensagensRef = database.child("mensagens").child(idUsuarioRemetente).child(idUsuarioDestinatario);

        //Configurar Adapter
        adapter = new MensagensAdapter(mensagens, getApplicationContext());
        //Configurar RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagens.setLayoutManager(layoutManager);
        recyclerMensagens.setHasFixedSize(true);
        recyclerMensagens.setAdapter(adapter);


    }

    public void enviarMensagem(View view){
        String textoMensagem = editMensagem.getText().toString();
        if (usuarioDestinatario != null) { //Mensagem privada
            if (!textoMensagem.isEmpty()) {
                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario(idUsuarioRemetente);
                mensagem.setMensagem(textoMensagem);

                //Salvar mensagem para o remetente
                salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                //Salvar mensagem para o destinatario
                salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                //Salvar conversa remetente
                salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, usuarioDestinatario, mensagem, false);

                //Salvar conversa destinatário
                salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, usuarioRemetente, mensagem, false);

                editMensagem.setText("");
            } else {
                Toast.makeText(ChatActivity.this, "Nenhuma mensagem digitada", Toast.LENGTH_SHORT).show();
            }
        } else { //Mensagem de grupos
            for (Usuario usuario : grupo.getUsuarios()){
                String idRemetenteGrupo = Base64Custom.codificarBase64(usuario.getEmail());
                String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                mensagem.setMensagem(textoMensagem);
                mensagem.setNome(usuarioRemetente.getNome());

                //Salvar mensagem para o membro
                salvarMensagem(idRemetenteGrupo, idUsuarioDestinatario, mensagem);

                //Salvar a conversa
                salvarConversa(idRemetenteGrupo, idUsuarioDestinatario, usuarioDestinatario, mensagem, true);
            }
        }
    }

    private void salvarMensagem(String idRemetente, String idDestinatario, Mensagem mensagem){

        DatabaseReference mensagensRef = ConfiguracaoFirebase.getDatabase().child("mensagens");

        mensagensRef.child(idRemetente).child(idDestinatario).push().setValue(mensagem);
    }

    private void recuperarMensagens(){

        childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Mensagem mensagem = dataSnapshot.getValue(Mensagem.class);
                mensagens.add(mensagem);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void tirarFoto(View view){
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (i.resolveActivity(getPackageManager()) != null){
            startActivityForResult(i, SELECAO_CAMERA);
        }
    }

    private void salvarConversa(String idRemetente, String idDestinatario, Usuario usuarioExibicao, Mensagem mensagem, boolean isGroup){

        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente(idRemetente);
        conversaRemetente.setIdDestinatario(idDestinatario);
        conversaRemetente.setUltimaMensagem(mensagem.getMensagem());
        conversaRemetente.setUsuarioExibicao(usuarioDestinatario);

        if (!isGroup) { //Conversa normal

            conversaRemetente.setIsGroup("false");
            conversaRemetente.setUsuarioExibicao(usuarioExibicao);

        } else { //Conversa em grupo
            conversaRemetente.setIsGroup("true");
            conversaRemetente.setGrupo(grupo);
        }

        conversaRemetente.salvar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){

            Bitmap imagem = null;

            try {
                imagem = (Bitmap) data.getExtras().get("data");

                if (imagem != null){
                    //Recuperar dados da imagem para o Firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosDaImagem = baos.toByteArray();

                    //Recuperar referencia do Firebase
                    String nomeImagem = UUID.randomUUID().toString();
                    final StorageReference imagemRef = storage.child("imagens").child("fotos").child(idUsuarioRemetente).child(nomeImagem);

                    UploadTask uploadTask = imagemRef.putBytes(dadosDaImagem);
                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()){
                                throw task.getException();
                            }
                            return imagemRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            String url = task.getResult().toString();

                            Mensagem mensagem = new Mensagem();
                            mensagem.setIdUsuario(idUsuarioRemetente);
                            mensagem.setMensagem("");
                            mensagem.setImagem(url);

                            //Salvar imagem para Remetente
                            salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                            //Salvar imagem para Destinatário
                            salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                            Toast.makeText(ChatActivity.this, "Sucesso ao enviar imagem", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener(childEventListenerMensagens);
    }
}
