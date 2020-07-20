package com.yuvraj.passwordgeneratorandmanager;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class create_vault_dialog extends AppCompatDialogFragment {
    private EditText vault_name,vault_pass,vault_confirm_pass;
    private Button ok_button,cancel_button;
    private create_vault_dialog_listener listener;
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity(),R.style.CustomDialogTheme);
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.create_vault_dialog,null);
        builder.setView(view);
        vault_name=view.findViewById(R.id.enter_vault_name);
        vault_pass=view.findViewById(R.id.enter_vault_Pass);
        vault_confirm_pass=view.findViewById(R.id.enter_vault_pass_again);
        ok_button=view.findViewById(R.id.vault_create_ok);
        cancel_button=view.findViewById(R.id.vault_create_cancel);

        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(vault_name.getText().toString().isEmpty()==false)
                {
                    if(vault_pass.getText().toString().isEmpty()==false )
                    {
                        if(vault_confirm_pass.getText().toString().isEmpty()==false)
                        {
                            if(vault_confirm_pass.getText().toString().equals(vault_pass.getText().toString())==true)
                            {
                                listener.create_new_vault_dialog(0,0,vault_name.getText().toString(),vault_pass.getText().toString());
                            }
                            else
                            {
                                listener.create_new_vault_dialog(0,4,"","");
                            }
                        }
                        else
                        {
                            listener.create_new_vault_dialog(0,3,"","");
                        }
                    }
                    else
                    {
                        listener.create_new_vault_dialog(0,2,"","");
                    }
                }
                else
                {
                    listener.create_new_vault_dialog(0,1,"","");
                }
            }
        });
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.create_new_vault_dialog(1,0,"","");
            }
        });
        return builder.create();
    }

    interface create_vault_dialog_listener
    {
        void create_new_vault_dialog(int option,int error_code,String vault_name,String password);
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof create_vault_dialog.create_vault_dialog_listener)
        {
            listener=(create_vault_dialog.create_vault_dialog_listener)context;
        }
        else
        {
            throw new RuntimeException(context.toString()+"must implement create_vault_dialog_listener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }
}
