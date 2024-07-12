package com.example.race_mini_game.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.race_mini_game.Models.Record;
import com.example.race_mini_game.Models.Leaderboards;
import com.example.race_mini_game.R;

import java.util.List;

public class ListFragment extends Fragment {
    private Leaderboards leaderboards;
    private RecyclerView recyclerView;
    private RecordAdapter adapter;

    public interface OnRecordSelectedListener {
        void onRecordSelected(Record record);
    }

    private OnRecordSelectedListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnRecordSelectedListener) {
            listener = (OnRecordSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnScoreSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        leaderboards = new Leaderboards(requireContext());
        recyclerView = view.findViewById(R.id.recyclerViewScores);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        updateScores();

        return view;
    }

    private void updateScores() {
        List<Record> topRecords = leaderboards.getTopScores();
        adapter = new RecordAdapter(topRecords, score -> {
            if (listener != null) {
                listener.onRecordSelected(score);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private static class RecordAdapter extends RecyclerView.Adapter<RecordViewHolder> {
        private List<Record> records;
        private OnRecordClickListener listener;

        interface OnRecordClickListener {
            void onRecordClick(Record record);
        }

        RecordAdapter(List<Record> records, OnRecordClickListener listener) {
            this.records = records;
            this.listener = listener;
        }

        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.single_record, parent, false);
            return new RecordViewHolder(view, listener);
        }

        @Override
        public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
            Record record = records.get(position);
            holder.bind(record, position + 1);

        }

        @Override
        public int getItemCount() {
            return records.size();
        }
    }

    private static class RecordViewHolder extends RecyclerView.ViewHolder {
        TextView textViewRank;
        TextView textViewPlayerName;
        TextView textViewScore;
        RecordAdapter.OnRecordClickListener listener;



        RecordViewHolder(View itemView, RecordAdapter.OnRecordClickListener listener) {
            super(itemView);
            textViewRank = itemView.findViewById(R.id.textViewRank);
            textViewPlayerName = itemView.findViewById(R.id.textViewPlayerName);
            textViewScore = itemView.findViewById(R.id.textViewScore);
            this.listener = listener;
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecordClick((Record) itemView.getTag());
                }
            });
        }



        void bind(final Record record, int rank) {
            textViewRank.setText(String.valueOf(rank));
            textViewPlayerName.setText(record.getName());
            textViewScore.setText(String.valueOf(record.getScore()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecordClick(record);
                }
            });
        }
    }
}