/*
 *
 *  * Created by the Djowda Project Team
 *  * Copyright (c) 2017-2025 Djowda. All rights reserved.
 *  *
 *  * This file is part of the Djowda Project.
 *  *
 *  * Licensed under the Djowda Non-Commercial, Non-Profit License v1.0
 *  *
 *  * Permissions:
 *  * - You may use, modify, and share this file for non-commercial and non-profit purposes only.
 *  * - Commercial use of this file, in any form, requires prior written permission
 *  *   from the Djowda Project maintainers.
 *  *
 *  * Notes:
 *  * - This project is community-driven and continuously evolving.
 *  * - The Djowda Project reserves the right to relicense future versions.
 *  *
 *  * Last Modified: 2025-08-16 18:01
 *
 */

package com.djowda.djowdageminimap.MapTest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.djowda.djowdageminimap.R;


public class GridAdapter extends RecyclerView.Adapter<GridAdapter.GridViewHolder> {
    private final Context context;
    private final CellData cellData;
    private final int centerPosition;
    private ItemClickListener mItemClickListener;

    public GridAdapter(Context context) {
        this.context = context;
        this.cellData = new CellData();
        this.centerPosition = TileMap.getCenterPosition();
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tile_item, parent, false);
        return new GridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {
        if (position == centerPosition) {
            holder.imageView.setImageResource(R.drawable.djowda_logo_02);
        } else if (cellData.hasData(position)) {
            holder.imageView.setImageResource(R.drawable.tile_test3);
        } else {
            holder.imageView.setImageResource(R.drawable.tile_test);
        }

        holder.itemView.setOnClickListener(v -> {
            if (mItemClickListener != null) {
                long cellId = cellData.getCellIdForPosition(position);
                mItemClickListener.onItemClick(v, position, cellId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return TileMap.getTotalItems();
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    // Single cell update method - following the working pattern
    public void updateCell(int position, long cellId, Component component) {
        cellData.updateCell(position, cellId, component);
        notifyItemChanged(position);
    }

    // Clear all data when navigating to new location
    public void clearAllData() {
        cellData.clearAllData();
        notifyDataSetChanged(); // Refresh entire grid
    }

    // Alternative method to clear grid with range notification for better performance
    public void clearGridEfficient() {
        int itemCount = getItemCount();
        cellData.clearAllData();
        notifyItemRangeChanged(0, itemCount);
    }

    // Method to check if grid has any data
    public boolean hasAnyData() {
        return cellData.hasAnyData();
    }

    // Method to get total number of cells with data
    public int getDataCellCount() {
        return cellData.getDataCellCount();
    }

    public class GridViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;

        GridViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.tileImageView);
        }
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position, long cellId);
    }

    public void clearAll() {
        cellData.clear();
        notifyDataSetChanged();
    }


}

//public class GridAdapter extends RecyclerView.Adapter<GridAdapter.GridViewHolder> {
//    private final Context context;
//    private final CellData cellData;
//    private final int centerPosition;
//    private ItemClickListener mItemClickListener;
//
//    public GridAdapter(Context context) {
//        this.context = context;
//        this.cellData = new CellData();
//        this.centerPosition = TileMap.getCenterPosition();
//    }
//
//    @NonNull
//    @Override
//    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.tile_item, parent, false);
//        return new GridViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {
//        if (position == centerPosition) {
//            holder.imageView.setImageResource(R.drawable.djowda_logo_02);
//        } else if (cellData.hasData(position)) {
//            holder.imageView.setImageResource(R.drawable.tile_test3);
//        } else {
//            holder.imageView.setImageResource(R.drawable.tile_test);
//        }
//
//        holder.itemView.setOnClickListener(v -> {
//            if (mItemClickListener != null) {
//                long cellId = cellData.getCellIdForPosition(position);
//                mItemClickListener.onItemClick(v, position, cellId);
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return TileMap.getTotalItems();
//    }
//
//    public void setClickListener(ItemClickListener itemClickListener) {
//        this.mItemClickListener = itemClickListener;
//    }
//
//    // Single cell update method - following the working pattern
//    public void updateCell(int position, long cellId, Component component) {
//        cellData.updateCell(position, cellId, component);
//        notifyItemChanged(position);
//    }
//
//    public class GridViewHolder extends RecyclerView.ViewHolder {
//        final ImageView imageView;
//
//        GridViewHolder(@NonNull View itemView) {
//            super(itemView);
//            imageView = itemView.findViewById(R.id.tileImageView);
//        }
//    }
//
//    public interface ItemClickListener {
//        void onItemClick(View view, int position, long cellId);
//    }
//}


