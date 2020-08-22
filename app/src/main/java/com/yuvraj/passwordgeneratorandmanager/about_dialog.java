package com.yuvraj.passwordgeneratorandmanager;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class about_dialog extends AppCompatDialogFragment {

    private about_dialog.about_dialog_listener listener;
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity(),R.style.CustomDialogTheme);
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.about_dialog,null);
        builder.setView(view);
        Button ok_button=view.findViewById(R.id.about_ok_button);
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.about_dialog_ok();
            }
        });

        return builder.create();
    }

    interface about_dialog_listener
    {
        void about_dialog_ok();
    }
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof about_dialog.about_dialog_listener)
        {
            listener=(about_dialog.about_dialog_listener)context;
        }
        else
        {
            throw new RuntimeException(context.toString()+"must implement delete_vault_dialog_listener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }
}
