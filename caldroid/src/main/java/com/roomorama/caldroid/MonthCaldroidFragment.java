package com.roomorama.caldroid;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.caldroid.R;
import com.caldroid.databinding.MonthCalendarViewBinding;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;
import hirondelle.date4j.DateTime;

public class MonthCaldroidFragment extends DialogFragment {
    private final YearViewPagerHelper viewPagerHelper = new YearViewPagerHelper();

    /*
     * Caldroid view components
     */
    MonthCalendarViewBinding binding;
    private TextView titleTextView;
    private MonthPageChangeListener pageChangeListener;

    private int themeResource = R.style.CaldroidDefault;
    private boolean clickableTitle = false;

    /*
     * Initial params key
     */
    public final static String
            DIALOG_TITLE = "dialogTitle",
            YEAR = "year",
            SHOW_NAVIGATION_ARROWS = "showNavigationArrows",
            DISABLE_DATES = "disableDates",
            SELECTED_DATES = "selectedDates",
            MIN_DATE = "minDate",
            MAX_DATE = "maxDate",
            ENABLE_SWIPE = "enableSwipe",
            ENABLE_CLICK_ON_DISABLED_DATES = "enableClickOnDisabledDates",
            SQUARE_TEXT_VIEW_CELL = "squareTextViewCell",
            THEME_RESOURCE = "themeResource",
            CLICKABLE_TITLE = "clickableTitle";

    /*
     * For internal use
     */
    public final static String
            MIN_DATE_TIME = "_minDateTime",
            MAX_DATE_TIME = "_maxDateTime",
            CELL_TEXTS = "celltexts",
            BACKGROUND_FOR_DATETIME_MAP = "_backgroundForDateTimeMap",
            TEXT_COLOR_FOR_DATETIME_MAP = "_textColorForDateTimeMap";

    /*
     * Initial data
     */
    protected String dialogTitle;
    protected int year = -1;
    protected final ArrayList<DateTime> disableDates = new ArrayList<>();
    protected final ArrayList<DateTime> selectedDates = new ArrayList<>();
    protected DateTime minDateTime;
    protected DateTime maxDateTime;

    /*
     * extraData belongs to client
     */
    protected Map<String, Object> extraData = new HashMap<>();

    /*
     * backgroundForDateMap holds background resource for each date
     */
    protected final Map<DateTime, Drawable> backgroundForDateTimeMap = new HashMap<>();

    /*
     * textColorForDateMap holds color for text for each date
     */
    protected final Map<DateTime, Integer> textColorForDateTimeMap = new HashMap<>();

    /*
     * To control the navigation
     */
    protected boolean enableSwipe = true;
    protected boolean showNavigationArrows = true;
    protected boolean enableClickOnDisabledDates = false;

    /*
     * To use SquareTextView to display Date cell.By default, it is true,
     * however in many cases with compact screen, it can be collapsed to save space
     */
    protected boolean squareTextViewCell;

    /*
     * dateItemClickListener is fired when user click on the date cell
     */
    private AdapterView.OnItemClickListener monthItemClickListener;

    /*
     * dateItemLongClickListener is fired when user does a longclick on the date
     * cell
     */
    private AdapterView.OnItemLongClickListener monthItemLongClickListener;

    /*
     * caldroidListener inform library client of the event happens inside
     * Caldroid
     */
    private MonthCaldroidListener monthCaldroidListener;

    /*
     * Retrieve current year
     * @return
     */
    public int getYear() {
        return year;
    }

    public MonthCaldroidListener getMonthCaldroidListener() {
        return monthCaldroidListener;
    }

    public MonthGridAdapter getNewMonthsGridAdapter(int year) {
        Map<String, Object> caldroidData = getCaldroidData();

        if (monthCaldroidListener != null) {
            Map<DateTime, Drawable> backgrounds = monthCaldroidListener.getBackgroundMonthMap(year);
            caldroidData.put(BACKGROUND_FOR_DATETIME_MAP, backgrounds);
            caldroidData.put(CELL_TEXTS, monthCaldroidListener.getCellTexts(year));
        }

        return new MonthGridAdapter(requireActivity(), year,
                caldroidData, extraData);
    }

    /*
     * For client wants to access ViewPager
     *
     * @return
     */
    public ViewPager2 getMonthViewPager() {
        return binding.infinitePager;
    }


