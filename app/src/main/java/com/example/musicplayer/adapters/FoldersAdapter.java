package com.example.musicplayer.adapters;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicplayer.MediaPlayerService;
import com.example.musicplayer.R;
import com.example.musicplayer.StorageUtil;
import com.example.musicplayer.database.Songs;
import com.example.musicplayer.nowplaying.NowPlaying;
import com.example.musicplayer.ui.folders.FoldersFragment;
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FoldersAdapter extends SelectableAdapter<RecyclerView.ViewHolder> implements RecyclerViewFastScroller.OnPopupTextUpdate {


    private final List<String> folders2;
    private final androidx.collection.ArrayMap<String, Integer> folders;
    private final FolderItemClicked activityFolder;
    private final ItemClicked activitySong;
    private final Context context;
    private final StorageUtil storage;
    private ArrayList<Songs> songs;
    private final ArrayList<Songs> songsInPath;
    private final Fragment fragment;
    private final SharedPreferences sort;

    private final int TYPE_PARENT = 0;
    private final int TYPE_FOLDER = 1;
    private final int TYPE_SONG = 2;

    public FoldersAdapter(Context context,Fragment fragment, ArrayMap<String, Integer> foldersArrayList, ArrayList<Songs> songs, List<String> list){

        folders = foldersArrayList;
        folders2 = list;
        songsInPath = songs;
        this.context = context;
        activityFolder = (FolderItemClicked) fragment;
        activitySong = (ItemClicked) fragment;
        storage = new StorageUtil(context);
        this.fragment = fragment;
        sort = context.getSharedPreferences("Sort", Context.MODE_PRIVATE);

    }

    @NotNull
    @Override
    public CharSequence onChange(int i) {
        return "";
    }


    public class ParentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{



        public ParentViewHolder(@NonNull View itemView) {
            super(itemView);

            final TextView tv_folder_name, tv_num_songs;
            final ImageView folders_popup_menu;

            tv_folder_name = itemView.findViewById(R.id.tv_folder_name);
            tv_folder_name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            tv_folder_name.setText("..");

            tv_num_songs = itemView.findViewById(R.id.tv_num_songs);
            tv_num_songs.setVisibility(View.GONE);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }


        @Override
        public void onClick(View v) {

            activityFolder.onFolderItemClicked(getAdapterPosition());
            notifyDataSetChanged();
        }

        @Override
        public boolean onLongClick(View v) {
            activityFolder.onFolderItemLongClicked(getAdapterPosition());
            notifyDataSetChanged();
            return true;
        }
    }


    public class FoldersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{


        private final TextView tv_folder_name, tv_num_songs;


        public FoldersViewHolder(@NonNull View itemView) {
            super(itemView);


            tv_folder_name = itemView.findViewById(R.id.tv_folder_name);
            tv_num_songs = itemView.findViewById(R.id.tv_num_songs);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }


        @Override
        public void onClick(View v) {

            activityFolder.onFolderItemClicked(getAdapterPosition());
            notifyDataSetChanged();
        }

        @Override
        public boolean onLongClick(View v) {
            activityFolder.onFolderItemLongClicked(getAdapterPosition());
            notifyDataSetChanged();
            return true;
        }
    }

    public class SongsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{


        private final TextView tv_song_name, tv_artist_name, tv_song_duration;
        private final ImageView iv_album_art;


        public SongsViewHolder(@NonNull View itemView) {
            super(itemView);


            tv_song_name = itemView.findViewById(R.id.tv_song_name);
            tv_artist_name = itemView.findViewById(R.id.tv_artist_name);
            tv_song_duration = itemView.findViewById(R.id.tv_song_duration);
            iv_album_art = itemView.findViewById(R.id.iv_album_art);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }


        @Override
        public void onClick(View v) {

            activitySong.onItemClicked(getAdapterPosition());
            notifyDataSetChanged();
        }

        @Override
        public boolean onLongClick(View v) {

            activitySong.onItemLongClicked(getAdapterPosition());
            notifyDataSetChanged();
            return true;
        }
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == TYPE_FOLDER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_folders, parent, false);
            return new FoldersAdapter.FoldersViewHolder(view);
        }
        else if (viewType == TYPE_PARENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_folders, parent, false);
            return new FoldersAdapter.ParentViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_songs, parent, false);
            return new FoldersAdapter.SongsViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {

        if (holder.getItemViewType() == TYPE_FOLDER) {

            FoldersViewHolder holder0 = (FoldersViewHolder) holder;

            if (!FoldersFragment.currentPath.equals("/")) {

                String name = folders2.get(position - 1);

                if (MediaPlayerService.activeAudio != null) {
                    if (MediaPlayerService.activeAudio.getData().contains(FoldersFragment.currentPath + name + "/")) {
                        holder0.tv_folder_name.setTextColor(context.getResources().getColor(R.color.programmer_green));
                    } else {
                        holder0.tv_folder_name.setTextColor(context.getResources().getColor(R.color.white));
                    }
                }

                holder0.tv_folder_name.setText(name.trim());
                holder0.tv_num_songs.setText(folders.get(name) + "");
                holder0.tv_folder_name.setSelected(true);

                if (isSelected(position)) {
                    holder0.itemView.setBackground(context.getResources().getDrawable(R.drawable.selected));
                }
                else {
                    holder0.itemView.setBackground(context.getResources().getDrawable(R.drawable.not_selected));

                }

            } else {

                String name = folders2.get(position);

                if (MediaPlayerService.activeAudio != null) {
                    if (MediaPlayerService.activeAudio.getData().contains(FoldersFragment.currentPath + name + "/")) {
                        holder0.tv_folder_name.setTextColor(context.getResources().getColor(R.color.programmer_green));
                    } else {
                        holder0.tv_folder_name.setTextColor(context.getResources().getColor(R.color.white));
                    }
                }

                holder0.tv_folder_name.setText(name.trim());
                holder0.tv_num_songs.setText(folders.get(name) + "");
                holder0.tv_folder_name.setSelected(true);


                if (isSelected(position)){
                    holder0.itemView.setBackground(context.getResources().getDrawable(R.drawable.selected));
                }
                else {
                    holder0.itemView.setBackground(context.getResources().getDrawable(R.drawable.not_selected));


                }
            }
        }

        else if (holder.getItemViewType() == TYPE_SONG){

            SongsViewHolder holder1 = (SongsViewHolder) holder;

            if (MediaPlayerService.activeAudio != null) {
                if (MediaPlayerService.activeAudio.getId() == songsInPath.get(position-folders2.size()-1).getId()) {
                    holder1.tv_song_name.setTextColor(context.getResources().getColor(R.color.programmer_green));
                    holder1.tv_artist_name.setTextColor(context.getResources().getColor(R.color.programmer_green));
                }
                else {
                    holder1.tv_song_name.setTextColor(context.getResources().getColor(R.color.white));
                    holder1.tv_artist_name.setTextColor(context.getResources().getColor(R.color.notification_artist));
                }
            }

            holder1.tv_song_name.setText(songsInPath.get(position-folders2.size()-1).getTitle());
            holder1.tv_artist_name.setText(songsInPath.get(position-folders2.size()-1).getArtist());
            holder1.tv_song_duration.setText(NowPlaying.getTimeInMins(songsInPath.get(position-folders2.size()-1).getDuration()/1000));
            holder1.tv_song_name.setSelected(true);

            Glide.with(holder1.iv_album_art.getContext()).load(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), songsInPath.get(position-folders2.size()-1).getAlbumid()))
                    .error(R.mipmap.cassette_image_foreground)
                    .placeholder(R.mipmap.cassette_image_foreground)
                    .centerCrop()
                    .fallback(R.mipmap.cassette_image_foreground)
                    .into(holder1.iv_album_art);

            if (isSelected(position)){
                holder1.itemView.setBackground(context.getResources().getDrawable(R.drawable.selected));
            }
            else {
                holder1.itemView.setBackground(context.getResources().getDrawable(R.drawable.not_selected));


            }

        }
    }



    @Override
    public int getItemCount() {
        if (!FoldersFragment.currentPath.equals("/")) return folders2.size()+1+songsInPath.size();
        else return folders2.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (!FoldersFragment.currentPath.equals("/")) {
            if (position == 0) return TYPE_PARENT;
            else if (position <= folders2.size()) return TYPE_FOLDER;
            else return TYPE_SONG;
        }
        else{
            return TYPE_FOLDER;
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder.getItemViewType() == TYPE_SONG){
            SongsViewHolder holder1 = (SongsViewHolder) holder;
            Glide.with(holder1.iv_album_art).clear(holder1.iv_album_art);
            holder1.iv_album_art.setImageDrawable(null);
        }
        else if (holder.getItemViewType() == TYPE_FOLDER){
            FoldersViewHolder holder1 = (FoldersViewHolder) holder;
        }

        super.onViewRecycled(holder);
    }

    public ArrayList<Songs> loadFolderAudio(String path)
    {
        ArrayList<Songs> arrayList = new ArrayList<>();

        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = android.provider.MediaStore.Audio.Media.IS_MUSIC + "!= 0 AND " + MediaStore.Audio.Media.DATA + " LIKE ? ";

        String sortOrder;
        if (sort.getBoolean("FolderReverse", false)) sortOrder = sort.getString("FolderSortOrder", MediaStore.Audio.Media.DEFAULT_SORT_ORDER) + " DESC";
        else sortOrder = sort.getString("FolderSortOrder", MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        Cursor cursor = contentResolver.query(uri, new String[]{"_id","_data", "title", "artist", "album", "duration", "track", "artist_id", "album_id", "year"}, selection, new String[]{path + "%"}, sortOrder);



        if (cursor != null && cursor.getCount()>0) {

            cursor.moveToFirst();

            do {
                long id = cursor.getLong(0);
                String data = cursor.getString(1).trim();
                String title = cursor.getString(2).trim();
                String artist = cursor.getString(3).trim();
                String album = cursor.getString(4).trim();
                int duration = cursor.getInt(5);
                int trackNumber = cursor.getInt(6);
                long artistId = cursor.getInt(7);
                long albumId = cursor.getLong(8);
                long year = cursor.getLong(9);


                arrayList.add(new Songs(id, data, title, artist, album, artistId, albumId, trackNumber, duration, year));
            }
            while (cursor.moveToNext());

        }
        if (cursor != null) {

            cursor.close();
        }

        return arrayList;
    }



    private void deleteFolderSongs(){

        final Dialog dialog = new Dialog(context);

        dialog.setContentView(R.layout.delete_song_layout);

        TextView delete = dialog.findViewById(R.id.tv_delete);

        delete.setText("Are you sure you want to delete multiple songs?");

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
                new Runnable() {
                    @Override
                    public void run() {

                        for (int i = 0; i < songs.size(); i++) {

                            dialog.dismiss();
                            context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "_id =? ", new String[]{songs.get(i).getId() + ""});
                            new File(songs.get(i).getData()).delete();
                            songs.remove(i);
                        }
                        notifyDataSetChanged();
                    }
                }.run();

            }
        });



    }


    private void deleteSongs(final int position){

        final Dialog dialog = new Dialog(context);

        dialog.setContentView(R.layout.delete_song_layout);

        TextView delete = dialog.findViewById(R.id.tv_delete);

        delete.setText("Are you sure you want to delete " + songsInPath.get(position).getTitle() + "?");

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
                context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "_id =? ", new String[]{songsInPath.get(position).getId() + ""});
                new File(songsInPath.get(position).getData()).delete();
                songsInPath.remove(position);
                notifyDataSetChanged();
            }
        });



    }



}

