package com.snarfapps.gitusers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.snarfapps.gitusers.models.User;

import java.util.ArrayList;
import java.util.List;

public class GitUsersAdapter extends RecyclerView.Adapter<GitUsersAdapter.GitUserViewHolder> {

    private List<User> data;

    private OnItemClickListener itemClickListener;
    public GitUsersAdapter(List<User> data) {
        this.data = data;
    }
    public GitUsersAdapter setOnItemClickListener(OnItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
        return this;
    }

    public List<User> getData(){
        return this.data;
    }
    public void setData(List<User> data){
        this.data = data;
    }

    @NonNull
    @Override
    public GitUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user,parent,false);

        return new GitUserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GitUsersAdapter.GitUserViewHolder holder, int position) {
            holder.bind(data.get(position), position);
    }

    @Override
    public int getItemCount() {
        return data.size() ;
    }


    public class GitUserViewHolder extends RecyclerView.ViewHolder{

        TextView tvUsername,tvUserID;
        ImageView ivAvatar;
        ShimmerFrameLayout shimmerFrameLayout;
        ImageButton ibNotes;


        public GitUserViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvUserID = itemView.findViewById(R.id.tvUserId);
            shimmerFrameLayout = itemView.findViewById(R.id.shimmer);
            ibNotes = itemView.findViewById(R.id.ibNotes);
            ibNotes.setVisibility(View.GONE);
        }
        public void bind(User u, int pos){

            //Check if the currently displyed user is a dummy user
            if(u.id >= 0) {
                //if not then stop the shimmer effect and
                //remove the skeleton colors
                shimmerFrameLayout.hideShimmer();
                tvUserID.setBackgroundColor(Color.TRANSPARENT);
                tvUsername.setBackgroundColor(Color.TRANSPARENT);
            }
            else return;


            /**
             * Load the actual user data
             */

            Glide.with(itemView.getContext()).clear(ivAvatar);

            tvUsername.setText(u.username );
            tvUserID.setText("ID: "+u.id +" "+ (shouldInvertPos(pos)?"(Inverted)":""));

            Glide.with(itemView.getContext()).load(u.avatarUrl)
                    /**
                     * Set image inversion every 4th person
                     * on the recycler view list
                     */
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                            //Check if user pos is divisible by 4
                            if( shouldInvertPos(pos)) {
                                ivAvatar.setColorFilter(new ColorMatrixColorFilter(Constants.NEGATIVE));
                                Log.e("INVERT", "Inverting position "+pos);
                            }
                            else
                                ivAvatar.setColorFilter(null);
                            return false;
                        }
                    })
                    .into(ivAvatar);



            /**
             *
             * Set View listeners
             */

            itemView.setOnClickListener(view -> {
                if(itemClickListener!=null)
                itemClickListener.onItemClick(u,pos);
            });
        }

        void showNotes(String){
            new AsyncTask<Void,Void,Void>(){
                @Override
                protected Void doInBackground(Void... voids) {

                    return null;
                }
            }.execute();

        }

        private boolean shouldInvertPos(int pos){
            return (((pos+1) % 4) == 0);
        }
    }
}
