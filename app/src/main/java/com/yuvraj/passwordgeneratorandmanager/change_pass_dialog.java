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

public class change_pass_dialog extends AppCompatDialogFragment implements AdapterView.OnItemSelectedListener {
    private EditText current_pass_edittext,new_pass1_editText,new_pass2_editText;
    private Button ok_button,cancel_button;
    private change_pass_dialog_listener listener;
    private Spinner select_vault_spinner;
    int spinner_option=-1;

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity(),R.style.CustomDialogTheme);
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.change_pass_dialog,null);
        builder.setView(view);

        MainActivity main_activity=(MainActivity)getActivity();
        //for handling spinner
        ArrayList<String> vault_name_list;
        vault_name_list=main_activity.get_vault_names();
        select_vault_spinner=view.findViewById(R.id.change_pass_dialog_vault_spinner);
        ArrayAdapter select_vault_spinner_adapter = new ArrayAdapter(main_activity.getBaseContext(),R.layout.spinner_color_layout,vault_name_list);
        select_vault_spinner_adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        select_vault_spinner.setAdapter(select_vault_spinner_adapter);
        select_vault_spinner.setOnItemSelectedListener(this);
        //for the edit text
        current_pass_edittext=view.findViewById(R.id.current_pass_edittext_change_pass_dialog);
        new_pass1_editText=view.findViewById(R.id.new_pass_edittext1_change_pass_dialog);
        new_pass2_editText=view.findViewById(R.id.new_pass_edittext2_change_pass_dialog);
        //handling ok button
        ok_button=view.findViewById(R.id.ok_change_pass_dialog);
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(spinner_option==0)
                {   listener.change_pass_dialog(2,0,"","");}
                else if(current_pass_edittext.getText().toString().equals(""))
                {   listener.change_pass_dialog(3,0,"","");}
                else if(new_pass1_editText.getText().toString().equals(""))
                {   listener.change_pass_dialog(4,0,"","");}
                else if(new_pass2_editText.getText().toString().equals(""))
                {   listener.change_pass_dialog(5,0,"","");}
                else if(!new_pass1_editText.getText().toString().equals(new_pass2_editText.getText().toString()))
                {   listener.change_pass_dialog(6,0,"","");}
                else
                {   listener.change_pass_dialog(0,spinner_option,current_pass_edittext.getText().toString(),new_pass1_editText.getText().toString());}
            }
        });
        //handling cancel button
        cancel_button=view.findViewById(R.id.cancel_change_pass_dialog);
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.change_pass_dialog(1,0,"","");
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

    interface change_pass_dialog_listener
    {
        void change_pass_dialog(int error_code,int spinner_position,String current_password,String new_password);
        Map<String,Integer> get_color_id();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof change_pass_dialog.change_pass_dialog_listener)
        {
            listener=(change_pass_dialog.change_pass_dialog_listener)context;
        }
        else
        {
            throw new RuntimeException(context.toString()+"must implement change_vault_dialog_listener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }
}
