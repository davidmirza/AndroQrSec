package com.zarda.simpleqrcodereader;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
public class HistoriAdapter extends RecyclerView.Adapter<HistoriAdapter.HistoriAdapterVH> {
    private List<Histori> historiList;
    private Context context;

    public HistoriAdapter() {
    }

    public void setData(List<Histori> historiList){
        this.historiList = historiList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoriAdapterVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new HistoriAdapter.HistoriAdapterVH(LayoutInflater.from(context).inflate(R.layout.row_histori, null));
    }

    @Override
    public void onBindViewHolder(@NonNull HistoriAdapterVH holder, int position) {
        Histori histori = historiList.get(position);
        String hasilnya  = histori.getHisto();
        holder.histori.setText(hasilnya);
    }

    @Override
    public int getItemCount() {
        return historiList.size();
    }

    public class HistoriAdapterVH extends RecyclerView.ViewHolder {
        TextView histori;

        public HistoriAdapterVH(@NonNull View itemView) {
            super(itemView);
            histori = itemView.findViewById(R.id.his_row);
        }
    }
}
