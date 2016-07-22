package io.github.icyflame.read.github.amas;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import eu.fiskur.markdownview.MarkdownView;
import in.uncod.android.bypass.Bypass;

/**
 * Created by siddharth on 22/7/16.
 */

/**
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.SimpleViewHolder> {
    private static final int COUNT = 100;
    public static final String TAG = "adapter-main";

    private final Context mContext;
    private List<JsonObject> mItems;
    private int mCurrentItemId = 0;

    public interface parentProvidesOnClickListener {
        View.OnClickListener setListenerForClick(JsonObject item);
    }

    parentProvidesOnClickListener mParentCallback;

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public final TextView title, description;

        public SimpleViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.item_title);
            description = (TextView) view.findViewById(R.id.item_desc);
        }
    }

    public MainAdapter(Context context, List<JsonObject> items, Activity parent) {
        mContext = context;
        mItems = items;

        try {
            mParentCallback = ((parentProvidesOnClickListener) parent);
        } catch (Exception err) {
            Log.e(TAG, "MainAdapter: " + parent.toString() +
                    " must implement the parentProvidesOnClickListener interface", err);
        }
    }

    public void replaceDataset(List<JsonObject> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.view_preview_item, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {
        Bypass bypass = new Bypass(mContext);
        holder.title.setText(bypass.markdownToSpannable(mItems.get(position).get("title").getAsString()));
        holder.description.setText(bypass.markdownToSpannable(mItems.get(position).get("body").getAsString()));
        View.OnClickListener listener = mParentCallback.setListenerForClick(mItems.get(position));
        holder.title.setOnClickListener(listener);
        holder.description.setOnClickListener(listener);
    }

    /*
    public void addItem(int position) {
        final int id = mCurrentItemId++;
        mItems.add(position, id);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }
    */

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}