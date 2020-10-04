package com.yuvraj.passwordgeneratorandmanager;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;

public class enter_new_pass_dialog extends AppCompatDialogFragment {
    private enter_new_pass_dialog_listener listener;
    EditText enter_account_id_EditText,account_type_EditText,enter_password_EditText;
    public int called_from;
    public String generated_pass;
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity(),R.style.CustomDialogTheme);
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.enter_new_pass_dialog,null);
        builder.setView(view);

        ImageButton paste_id=view.findViewById(R.id.id_paste);
        paste_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.paste_id();
            }
        });

        final ImageButton paste_account_type=view.findViewById(R.id.account_type_paste);
        paste_account_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.paste_account_type();
            }
        });

        ImageButton paste_password_type=view.findViewById(R.id.password_paste);
        paste_password_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.paste_password();
            }
        });

        enter_account_id_EditText = view.findViewById(R.id.enter_account_id_EditText);
        account_type_EditText = view.findViewById(R.id.account_type_EditText);
        enter_password_EditText = view.findViewById(R.id.enter_password_EditText);
        if(called_from==0)
        {   enter_password_EditText.setText(generated_pass);}

        Button ok_button = view.findViewById(R.id.enter_new_password_dialog_ok);
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.enter_new_pass_dialog_ok_button(enter_account_id_EditText.getText().toString(),account_type_EditText.getText().toString(),enter_password_EditText.getText().toString(),called_from);
            }
        });

        Button cancel_button = view.findViewById(R.id.enter_new_password_dialog_cancel);
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.enter_new_pass_dialog_cancel();
            }
        });

        return builder.create();
    }

    void set_enter_account_id_EditText(String str)
    {   enter_account_id_EditText.setText(str);}

    void set_enter_account_type_EditText(String str)
    {   account_type_EditText.setText(str);}

    void set_enter_account_password_EditText(String str)
    {   enter_password_EditText.setText(str);}

    interface enter_new_pass_dialog_listener
    {
        void enter_new_pass_dialog_ok_button(String id,String type,String pass,int called_from_id);
        void enter_new_pass_dialog_cancel();
        void paste_id();
        void paste_account_type();
        void paste_password();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof delete_vault_dialog.delete_vault_dialog_listener)
        {
            listener=(enter_new_pass_dialog.enter_new_pass_dialog_listener)context;
        }
        else
        {
            throw new RuntimeException(context.toString()+"must implement enter_new_pass_dialog_listener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }
}
