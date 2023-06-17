package com.example.musicplayer.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicplayer.MediaPlayerService;
import com.example.musicplayer.R;
import com.example.musicplayer.StorageUtil;
import com.example.musicplayer.database.DataLoader;
import com.example.musicplayer.database.Songs;
import com.example.musicplayer.nowplaying.NowPlaying;
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SongAdapter extends SelectableAdapter<SongAdapter.ViewHolder> implements RecyclerViewFastScroller.OnPopupTextUpdate {


    private final ArrayList<Songs> songs;
    private final ItemClicked activity;
    private final Context context;
    private final StorageUtil storage;
    private final Fragment fragment;
private DataLoader dataLoader;

    public SongAdapter(Context context, Fragment fragment, ArrayList<Songs> songsArrayList){

        songs = songsArrayList;
        this.context = context;
        activity = (ItemClicked) fragment;
        this.fragment = fragment;
        storage = new StorageUtil(context);
        dataLoader=new DataLoader();

    }

    @NotNull
    @Override
    public CharSequence onChange(int i) {

        SharedPreferences sort = context.getSharedPreferences("SongSort", Context.MODE_PRIVATE);

        if (sort.getString("sortOrder", MediaStore.Audio.Media.DEFAULT_SORT_ORDER).equals(MediaStore.Audio.Media.DEFAULT_SORT_ORDER)) {
            String s;
            int x = songs.get(i).getTitle().toUpperCase().charAt(0);

            if (songs.get(i).getTitle().toUpperCase().indexOf("THE ") == 0) {
                s = songs.get(i).getTitle().substring(4, 5).toUpperCase();
            } else if (songs.get(i).getTitle().toUpperCase().indexOf("A ") == 0) {
                s = songs.get(i).getTitle().substring(2, 3).toUpperCase();
            }else if (songs.get(i).getTitle().toUpperCase().indexOf("'") == 0 || songs.get(i).getTitle().toUpperCase().indexOf("\"") == 0) {
                s = songs.get(i).getTitle().substring(1, 2).toUpperCase();
            } else if (!(x >= 65 && x <= 90)) s = "#";
            else {
                s = songs.get(i).getTitle().substring(0, 1).toUpperCase();
            }
            return s;
        }
        return "";
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{


        private final TextView tv_song_name, tv_artist_name, tv_song_duration;
        private final ImageView iv_album_art,song_add_to_playlist;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            tv_song_name = itemView.findViewById(R.id.tv_song_name);
            tv_artist_name = itemView.findViewById(R.id.tv_artist_name);
            tv_song_duration = itemView.findViewById(R.id.tv_song_duration);
            iv_album_art = itemView.findViewById(R.id.iv_album_art);
            song_add_to_playlist=itemView.findViewById(R.id.song_add_to_playlist);

           itemView.setOnClickListener(this);
           itemView.setOnLongClickListener(this);

        }


        @Override
        public void onClick(View v) {

            activity.onItemClicked(getAdapterPosition());

            notifyDataSetChanged();
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
    public SongAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_songs,parent,false);
        return new SongAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SongAdapter.ViewHolder holder, final int position) {

        if (MediaPlayerService.activeAudio != null) {
            if (MediaPlayerService.activeAudio.getId() == songs.get(position).getId()) {
                holder.tv_song_name.setTextColor(context.getResources().getColor(R.color.programmer_green));
                holder.tv_artist_name.setTextColor(context.getResources().getColor(R.color.programmer_green));
            }
            else {
                holder.tv_song_name.setTextColor(context.getResources().getColor(R.color.white));
                holder.tv_artist_name.setTextColor(context.getResources().getColor(R.color.notification_artist));
            }
        }

        new Runnable() {
            @Override
            public void run() {

                holder.itemView.setBackground(isSelected(position) ? context.getResources().getDrawable(R.drawable.selected) : context.getResources().getDrawable(R.drawable.not_selected));

                holder.tv_song_name.setText(songs.get(position).getTitle());
                holder.tv_artist_name.setText(songs.get(position).getArtist());
                holder.tv_song_duration.setText(NowPlaying.getTimeInMins(songs.get(position).getDuration()/1000));
                holder.tv_song_name.setSelected(true);
            }
        }.run();

        new Runnable() {
            @Override
            public void run() {

                Glide.with(context).load(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), songs.get(position).getAlbumid()))
                        .error(R.mipmap.cassette_image_foreground)
                        .placeholder(R.mipmap.cassette_image_foreground)
                        .centerCrop()
                        .fallback(R.mipmap.cassette_image_foreground)
                        .into(holder.iv_album_art);
            }
        }.run();

holder.song_add_to_playlist.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {


        DataLoader.addToPlaylist(songs.get(position),context,fragment);

    }
});

    }




    @Override
    public int getItemCount() {
        return songs.size();
    }


    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.iv_album_art.setImageDrawable(null);
        holder.tv_song_name.setText(null);
        holder.tv_artist_name.setText(null);

        super.onViewRecycled(holder);
    }






}