    /*
     * For client to access background and text color maps
     */
    public Map<DateTime, Drawable> getBackgroundForDateTimeMap() {
        return backgroundForDateTimeMap;
    }

    public Map<DateTime, Integer> getTextColorForDateTimeMap() {
        return textColorForDateTimeMap;
    }

    /*
     * To let user customize the navigation buttons
     */
    public Button getLeftArrowButton() {
        return binding.calendarLeftArrow;
    }

    public Button getRightArrowButton() {
        return binding.calendarRightArrow;
    }

    /*
     * To let client customize month title textview
     */
    public TextView getTitleTextView() {
        return titleTextView;
    }

    public void setTitleTextView(TextView titleTextView) {
        this.titleTextView = titleTextView;
    }

    /*
     * caldroidData return data belong to Caldroid
     *
     * @return
     */
    public Map<String, Object> getCaldroidData() {
        Map<String, Object> caldroidData = new HashMap<>();

        caldroidData.put(DISABLE_DATES, disableDates);
        caldroidData.put(SELECTED_DATES, selectedDates);
        caldroidData.put(MIN_DATE_TIME, minDateTime);
        caldroidData.put(MAX_DATE_TIME, maxDateTime);
        caldroidData.put(SQUARE_TEXT_VIEW_CELL, squareTextViewCell);
        caldroidData.put(THEME_RESOURCE, themeResource);


        // For internal use
        caldroidData
                .put(BACKGROUND_FOR_DATETIME_MAP, backgroundForDateTimeMap);
        caldroidData.put(TEXT_COLOR_FOR_DATETIME_MAP, textColorForDateTimeMap);

        return caldroidData;
    }

    /*
     * Extra data is data belong to Client
     *
     * @return
     */
    public Map<String, Object> getExtraData() {
        return extraData;
    }

    /*
     * Client can set custom data in this HashMap
     *
     * @param extraData
     */
    public void setExtraData(Map<String, Object> extraData) {
        this.extraData = extraData;
    }

    /*
     * Set backgroundForDateMap
     */
    public void setBackgroundDrawableForDates(
            Map<Date, Drawable> backgroundForDateMap) {
        if (backgroundForDateMap == null || backgroundForDateMap.size() == 0) {
            return;
        }

        backgroundForDateTimeMap.clear();

        for (Date date : backgroundForDateMap.keySet()) {
            Drawable drawable = backgroundForDateMap.get(date);
            DateTime dateTime = CalendarHelper.convertDateToDateTime(date);
            backgroundForDateTimeMap.put(dateTime, drawable);
        }
    }

    public void clearBackgroundDrawableForDates(List<Date> dates) {
        if (dates == null || dates.size() == 0) {
            return;
        }

        for (Date date : dates) {
            clearBackgroundDrawableForDate(date);
        }
    }

    public void setBackgroundDrawableForDateTimes(
            Map<DateTime, Drawable> backgroundForDateTimeMap) {
        this.backgroundForDateTimeMap.putAll(backgroundForDateTimeMap);
    }

    public void clearBackgroundDrawableForDateTimes(List<DateTime> dateTimes) {
        if (dateTimes == null || dateTimes.size() == 0) return;

        for (DateTime dateTime : dateTimes) {
            backgroundForDateTimeMap.remove(dateTime);
        }
    }

    public void setBackgroundDrawableForDate(Drawable drawable, Date date) {
        DateTime dateTime = CalendarHelper.convertDateToDateTime(date);
        backgroundForDateTimeMap.put(dateTime, drawable);
    }

    public void clearBackgroundDrawableForDate(Date date) {
        DateTime dateTime = CalendarHelper.convertDateToDateTime(date);
        backgroundForDateTimeMap.remove(dateTime);
    }

    public void setBackgroundDrawableForDateTime(Drawable drawable,
                                                 DateTime dateTime) {
        backgroundForDateTimeMap.put(dateTime, drawable);
    }

    public void clearBackgroundDrawableForDateTime(DateTime dateTime) {
        backgroundForDateTimeMap.remove(dateTime);
    }

    /*
     * Set textColorForDateMap
     *
     * @return
     */
    public void setTextColorForDates(Map<Date, Integer> textColorForDateMap) {
        if (textColorForDateMap == null || textColorForDateMap.size() == 0) {
            return;
        }

        textColorForDateTimeMap.clear();

        for (Date date : textColorForDateMap.keySet()) {
            Integer resource = textColorForDateMap.get(date);
            DateTime dateTime = CalendarHelper.convertDateToDateTime(date);
            textColorForDateTimeMap.put(dateTime, resource);
        }
    }

