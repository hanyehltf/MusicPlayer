package com.example.musicplayer.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.MainActivity;
import com.example.musicplayer.MediaPlayerService;
import com.example.musicplayer.R;
import com.example.musicplayer.StorageUtil;
import com.example.musicplayer.database.DataLoader;
import com.example.musicplayer.database.Songs;
import com.example.musicplayer.nowplaying.NowPlaying;
import com.example.musicplayer.ui.EditTags;
import com.example.musicplayer.ui.playlists.PlaylistsViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class QueueAdapter extends SelectableAdapter<QueueAdapter.ViewHolder> {

    private final List<Songs> songs;
    private final ItemClicked activity;
    private final Context context;
    private Toast toast;
    private final StorageUtil storage;
    private final Fragment fragment;
    private final ItemTouchHelper itemTouchHelper;


    public QueueAdapter (Context context, @Nullable Fragment fragment, List<Songs> songs, ItemTouchHelper itemTouchHelper){

        this.songs = songs;
        if (fragment != null) {
            activity = (ItemClicked) fragment;
        }
        else activity = (ItemClicked) context;
        this.fragment = fragment;
        this.context = context;
        this.itemTouchHelper = itemTouchHelper;
        storage = new StorageUtil(context);

    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private final TextView queue_song_title, queue_song_artist, queue_song_duration;
        private final ImageView queue_drag;
        private final FrameLayout rv_queue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            queue_drag = itemView.findViewById(R.id.queue_drag);
            queue_song_title= itemView.findViewById(R.id.queue_song_title);
            queue_song_artist = itemView.findViewById(R.id.queue_song_artist);
            queue_song_duration = itemView.findViewById(R.id.queue_song_duration);
            rv_queue = itemView.findViewById(R.id.rv_queue);

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
    public QueueAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_queue, parent, false);
        return new QueueAdapter.ViewHolder (root);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final QueueAdapter.ViewHolder holder, final int position) {

        if (fragment == null) {
            if (MediaPlayerService.activeAudio != null) {
                if (MediaPlayerService.activeAudio.getId() == MediaPlayerService.audioList.get(position).getId() && MediaPlayerService.audioIndex == position) {
                    holder.queue_song_title.setTextColor(context.getResources().getColor(R.color.programmer_green));
                    holder.queue_song_artist.setTextColor(context.getResources().getColor(R.color.programmer_green));
                } else {
                    holder.queue_song_title.setTextColor(context.getResources().getColor(R.color.white));
                    holder.queue_song_artist.setTextColor(context.getResources().getColor(R.color.notification_artist));
                }
            }

        }

        if (fragment !=null ) {
            holder.queue_song_title.setText(songs.get(position).getTitle());
            holder.queue_song_artist.setText(songs.get(position).getArtist());
            holder.queue_song_duration.setText(NowPlaying.getTimeInMins(songs.get(position).getDuration() / 1000));
        }
        else{
            holder.queue_song_title.setText(MediaPlayerService.audioList.get(position).getTitle());
            holder.queue_song_artist.setText(MediaPlayerService.audioList.get(position).getArtist());
            holder.queue_song_duration.setText(NowPlaying.getTimeInMins(MediaPlayerService.audioList.get(position).getDuration() / 1000));
        }

        holder.queue_song_title.setSelected(true);

        if (isSelected(position)){
            holder.rv_queue.setBackground(context.getResources().getDrawable(R.drawable.selected));
            holder.queue_drag.setClickable(false);
        }

        else {
            if (itemTouchHelper != null){
                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorDark));
            }
            else holder.rv_queue.setBackgroundColor(context.getResources().getColor(R.color.dark_transparent_black));

            holder.queue_drag.setClickable(true);

            holder.queue_drag.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {

                        if (itemTouchHelper != null) {
                            itemTouchHelper.startDrag(holder);
                        }
                    }

                    return false;
                }
            });




        }

    }

    @Override
    public int getItemCount() {

        if (songs != null && fragment != null) return songs.size();
        else if (MediaPlayerService.audioList != null) return MediaPlayerService.audioList.size();
        else return 0;

    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
    }


    public void deleteSongs(final int position){

        final Dialog dialog = new Dialog(context);

        dialog.setContentView(R.layout.delete_song_layout);

        TextView delete = dialog.findViewById(R.id.tv_delete);

        if (fragment == null) delete.setText("Are you sure you want to delete " + MediaPlayerService.audioList.get(position).getTitle() + "?");
        else delete.setText("Are you sure you want to delete " + songs.get(position).getTitle() + "?");

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

                if (fragment == null) {
                    context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "_id =? ", new String[]{MediaPlayerService.audioList.get(position).getId() + ""});
                    new File(MediaPlayerService.audioList.get(position).getData()).delete();
                    MediaPlayerService.audioList.remove(position);
                }
                else {
                    context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "_id =? ", new String[]{songs.get(position).getId() + ""});
                    new File(songs.get(position).getData()).delete();
                    songs.remove(position);
                }
                notifyDataSetChanged();
            }
        });



    }

    public void delete (int index){

        if (index >-1) {

            if (fragment == null) MediaPlayerService.audioList.remove(index);
            else songs.remove(index);
            notifyItemRemoved(index);
        }

    }



}