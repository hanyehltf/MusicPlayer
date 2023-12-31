package com.example.musicplayer.adapters;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicplayer.R;
import com.example.musicplayer.StorageUtil;

import com.example.musicplayer.database.Playlists;
import com.example.musicplayer.database.Songs;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsAdapter extends SelectableAdapter<PlaylistsAdapter.ViewHolder> {


    private final List<Playlists> playlists;
    private final ItemClicked activity;
    private final Context context;
    private ArrayList<Songs> songs;
    private final StorageUtil storage;
    private final Fragment fragment;


    public PlaylistsAdapter(Context context, @Nullable Fragment fragment, List<Playlists> playlistsArrayList){

        playlists = playlistsArrayList;
        this.context = context;
        if (fragment != null) activity = (ItemClicked) fragment;
        else activity = (ItemClicked) context;
        this.fragment = fragment;
        storage = new StorageUtil(context);
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{


        private final TextView tv_playlist_name;
        private final ImageView iv_playlist_art;
        private final FrameLayout playlist_selected;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            tv_playlist_name = itemView.findViewById(R.id. tv_playlist_name);
            iv_playlist_art = itemView.findViewById(R.id.iv_playlist_art);
            playlist_selected = itemView.findViewById(R.id.playlist_selected);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }


        @Override
        public void onClick(View v) {

            activity.onItemClicked(getAdapterPosition());

        }

        @Override
        public boolean onLongClick(View v) {

            activity.onItemLongClicked(getAdapterPosition());
            notifyDataSetChanged();
            return true;
        }
    }


    @NonNull
    @Override
    public PlaylistsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_playlists,parent,false);
        return new PlaylistsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PlaylistsAdapter.ViewHolder holder, final int position) {


        holder.tv_playlist_name.setText(playlists.get(position).getName());
        holder.tv_playlist_name.setSelected(true);

        Glide.with(context).load(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), loadAlbumId(playlists.get(position).getId(), context)))
                .error(R.mipmap.cassette_image_foreground)
                .placeholder(R.mipmap.cassette_image_foreground)
                .centerCrop()
                .fallback(R.mipmap.cassette_image_foreground)
                .into(holder.iv_playlist_art);

        if (isSelected(position)){
            holder.playlist_selected.setBackground(context.getResources().getDrawable(R.drawable.selected));
        }
        else {
            holder.playlist_selected.setBackground(context.getResources().getDrawable(R.drawable.not_selected));

        }

    }




    @Override
    public int getItemCount() {
        return playlists.size();
    }


    @Override
    public void onViewRecycled(@NonNull PlaylistsAdapter.ViewHolder holder) {

        holder.iv_playlist_art.setImageDrawable(null);
        holder.tv_playlist_name.setText(null);
    }


    private void deletePlaylist (final int position){

        final Dialog dialog = new Dialog(context);

        dialog.setContentView(R.layout.delete_song_layout);

        TextView delete = dialog.findViewById(R.id.tv_delete);

        delete.setText("Are you sure you want to delete " + playlists.get(position).getName() + "?");

        dialog.show();

        dialog.findViewById(R.id.delete_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();

            }
        });

        dialog.findViewById(R.id.delete_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                ContentResolver contentResolver = context.getContentResolver();
                Uri uri =  MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
                contentResolver.delete(uri, "_id =? ", new String[]{String.valueOf(playlists.get(position).getId())});
                playlists.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    private long loadAlbumId(long id_, Context context)
    {
        long id = 0;

        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", id_);
        String selection = android.provider.MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = "play_order";

        Cursor cursor = contentResolver.query(uri, new String[]{"album_id"}, selection, null, sortOrder);

        if (cursor != null && cursor.getCount()>0) {

            cursor.moveToFirst();

            id = cursor.getLong(0);

        }
        if (cursor != null) {

            cursor.close();
        }

        return id;
    }

}
