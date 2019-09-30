package com.rotciv.whatsapp.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rotciv.whatsapp.R;
import com.rotciv.whatsapp.helper.Base64Custom;
import com.rotciv.whatsapp.helper.ConfiguracaoFirebase;
import com.rotciv.whatsapp.helper.Permissao;
import com.rotciv.whatsapp.helper.UsuarioFirebase;
import com.rotciv.whatsapp.model.Usuario;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfiguracoesActivity extends AppCompatActivity {

    private String[] permicoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;

    private CircleImageView fotoPerfil;

    private StorageReference imagemRef = ConfiguracaoFirebase.getFirebaseStorage();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getAutenticacao();
    private DatabaseReference databaseReference = ConfiguracaoFirebase.getDatabase();

    private String uId = Base64Custom.codificarBase64(autenticacao.getCurrentUser().getEmail());

    private EditText editNome;

    private Usuario usuarioLogado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        //Validar permissões
        Permissao.validarPermissoes(permicoesNecessarias, this, 1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Configurar");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        editNome = findViewById(R.id.editNome);

        fotoPerfil = findViewById(R.id.fotoPerfil);

        //Recuperar Dados do Usuário
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        FirebaseUser usuario = UsuarioFirebase.getUsuarioAtual();
        Uri url = usuario.getPhotoUrl();
        if (url != null){
            Glide.with(ConfiguracoesActivity.this).load(url).into(fotoPerfil);
        } else {
            fotoPerfil.setImageResource(R.drawable.padrao);
        }

        editNome.setText(usuario.getDisplayName());


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults){
            if (permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Tu tá me zuando que você negou a porcaria da permissão." +
                "Puta que pariu. Se quiser usar esse app, precisa ativar a permissão.");
        builder.setCancelable(false);
        builder.setPositiveButton("Tá :/", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void abrirCamera(View view){
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (i.resolveActivity(getPackageManager()) != null){
            startActivityForResult(i, SELECAO_CAMERA);
        }
    }

    public void abrirGaleria(View view){
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
                switch (requestCode){
                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;
                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);
                        break;
                }
                if (imagem != null){
                    fotoPerfil.setImageBitmap(imagem);

                    //Recuperar dados da imagem para o Firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosDaImagem = baos.toByteArray();

                    //Salvar imagem no FireBase
                    imagemRef = imagemRef.child("imagens").child("perfil").child(uId).child("perfil.jpeg");
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
                                Uri url = task.getResult();
                                atualizaFotoUsuario(url);

                                    Toast.makeText(ConfiguracoesActivity.this, "Sucesso ao fazer upload da imagem", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(ConfiguracoesActivity.this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void atualizaFotoUsuario(Uri url){
        UsuarioFirebase.atualizarFotoUsuario(url);
        usuarioLogado.setFoto(url.toString());
        usuarioLogado.atualizar();
    }

    public void atualizarNomeDeUsuario(View view){
        String nome = editNome.getText().toString();
        if (!nome.isEmpty()){
            UsuarioFirebase.atualizarNomeUsuario(nome);
            String uId = UsuarioFirebase.getIdentificadorUsuario();
            usuarioLogado.setNome(nome);
            usuarioLogado.atualizar();
            Toast.makeText(ConfiguracoesActivity.this, "Nome de perfil atualizado!", Toast.LENGTH_SHORT).show();
        } else{
            Toast.makeText(ConfiguracoesActivity.this, "Preencha o campo", Toast.LENGTH_SHORT).show();
        }
    }
}
