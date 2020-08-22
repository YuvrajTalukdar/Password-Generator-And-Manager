package com.yuvraj.passwordgeneratorandmanager;

import android.content.Context;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Key_Generator_Fragment extends Fragment {

    private Key_Generator_Fragment_Listener listener;
    private EditText password_edittext;
    private SeekBar pass_length_seekbar;
    private TextView pass_length_textview;
    private Switch switch_az,switch_AZ,switch_09,switch_SPLCHAR;
    private LinearLayout layout1;
    public Key_Generator_Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public interface Key_Generator_Fragment_Listener
    {
        void on_copied_pass_sent(CharSequence pass);
        void add_to_vault_button(String generated_pass);
        Map<String,Integer> get_color_id();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_key_generator, container, false);

        password_edittext = v.findViewById(R.id.password_edit_text);
        ImageButton copy_button = v.findViewById(R.id.copy_button);
        Button generator_button = v.findViewById(R.id.generate_pass_button);

        Button add_to_vault_button = v.findViewById((R.id.add_to_vault_button));
        add_to_vault_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.add_to_vault_button(password_edittext.getText().toString());
            }
        });

        pass_length_seekbar=v.findViewById(R.id.pass_length_seekbar);
        pass_length_seekbar.setProgress(15);
        pass_length_textview=v.findViewById(R.id.passlength_textview);
        pass_length_textview.setText("7");
        switch_09=v.findViewById(R.id.switch_09);
        switch_AZ=v.findViewById(R.id.switch_AZ);
        switch_az=v.findViewById(R.id.switch_az);
        switch_SPLCHAR=v.findViewById(R.id.switch_SPLCHAR);
        layout1=v.findViewById(R.id.generator_fragment_layout);

        copy_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CharSequence copied_pass=password_edittext.getText();
                listener.on_copied_pass_sent(copied_pass);
            }
        });

        pass_length_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                pass_length_textview.setText(""+progress/2+"");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        generator_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                password_edittext.setText(generate_password(pass_length_seekbar.getProgress()/2,switch_AZ.isChecked(),switch_az.isChecked(),switch_09.isChecked(),switch_SPLCHAR.isChecked()));
            }
        });
        return v;
    }

    private String generate_password(int length,boolean c_letter,boolean s_letters,boolean numbers,boolean spl_char)
    {
        StringBuffer string_buff = new StringBuffer();
        if(c_letter==true || s_letters==true || numbers==true || spl_char==true) {
            while (length != 0) {
                byte[] array = new byte[256];
                new Random().nextBytes(array);
                String random_string = new String(array, Charset.forName("UTF-8"));
                int a;
                for (a = 0; a < random_string.length(); a++) {

                    char ch = random_string.charAt(a);

                    if ((((ch >= 'a') && (ch <= 'z') && (s_letters == true))
                            || ((ch >= 'A') && (ch <= 'Z') && (c_letter == true))
                            || ((ch >= '0') && (ch <= '9') && (numbers == true))
                            || ((ch >= '!') && (ch <= '/') && (spl_char == true))
                    )
                            && (length > 0)) {

                        string_buff.append(ch);
                        length--;
                    }
                }
            }
        }
        else
        {
            Map<String,Integer> map=listener.get_color_id();
            String medium_color=String.format("#%06X", (0xFFFFFF & map.get("MediumColor")));
            map.clear();
            Snackbar.make(layout1, Html.fromHtml("<font color="+medium_color+">Please select some character....</font>"),Snackbar.LENGTH_LONG).show();
        }
        return string_buff.toString();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof Key_Generator_Fragment_Listener)
        {
            listener=(Key_Generator_Fragment_Listener)context;
        }
        else
        {
            throw new RuntimeException(context.toString()+"must implement Key_Generator_Fragment_Listener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }
}