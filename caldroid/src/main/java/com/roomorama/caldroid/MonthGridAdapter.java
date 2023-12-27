package com.roomorama.caldroid;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.caldroid.R;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import hirondelle.date4j.DateTime;

/*
 * The MonthGridAdapter provides customized view for the months gridview
 */
public class MonthGridAdapter extends BaseAdapter {
    protected int year;
    protected final Context context;

    protected DateTime minDateTime;
    protected DateTime maxDateTime;
    protected DateTime today;
    protected boolean squareTextViewCell;
    protected int themeResource;
    protected final Resources resources;

    protected int defaultCellBackgroundRes = -1;
    protected ColorStateList defaultTextColorRes;

    /*
     * caldroidData belongs to Caldroid
     */
    protected Map<String, Object> caldroidData;
    /*
     * extraData belongs to client
     */
    protected Map<String, Object> extraData;

    protected final LayoutInflater localInflater;

    public void setAdapterDateTime(DateTime dateTime) {
        this.year = dateTime.getYear();
    }

    // GETTERS AND SETTERS
    public DateTime getMinDateTime() {
        return minDateTime;
    }

    public void setMinDateTime(DateTime minDateTime) {
        this.minDateTime = minDateTime;
    }

    public DateTime getMaxDateTime() {
        return maxDateTime;
    }

    public void setMaxDateTime(DateTime maxDateTime) {
        this.maxDateTime = maxDateTime;
    }

    public int getThemeResource() {
        return themeResource;
    }

    public Map<String, Object> getCaldroidData() {
        return caldroidData;
    }

    public void setCaldroidData(Map<String, Object> caldroidData) {
        this.caldroidData = caldroidData;

        // Reset parameters
        populateFromCaldroidData();
    }

    public Map<String, Object> getExtraData() {
        return extraData;
    }

    public void setExtraData(Map<String, Object> extraData) {
        this.extraData = extraData;
    }

    /*
     * Constructor
     *
     * @param context
     * @param month
     * @param year
     * @param caldroidData
     * @param extraData
     */
    public MonthGridAdapter(Context context, int year,
                           Map<String, Object> caldroidData,
                           Map<String, Object> extraData) {
        super();
        this.year = year;
        this.context = context;
        this.caldroidData = caldroidData;
        this.extraData = extraData;
        this.resources = context.getResources();

        // Get data from caldroidData
        populateFromCaldroidData();

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        localInflater = MonthCaldroidFragment.getThemeInflater(context, inflater, themeResource);
    }

    /*
     * Retrieve internal parameters from caldroid data
     */
    private void populateFromCaldroidData() {
        minDateTime = (DateTime) caldroidData
                .get(MonthCaldroidFragment.MIN_DATE_TIME);
        maxDateTime = (DateTime) caldroidData
                .get(MonthCaldroidFragment.MAX_DATE_TIME);
        squareTextViewCell = Optional.ofNullable ((Boolean) caldroidData
                .get(MonthCaldroidFragment.SQUARE_TEXT_VIEW_CELL)).orElse(false);

        // Get theme
        themeResource = Optional.ofNullable((Integer) caldroidData
                .get(MonthCaldroidFragment.THEME_RESOURCE)).orElse(0);

        getDefaultResources();
    }

    // This method retrieve default resources for background and text color,
    // based on the Caldroid theme
    private void getDefaultResources() {
        Context wrapped = new ContextThemeWrapper(context, themeResource);

        // Get style of normal cell or square cell in the theme
        Resources.Theme theme = wrapped.getTheme();
        TypedValue styleCellVal = new TypedValue();
        if (squareTextViewCell) {
            theme.resolveAttribute(R.attr.styleCaldroidSquareCell, styleCellVal, true);
        } else {
            theme.resolveAttribute(R.attr.styleCaldroidNormalCell, styleCellVal, true);
        }

        // Get default background of cell
        TypedArray typedArray = wrapped.obtainStyledAttributes(styleCellVal.data, R.styleable.Cell);
        try {
            defaultCellBackgroundRes = typedArray.getResourceId(R.styleable.Cell_android_background, -1);
            defaultTextColorRes = typedArray.getColorStateList(R.styleable.Cell_android_textColor);
        }
        finally {
            typedArray.recycle();
        }
    }

