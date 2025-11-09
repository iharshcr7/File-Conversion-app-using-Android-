package com.example.mad_project;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private List<ConversionHistory> historyList;
    private OnHistoryItemClickListener listener;

    public interface OnHistoryItemClickListener {
        void onItemClick(ConversionHistory history);
    }

    public HistoryAdapter(List<ConversionHistory> historyList, OnHistoryItemClickListener listener) {
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        if (historyList != null && position < historyList.size()) {
            ConversionHistory history = historyList.get(position);
            if (history != null) {
                holder.bind(history, listener);
            }
        }
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    public void updateHistory(List<ConversionHistory> newHistoryList) {
        this.historyList = newHistoryList;
        notifyDataSetChanged();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView text1;
        private TextView text2;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(R.id.title);
            text2 = itemView.findViewById(R.id.subtitle);
        }

        public void bind(ConversionHistory history, OnHistoryItemClickListener listener) {
            text1.setText(history.getConversionType() + ": " + history.getFileName());
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            String dateStr = sdf.format(new Date(history.getTimestamp()));
            text2.setText(dateStr);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(history);
                }
            });
        }
    }
}

