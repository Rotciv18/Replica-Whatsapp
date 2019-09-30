package com.rotciv.whatsapp.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Adapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.rotciv.whatsapp.R;
import com.rotciv.whatsapp.adapter.ContatosAdapter;
import com.rotciv.whatsapp.fragments.ContatosFragment;
import com.rotciv.whatsapp.fragments.ConversasFragment;
import com.rotciv.whatsapp.helper.ConfiguracaoFirebase;
import com.rotciv.whatsapp.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class PrincipalActivity extends AppCompatActivity {

    FirebaseAuth autenticacao = ConfiguracaoFirebase.getAutenticacao();
    DatabaseReference database = ConfiguracaoFirebase.getDatabase();

    private MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("WhatsApp");
        setSupportActionBar(toolbar);

        searchView = findViewById(R.id.materialSearchPrincipal);

        //configurar abas
        final FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("Conversas", ConversasFragment.class)
                .add("Contatos", ContatosFragment.class)
                .create());

        final ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = findViewById(R.id.viewPagerTab);
        viewPagerTab.setViewPager(viewPager);


        //Listener para a caixa de texto
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                //Verifica se está na aba Conversas ou Contatos
                switch (viewPager.getCurrentItem()){
                    case 0:

                        ConversasFragment conversasFragment = (ConversasFragment) adapter.getPage(0);
                        if (!newText.equals("") && newText!=null){
                            conversasFragment.pesquisarConversas(newText.toLowerCase());
                        } else {
                            conversasFragment.recarregarConversas();
                        }
                        break;

                    case 1:

                        ContatosFragment contatosFragment = (ContatosFragment) adapter.getPage(1);
                        if (!newText.equals("") && newText!=null){
                            contatosFragment.pesquisarContatos(newText.toLowerCase());
                        } else {
                            contatosFragment.recarregarContatos();
                        }
                        break;
                }
                return true;
            }
        });

        //Listener para o SearchView
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                ConversasFragment fragment = (ConversasFragment) adapter.getPage(0); //0 para a primeira fragment

                fragment.recarregarConversas();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        //Configura botao de pesquisa
        MenuItem item = menu.findItem(R.id.menuPesquisa);
        searchView.setMenuItem(item);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sair:
                deslogarUsuario();
                break;

            case R.id.menuPesquisa:
                //pesquisa alguma merda
                break;

            case R.id.menuConfiguracoes:
                startActivity(new Intent(PrincipalActivity.this, ConfiguracoesActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void deslogarUsuario(){
        try {
            autenticacao.signOut();
            finish();
            startActivity(new Intent(this, MainActivity.class));
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