    public void updateToday() {
        today = CalendarHelper.convertDateToDateTime(new Date());
    }

    protected DateTime getToday() {
        if (today == null) {
            today = CalendarHelper.convertDateToDateTime(new Date());
            today = new DateTime(today.getYear(), today.getMonth(), 1, 0, 0, 0, 0);
        }
        return today;
    }

    @SuppressWarnings("unchecked")
    protected void setCustomResources(DateTime dateTime, View backgroundView,
                                      TextView textView) {
        // Set custom background resource
        Map<DateTime, Drawable> backgroundForDateTimeMap = (Map<DateTime, Drawable>) caldroidData
                .get(MonthCaldroidFragment.BACKGROUND_FOR_DATETIME_MAP);
        if (backgroundForDateTimeMap != null) {
            // Get background resource for the dateTime
            Drawable drawable = backgroundForDateTimeMap.get(dateTime);

            // Set it
            if (drawable != null) {
                backgroundView.setBackground(drawable);
            }
        }

        // Set custom text color
        Map<DateTime, Integer> textColorForDateTimeMap = (Map<DateTime, Integer>) caldroidData
                .get(MonthCaldroidFragment.TEXT_COLOR_FOR_DATETIME_MAP);
        if (textColorForDateTimeMap != null) {
            // Get textColor for the dateTime
            Integer textColorResource = textColorForDateTimeMap.get(dateTime);

            // Set it
            if (textColorResource != null) {
                textView.setTextColor(resources.getColor(textColorResource, null));
            }
        }
    }

    private void resetCustomResources(CellView cellView) {
        cellView.setBackgroundResource(defaultCellBackgroundRes);
        cellView.setTextColor(defaultTextColorRes);
    }

    /*
     * Customize colors of text and background based on states of the cell
     * (disabled, active, selected, etc)
     * <p/>
     * To be used only in getView method
     *
     * @param position
     * @param cellView
     */
    protected void customizeTextView(int position, CellView cellView) {
        // Get the padding of cell so that it can be restored later
        int topPadding = cellView.getPaddingTop();
        int leftPadding = cellView.getPaddingLeft();
        int bottomPadding = cellView.getPaddingBottom();
        int rightPadding = cellView.getPaddingRight();

        // Get dateTime of this cell
        DateTime dateTime = getCellDateTime(position);

        cellView.resetCustomStates();
        resetCustomResources(cellView);

        if (dateTime.equals(getToday())) {
            cellView.addCustomState(CellView.STATE_TODAY);
        }

        // Customize for disabled dates and date outside min/max dates
        if ((minDateTime != null && ((dateTime.getYear() < minDateTime.getYear())
                    || ((dateTime.getYear().equals(minDateTime.getYear())) && (dateTime.getMonth() < minDateTime.getMonth()))))
                || (maxDateTime != null && ((dateTime.getYear() > maxDateTime.getYear())
                    || ((dateTime.getYear().equals(maxDateTime.getYear())) && (dateTime.getMonth() > maxDateTime.getMonth()))))) {

            cellView.addCustomState(CellView.STATE_DISABLED);
        }

        cellView.refreshDrawableState();

        Map<Integer, String> texts = (Map<Integer, String>) caldroidData.get(MonthCaldroidFragment.CELL_TEXTS);
        if (texts != null) {
            String text = texts.get(dateTime.getMonth());
            cellView.setText(text);
        }

        // Set custom color if required
        setCustomResources(dateTime, cellView, cellView);

        // Somehow after setBackgroundResource, the padding collapse.
        // This is to recover the padding
        cellView.setPadding(leftPadding, topPadding, rightPadding,
                bottomPadding);
    }

    public DateTime getCellDateTime(int _position) {
        return new DateTime(year, _position + 1, 1, 0, 0, 0, 0);
    }

    @Override
    public int getCount() {
        return 12;
    }

    @Override
    public Object getItem(int position) {
        return getCellDateTime(position);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CellView cellView;

        // For reuse
        if (convertView == null) {
            final int squareDateCellResource = squareTextViewCell ? R.layout.square_date_cell : R.layout.normal_date_cell;
            cellView = (CellView) localInflater.inflate(squareDateCellResource, parent, false);
        } else {
            cellView = (CellView) convertView;
        }

        customizeTextView(position, cellView);

        return cellView;
    }
}
