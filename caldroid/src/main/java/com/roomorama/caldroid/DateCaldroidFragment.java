package com.roomorama.caldroid;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.caldroid.R;
import com.caldroid.databinding.DateCalendarViewBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import hirondelle.date4j.DateTime;

/*
 * Caldroid is a fragment that display calendar with dates in a month. Caldroid
 * can be used as embedded fragment, or as dialog fragment. <br/>
 * <br/>
 * Caldroid fragment includes 4 main parts:<br/>
 * <br/>
 * 1) Month title view: show the month and year (e.g MARCH, 2013) <br/>
 * <br/>
 * 2) Navigation arrows: to navigate to next month or previous month <br/>
 * <br/>
 * 3) Weekday gridview: contains only 1 row and 7 columns. To display
 * "SUN, MON, TUE, WED, THU, FRI, SAT" <br/>
 * <br/>
 * 4) An infinite view pager that allow user to swipe left/right to change
 * month. This library is taken from
 * https://github.com/antonyt/InfiniteViewPager <br/>
 * <br/>
 * This infinite view pager recycles 4 fragment, each fragment contains a grid
 * view with 7 columns to display the dates in month. Whenever user swipes
 * different screen, the date grid views are updated. <br/>
 * <br/>
 * Caldroid fragment supports setting min/max date, selecting dates in a range,
 * setting disabled dates, highlighting today. It includes convenient methods to
 * work with date and string, enable or disable the navigation arrows. User can
 * also swipe left/right to change months.<br/>
 * <br/>
 * Caldroid code is simple and clean partly because of powerful Date4J DateTime
 * library!
 *
 * @author thomasdao
 */

@SuppressLint("DefaultLocale")
public class DateCaldroidFragment extends DialogFragment {
    /*
     * Weekday conventions
     */
    public static final int
            SUNDAY = 1,
            /** @noinspection unused*/
            MONDAY = 2,
            /** @noinspection unused*/
            TUESDAY = 3,
            /** @noinspection unused*/
            WEDNESDAY = 4,
            /** @noinspection unused*/
            THURSDAY = 5,
            /** @noinspection unused*/
            FRIDAY = 6,
            /** @noinspection unused*/
            SATURDAY = 7;

    /*
     * Flags to display month
     */
    private static final int MONTH_YEAR_FLAG = DateUtils.FORMAT_SHOW_DATE
            | DateUtils.FORMAT_NO_MONTH_DAY | DateUtils.FORMAT_SHOW_YEAR;

    /*
     * First day of month time
     */
    private final GregorianCalendar firstMonthTime = new GregorianCalendar();

    /*
     * Reuse formatter to print "MMMM yyyy" format
     */
    private final StringBuilder monthYearStringBuilder = new StringBuilder(50);
    private final Formatter monthYearFormatter = new Formatter(
            monthYearStringBuilder, Locale.getDefault());

    private final MonthViewPagerHelper viewPagerHelper = new MonthViewPagerHelper();

    /*
     * Caldroid view components
     */
    DateCalendarViewBinding binding;
    private TextView titleTextView;
    private DatePageChangeListener pageChangeListener;

    private int themeResource = R.style.CaldroidDefault;
    private boolean clickableTitle = false;

    /*
     * Initial params key
     */
    public final static String
            DIALOG_TITLE = "dialogTitle",
            MONTH = "month",
            YEAR = "year",
            SHOW_NAVIGATION_ARROWS = "showNavigationArrows",
            DISABLE_DATES = "disableDates",
            SELECTED_DATES = "selectedDates",
            MIN_DATE = "minDate",
            MAX_DATE = "maxDate",
            ENABLE_SWIPE = "enableSwipe",
            START_DAY_OF_WEEK = "startDayOfWeek",
            SIX_WEEKS_IN_CALENDAR = "sixWeeksInCalendar",
            ENABLE_CLICK_ON_DISABLED_DATES = "enableClickOnDisabledDates",
            SQUARE_TEXT_VIEW_CELL = "squareTextViewCell",
            THEME_RESOURCE = "themeResource",
            CLICKABLE_TITLE = "clickableYear";

