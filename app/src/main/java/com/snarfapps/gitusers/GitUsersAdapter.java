package com.snarfapps.gitusers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.snarfapps.gitusers.models.User;

import java.util.ArrayList;
import java.util.List;

public class GitUsersAdapter extends RecyclerView.Adapter<GitUsersAdapter.GitUserViewHolder> {

    private List<User> data;

    public GitUsersAdapter(List<User> data) {
        this.data = data;
    }

    public List<User> getData(){
        return this.data;
    }

    @NonNull
    @Override
    public GitUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user,parent,false);

        return new GitUserViewHolder(v, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull GitUsersAdapter.GitUserViewHolder holder, int position) {
            holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size() ;
    }


    public static class GitUserViewHolder extends RecyclerView.ViewHolder{

        TextView tvUsername;
        ImageView ivAvatar;
        Context holderContext;
        public GitUserViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);

            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            this.holderContext = ctx;

        }
        public void bind(User u){
            tvUsername.setText(u.username);
            Log.e("Setting user text", ""+u.username);
            Glide.with(holderContext).load(u.avatarUrl).into(ivAvatar);
        }
    }
}
