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

public class new_folder_dialog extends AppCompatDialogFragment{
    private EditText edit_text;
    private new_folder_dialog_listener listener;

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity(),R.style.CustomDialogTheme);
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.new_folder_dialog,null);
        builder.setView(view);

        edit_text=view.findViewById(R.id.new_folder_dialog_edit_text);

        Button ok_button = view.findViewById(R.id.new_folder_dialog_ok_button);
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.on_new_folder_name_send(0,edit_text.getText().toString());
            }
        });

        Button cancel_button = view.findViewById(R.id.new_folder_dialog_cancel_button);
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.on_new_folder_name_send(1,null);
            }
        });

        return builder.create();
    }

    interface new_folder_dialog_listener
    {   void on_new_folder_name_send(int option,String name);}

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof new_folder_dialog.new_folder_dialog_listener)
        {   listener=(new_folder_dialog.new_folder_dialog_listener)context; }
        else
        {   throw new RuntimeException(context.toString()+"must implement new_folder_dialog_listener"); }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }
}
