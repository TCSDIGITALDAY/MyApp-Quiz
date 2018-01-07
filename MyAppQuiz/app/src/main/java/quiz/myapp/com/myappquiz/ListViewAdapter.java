package quiz.myapp.com.myappquiz;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by venkatesh on 11/11/2017.
 */


public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.ListDataObjectHolder> {
      private ArrayList<DataObject> mDataset;
   // AssetManager assetManager = getAssets();

    private static String LOG_TAG = "ListViewAdapter";
    private static ListClickListener myClickListener;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
   /* public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;

        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }*/

    public static class ListDataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        TextView label;
    public ListDataObjectHolder(View itemView) {
        super(itemView);
        label = (TextView) itemView.findViewById(R.id.lineItem);

        Log.i(LOG_TAG, "Adding Listener");
        itemView.setOnClickListener(this);
    }


    @Override
    public void onClick(View v)
    {
        Log.d("ADAPTER_ACT","onClick");
        myClickListener.onItemClick(getAdapterPosition(), v);
        label.setBackgroundColor(Color.rgb(255,64,129));
    }
}

    public void setOnItemClickListener(ListClickListener myClickListener) {
        Log.d("ADAPTER_ACT","setOnItemClickListener");
        this.myClickListener = myClickListener;
    }
    // Provide a suitable constructor (depends on the kind of dataset)
    public ListViewAdapter(ArrayList<DataObject> myDataset) {
        mDataset = myDataset;
    }



    // Create new views (invoked by the layout manager)
    @Override
    public ListDataObjectHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_view_row, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ListDataObjectHolder vh = new ListDataObjectHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(ListDataObjectHolder holder, int position) {
        //Typeface typeFace=Typeface.createFromAsset(MainActivity.getAssets(),"fonts/calibri.ttf");
        //label.setTypeface(typeFace);
        Log.d("ADAPTER_ACT","onBindViewHolder");
        holder.label.setBackgroundColor(Color.parseColor("#33FFFFFF"));
        holder.label.setText(mDataset.get(position).getmText1());
        //holder.label.getBackground().setAlpha(100);
    }


    /*@Override
    public void onBindViewHolder(ListDataObjectHolder holder, int position,List<Object> payloads) {
        Log.d("ADAPTER_ACT","onBindViewHolder");
        if(!payloads.isEmpty()) {
            if (payloads.get(0) instanceof Integer) {
                holder.label.setBackgroundColor(Color.parseColor("#FF4081"));
                //holder.label.setText(mDataset.get(position).getmText1());
            }
        }
        //holder.label.getBackground().setAlpha(100);
    }*/

    public void addItem(DataObject dataObj, int index) {
        mDataset.add(index, dataObj);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        mDataset.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface ListClickListener {
        public void onItemClick(int position, View v);
    }
}