    /*
     * For internal use
     */
    public final static String
            MIN_DATE_TIME = "minDateTime",
            MAX_DATE_TIME = "maxDateTime",
            BACKGROUND_FOR_DATETIME_MAP = "backgroundForDateTimeMap",
            TEXT_COLOR_FOR_DATETIME_MAP = "textColorForDateTimeMap";

    private static final String STATE_BUNDLE_KEY = "CALDROID_DATE_SAVED_STATE";

    /*
     * Initial data
     */
    protected String dialogTitle;
    protected int month = -1;
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
     * First column of calendar is Sunday
     */
    protected int startDayOfWeek = SUNDAY;

    /*
     * A calendar height is not fixed, it may have 5 or 6 rows. Set fitAllMonths
     * to true so that the calendar will always have 6 rows
     */
    private boolean sixWeeksInCalendar = true;

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
    private OnItemClickListener dateItemClickListener;

    /*
     * dateItemLongClickListener is fired when user does a longclick on the date
     * cell
     */
    private OnItemLongClickListener dateItemLongClickListener;

    /*
     * dateCaldroidListener inform library client of the event happens inside
     * Caldroid
     */
    private DateCaldroidListener dateCaldroidListener;

    private CaldroidViewModel caldroidViewModel;

    /*
     * Retrieve current month
     * @return
     */
    public int getMonth() {
        return month;
    }

    /*
     * Retrieve current year
     * @return
     */
    public int getYear() {
        return year;
    }

    public DateCaldroidListener getCaldroidListener() {
        return dateCaldroidListener;
    }

    /*
     * Meant to be subclassed. User who wants to provide custom view, need to
     * provide custom adapter here
     */
    public DateGridAdapter getNewDatesGridAdapter(int month, int year) {
        Map<String, Object> caldroidData = getCaldroidData();

        if (dateCaldroidListener != null) {
            Map<DateTime, Drawable> backgrounds = dateCaldroidListener.getBackgroundDateTimeMap(month, year);
            caldroidData.put(BACKGROUND_FOR_DATETIME_MAP, backgrounds);
        }

        return new DateGridAdapter(requireActivity(), month, year,
                caldroidData, extraData);
    }

    /*
     * Meant to be subclassed. User who wants to provide custom view, need to
     * provide custom adapter here
     */
    public WeekdayArrayAdapter getNewWeekdayAdapter(int themeResource) {
        return new WeekdayArrayAdapter(
                getActivity(), android.R.layout.simple_list_item_1,
                getDaysOfWeek(), themeResource);
    }

    /*
     * For client to customize the weekDayGridView
     *
     * @return
     */
    public GridView getWeekdayGridView() {
        return binding.weekdayGridview;
    }

