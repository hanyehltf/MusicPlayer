package com.example.musicplayer.adapters;


import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private final SelectionArray selectedItems;

    public SelectableAdapter() {
        selectedItems = new SelectionArray();
    }

    public boolean isSelected(int position) {
        return selectedItems.contains(position);
    }

    public void toggleSelection(int position) {

        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
        } else {
            selectedItems.put(position);
        }
        notifyItemChanged(position);
    }



    public int getSelectedItemCount() {
        return selectedItems.size();
    }

}