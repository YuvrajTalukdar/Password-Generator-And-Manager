package com.yuvraj.passwordgeneratorandmanager;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Vault_Fragment extends Fragment {

    private RecyclerView vault_item_recycler_view;
    private LinearLayoutManager linear_layout;
    private recycler_view_adapter recycler_view_adapter_obj;
    private Button open_close_vault;
    private Vault_Fragment_Listener listener;
    private MainActivity main_activity;
    private Button add_to_vault;
    private Button delete_vault;
    private ArrayList<vault_data> vault_data_list;
    private TextView status_textview;
    private EditText search_editText;
    private ImageButton searchButton;
    private int focus_semaphore=1;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    public Vault_Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    interface Vault_Fragment_Listener
    {
        void copy_password_button_onclick(String copy_password);
        void copy_id_button_onclick(String copy_id);
        void delete_data_button_onclick(int data_id,int position);
        void create_new_vault_dialog();
        void delete_vault_dialog();
        void open_vault_dialog(int called_from);
        void close_vault();
        void enter_new_password_dialog(int called_from_id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_vault, container, false);

        add_to_vault = v.findViewById(R.id.add_to_vault_button2);
        add_to_vault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.enter_new_password_dialog(1);
            }
        });

        Button create_new_vault_button = v.findViewById(R.id.create_vault_button);
        create_new_vault_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.create_new_vault_dialog();
            }
        });

        delete_vault = v.findViewById(R.id.delete_vault_button);
        delete_vault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             listener.delete_vault_dialog();
            }
        });

        main_activity=(MainActivity)getActivity();
        open_close_vault=v.findViewById(R.id.open_close_vault_button);
        open_close_vault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(main_activity.is_a_vault_open())
                {   listener.close_vault();}
                else
                {   listener.open_vault_dialog(1);}
            }
        });

        vault_item_recycler_view=v.findViewById(R.id.recyclerView);
        vault_data_list = new ArrayList();
        vault_data_list.clear();
        vault_data_list=(ArrayList<vault_data>) main_activity.vault_data_list.clone();
        recycler_view_adapter_obj=new recycler_view_adapter(v.getContext(), vault_data_list,new recycler_view_adapter.vault_data_adapter_listener()
        {
            @Override
            public void copy_password_button_onclick(View v, String copy_password)
            {
                listener.copy_password_button_onclick(copy_password);
            }
            @Override
            public void copy_id_button_onclick(View v,String copy_id)
            {
                listener.copy_id_button_onclick(copy_id);
            }
            @Override
            public void delete_data_button_onclick(View v,int data_id,int position) {
                 listener.delete_data_button_onclick(data_id,position);
            }
        });
        linear_layout=new LinearLayoutManager(v.getContext());
        vault_item_recycler_view.setAdapter(recycler_view_adapter_obj);
        vault_item_recycler_view.setLayoutManager(linear_layout);

        search_editText=v.findViewById(R.id.search_editText);
        searchButton=v.findViewById(R.id.search_button);
        status_textview=v.findViewById(R.id.status_textview);
        InputMethodManager imm = (InputMethodManager) main_activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(search_editText.isFocused())
                {
                    focus_semaphore++;
                    search_editText.setText("");
                    search_editText.clearFocus();
                    searchButton.setImageResource(R.drawable.search_icon);
                    imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                }
                else
                {
                    focus_semaphore--;
                    search_editText.requestFocus();
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                    searchButton.setImageResource(R.drawable.cancel_icon);
                }
            }
        });
        search_editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(focus_semaphore==1)
                {
                    focus_semaphore--;
                    search_editText.requestFocus();
                    searchButton.setImageResource(R.drawable.cancel_icon);
                    return true;
                }
                return false;
            }
        });
        search_editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<Integer> result=new ArrayList();
                result.clear();
                vault_data_list.clear();
                vault_data_list.addAll(main_activity.vault_data_list);
                for(int a=vault_data_list.size()-1;a>=0;a--)
                {
                    if(!vault_data_list.get(a).account_type.toUpperCase().contains(s.toString().toUpperCase()) && !vault_data_list.get(a).account_id.toUpperCase().contains(s.toString().toUpperCase()))
                    {   vault_data_list.remove(a);}
                }
                recycler_view_adapter_obj.notifyDataSetChanged();
            }
        });

        if(!main_activity.is_a_vault_open())
        {
            status_textview.setText(R.string.vault_not_opened_status);
            status_textview.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.GONE);
            search_editText.setVisibility(View.GONE);
        }
        else if(main_activity.is_a_vault_open() && vault_data_list.isEmpty())
        {
            status_textview.setText(R.string.empty_vault_status);
            status_textview.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.VISIBLE);
            search_editText.setVisibility(View.VISIBLE);
        }

        if(main_activity.pending_data_entry==true)
        {
            main_activity.pending_data_entry=false;
            add_single_data_to_recycle_view(main_activity.pending_data);
            delete_vault.setEnabled(false);
        }

        enable_add_button(main_activity.is_a_vault_open());
        if(main_activity.is_a_vault_open())
        {   open_close_vault.setText("Close Vault");}
        else
        {
            open_close_vault.setText("Open Vault");

            enable_add_button(false);
            enable_delete_vault_button(true);
            if(vault_data_list!=null && vault_data_list.size()>0)
            {   vault_data_list.clear();}
            empty_recycler_view();
        }
        return v;
    }

    public void remove_item_from_recycler_view(int data_id,int position)
    {
        for (int a = 0; a < vault_data_list.size(); a++)
        {
            if (vault_data_list.get(a).id == data_id)
            {
                vault_data_list.remove(a);
                recycler_view_adapter_obj.notifyItemRemoved(position);
                break;
            }
        }
        if(vault_data_list.isEmpty())
        {
            status_textview.setText(R.string.empty_vault_status);
            status_textview.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.VISIBLE);
            search_editText.setVisibility(View.VISIBLE);
        }
    }

    public void add_multiple_data_to_recyclerview(ArrayList<vault_data> data_list)
    {
        if(data_list.isEmpty()) {
            status_textview.setText(R.string.empty_vault_status);
            status_textview.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.VISIBLE);
            search_editText.setVisibility(View.VISIBLE);
        }
        else
        {
            status_textview.setVisibility(View.GONE);
            searchButton.setVisibility(View.VISIBLE);
            search_editText.setVisibility(View.VISIBLE);
        }
        vault_data_list.addAll(0,data_list);
        recycler_view_adapter_obj.notifyItemRangeInserted(0,data_list.size());
    }

    public void add_single_data_to_recycle_view(vault_data data)
    {
        status_textview.setVisibility(View.GONE);
        vault_data_list.add(0,data);
        recycler_view_adapter_obj.notifyItemInserted(0);
        vault_item_recycler_view.scrollToPosition(0);
    }

    public void empty_recycler_view()
    {
        status_textview.setText(R.string.vault_not_opened_status);
        status_textview.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.GONE);
        search_editText.setVisibility(View.GONE);
        vault_data_list.clear();
        recycler_view_adapter_obj.notifyDataSetChanged();
    }

    public void enable_delete_vault_button(boolean status)
    {
        if(status==true)
        {   delete_vault.setEnabled(true);}
        else
        {   delete_vault.setEnabled(false);}
    }
    public void enable_add_button(boolean status)
    {
        if(status==true)
        {   add_to_vault.setEnabled(true); }
        else
        {   add_to_vault.setEnabled(false); }
    }

    public void set_open_close_vault_button_text(String str)
    {   open_close_vault.setText(str);}

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof Vault_Fragment.Vault_Fragment_Listener)
        {
            listener=(Vault_Fragment.Vault_Fragment_Listener)context;
        }
        else
        {
            throw new RuntimeException(context.toString()+"must implement Vault_Fragment_Listener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }
}