    /*
     * For client wants to access dateViewPager
     *
     * @return
     */
    public ViewPager2 getDateViewPager() {
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
        caldroidData.put(START_DAY_OF_WEEK, startDayOfWeek);
        caldroidData.put(SIX_WEEKS_IN_CALENDAR, sixWeeksInCalendar);
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
        if (backgroundForDateMap == null || backgroundForDateMap.isEmpty()) {
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
        if (dates == null || dates.isEmpty()) {
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
        if (dateTimes == null || dateTimes.isEmpty()) return;

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
        if (textColorForDateMap == null || textColorForDateMap.isEmpty()) {
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
        if (dates == null || dates.isEmpty()) return;

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
        bundle.putInt(MONTH, month);
        bundle.putInt(YEAR, year);

        if (dialogTitle != null) {
            bundle.putString(DIALOG_TITLE, dialogTitle);
        }

        if (!selectedDates.isEmpty()) {
            bundle.putStringArrayList(SELECTED_DATES,
                    CalendarHelper.convertToStringList(selectedDates));
        }

        if (!disableDates.isEmpty()) {
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
        bundle.putInt(START_DAY_OF_WEEK, startDayOfWeek);
        bundle.putBoolean(SIX_WEEKS_IN_CALENDAR, sixWeeksInCalendar);
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

        DateCaldroidFragment existingDialog = (DateCaldroidFragment) manager
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

        DateTime firstOfMonth = new DateTime(year, month, 1, 0, 0, 0, 0);
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
        month = dateTime.getMonth();
        year = dateTime.getYear();

        // Notify listener
        if (dateCaldroidListener != null) {
            dateCaldroidListener.onChangeMonth(month, year);
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
        if (disableDateList == null || disableDateList.isEmpty()) {
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

    public boolean isSixWeeksInCalendar() {
        return sixWeeksInCalendar;
    }

    public void setSixWeeksInCalendar(boolean sixWeeksInCalendar) {
        this.sixWeeksInCalendar = sixWeeksInCalendar;
        viewPagerHelper.setSixWeeksInCalendar(sixWeeksInCalendar);
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
     * @param dateCaldroidListener
     */
    public void setCaldroidListener(DateCaldroidListener dateCaldroidListener) {
        this.dateCaldroidListener = dateCaldroidListener;
    }

    /*
     * Callback to listener when date is valid (not disable, not outside of
     * min/max date)
     *
     * @return
     */
    public OnItemClickListener getDateItemClickListener() {
        if (dateItemClickListener == null) {
            //noinspection Convert2Lambda
            dateItemClickListener = new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    DateGridAdapter pageAdapter = getPageAdapter(getCurrentPagerPoistion());
                    if (pageAdapter != null) {
                        DateTime dateTime = pageAdapter.getDatetimeList().get(position);

                        if (!enableClickOnDisabledDates) {
                            if (minDateTime != null && dateTime
                                    .lt(minDateTime) || maxDateTime != null && dateTime
                                    .gt(maxDateTime) || disableDates.contains(dateTime)) {
                                return;
                            }
                        }

                        Date date = CalendarHelper.convertDateTimeToDate(dateTime);
                        caldroidViewModel.selectDate(date);
                        if (dateCaldroidListener != null) {
                            dateCaldroidListener.onSelectDate(date, view);
                        }
                    }
                    else {
                        throw new InternalError("Current page adapter not found");
                    }
                }
            };
        }

        return dateItemClickListener;
    }

    /*
     * Callback to listener when date is valid (not disable, not outside of
     * min/max date)
     *
     * @return
     */
    public OnItemLongClickListener getDateItemLongClickListener() {
        if (dateItemLongClickListener == null) {
            //noinspection Convert2Lambda
            dateItemLongClickListener = new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent,
                                               View view, int position, long id) {

                    if (dateCaldroidListener != null) {
                        DateGridAdapter pageAdapter = getPageAdapter(getCurrentPagerPoistion());
                        if (pageAdapter != null) {
                            DateTime dateTime = pageAdapter.getDatetimeList().get(position);

                            if (!enableClickOnDisabledDates) {
                                if (minDateTime != null && dateTime
                                        .lt(minDateTime) || maxDateTime != null && dateTime
                                        .gt(maxDateTime) || disableDates.contains(dateTime)) {
                                    return false;
                                }
                            }
                            Date date = CalendarHelper
                                    .convertDateTimeToDate(dateTime);
                            dateCaldroidListener.onLongClickDate(date, view);
                        }
                        else {
                            throw new InternalError("Current page adapter not found");
                        }
                    }

                    return true;
                }
            };
        }

        return dateItemLongClickListener;
    }

    /*
     * Refresh month title text view when user swipe
     */
    protected void refreshTitleTextView() {
        // Refresh title view
        firstMonthTime.set(Calendar.YEAR, year);
        firstMonthTime.set(Calendar.MONTH, month - 1);
        firstMonthTime.set(android.icu.util.Calendar.DAY_OF_MONTH, 15);
        long millis = firstMonthTime.getTimeInMillis();

        // This is the method used by the platform Calendar app to get a
        // correctly localized month name for display on a wall calendar
        monthYearStringBuilder.setLength(0);
        String monthTitle = DateUtils.formatDateRange(getActivity(),
                monthYearFormatter, millis, millis, MONTH_YEAR_FLAG).toString();

        titleTextView.setText(monthTitle.toUpperCase(Locale.getDefault()));
    }

    /*
     * Refresh view when parameter changes. You should always change all
     * parameters first, then call this method.
     */
    public void refreshView() {
        // If month and year is not yet initialized, refreshView doesn't do
        // anything
        if (month == -1 || year == -1) {
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
            month = args.getInt(MONTH, -1);
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

            // Get start day of Week. Default calendar first column is SUNDAY
            startDayOfWeek = args.getInt(START_DAY_OF_WEEK, 1);
            if (startDayOfWeek > 7) {
                startDayOfWeek = startDayOfWeek % 7;
            }

            // Should show arrow
            showNavigationArrows = args
                    .getBoolean(SHOW_NAVIGATION_ARROWS, true);

            // Should enable swipe to change month
            enableSwipe = args.getBoolean(ENABLE_SWIPE, true);

            // Get sixWeeksInCalendar
            sixWeeksInCalendar = args.getBoolean(SIX_WEEKS_IN_CALENDAR, true);

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
            if (disableDateStrings != null && !disableDateStrings.isEmpty()) {
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
            if (selectedDateStrings != null && !selectedDateStrings.isEmpty()) {
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
        if (month == -1 || year == -1) {
            DateTime dateTime = DateTime.today(TimeZone.getDefault());
            month = dateTime.getMonth();
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
    public static DateCaldroidFragment newInstance(String dialogTitle, int month,
                                                   int year) {
        DateCaldroidFragment f = new DateCaldroidFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, dialogTitle);
        args.putInt(MONTH, month);
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

        binding = DateCalendarViewBinding.inflate(localInflater, container, false);

        // For the monthTitleTextView
        titleTextView = binding.calendarDaytitleButton;

        // Navigate to previous month when user click
        binding.calendarLeftArrow.setOnClickListener(v -> prevMonth());

        // Navigate to next month when user click
        binding.calendarRightArrow.setOnClickListener(v -> nextMonth());

        if (clickableTitle) {
            binding.calendarDaytitleButton.setOnClickListener(v -> {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.YEAR, year);
                caldroidViewModel.dateTitleClicked(cal.getTime());

                if (dateCaldroidListener != null) {
                    dateCaldroidListener.onTitleClicked(month, year);
                }
            });
        }
        // Show navigation arrows depend on initial arguments
        setShowNavigationArrows(showNavigationArrows);

        // For the weekday gridview ("SUN, MON, TUE, WED, THU, FRI, SAT")
        WeekdayArrayAdapter weekdaysAdapter = getNewWeekdayAdapter(themeResource);
        binding.weekdayGridview.setAdapter(weekdaysAdapter);

        // Setup all the pages of date grid views. These pages are recycled
        setupDateGridPages();

        // Refresh view
        refreshView();

        return binding.getRoot();
    }

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            restoreStatesFromKey(savedInstanceState, STATE_BUNDLE_KEY);
        }

        caldroidViewModel = new ViewModelProvider(requireActivity()).get(CaldroidViewModel.class);

		// Inform client that all views are created and not null
		// Client should perform customization for buttons and textviews here
		if (dateCaldroidListener != null) {
			dateCaldroidListener.onCaldroidViewCreated();
		}
	}

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveStatesToKey(outState, STATE_BUNDLE_KEY);
    }

    public void resizeViewPager(@NonNull View _childView, ArrayList<DateTime> _datesInMonth) {
        viewPagerHelper.resizeCalendarViewPager(binding.infinitePager, _childView, _datesInMonth);
    }

	/*
     * This method can be used to provide different gridview.
     *
     * @return
     */
    protected int getGridViewRes() {
        return R.layout.date_grid_fragment;
    }

    private MonthPagerAdapter mMonthPagerAdapter;

    /*
     * Setup 4 pages contain date grid views. These pages are recycled to use
     * memory efficient
     *
     * @param view
     */
    private void setupDateGridPages() {
        // Get current date time
        DateTime shownDateTime = new DateTime(year, month, 1, 0, 0, 0, 0);

        // Set enable swipe
        binding.infinitePager.setEnabled(enableSwipe);

        // Set if viewpager wrap around particular month or all months (6 rows)
        viewPagerHelper.setSixWeeksInCalendar(sixWeeksInCalendar);

        // MonthPagerAdapter
        mMonthPagerAdapter = new MonthPagerAdapter(
                getChildFragmentManager(), getLifecycle(), this);

        // Set to pageChangeListener
        pageChangeListener = new DatePageChangeListener(mMonthPagerAdapter.getItemCount() - 1);
        setCalendarDateTime(shownDateTime);

        // Use the infinitePagerAdapter to provide data for dateViewPager
        binding.infinitePager.setAdapter(mMonthPagerAdapter);

        binding.infinitePager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                String childTag = getViewPager2FragmentTag(position);
                Fragment child = getChildFragmentManager().findFragmentByTag(childTag);
                if (child instanceof DateGridFragment) {
                    DateGridFragment dateGridFragment = (DateGridFragment) child;
                    View childView = child.getView();
                    if (childView != null) {
                        resizeViewPager(childView, dateGridFragment.getGridAdapter().getDatetimeList());
                    }
                }
            }
        });

        // Setup pageChangeListener
        binding.infinitePager.registerOnPageChangeCallback(pageChangeListener);
        int pos = mMonthPagerAdapter.dateTimeToPosition(shownDateTime);
        binding.infinitePager.setCurrentItem(pos, false);
    }

    @NonNull
    public DateGridFragment createDateGridFragment(int _position) {
        DateGridFragment dateGridFragment = new DateGridFragment();

        DateTime fragmentDateTime = mMonthPagerAdapter.positionToDateTime(_position);
        DateGridAdapter adapter = getNewDatesGridAdapter(fragmentDateTime.getMonth(), fragmentDateTime.getYear());

        dateGridFragment.setGridViewRes(getGridViewRes());
        dateGridFragment.setGridAdapter(adapter);
        dateGridFragment.setOnItemClickListener(getDateItemClickListener());
        dateGridFragment
                .setOnItemLongClickListener(getDateItemLongClickListener());

        return dateGridFragment;
    }

    public static String getViewPager2FragmentTag(int _position) {
        return "f" + _position;
    }

    @Nullable
    DateGridAdapter getPageAdapter(int _position) {
        Fragment currentPageFragment = getChildFragmentManager().findFragmentByTag(getViewPager2FragmentTag(_position));
        if (currentPageFragment instanceof DateGridFragment) {
            DateGridFragment currentDateGrid = (DateGridFragment) currentPageFragment;
            return currentDateGrid.getGridAdapter();
        }
        else {
            return null;
        }
    }

    /*
     * To display the week day title
     *
     * @return "SUN, MON, TUE, WED, THU, FRI, SAT"
     */
    protected ArrayList<String> getDaysOfWeek() {
        ArrayList<String> list = new ArrayList<>();

        SimpleDateFormat fmt = new SimpleDateFormat("EEE", Locale.getDefault());

        // 17 Feb 2013 is Sunday
        DateTime sunday = new DateTime(2013, 2, 17, 0, 0, 0, 0);
        DateTime nextDay = sunday.plusDays(startDayOfWeek - SUNDAY);

        for (int i = 0; i < 7; i++) {
            Date date = CalendarHelper.convertDateTimeToDate(nextDay);
            list.add(fmt.format(date).toUpperCase());
            nextDay = nextDay.plusDays(1);
        }

        return list;
    }

    /*
     * MonthPageChangeListener refresh the date grid views when user swipe the
     * calendar
     *
     * @author thomasdao
     */
    public class DatePageChangeListener extends ViewPager2.OnPageChangeCallback {
        DatePageChangeListener(int _initialPos) {
            currentPage = _initialPos;
        }

        private int currentPage;

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
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        /*
         * Refresh the fragments
         */
        @Override
        public void onPageSelected(int position) {
            setCurrentPage(position);

            // Update current date time of the selected page
            setCalendarDateTime(mMonthPagerAdapter.positionToDateTime(position));
       }
    }
}