    public void clearTextColorForDates(List<Date> dates) {
        if (dates == null || dates.size() == 0) return;

        for (Date date : dates) {
            clearTextColorForDate(date);
        }
    }

    public void setTextColorForDateTimes(
            Map<DateTime, Integer> textColorForDateTimeMap) {
        this.textColorForDateTimeMap.putAll(textColorForDateTimeMap);
    }

    public void setTextColorForDate(int textColorRes, Date date) {
        DateTime dateTime = CalendarHelper.convertDateToDateTime(date);
        textColorForDateTimeMap.put(dateTime, textColorRes);
    }

    public void clearTextColorForDate(Date date) {
        DateTime dateTime = CalendarHelper.convertDateToDateTime(date);
        textColorForDateTimeMap.remove(dateTime);
    }

    public void setTextColorForDateTime(int textColorRes, DateTime dateTime) {
        textColorForDateTimeMap.put(dateTime, textColorRes);
    }

    /*
     * Get current saved sates of the Caldroid. Useful for handling rotation.
     * It does not need to save state of SQUARE_TEXT_VIEW_CELL because this
     * may change on orientation change
     */
    public Bundle getSavedStates() {
        Bundle bundle = new Bundle();
        bundle.putInt(YEAR, year);

        if (dialogTitle != null) {
            bundle.putString(DIALOG_TITLE, dialogTitle);
        }

        if (selectedDates.size() > 0) {
            bundle.putStringArrayList(SELECTED_DATES,
                    CalendarHelper.convertToStringList(selectedDates));
        }

        if (disableDates.size() > 0) {
            bundle.putStringArrayList(DISABLE_DATES,
                    CalendarHelper.convertToStringList(disableDates));
        }

        if (minDateTime != null) {
            bundle.putString(MIN_DATE, minDateTime.format("YYYY-MM-DD"));
        }

        if (maxDateTime != null) {
            bundle.putString(MAX_DATE, maxDateTime.format("YYYY-MM-DD"));
        }

        bundle.putBoolean(SHOW_NAVIGATION_ARROWS, showNavigationArrows);
        bundle.putBoolean(ENABLE_SWIPE, enableSwipe);
        bundle.putInt(THEME_RESOURCE, themeResource);
        bundle.putBoolean(CLICKABLE_TITLE, clickableTitle);

        Bundle args = getArguments();
        if (args != null && args.containsKey(SQUARE_TEXT_VIEW_CELL)) {
            bundle.putBoolean(SQUARE_TEXT_VIEW_CELL, args.getBoolean(SQUARE_TEXT_VIEW_CELL));
        }

        return bundle;
    }

    /*
     * Save current state to bundle outState
     *
     * @param outState
     * @param key
     */
    public void saveStatesToKey(Bundle outState, String key) {
        outState.putBundle(key, getSavedStates());
    }

    /*
     * Restore current states from savedInstanceState
     *
     * @param savedInstanceState
     * @param key
     */
    public void restoreStatesFromKey(Bundle savedInstanceState, String key) {
        if (savedInstanceState != null && savedInstanceState.containsKey(key)) {
            Bundle caldroidSavedState = savedInstanceState.getBundle(key);
            setArguments(caldroidSavedState);
        }
    }

    /*
     * Restore state for dialog
     *
     * @param savedInstanceState
     * @param key
     * @param dialogTag
     */
    public void restoreDialogStatesFromKey(FragmentManager manager,
                                           Bundle savedInstanceState, String key, String dialogTag) {
        restoreStatesFromKey(savedInstanceState, key);

        MonthCaldroidFragment existingDialog = (MonthCaldroidFragment) manager
                .findFragmentByTag(dialogTag);
        if (existingDialog != null) {
            existingDialog.dismiss();
            show(manager, dialogTag);
        }
    }

    /*
     * Move calendar to the specified date
     *
     * @param date
     */
    public void moveToDate(Date date) {
        moveToDateTime(CalendarHelper.convertDateToDateTime(date));
    }

