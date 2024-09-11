package com.example.showattendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {
    private List<Student> studentList;

    public StudentAdapter(List<Student> studentList) {
        this.studentList = studentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.textViewName.setText(student.getName());
        holder.textViewGPS.setText(student.getGpsPoints());
        holder.textViewDistance.setText(String.valueOf(student.getDistance()));
        holder.textViewCheckInTime.setText(student.getCheckInTime());
        holder.textViewAttendance.setText(student.getAttendance());
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewName;
        public TextView textViewGPS;
        public TextView textViewDistance;
        public TextView textViewCheckInTime;
        public TextView textViewAttendance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewGPS = itemView.findViewById(R.id.textViewGpsPoints);
            textViewDistance = itemView.findViewById(R.id.textViewDistance);
            textViewCheckInTime = itemView.findViewById(R.id.textViewCheckInTime);
            textViewAttendance = itemView.findViewById(R.id.textViewAttendance);
        }
    }
}


