package com.example.weather;

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

import com.example.weather.weather.CityWeatherSettings;

import java.util.ArrayList;

public class CityListAdapter extends RecyclerView.Adapter<CityListAdapter.CityViewHolder> {
    private Context context;
    private ArrayList<CityWeatherSettings> mWeatherSet;
    private OnItemClickListener onItemClickListener;
    private OnItemUndoDeleteNotify onUndoDeleteNotifyListener;
    private int checkedPosition = -1;
    private int viewMode;

    private CityWeatherSettings itemRecentlyRemoved;
    private int itemRecentlyRemovedPosition;

    private int colorWindowBackground;
    private int colorAccent;

    public static int DISPLAY_TEMP_MODE = 0x1;
    public static int DISPLAY_SELECTION_MODE = 0x2;
    /**
     * Reference views for each data item
     */
    public class CityViewHolder extends RecyclerView.ViewHolder {
        private TextView cityNameView;
        private TextView briefTempView;
        private int mode;

        public CityViewHolder(View view, int mode) {
            super(view);
            this.mode = mode;
            findViews(view);
            if ( (mode & DISPLAY_TEMP_MODE) == 0 ) {
                briefTempView.setVisibility(View.GONE);
            }
        }


        /**
         * Bind new data to view
         * @param weatherSettings displayed weather settings
         */
        void bind( final CityWeatherSettings weatherSettings ) {
            if (checkedPosition == getAdapterPosition() && checkedPosition >= 0) {
                if ( (mode & DISPLAY_SELECTION_MODE) > 0 ) {
                    cityNameView.setBackgroundColor(colorAccent);
                    briefTempView.setBackgroundColor(colorAccent);
                }
            } else {
                cityNameView.setBackgroundColor(colorWindowBackground);
                briefTempView.setBackgroundColor(colorWindowBackground);
            }

            cityNameView.setText(weatherSettings.getCurrentCity());

            String tempUnit = context.getString(R.string.not_avaliable);

            if ( weatherSettings.getWeather() != null ) {
                tempUnit = Integer.toString(weatherSettings.getWeather().getTemperature());
                if (weatherSettings.getWeather().isFahrenheitTempUnit()) {
                    tempUnit += context.getString(R.string.temp_unit_fahrenheit);
                } else {
                    tempUnit += context.getString(R.string.temp_unit_celsius);
                }
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

    public class CityListSwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        private CityListAdapter adapter;
        private Drawable icon;
        private final ColorDrawable background;

        public CityListSwipeToDeleteCallback(CityListAdapter adapter) {
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
     * @param mWeatherSet - cities weather array set
     */
    public CityListAdapter( Context context, @NonNull ArrayList<CityWeatherSettings> mWeatherSet, int mode ) {
        this.context = context;
        this.mWeatherSet = mWeatherSet;
        this.viewMode = mode;
        onItemClickListener = null;
        onUndoDeleteNotifyListener = null;
        findColors();
        itemRecentlyRemoved = null;
        itemRecentlyRemovedPosition = -1;
    }

    /**
     * Setup item selected callback. Previous will be erased
     * @param callBack new callback class
     */
    public void setOnItemSelectedCallBack( OnItemClickListener callBack) {
        onItemClickListener = callBack;
    }

    /**
     * Setup new weather set
     * @param mWeatherSet new weather set
     */
    public void setWeatherSet(@NonNull ArrayList<CityWeatherSettings> mWeatherSet) {
        this.mWeatherSet = mWeatherSet;
        notifyDataSetChanged();
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
    public CityListAdapter.CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.city_info_layout, parent, false);
        return new CityViewHolder(view, viewMode);
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     * @param holder element
     * @param position position
     */
    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        // - get element from your data set at this position
        // - replace the contents of the view with that element
        holder.bind(mWeatherSet.get(position));
    }

    public void deleteItem(int position) {
        itemRecentlyRemoved = mWeatherSet.get(position);
        itemRecentlyRemovedPosition = position;
        mWeatherSet.remove(position);
        notifyItemRemoved(position);
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
            mWeatherSet.add(itemRecentlyRemovedPosition,
                    itemRecentlyRemoved);
            notifyItemInserted(itemRecentlyRemovedPosition);
            itemRecentlyRemovedPosition = -1;
            itemRecentlyRemoved = null;
        }
    }

    /**
     * Get item count
     * @return item count
     */
    @Override
    public int getItemCount() {
        return mWeatherSet.size();
    }

    public void setViewMode(int mode) {
        viewMode = mode;
        notifyDataSetChanged();
    }

    public int getViewMode() {
        return viewMode;
    }
}