    /*
     * Move calendar to specified dateTime, with animation
     *
     * @param dateTime
     */
    public void moveToDateTime(DateTime dateTime) {

        DateTime firstOfMonth = new DateTime(year, 1, 1, 0, 0, 0, 0);
        DateTime lastOfMonth = firstOfMonth.getEndOfMonth();

        // To create a swipe effect
        // Do nothing if the dateTime is in current month

        // Calendar swipe left when dateTime is in the past
        if (dateTime.lt(firstOfMonth)) {
            // Get next month of dateTime. When swipe left, month will
            // decrease
            DateTime firstDayNextMonth = dateTime.plus(0, 1, 0, 0, 0, 0, 0,
                    DateTime.DayOverflow.LastDay);

            // Refresh adapters
            setCalendarDateTime(firstDayNextMonth);
            int currentItem = binding.infinitePager.getCurrentItem();
            pageChangeListener.setCurrentPage(currentItem);

            // Swipe left
            binding.infinitePager.setCurrentItem(currentItem - 1);
        }

        // Calendar swipe right when dateTime is in the future
        else if (dateTime.gt(lastOfMonth)) {
            // Get last month of dateTime. When swipe right, the month will
            // increase
            DateTime firstDayLastMonth = dateTime.minus(0, 1, 0, 0, 0, 0, 0,
                    DateTime.DayOverflow.LastDay);

            // Refresh adapters
            setCalendarDateTime(firstDayLastMonth);
            int currentItem = binding.infinitePager.getCurrentItem();
            pageChangeListener.setCurrentPage(currentItem);

            // Swipe right
            binding.infinitePager.setCurrentItem(currentItem + 1);
        }

    }

    /*
     * Set month and year for the calendar. This is to avoid naive
     * implementation of manipulating month and year. All dates within same
     * month/year give same result
     *
     * @param date
     */
    public void setCalendarDate(Date date) {
        setCalendarDateTime(CalendarHelper.convertDateToDateTime(date));
    }

    public void setCalendarDateTime(DateTime dateTime) {
        year = dateTime.getYear();

        // Notify listener
        if (monthCaldroidListener != null) {
            monthCaldroidListener.onChangeYear(year);
        }

        refreshView();
    }

    /*
     * Set calendar to previous month
     */
    public void prevMonth() {
        binding.infinitePager.setCurrentItem(pageChangeListener.getCurrentPage() - 1);
    }

    /*
     * Set calendar to next month
     */
    public void nextMonth() {
        binding.infinitePager.setCurrentItem(pageChangeListener.getCurrentPage() + 1);
    }

    public int getCurrentPagerPoistion() {
        return binding.infinitePager.getCurrentItem();
    }

    /*
     * Clear all disable dates. Notice this does not refresh the calendar, need
     * to explicitly call refreshView()
     */
    public void clearDisableDates() {
        disableDates.clear();
    }

    /*
     * Set disableDates from ArrayList of Date
     *
     * @param disableDateList
     */
    public void setDisableDates(ArrayList<Date> disableDateList) {
        if (disableDateList == null || disableDateList.size() == 0) {
            return;
        }

        disableDates.clear();

        for (Date date : disableDateList) {
            DateTime dateTime = CalendarHelper.convertDateToDateTime(date);
            disableDates.add(dateTime);
        }

    }

    /*
     * Set disableDates from ArrayList of String. By default, the date formatter
     * is yyyy-MM-dd. For e.g 2013-12-24
     *
     * @param disableDateStrings
     */
    public void setDisableDatesFromString(ArrayList<String> disableDateStrings) {
        setDisableDatesFromString(disableDateStrings, null);
    }

    /*
     * Set disableDates from ArrayList of String with custom date format. For
     * example, if the date string is 06-Jan-2013, use date format dd-MMM-yyyy.
     * This method will refresh the calendar, it's not necessary to call
     * refreshView()
     *
     * @param disableDateStrings
     * @param dateFormat
     */
    public void setDisableDatesFromString(ArrayList<String> disableDateStrings,
                                          String dateFormat) {
        if (disableDateStrings == null) {
            return;
        }

        disableDates.clear();

        for (String dateString : disableDateStrings) {
            DateTime dateTime = CalendarHelper.getDateTimeFromString(
                    dateString, dateFormat);
            disableDates.add(dateTime);
        }
    }

    /*
     * To clear selectedDates. This method does not refresh view, need to
     * explicitly call refreshView()
     */
    public void clearSelectedDates() {
        selectedDates.clear();
    }

