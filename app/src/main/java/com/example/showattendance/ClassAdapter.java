package com.example.showattendance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    ArrayList<ClassItem> classItems;
    Context context;

    public ClassAdapter(Context context, ArrayList<ClassItem> classItems) {
        this.classItems = classItems;
        this.context = context;
    }



    public static class ClassViewHolder extends RecyclerView.ViewHolder{
        TextView className;
        TextView CRNs;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            className = itemView.findViewById(R.id.tvClassName);
            CRNs = itemView.findViewById(R.id.tv_CRNs);

        }
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.class_item, parent, false);
        return new ClassViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        holder.className.setText(classItems.get(position).getClassName());
//        holder.CRNs.setText(classItems.get(position).getCRNs());
        holder.CRNs.setText(String.valueOf(classItems.get(position).getCRNs()));
    }

    @Override
    public int getItemCount() {
        return classItems.size();
    }
}
