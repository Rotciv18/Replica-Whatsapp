package com.rotciv.whatsapp.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permissao {

    public static boolean validarPermissoes(String[] permissoes, Activity activity, int requestCode){
        if (Build.VERSION.SDK_INT >= 23){
            List<String> listaPermissoes = new ArrayList<>();

            for (String permissao : permissoes){
                Boolean temPermissao = ContextCompat.checkSelfPermission(activity, permissao) == PackageManager.PERMISSION_GRANTED;
                if (!temPermissao) listaPermissoes.add(permissao);
            }

            if (listaPermissoes.isEmpty()) return true;

            String[] novasPermissoes = new String[listaPermissoes.size()];
            listaPermissoes.toArray(novasPermissoes);

            //Solicitar Permissão
            ActivityCompat.requestPermissions(activity, novasPermissoes, requestCode);
        }

        return true;
    }
}
