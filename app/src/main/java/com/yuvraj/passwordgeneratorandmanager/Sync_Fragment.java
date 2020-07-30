package com.yuvraj.passwordgeneratorandmanager;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class Sync_Fragment extends Fragment {
    public boolean is_signed_in=false;
    public Button sign_in_button;
    private Button sync_now_button;
    private Button perform_local_backup;
    private Button load_backup_file;
    private Switch auto_sync_enable;
    public TextView signed_in_account_name_textView;
    private MainActivity main_activity;
    sync_fragment_listener listener;

    public Sync_Fragment()
    {}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    interface sync_fragment_listener
    {
        GoogleSignInAccount get_sign_in_status();
        void sign_in_with_google();
        void sign_out();
        void local_backup_restore(int start_code);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sync, container, false);
        //initialize all the UI components
        main_activity=(MainActivity)getActivity();

        signed_in_account_name_textView=v.findViewById(R.id.account_name_TextView);

        sign_in_button=v.findViewById(R.id.Sign_In_With_Google_Account);
        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(is_signed_in==false)
                {   listener.sign_in_with_google();}
                else
                {   listener.sign_out();}
            }
        });
        if(main_activity.is_signed_in==false)
        {
            sign_in_button.setText(R.string.sign_in_button_text);
            signed_in_account_name_textView.setText(R.string.not_signed_in);
        }
        else
        {
            sign_in_button.setText(R.string.sign_out_button_text);
            GoogleSignInAccount account=listener.get_sign_in_status();
            signed_in_account_name_textView.setText(account.getEmail());
        }
        is_signed_in=main_activity.is_signed_in;

        sync_now_button=v.findViewById(R.id.sync_now_button);
        sync_now_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        perform_local_backup=v.findViewById(R.id.Perform_Local_Backup);
        perform_local_backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.local_backup_restore(1);
            }
        });

        load_backup_file=v.findViewById(R.id.Load_Backup_file);
        load_backup_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.local_backup_restore(2);
            }
        });

        auto_sync_enable=v.findViewById(R.id.Auto_sync_switch);
        //check if the app is already signed in
        //check for rest of the stuff
        return v;
    }

    public void check_sign_in_status(String str)
    {
        if(main_activity.is_signed_in)
        {
            is_signed_in=true;
            sign_in_button.setText(R.string.sign_out_button_text);
            signed_in_account_name_textView.setText(str);
        }
        else
        {
            is_signed_in=false;
            sign_in_button.setText(R.string.sign_in_button_text);
            signed_in_account_name_textView.setText(R.string.not_signed_in);
        }
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof Vault_Fragment.Vault_Fragment_Listener)
        {
            listener=(Sync_Fragment.sync_fragment_listener) context;
        }
        else
        {   throw new RuntimeException(context.toString()+"must implement Sync_Fragment_Listener");}
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }

}