    /*
     * Select the dates from fromDate to toDate. By default the background color
     * is holo_blue_light, and the text color is black. You can customize the
     * background by changing DateCaldroidFragment.selectedBackgroundDrawable, and
     * change the text color DateCaldroidFragment.selectedTextColor before call this
     * method. This method does not refresh view, need to call refreshView()
     *
     * @param fromDate
     * @param toDate
     */
    public void setSelectedDates(Date fromDate, Date toDate) {
        // Ensure fromDate is before toDate
        if (fromDate == null || toDate == null || fromDate.after(toDate)) {
            return;
        }

        selectedDates.clear();

        DateTime fromDateTime = CalendarHelper.convertDateToDateTime(fromDate);
        DateTime toDateTime = CalendarHelper.convertDateToDateTime(toDate);

        DateTime dateTime = fromDateTime;
        while (dateTime.lt(toDateTime)) {
            selectedDates.add(dateTime);
            dateTime = dateTime.plusDays(1);
        }
        selectedDates.add(toDateTime);
    }

    /*
     * Convenient method to select dates from String
     *
     * @param fromDateString
     * @param toDateString
     * @param dateFormat
     * @throws ParseException
     */
    public void setSelectedDateStrings(String fromDateString,
                                       String toDateString, String dateFormat) throws ParseException {

        Date fromDate = CalendarHelper.getDateFromString(fromDateString,
                dateFormat);
        Date toDate = CalendarHelper
                .getDateFromString(toDateString, dateFormat);
        setSelectedDates(fromDate, toDate);
    }

    /*
     * Select single date
     * @author Alov Maxim <alovmax@yandex.ru>
     */
    public void setSelectedDate(Date date) {
        if (date == null) {
            return;
        }
        DateTime dateTime = CalendarHelper.convertDateToDateTime(date);
        selectedDates.add(dateTime);
    }

    /*
     * Clear selection of the specified date
     * @author Alov Maxim <alovmax@yandex.ru>
     */
    public void clearSelectedDate(Date date) {
        if (date == null) {
            return;
        }
        DateTime dateTime = CalendarHelper.convertDateToDateTime(date);
        selectedDates.remove(dateTime);
    }

    /*
     * Checks whether the specified date is selected
     * @author Alov Maxim <alovmax@yandex.ru>
     */
    public boolean isSelectedDate(Date date) {
        if (date == null) {
            return false;
        }
        DateTime dateTime = CalendarHelper.convertDateToDateTime(date);
        return selectedDates.contains(dateTime);
    }

    /*
     * Check if the navigation arrow is shown
     *
     * @return
     */
    public boolean isShowNavigationArrows() {
        return showNavigationArrows;
    }

    /*
     * Show or hide the navigation arrows
     *
     * @param showNavigationArrows
     */
    public void setShowNavigationArrows(boolean showNavigationArrows) {
        this.showNavigationArrows = showNavigationArrows;
        if (showNavigationArrows) {
            binding.calendarLeftArrow.setVisibility(View.VISIBLE);
            binding.calendarRightArrow.setVisibility(View.VISIBLE);
        } else {
            binding.calendarLeftArrow.setVisibility(View.INVISIBLE);
            binding.calendarRightArrow.setVisibility(View.INVISIBLE);
        }
    }

    /*
     * Enable / Disable swipe to navigate different months
     *
     * @return
     */
    public boolean isEnableSwipe() {
        return enableSwipe;
    }

    public void setEnableSwipe(boolean enableSwipe) {
        this.enableSwipe = enableSwipe;
        binding.infinitePager.setEnabled(enableSwipe);
    }

    /*
     * Set min date. This method does not refresh view
     *
     * @param minDate
     */
    public void setMinDate(Date minDate) {
        if (minDate == null) {
            minDateTime = null;
        } else {
            minDateTime = CalendarHelper.convertDateToDateTime(minDate);
        }
    }

    /*
     * Convenient method to set min date from String. If dateFormat is null,
     * default format is yyyy-MM-dd
     *
     * @param minDateString
     * @param dateFormat
     */
    public void setMinDateFromString(String minDateString, String dateFormat) {
        if (minDateString == null) {
            setMinDate(null);
        } else {
            minDateTime = CalendarHelper.getDateTimeFromString(minDateString,
                    dateFormat);
        }
    }

    @Nullable
    public DateTime getMinDateTime() {
        return minDateTime;
    }

