package com.defch.myapplication;

public class MainActivity extends ViewGroup {

    /**
     * Accent types
     */
    public static final int ACCENT_NONE = 0;
    public static final int ACCENT_ALL = 1;
    public static final int ACCENT_CHARACTER = 2;

    /**
     * Number of digits
     */
    private int mDigits;

    /**
     * Pin digit dimensions and styles
     */
    private int mDigitWidth;
    private int mDigitHeight;
    private int mDigitBackground;
    private int mDigitSpacing;
    private int mDigitTextSize;
    private int mDigitTextColor;
    private int mDigitElevation;

    /**
     * Accent dimensions and styles
     */
    private int mAccentType;
    private int mAccentWidth;
    private int mAccentColor;

    /**
     * Edit text to handle input
     */
    protected EditText mEditText;

    /**
     * Focus change listener to send focus events to
     */
    private OnFocusChangeListener mOnFocusChangeListener;

    /**
     * HolderListener
     */
    private MainActivity.HolderEntryListener listener;


    public MainActivity(Context context) {
        this(context, null);
    }

    public MainActivity(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainActivity(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Get style information
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.CardActivationEditText);
        mDigits = array.getInt(R.styleable.CardActivationEditText_numDigits, 4);
        mAccentType = array.getInt(R.styleable.CardActivationEditText_accentType, ACCENT_NONE);

        // Dimensions
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mDigitWidth = array.getDimensionPixelSize(R.styleable.CardActivationEditText_digitWidth,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, metrics));
        mDigitHeight = array.getDimensionPixelSize(R.styleable.CardActivationEditText_digitHeight,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, metrics));
        mDigitSpacing = array.getDimensionPixelSize(R.styleable.CardActivationEditText_digitSpacing,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics));
        mDigitTextSize = array.getDimensionPixelSize(R.styleable.CardActivationEditText_digitTextSize,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, metrics));
        mAccentWidth = array.getDimensionPixelSize(R.styleable.CardActivationEditText_accentWidth,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, metrics));


        // Background colour, default to android:windowBackground from theme
        mDigitBackground = array.getResourceId(R.styleable.CardActivationEditText_digitBackground, R.color.white);

        // Text colour, default to android:textColorPrimary from theme
        mDigitTextColor = array.getColor(R.styleable.CardActivationEditText_digitTextColor, R.color.chime_subtext_color);

        // Accent colour, default to android:colorAccent from theme
        mAccentColor = array.getColor(R.styleable.CardActivationEditText_pinAccentColor, R.color.chime_subtext_color);

        mDigitElevation = 0;

        // Recycle the typed array
        array.recycle();

        // Add child views
        addViews();
    }

    public void setListener(MainActivity.HolderEntryListener listener)
    {
        this.listener = listener;
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // Calculate the size of the view
        int width = (mDigitWidth * mDigits) + (mDigitSpacing * (mDigits - 1));
        setMeasuredDimension(
                width + getPaddingLeft() + getPaddingRight() + (mDigitElevation * 2),
                mDigitHeight + getPaddingTop() + getPaddingBottom() + (mDigitElevation * 2));

        int height = MeasureSpec.makeMeasureSpec(getMeasuredHeight() / getChildCount(), MeasureSpec.EXACTLY);


        // Measure children
        for (int i = 0; i < getChildCount(); i ++) {
            getChildAt(i).measure(width, height);
            getChildAt(i).setSelected(true && (mAccentType == ACCENT_ALL ||
                    (mAccentType == ACCENT_CHARACTER && (i == mDigits ||
                            (i == mDigits - 1 && mDigits == mDigits)))));
        }


    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // Position the text views
        for (int i = 0; i < mDigits; i++) {
            View child = getChildAt(i);
            int left = i * mDigitWidth + (i > 0 ? i * mDigitSpacing : 0);
            child.layout(
                    left + getPaddingLeft() + mDigitElevation,
                    getPaddingTop() + (mDigitElevation / 2),
                    left + getPaddingLeft() + mDigitElevation + mDigitWidth,
                    getPaddingTop() + (mDigitElevation / 2) + mDigitHeight);
        }

        // Add the edit text as a 1px wide view to allow it to focus
        getChildAt(mDigits).layout(0, 0, 1, getMeasuredHeight());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Make sure this view is focused
            mEditText.requestFocus();

            // Show keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(mEditText, 0);
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        SavedState savedState = new SavedState(parcelable);
        savedState.editTextValue = mEditText.getText().toString();
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mEditText.setText(savedState.editTextValue);
        mEditText.setSelection(savedState.editTextValue.length());
    }

    @Override
    public OnFocusChangeListener getOnFocusChangeListener() {
        return mOnFocusChangeListener;
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        mOnFocusChangeListener = l;
    }

    /**
     * Add a TextWatcher to the EditText
     *
     * @param watcher
     */
    public void addTextChangedListener(TextWatcher watcher) {
        mEditText.addTextChangedListener(watcher);
    }

    /**
     * Remove a TextWatcher from the EditText
     *
     * @param watcher
     */
    public void removeTextChangedListener(TextWatcher watcher) {
        mEditText.removeTextChangedListener(watcher);
    }

    /**
     * Get the {@link Editable} from the EditText
     *
     * @return
     */
    public Editable getText() {
        return mEditText.getText();
    }

    /**
     * Get the {@link String} from the EditText
     *
     * @return
     */
    public String getString()
    {
        return mEditText.getText().toString();
    }

    /**
     * Set text to the EditText
     *
     * @param text
     */
    public void setText(CharSequence text) {
        if (text.length() > mDigits) {
            text = text.subSequence(0, mDigits);
        }
        mEditText.setText(text);
    }

    /**
     * Clear pin input
     */
    public void clearText() {
        mEditText.setText("");
    }

    /**
     * Create views and add them to the view group
     */
    @TargetApi(21)
    private void addViews() {
        // Add a digit view for each digit
        for (int i = 0; i < mDigits; i++) {
            DigitView digitView = new DigitView(getContext());
            digitView.setWidth(mDigitWidth);
            digitView.setHeight(mDigitHeight);
            digitView.setBackgroundResource(mDigitBackground);
            digitView.setTextColor(mDigitTextColor);
            digitView.setTextSize(mDigitTextSize);
            digitView.setGravity(Gravity.CENTER);
            addView(digitView);
        }

        // Add an "invisible" edit text to handle input
        mEditText = new EditText(getContext());
        mEditText.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        mEditText.setTextColor(getResources().getColor(android.R.color.transparent));
        mEditText.setCursorVisible(false);
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mDigits)});
        mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        /*mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // Update the selected state of the views
                int length = mEditText.getText().length();
                for (int i = 0; i < mDigits; i++) {
                    getChildAt(i).setSelected(hasFocus && (mAccentType == ACCENT_ALL ||
                            (mAccentType == ACCENT_CHARACTER && (i == length ||
                                    (i == mDigits - 1 && length == mDigits)))));
                }

                // Make sure the cursor is at the end
                mEditText.setSelection(length);

                // Provide focus change events to any listener
                if (mOnFocusChangeListener != null) {
                    mOnFocusChangeListener.onFocusChange(CardActivationEditText.this, hasFocus);
                }
            }
        });*/
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                for (int i = 0; i < mDigits; i++) {
                    if (s.length() > i) {
                        ((TextView) getChildAt(i)).setText(String.valueOf(s.charAt(i)));
                    } else {
                        ((TextView) getChildAt(i)).setText("");
                    }
                    if (mEditText.hasFocus()) {
                        getChildAt(i).setSelected(mAccentType == ACCENT_ALL ||
                                (mAccentType == ACCENT_CHARACTER && (i == length ||
                                        (i == mDigits - 1 && length == mDigits))));
                    }
                }
            }
        });
        addView(mEditText);
    }


    /**
     * Save state of the view
     */
    static class SavedState extends BaseSavedState {

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
        String editTextValue;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel source) {
            super(source);
            editTextValue = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(editTextValue);
        }

    }

    /**
     * Custom text view that adds a coloured accent when selected
     */
    private class DigitView extends TextView {

        /**
         * Paint used to draw accent
         */
        private Paint mPaint;

        public DigitView(Context context) {
            this(context, null);
        }

        public DigitView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public DigitView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);

            // Setup paint to keep onDraw as lean as possible
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mAccentColor);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // If selected draw the accent
            if (isSelected()) {
                canvas.drawRect(0, getHeight() - mAccentWidth, getWidth(), getHeight(), mPaint);
            }
        }

    }

}

