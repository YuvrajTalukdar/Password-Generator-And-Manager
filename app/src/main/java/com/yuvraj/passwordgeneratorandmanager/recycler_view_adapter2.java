package com.yuvraj.passwordgeneratorandmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class recycler_view_adapter2 extends RecyclerView.Adapter<recycler_view_adapter2.directory_data_handler_holder> {

    Context context;
    ArrayList<File_Explorer_Activity.directory_data_handler> directory_data_handler_list;

    public recycler_view_adapter2(Context context, ArrayList<File_Explorer_Activity.directory_data_handler> directory_data_handler_list, directory_data_handler_adapter_listener listener) {
        this.context = context;
        this.directory_data_handler_list = directory_data_handler_list;
        this.onclicklistener=listener;
    }

    @Override
    public directory_data_handler_holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.file_explorer_recycler_view,parent,false);
        return new directory_data_handler_holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull directory_data_handler_holder holder, int position) {
        holder.file_folder_text_view.setText(directory_data_handler_list.get(position).file_folder_name);
        if(directory_data_handler_list.get(position).is_folder)
        {   holder.file_folder_icon_image_view.setImageResource(R.drawable.folder_icon); }
        else
        {   holder.file_folder_icon_image_view.setImageResource(R.drawable.file_icon);}
    }

    @Override
    public int getItemCount() {
        return directory_data_handler_list.size();
    }

    public interface directory_data_handler_adapter_listener
    {
        void file_folder_onclick(View v,int data_id,int position);
        void delete_file_folder(View v,int data_id,int position);
    }
    private directory_data_handler_adapter_listener onclicklistener;

    public class directory_data_handler_holder extends RecyclerView.ViewHolder{
        TextView file_folder_text_view;
        ImageView file_folder_icon_image_view;
        ImageButton delete_button;
        public directory_data_handler_holder(@NonNull View itemView) {
            super(itemView);
            delete_button=itemView.findViewById(R.id.file_folder_delete_button);
            file_folder_text_view= itemView.findViewById(R.id.file_folder_name_text_view);
            file_folder_icon_image_view= itemView.findViewById(R.id.file_folder_icon_imageView);

            file_folder_text_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onclicklistener.file_folder_onclick(view,directory_data_handler_list.get(getAdapterPosition()).id,getAdapterPosition());
                }
            });

            delete_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onclicklistener.delete_file_folder(view,directory_data_handler_list.get(getAdapterPosition()).id,getAdapterPosition());
                }
            });
        }
    }
}
