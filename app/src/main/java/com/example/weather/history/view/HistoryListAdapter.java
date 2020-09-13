package com.example.weather.history.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.CityID;
import com.example.weather.OnItemClickListener;
import com.example.weather.R;
import com.example.weather.history.WeatherHistory;
import com.example.weather.history.WeatherHistoryWithCityAndIcon;
import com.example.weather.weather.CityWeatherSettings;
import com.example.weather.weather.WeatherEntity;

import java.util.ArrayList;

public class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.HistoryListViewHolder> {
    private Context context;
    private HistorySource historySource;
    private OnItemClickListener onItemClickListener;
    private OnItemUndoDeleteNotify onUndoDeleteNotifyListener;
    private int checkedPosition = -1;
    private CityID cityIDFilter;

    private WeatherHistory itemRecentlyRemoved;
    private int itemRecentlyRemovedPosition;
    private boolean itemRecentlyRemovedPositionWasSelected;

    private int colorWindowBackground;
    private int colorAccent;

    public static int DISPLAY_TEMP_MODE = 0x1;
    public static int DISPLAY_SELECTION_MODE = 0x2;
    /**
     * Reference views for each data item
     */
    public class HistoryListViewHolder extends RecyclerView.ViewHolder {
        private TextView cityNameView;
        private TextView briefTempView;

        public HistoryListViewHolder(View view) {
            super(view);
            findViews(view);
        }


        /**
         * Bind new data to view
         * @param weatherHistoryWithCityAndIcon displayed weather settings
         */
        void bind( final WeatherHistoryWithCityAndIcon weatherHistoryWithCityAndIcon ) {
            cityNameView.setText(weatherHistoryWithCityAndIcon.city.name);
            String tempUnit = context.getString(R.string.not_avaliable);

            WeatherEntity entity = WeatherHistoryWithCityAndIcon.make(weatherHistoryWithCityAndIcon);
            tempUnit = Integer.toString(entity.getTemperature());
            if (entity.isFahrenheitTempUnit()) {
                tempUnit += context.getString(R.string.temp_unit_fahrenheit);
            } else {
                tempUnit += context.getString(R.string.temp_unit_celsius);
            }

            briefTempView.setText(tempUnit);

            cityNameView.setOnClickListener(view -> handleOnItemClick());
            briefTempView.setOnClickListener(v -> handleOnItemClick());

        }

        private void handleOnItemClick() {
            if (checkedPosition != getAdapterPosition()) {
                notifyItemChanged(checkedPosition); //For uncheck previous
                checkedPosition = getAdapterPosition();
                notifyItemChanged(checkedPosition); //For uncheck previous
            }
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(checkedPosition);
            }
        }

