package com.yuvraj.passwordgeneratorandmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class recycler_view_adapter extends RecyclerView.Adapter<recycler_view_adapter.vault_data_holder> {

    Context context;
    List<vault_data> vault_data_list;

    public recycler_view_adapter(Context context, List<vault_data> vault_data_list,vault_data_adapter_listener listener) {
        this.context = context;
        this.vault_data_list = vault_data_list;
        this.onclicklistener=listener;
    }

    @Override
    public vault_data_holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.vault_item_recycler_view,parent,false);
        return new vault_data_holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull vault_data_holder holder, int position) {
        holder.account_type.setText(vault_data_list.get(position).account_type);
        holder.account_id.setText(vault_data_list.get(position).account_id);
        holder.password.setText(vault_data_list.get(position).account_password);
        holder.date_of_modification.setText(vault_data_list.get(position).date_of_modification);
    }

    @Override
    public int getItemCount() {
        return vault_data_list.size();
    }

    public interface vault_data_adapter_listener
    {
        void copy_password_button_onclick(View v,String copy_password);
        void copy_id_button_onclick(View v,String copy_id);
        void delete_data_button_onclick(View v,int data_id,int position);
    }
    public vault_data_adapter_listener onclicklistener;

    public class vault_data_holder extends RecyclerView.ViewHolder{
        TextView account_type,account_id,password,date_of_modification;
        Button copy_id,copy_password,delete_button;
        public vault_data_holder(@NonNull View itemView) {
            super(itemView);
            account_type=(TextView)itemView.findViewById(R.id.account_type_textview);
            account_id=(TextView)itemView.findViewById(R.id.account_id_textview);
            password=(TextView)itemView.findViewById(R.id.account_password_textview);
            date_of_modification=(TextView)itemView.findViewById(R.id.date_of_modification_textview);
            copy_id=(Button)itemView.findViewById((R.id.copy_id));
            copy_password=(Button)itemView.findViewById(R.id.copy_password);
            delete_button=(Button)itemView.findViewById(R.id.delete_data);

            copy_id.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v)
                {
                    onclicklistener.copy_id_button_onclick(v,vault_data_list.get(getAdapterPosition()).account_id);
                }
            });
            copy_password.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    onclicklistener.copy_password_button_onclick(v,vault_data_list.get(getAdapterPosition()).account_password);
                }
            });
            delete_button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v)
                {
                    onclicklistener.delete_data_button_onclick(v,vault_data_list.get(getAdapterPosition()).id,getAdapterPosition());
                }
            });
        }
    }
}