    /*
     * Set max date. This method does not refresh view
     *
     * @param maxDate
     */
    public void setMaxDate(Date maxDate) {
        if (maxDate == null) {
            maxDateTime = null;
        } else {
            maxDateTime = CalendarHelper.convertDateToDateTime(maxDate);
        }
    }

    /*
     * Convenient method to set max date from String. If dateFormat is null,
     * default format is yyyy-MM-dd
     *
     * @param maxDateString
     * @param dateFormat
     */
    public void setMaxDateFromString(String maxDateString, String dateFormat) {
        if (maxDateString == null) {
            setMaxDate(null);
        } else {
            maxDateTime = CalendarHelper.getDateTimeFromString(maxDateString,
                    dateFormat);
        }
    }

    @Nullable
    public DateTime getMaxDateTime() {
        return maxDateTime;
    }

    /*
     * Set caldroid listener when user click on a date
     *
     * @param caldroidListener
     */
    public void setMonthCaldroidListener(MonthCaldroidListener monthCaldroidListener) {
        this.monthCaldroidListener = monthCaldroidListener;
    }

    /*
     * Callback to listener when date is valid (not disable, not outside of
     * min/max date)
     *
     * @return
     */
    public AdapterView.OnItemClickListener getMonthItemClickListener() {
        if (monthItemClickListener == null) {
            //noinspection Convert2Lambda
            monthItemClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    if (monthCaldroidListener != null) {
                        MonthGridAdapter pageAdapter = getPageAdapter(getCurrentPagerPoistion());
                        if (pageAdapter != null) {
                            DateTime dateTime = pageAdapter.getCellDateTime(position);

                            if (!enableClickOnDisabledDates) {
                                if (minDateTime != null && dateTime
                                        .lt(minDateTime) || maxDateTime != null && dateTime
                                        .gt(maxDateTime) || disableDates.contains(dateTime)) {
                                    return;
                                }
                            }

                            Date date = CalendarHelper
                                    .convertDateTimeToDate(dateTime);
                            monthCaldroidListener.onSelectDate(date, view);
                        }
                        else {
                            throw new InternalError("Current page adapter not found");
                        }
                    }
                }
            };
        }

        return monthItemClickListener;
    }

    public AdapterView.OnItemLongClickListener getMonthItemLongClickListener() {
        if (monthItemLongClickListener == null) {
            //noinspection Convert2Lambda
            monthItemLongClickListener = new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent,
                                               View view, int position, long id) {

                    if (monthCaldroidListener != null) {
                        MonthGridAdapter pageAdapter = getPageAdapter(getCurrentPagerPoistion());
                        if (pageAdapter != null) {
                            DateTime dateTime = pageAdapter.getCellDateTime(position);

                            if (!enableClickOnDisabledDates) {
                                if (minDateTime != null && dateTime
                                        .lt(minDateTime) || maxDateTime != null && dateTime
                                        .gt(maxDateTime) || disableDates.contains(dateTime)) {
                                    return false;
                                }
                            }
                            Date date = CalendarHelper
                                    .convertDateTimeToDate(dateTime);
                            monthCaldroidListener.onLongClickDate(date, view);
                        }
                        else {
                            throw new InternalError("Current page adapter not found");
                        }
                    }

                    return true;
                }
            };
        }

        return monthItemLongClickListener;
    }

    /*
     * Refresh month title text view when user swipe
     */
    protected void refreshTitleTextView() {
        titleTextView.setText(String.valueOf(year));
    }

    /*
     * Refresh view when parameter changes. You should always change all
     * parameters first, then call this method.
     */
    public void refreshView() {
        // If month and year is not yet initialized, refreshView doesn't do
        // anything
        if (year == -1) {
            return;
        }

        refreshTitleTextView();
    }

    /*
     * Retrieve initial arguments to the fragment Data can include: month, year,
     * dialogTitle, showNavigationArrows,(String) disableDates, selectedDates,
     * minDate, maxDate, squareTextViewCell
     */
    protected void retrieveInitialArgs() {
        // Get arguments
        Bundle args = getArguments();

        CalendarHelper.setup();

        if (args != null) {
            // Get month, year
            year = args.getInt(YEAR, -1);
            dialogTitle = args.getString(DIALOG_TITLE);
            Dialog dialog = getDialog();
            if (dialog != null) {
                if (dialogTitle != null) {
                    dialog.setTitle(dialogTitle);
                } else {
                    // Don't display title bar if user did not supply
                    // dialogTitle
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                }
            }

            // Should show arrow
            showNavigationArrows = args
                    .getBoolean(SHOW_NAVIGATION_ARROWS, true);

            // Should enable swipe to change month
            enableSwipe = args.getBoolean(ENABLE_SWIPE, true);

            // Get squareTextViewCell, by default, use square cell in portrait mode
            // and using normal cell in landscape mode
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                squareTextViewCell = args.getBoolean(SQUARE_TEXT_VIEW_CELL, true);
            } else {
                squareTextViewCell = args.getBoolean(SQUARE_TEXT_VIEW_CELL, false);
            }

            // Get clickable setting
            enableClickOnDisabledDates = args.getBoolean(
                    ENABLE_CLICK_ON_DISABLED_DATES, false);

            // Get disable dates
            ArrayList<String> disableDateStrings = args
                    .getStringArrayList(DISABLE_DATES);
            if (disableDateStrings != null && disableDateStrings.size() > 0) {
                disableDates.clear();
                for (String dateString : disableDateStrings) {
                    DateTime dt = CalendarHelper.getDateTimeFromString(
                            dateString, null);
                    disableDates.add(dt);
                }
            }

            // Get selected dates
            ArrayList<String> selectedDateStrings = args
                    .getStringArrayList(SELECTED_DATES);
            if (selectedDateStrings != null && selectedDateStrings.size() > 0) {
                selectedDates.clear();
                for (String dateString : selectedDateStrings) {
                    DateTime dt = CalendarHelper.getDateTimeFromString(
                            dateString, null);
                    selectedDates.add(dt);
                }
            }

            // Get min date and max date
            String minDateTimeString = args.getString(MIN_DATE);
            if (minDateTimeString != null) {
                minDateTime = CalendarHelper.getDateTimeFromString(
                        minDateTimeString, null);
            }

            String maxDateTimeString = args.getString(MAX_DATE);
            if (maxDateTimeString != null) {
                maxDateTime = CalendarHelper.getDateTimeFromString(
                        maxDateTimeString, null);
            }

            // Get theme
            themeResource = args.getInt(THEME_RESOURCE, R.style.CaldroidDefault);
            clickableTitle = args.getBoolean(CLICKABLE_TITLE, false);
        }

        if (year == -1) {
            DateTime dateTime = DateTime.today(TimeZone.getDefault());
            year = dateTime.getYear();
        }
    }

    /*
     * To support faster init
     *
     * @param dialogTitle
     * @param month
     * @param year
     * @return
     */
    public static MonthCaldroidFragment newInstance(String dialogTitle, int year) {
        MonthCaldroidFragment f = new MonthCaldroidFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, dialogTitle);
        args.putInt(YEAR, year);

        f.setArguments(args);

        return f;
    }

    /*
     * Below code fixed the issue viewpager disappears in dialog mode on
     * orientation change
     * <p/>
     * Code taken from Andy Dennie and Zsombor Erdody-Nagy
     * http://stackoverflow.com/questions/8235080/fragments-dialogfragment
     * -and-screen-rotation
     */
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    public void setThemeResource(int id) {
        themeResource = id;
    }

    public int getThemeResource() {
        return themeResource;
    }

    public static LayoutInflater getThemeInflater(Context context, LayoutInflater origInflater, int themeResource) {
        Context wrapped = new ContextThemeWrapper(context, themeResource);
        return origInflater.cloneInContext(wrapped);
    }

    /*
     * Setup view
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        retrieveInitialArgs();

        // To support keeping instance for dialog
        if (getDialog() != null) {
            try {
                setRetainInstance(true);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        LayoutInflater localInflater = getThemeInflater(getActivity(), inflater, themeResource);

        // This is a hack to fix issue localInflater doesn't use the themeResource, make Android
        // complain about layout_width and layout_height missing. I'm unsure about its impact
        // for app that wants to change theme dynamically.
        requireActivity().setTheme(themeResource);

        binding = MonthCalendarViewBinding.inflate(localInflater, container, false);

        // For the monthTitleTextView
        titleTextView = binding.calendarYearTextview;

        // Navigate to previous month when user click
        binding.calendarLeftArrow.setOnClickListener(v -> prevMonth());

        // Navigate to next month when user click
        binding.calendarRightArrow.setOnClickListener(v -> nextMonth());

        if (clickableTitle) {
            binding.calendarYearTextview.setOnClickListener(v -> {
                if (monthCaldroidListener != null) {
                    monthCaldroidListener.onTitleClicked(year);
                }
            });
        }

        // Show navigation arrows depend on initial arguments
        setShowNavigationArrows(showNavigationArrows);

        // Setup all the pages of date grid views. These pages are recycled
        setupDateGridPages();

        // Refresh view
        refreshView();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inform client that all views are created and not null
        // Client should perform customization for buttons and textviews here
        if (monthCaldroidListener != null) {
            monthCaldroidListener.onCaldroidViewCreated();
        }
    }

    public void resizeViewPager(@NonNull View _childView) {
        viewPagerHelper.resizeCalendarViewPager(binding.infinitePager, _childView);
    }

    /*
     * This method can be used to provide different gridview.
     *
     * @return
     */
    protected int getGridViewRes() {
        return R.layout.month_grid_fragment;
    }

    /*
     * Setup 4 pages contain date grid views. These pages are recycled to use
     * memory efficient
     *
     * @param view
     */
    private void setupDateGridPages() {
        // Get current date time
        DateTime shownDateTime = new DateTime(year, 1, 1, 0, 0, 0, 0);

        // Set enable swipe
        binding.infinitePager.setEnabled(enableSwipe);

        // MonthPagerAdapter
        mYearPagerAdapter = new YearPagerAdapter(
                getChildFragmentManager(), getLifecycle(), this);

        // Set to pageChangeListener
        pageChangeListener = new MonthPageChangeListener(mYearPagerAdapter.getItemCount() - 1);
        setCalendarDateTime(shownDateTime);

        // Use the infinitePagerAdapter to provide data for dateViewPager
        binding.infinitePager.setAdapter(mYearPagerAdapter);

        binding.infinitePager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                String childTag = getViewPager2FragmentTag(position);
                Fragment child = getChildFragmentManager().findFragmentByTag(childTag);
                if (child != null) {
                    View childView = child.getView();
                    if (childView != null) {
                        resizeViewPager(childView);
                    }
                }
            }
        });

        // Setup pageChangeListener
        binding.infinitePager.registerOnPageChangeCallback(pageChangeListener);
        int pos = mYearPagerAdapter.dateTimeToPosition(shownDateTime);
        binding.infinitePager.setCurrentItem(pos, false);
    }

    private YearPagerAdapter mYearPagerAdapter;

    @NonNull
    public MonthGridFragment createMonthGridFragment(int _position) {
        MonthGridFragment monthGridFragment = new MonthGridFragment();

        DateTime fragmentDateTime = mYearPagerAdapter.positionToDateTime(_position);
        MonthGridAdapter adapter = getNewMonthsGridAdapter(fragmentDateTime.getYear());

        monthGridFragment.setGridViewRes(getGridViewRes());
        monthGridFragment.setGridAdapter(adapter);
        monthGridFragment.setOnItemClickListener(getMonthItemClickListener());
        monthGridFragment
                .setOnItemLongClickListener(getMonthItemLongClickListener());

        return monthGridFragment;
    }

    public static String getViewPager2FragmentTag(int _position) {
        return "f" + _position;
    }

    @Nullable
    MonthGridAdapter getPageAdapter(int _position) {
        Fragment currentPageFragment = getChildFragmentManager().findFragmentByTag(getViewPager2FragmentTag(_position));
        if (currentPageFragment instanceof MonthGridFragment) {
            MonthGridFragment currentMonthGrid = (MonthGridFragment) currentPageFragment;
            return currentMonthGrid.getGridAdapter();
        }
        else {
            return null;
        }
    }

    /*
     * MonthPageChangeListener refresh the date grid views when user swipe the
     * calendar
     *
     * @author thomasdao
     */
    public class MonthPageChangeListener extends ViewPager2.OnPageChangeCallback {
        private int currentPage;

        public MonthPageChangeListener(int _initialPos) {
            currentPage = _initialPos;
        }
        /*
         * Return currentPage of the dateViewPager
         *
         * @return
         */
        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        @Override
        public void onPageScrollStateChanged(int position) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        /*
         * Refresh the fragments
         */
        @Override
        public void onPageSelected(int position) {
            setCurrentPage(position);

            // Update current date time of the selected page
            setCalendarDateTime(mYearPagerAdapter.positionToDateTime(position));
        }
    }
}