        private void findViews(View view) {
            cityNameView = view.findViewById(R.id.cityName);
            briefTempView = view.findViewById(R.id.briefTemp);
        }
    }

    public class HistoryListSwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        private HistoryListAdapter adapter;
        private Drawable icon;
        private final ColorDrawable background;

        public HistoryListSwipeToDeleteCallback(HistoryListAdapter adapter) {
            super(0, ItemTouchHelper.LEFT );
            this.adapter = adapter;
            icon = ContextCompat.getDrawable(adapter.context, R.drawable.ic_baseline_delete_24 );

            TypedValue typedValue = new TypedValue();
            if (context.getTheme().resolveAttribute(android.R.attr.colorPressedHighlight, typedValue, true)) {
                background = new ColorDrawable(typedValue.data);
            } else {
                background = new ColorDrawable(Color.RED);
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            View itemView = viewHolder.itemView;
            int backgroundCornerOffset = 20; //so background is behind the rounded corners of itemView

            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + icon.getIntrinsicHeight();

            if (dX < 0) { // Swiping to the left
                int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                        itemView.getTop(), itemView.getRight(), itemView.getBottom());
            } else { // view is unSwiped
                background.setBounds(0, 0, 0, 0);
            }

            background.draw(c);
            icon.draw(c);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            adapter.deleteItem(position);

            if ( position == checkedPosition ) setSelectedItemIndex(-1);
        }
    }

    private void findColors() {
        colorWindowBackground = 0;
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true)) {
            colorWindowBackground = typedValue.data;
        }
        colorAccent = 0;
        if (context.getTheme().resolveAttribute(android.R.attr.colorAccent, typedValue, true)) {
            colorAccent = typedValue.data;
        }
    }
    /**
     * Object constructor
     * @param historySource - source of history records
     */
    public HistoryListAdapter(Context context, @NonNull HistorySource historySource) {
        this.context = context;
        this.historySource = historySource;
        onItemClickListener = null;
        onUndoDeleteNotifyListener = null;
        findColors();
        itemRecentlyRemoved = null;
        itemRecentlyRemovedPosition = -1;
        itemRecentlyRemovedPositionWasSelected = false;
    }

    /**
     * Setup item selected callback. Previous will be erased
     * @param callBack new callback class
     */
    public void setOnItemSelectedCallBack( OnItemClickListener callBack) {
        onItemClickListener = callBack;
    }

    /**
     * Get index of selected item
     * @return index of selected item
     */
    public int getSelectedItemIndex() {
        return checkedPosition;
    }

    public void setSelectedItemIndex(int index) {
        int lastCheckedPosition = checkedPosition;
        if (index >= 0 && index < getItemCount() ) {
            checkedPosition = index;
        } else {
            checkedPosition = -1;
        }

        notifyItemChanged(lastCheckedPosition); //For uncheck previous

        if ( checkedPosition >= 0 ) {
            notifyItemChanged(checkedPosition); //For check new
        }
        if ( onItemClickListener != null ) {
            onItemClickListener.onItemClick(checkedPosition);
        }
    }

    /**
     * Inflate the item layout and create the holder
     * @param parent parent view
     * @param viewType ignored
     * @return created view holder entity
     */
    @NonNull
    @Override
    public HistoryListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.city_info_layout, parent, false);
        return new HistoryListViewHolder(view);
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     * @param holder element
     * @param position position
     */
    @Override
    public void onBindViewHolder(@NonNull HistoryListViewHolder holder, int position) {
        // - get element from your data set at this position
        // - replace the contents of the view with that element
        holder.bind(historySource.getHistory(cityIDFilter).get(position));
    }

    public void deleteItem(int position) {
        itemRecentlyRemoved = historySource.getHistory(cityIDFilter).get(position).history;
        itemRecentlyRemovedPosition = position;
        itemRecentlyRemovedPositionWasSelected = position == checkedPosition;
        historySource.removeHistory(
            historySource.getHistory(cityIDFilter).get(position).history
        );
        //notifyItemRemoved(position);
        notifyDataSetChanged();
        notifyAboutUndoOperation();
    }

    /**
     * Callback for undo delete
     */
    public interface OnItemUndoDeleteNotify {
        void onUndoDeleteListener();
    }

    /**
     * Setup new item deleted event listener
     * @param e new event handler
     */
    public void setOnItemDeletedListener(OnItemUndoDeleteNotify e) {
        onUndoDeleteNotifyListener = e;
    }

    private void notifyAboutUndoOperation() {
        if ( onUndoDeleteNotifyListener != null ) {
            onUndoDeleteNotifyListener.onUndoDeleteListener();
        }

    }

    public void undoLastDeleted() {
        if ( itemRecentlyRemovedPosition >= 0 ) {
            historySource.addHistory(itemRecentlyRemoved);
            notifyDataSetChanged();
            //notifyItemInserted(itemRecentlyRemovedPosition);
            if ( itemRecentlyRemovedPositionWasSelected ) {
                //setSelectedItemIndex(itemRecentlyRemovedPosition);
            }
            itemRecentlyRemovedPosition = -1;
            itemRecentlyRemovedPositionWasSelected = false;
            itemRecentlyRemoved = null;
        }
    }

    /**
     * Get item count
     * @return item count
     */
    @Override
    public int getItemCount() {
        return (int)historySource.getHistoryRecordsCount(cityIDFilter);
    }
}
