package com.yuvraj.passwordgeneratorandmanager;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.Map;

public class delete_vault_dialog extends AppCompatDialogFragment implements AdapterView.OnItemSelectedListener {
    private EditText vault_pass_edit_text;
    private TextView delete_vault_title_textView;
    private Button ok_button,cancel_button;
    private delete_vault_dialog_listener listener;
    private Spinner select_vault_spinner;
    int spinner_option=-1;
    public int called_from_option;
    boolean delete_dialog_start=false,open_vault_dialog_start=false;

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity(),R.style.CustomDialogTheme);
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.delete_vault_dialog,null);
        builder.setView(view);

        MainActivity main_activity=(MainActivity)getActivity();
        //for reusing delete dialog box for open vault dialog box
        delete_vault_title_textView=view.findViewById(R.id.delete_vault_title_textView);
        if(main_activity.delete_dialog_start==true && main_activity.open_vault_dialog_start==false)
        {
            main_activity.delete_dialog_start=false;
            delete_vault_title_textView.setText("Delete Vault");
            delete_dialog_start=true;
            open_vault_dialog_start=false;
        }
        else if(main_activity.open_vault_dialog_start==true && main_activity.delete_dialog_start==false)
        {
            main_activity.open_vault_dialog_start=false;
            delete_vault_title_textView.setText("Open Vault");
            delete_dialog_start=false;
            open_vault_dialog_start=true;
        }
        //for handling spinner
        ArrayList<String> vault_name_list;
        vault_name_list=main_activity.get_vault_names();
        select_vault_spinner=view.findViewById(R.id.select_vault_spinner);
        ArrayAdapter select_vault_spinner_adapter = new ArrayAdapter(main_activity.getBaseContext(),R.layout.spinner_color_layout,vault_name_list);
        select_vault_spinner_adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        select_vault_spinner.setAdapter(select_vault_spinner_adapter);
        select_vault_spinner.setOnItemSelectedListener(this);
        //for the edit text
        vault_pass_edit_text=view.findViewById(R.id.vault_pass_delete_vault);
        //handling ok button
        ok_button=view.findViewById(R.id.vault_delete_ok);
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (spinner_option < 1) {
                    listener.delete_vault_dialog(0, 1, spinner_option, vault_pass_edit_text.getText().toString(), open_vault_dialog_start, delete_dialog_start,called_from_option);
                } else if (vault_pass_edit_text.getText().toString().isEmpty() == true) {
                    listener.delete_vault_dialog(0, 2, spinner_option, vault_pass_edit_text.getText().toString(),open_vault_dialog_start,delete_dialog_start,called_from_option);
                } else {//no error
                    listener.delete_vault_dialog(0, 0, spinner_option, vault_pass_edit_text.getText().toString(),open_vault_dialog_start,delete_dialog_start,called_from_option);
                }
            }
        });
        //handling cancel button
        cancel_button=view.findViewById(R.id.vault_delete_cancel);
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.delete_vault_dialog(1,0,spinner_option,"",open_vault_dialog_start,delete_dialog_start,called_from_option);
            }
        });

        return builder.create();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Map<String,Integer> map=listener.get_color_id();
        if(i>0)
        {   ((TextView)adapterView.getChildAt(0)).setTextColor(map.get("MediumColor"));}
        else
        {   ((TextView)adapterView.getChildAt(0)).setTextColor(map.get("DeepColor"));}
        spinner_option=i;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    interface delete_vault_dialog_listener
    {
        void delete_vault_dialog(int dialog_option,int error_code,int spinner_position,String password,boolean open_vault_dialog_start,boolean delete_dialog_start,int called_from);
        Map<String,Integer> get_color_id();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof delete_vault_dialog.delete_vault_dialog_listener)
        {
            listener=(delete_vault_dialog.delete_vault_dialog_listener)context;
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
